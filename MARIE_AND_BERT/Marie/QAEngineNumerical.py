import json
import os, sys

sys.path.append("..")
from torch import no_grad
import torch
from Marie.Util.NHopExtractor import HopExtractor
from Marie.EntityLinking.ChemicalNEL import ChemicalNEL
from Marie.Util.Logging import MarieLogger
from Marie.Util.Models.TransEAScoreModel import TransEAScoreModel
from Marie.Util.CommonTools import NumericalTools
from Marie.Util.CommonTools.NLPTools import NLPTools
from Marie.Util.CommonTools.FileLoader import FileLoader
from Marie.Util.location import DATA_DIR


class QAEngineNumerical:
    def __init__(self, dataset_dir, dataset_name, embedding="transe", dim=20, dict_type="json", largest=False,
                 test=False):
        self.marie_logger = MarieLogger()
        self.test = test
        self.largest = largest
        self.dataset_dir = dataset_dir
        self.dataset_name = dataset_name
        self.model_name = f"bert_{self.dataset_name}"
        self.subgraph_extractor_numerical = HopExtractor(dataset_dir=self.dataset_dir, dataset_name=self.dataset_name,
                                                         numerical=True)
        self.subgraph_extractor = HopExtractor(dataset_dir=self.dataset_dir, dataset_name=self.dataset_name,
                                               numerical=False)
        self.device = torch.device("cpu")
        # ==================================== load all the files needed ===============================
        self.file_loader = FileLoader(full_dataset_dir=os.path.join(DATA_DIR, self.dataset_dir),
                                      dataset_name=self.dataset_name)
        self.entity2idx, self.idx2entity, self.rel2idx, self.idx2rel = self.file_loader.load_index_files()
        self.all_species_indices = self.file_loader.load_all_heads_tensor()
        self.triples = self.file_loader.load_all_triples()
        self.value_dictionary = self.file_loader.load_value_dict(dict_type=dict_type,
                                                                 file_name="wikidata_numerical_value_new.json")
        # ===============================================================================================
        # self.numerical_hash_list = \
        #     json.loads(open(os.path.join(DATA_DIR, self.dataset_dir, "numerical_hash_list.json")).read())
        # self.numerical_property_list = \
        #     json.loads(open(os.path.join(DATA_DIR, self.dataset_dir, "numerical_property_list.json")).read())
        # ================================================================================================

        self.operator_dict = {0: "smaller", 1: "larger", 2: "about", 3: "none"}
        self.nel = ChemicalNEL(dataset_name="wikidata_numerical")
        self.nlp = NLPTools()

        if embedding == "transe":
            self.score_model = TransEAScoreModel(device=self.device,
                                                 dataset_dir=self.dataset_dir, dim=dim)
            self.score_model = self.score_model.to(self.device)
            self.score_model.load_model(f"bert_{self.dataset_name}")

        '''Initialize tokenizer'''

        self.o_p_dict = {}
        for triple in self.triples:
            s, p, o = [e.strip() for e in triple.split("\t")]
            self.o_p_dict[o] = p

    def value_lookup(self, node):
        if node in self.value_dictionary:
            return self.value_dictionary[node]
        else:
            return "NODE HAS NO VALUE"

    def prepare_prediction_batch_numerical(self, heads, question_embedding):
        """
        :param question_embedding:
        :param heads:
        :return: the rel embedding predicted, the attr embedding predicted, the numerical operator
        """
        # TODO: cache all the possible triples ...

        heads_batch = []
        tails_batch = []
        split_indices = []

        for head in heads:
            # find the neighbours of this head
            # append the neighbours to tail_batch
            neighbours = self.subgraph_extractor_numerical.extract_neighbour_from_idx(head)
            heads_batch += [head] * len(neighbours)
            tails_batch += neighbours
            split_indices.append(len(tails_batch))
        self.marie_logger.info(f" - Preparing prediction batch")
        prediction_batch = {'e_h': torch.LongTensor(heads_batch),
                            'e_t': torch.LongTensor(tails_batch), 'single_question': question_embedding}
        self.marie_logger.info(f" - Prediction batch is prepared")
        return prediction_batch, torch.LongTensor(tails_batch), split_indices[:-1], torch.LongTensor(heads_batch)

    def filter_heads_by_numerical(self, numerical_operator, question_embedding, numerical_value):
        """
        :param numerical_operator: numerical operator (e.g. larger, smaller, near, none) classified by score model
        :param question_embedding: question embedding (e_q) predicted by score model
        :param numerical_value:  the numerical value identified by regular expression
        :return: heads that fulfill the numerical condition given
        """
        heads = self.all_species_indices
        predicted_attr = self.score_model.get_attribute_prediction(question_embedding)
        attr_batch = predicted_attr.repeat(len(heads), 1)
        numerical_values, _ = self.score_model.get_numerical_prediction(heads, attr_batch)
        if numerical_operator == "larger":
            indices = (numerical_values > numerical_value)
            indices = indices.nonzero().squeeze(1)
        elif numerical_operator == "smaller":
            indices = (numerical_values < numerical_value)
            indices = indices.nonzero().squeeze(1)
        elif numerical_operator == "about":
            indices = torch.abs(numerical_values - numerical_value)
            _, indices = torch.topk(indices, k=min(10, len(indices)), largest=False)
        else:
            return []

        heads = heads[indices]
        return heads.tolist(), numerical_values[indices].tolist()

    def prepare_prediction_batch(self, head, question_embedding, pred_p_label):
        """
        :param head: single IRI of the head entity
        :param question_embedding: one embedding of the question
        :return: prediction_batch (dict), tails: list
        """
        head_idx = self.entity2idx[head]
        candidates = self.subgraph_extractor.extract_neighbour_from_idx(head_idx)
        # candidates = self.filter_by_property_type(pred_p_label=pred_p_label, neighbours=candidates)
        print(candidates)
        self.marie_logger.info(f" - Preparing prediction batch")
        candidate_entities = torch.LongTensor(candidates).to(self.device)
        repeat_num = len(candidates)
        head_entity_batch = torch.LongTensor([head_idx]).repeat(repeat_num).to(self.device)
        self.marie_logger.info(f" - Head entity index {head_idx}")
        prediction_batch = {'single_question': question_embedding, 'e_h': head_entity_batch, 'e_t': candidate_entities}
        self.marie_logger.info(f" - Prediction batch is prepared")
        return prediction_batch, candidates

    def answer_numerical_question(self, question, numerical_operator, single_question_embedding,
                                  true_p=None, true_head=None, species_label=None, mention=None):
        print("================== Processing numerical question =================")
        numerical_value, numerical_string = NumericalTools.numerical_value_extractor(question=question)
        print("Identifed numerical_value", numerical_value)
        question = question.replace("'s ", " # ")
        question = question.replace(numerical_string, "")
        print("Received mention in numerical question", mention)
        if numerical_value is None:
            if mention is None:
                filtered_heads = self.all_species_indices.tolist()
                filtered_numerical_values = [None] * len(filtered_heads)
            else:
                print("=============== No numerical value but mention, fall back to normal question ======")
                return self.answer_normal_question(question=question, numerical_operator="none",
                                                   true_head=true_head, species_label=species_label, mention=mention)
        else:
            # TODO: filter heads by numerical values and operator
            filtered_heads, filtered_numerical_values = self.filter_heads_by_numerical(
                numerical_operator=numerical_operator,
                question_embedding=single_question_embedding,
                numerical_value=numerical_value)

        if len(filtered_heads) == 0:
            return [], [], [], "numerical"
        prediction_batch, tails, split_indices, filtered_heads_tensor = self.prepare_prediction_batch_numerical(
            heads=filtered_heads,
            question_embedding=single_question_embedding)

        predicted_tails = []
        predicted_tails_filtered = []
        predicted_values_list = []
        score_list = []
        target_list = []
        with no_grad():
            triple_scores, pred_p_idx = self.score_model.get_scores(prediction_batch)
            pred_p_label = self.idx2rel[pred_p_idx]
            for sub_score_list, sub_tail_list, head, predicted_numerical_value in zip(
                    torch.tensor_split(triple_scores, split_indices),
                    torch.tensor_split(tails, split_indices),
                    filtered_heads, filtered_numerical_values):
                top_scores, indices_top_k = torch.topk(sub_score_list, k=min(2, len(sub_score_list)),
                                                       largest=self.largest)
                labels_top_k = [self.idx2entity[sub_tail_list[index.item()].item()] for index in indices_top_k]
                if len(labels_top_k) > 0:
                    labels_top_k = labels_top_k[0]
                    numerical_value = self.value_lookup(labels_top_k)
                    # predicted_tails.append(labels_top_k)
                    # ============================================
                    node_p_label = self.o_p_dict[labels_top_k]
                    if pred_p_label == node_p_label:
                        predicted_tails.append(numerical_value)
                        score_list.append(top_scores[0].item())
                        target_list.append(self.idx2entity[head])
                    # ============================================
                    if numerical_value != "NODE HAS NO VALUE":
                        predicted_p = self.o_p_dict[labels_top_k]
                        if predicted_p == true_p:
                            predicted_tails_filtered.append(labels_top_k)
                            predicted_values_list.append(predicted_numerical_value)

        if self.test:
            return numerical_operator, predicted_tails, predicted_tails_filtered, predicted_values_list, pred_p_label
        else:
            return predicted_tails, score_list, target_list, "numerical"

    def filter_by_property_type(self, pred_p_label, neighbours):
        # numerical_hash_list,
        numerical_switch = (pred_p_label in self.numerical_property_list)
        # true if it is a numerical property
        # return only neighbours that are in numerical hash list
        print("nerghbours", neighbours)
        print("pred p label", pred_p_label)
        print("self.numerical_property_list", self.numerical_property_list)
        print("numerical_switch", numerical_switch)
        return [n for n in neighbours if (n in self.numerical_hash_list) == numerical_switch]

    def answer_normal_question(self, question, numerical_operator, true_head=None, species_label=None, mention=None):
        print("=================== Processing NORMAL question ===========================")
        if mention is None:
            try:
                print("========= Mention is None, trying to get mention ==============")
                mention = self.nel.get_mention(question=question)
                if mention == "":
                    _, tokenized_question = self.nlp.tokenize_question(question=question, repeat_num=1)
                    single_question_embedding = self.score_model.get_question_embedding(question=tokenized_question)
                    return self.answer_numerical_question(question=question, numerical_operator=numerical_operator,
                                                          true_head=true_head, true_p=None,
                                                          single_question_embedding=single_question_embedding,
                                                          species_label=species_label, mention=None)
            except IndexError:
                print("========= Mention retrieval failed, fall back to class query ==============")
                _, tokenized_question = self.nlp.tokenize_question(question=question, repeat_num=1)
                single_question_embedding = self.score_model.get_question_embedding(question=tokenized_question)
                return self.answer_numerical_question(question=question, numerical_operator=numerical_operator,
                                                      true_head=true_head, true_p=None,
                                                      single_question_embedding=single_question_embedding,
                                                      species_label=species_label, mention=None)
        # return [], [], [], "normal"
        question = question.replace(mention, "")
        _, tokenized_question = self.nlp.tokenize_question(question=question, repeat_num=1)
        single_question_embedding = self.score_model.get_question_embedding(question=tokenized_question)
        _, head, mention_str, head_key = self.nel.find_cid(mention)
        print("In normal question, found head", head)
        if head is None:
            _, tokenized_question = self.nlp.tokenize_question(question=question, repeat_num=1)
            single_question_embedding = self.score_model.get_question_embedding(question=tokenized_question)
            return self.answer_numerical_question(question=question, numerical_operator=numerical_operator,
                                                  true_head=true_head, true_p=None,
                                                  single_question_embedding=single_question_embedding,
                                                  species_label=species_label, mention=None)

        # make a prediction of the index first
        _, pred_p_idx = self.score_model.get_relation_prediction(question_embedding=single_question_embedding)
        pred_p_label = self.idx2rel[pred_p_idx]
        prediction_batch, candidates = self.prepare_prediction_batch(head=head,
                                                                     question_embedding=single_question_embedding,
                                                                     pred_p_label=pred_p_label)
        scores, _ = self.score_model.get_scores(prediction_batch)
        _, indices_top_k = torch.topk(scores, k=min(5, len(scores)), largest=False)
        labels_top_k = [self.idx2entity[candidates[index]] for index in indices_top_k]
        scores_top_k = [scores[index].item() for index in indices_top_k]
        targets_top_k = [head_key] * len(scores)
        print(f"predicted predicate: {self.idx2rel[pred_p_idx]}")
        print(f"predicted head: {head}")
        if self.test:
            nel_hit = (head == true_head) and (species_label.lower().strip() == mention.lower().strip())
            return numerical_operator, labels_top_k, nel_hit, [], self.idx2rel[pred_p_idx]
        else:
            return labels_top_k, scores_top_k, targets_top_k, "normal"

    def find_answers(self, question: str, true_p=None, true_operator=None, true_head=None, species_label=None,
                     mention=None):
        """
        The find answer method handles both normal questions and numerical questions for wikidata
        :param species_label:
        :param true_head:
        :param true_operator:
        :param true_p:
        :param question: question in string format
        :return: score of all candidate answers
        """
        if true_head is None:
            self.test = False
        question = question.replace("(", "").replace(")", "")
        stop_words = ["find", "all", "species", "with", "what", "is", "the"]
        question_tokens = [token for token in question.split(" ") if token not in stop_words]
        question = " ".join(question_tokens)
        # question = question.replace(" of ", " ")
        _, tokenized_question = self.nlp.tokenize_question(question=question, repeat_num=1)
        single_question_embedding = self.score_model.get_question_embedding(question=tokenized_question)
        predicted_numerical_operator = self.score_model.get_numerical_operator(single_question_embedding)
        predicted_operator_idx = torch.argmax(predicted_numerical_operator).item()
        numerical_operator = self.operator_dict[predicted_operator_idx]
        print(f"numerical_operator: {numerical_operator}  -- question {question}")
        print(f"predicted ")
        if numerical_operator == "none":
            return self.answer_normal_question(question=question, numerical_operator=numerical_operator,
                                               true_head=true_head, species_label=species_label, mention=mention)
        else:
            return self.answer_numerical_question(question=question, numerical_operator=numerical_operator,
                                                  true_head=true_head, true_p=true_p,
                                                  single_question_embedding=single_question_embedding,
                                                  species_label=species_label, mention=mention)

    def process_answer(self, answer_list, mention_string, score_list, answer_type):
        if answer_type == "normal":
            if len(mention_string) > 0:
                mention_string = mention_string[0]
            else:
                mention_string = "EMPTY"

        result_list = []
        self.marie_logger.info(f'=========== processing candidate answers ==============')
        for answer, score in zip(answer_list, score_list):
            self.marie_logger.info(f'The answer:\t\t {answer}')
            # self.marie_logger.info(f'NEL confidence:\t\t  {nel_confidence}')
            self.marie_logger.info(f'Mentioned string:\t\t {mention_string}')
            # self.marie_logger.info(f'Name in Dictionary:\t\t {name}')
            # combined_confidence = round(min(1, nel_confidence * (1 / score)), 2)
            # self.marie_logger.info(f'Confidence:\t\t {combined_confidence}')
            self.marie_logger.info(f'-------------------------')
            # row = {"answer": answer, "from node": answer, "mention": mention_string,
            #        "name": name, "confidence": combined_confidence}
            row = {"answer": answer, "from node": answer, "mention": mention_string}
            result_list.append(row)
        return result_list

    def run(self, question, head=None, mention=None):
        """
        :param mention:
        :param head: directly give a head for testing and evaluation purpose.
        :param question:
        :return:
        """
        answer_list, score_list, target_list, answer_type = self.find_answers(question=question, mention=mention)
        print("answer_list", answer_list)
        print("score_list", score_list)
        print("target_list", target_list)
        print("answer_type", answer_type)
        max_score = max(score_list)
        score_list = [(max_score + 1 - s) for s in score_list]
        return answer_list, score_list, target_list, answer_type


if __name__ == "__main__":
    my_engine = QAEngineNumerical(dataset_dir="CrossGraph/wikidata_numerical", dataset_name="wikidata_numerical",
                                  embedding="transe",
                                  dict_type="json", dim=40, test=False)


    def test_single(question, mention=None):
        rst = my_engine.run(question, mention=mention)
        print(rst)


    def test_normal(limit_num=-1):
        start_idx = 0
        result_list = []
        test_set_path = os.path.join(DATA_DIR, "CrossGraph/wikidata_numerical",
                                     "wikidata_numerical_test_set-normal_smaller.json")
        counter = start_idx
        with open(test_set_path) as f:
            test_set = json.loads(f.read())
            for question, s, p, o, _, species_label, _, _ in test_set[start_idx:limit_num]:
                # for question, s, p, o, _, species_label, _, _ in test_set:
                counter += 1
                print("=======================================================")
                print(f"Evaluating question {question} - Number {counter} out of len {len(test_set)}")
                rst = my_engine.find_answers(question, true_p=p, true_operator=None, true_head=s,
                                             species_label=species_label)
                result_list.append(rst)
        result_path = os.path.join(DATA_DIR, "CrossGraph/wikidata_numerical",
                                   "wikidata_numerical_test_result_normal_smaller.json")

        with open(result_path, "w") as f:
            f.write(json.dumps(result_list))
            f.close()


    def test_numerical():
        result_list = []
        test_set_path = os.path.join(DATA_DIR, "CrossGraph/wikidata_numerical",
                                     "wikidata_numerical_test_set-numerical.json")
        with open(test_set_path) as f:
            test_set = json.loads(f.read())
            # for question, _, true_p, _, _, _, true_operator in random.sample(test_set, 100):
            for question, _, true_p, _, _, _, true_operator in test_set:
                print(f"Evaluating question {question}")
                rst = my_engine.find_answers(question, true_p=true_p, true_operator=true_operator)
                result_list.append(rst)
        result_path = os.path.join(DATA_DIR, "CrossGraph/wikidata_numerical", "wikidata_numerical_test_result_3.json")

        with open(result_path, "w") as f:
            f.write(json.dumps(result_list))
            f.close()


    # test_single(question="what is the boiling point of 1-methoxy-2-propanol", mention="1-methoxy-2-propanol")
    test_single(question="find all the boiling point of all species")
    # test_single(question="find species with boiling point below 10 degrees")

    # test_numerical()
    # test_normal(limit_num=-1)