import json
import os

import pandas as pd
import torch

from Marie.Util.CommonTools.FileLoader import FileLoader
from Marie.Util.NHopExtractor import HopExtractor
from Marie.Util.location import DATA_DIR


class ComplexInferenceDataset(torch.utils.data.Dataset):
    """

    """

    def __init__(self, df, full_dataset_dir, ontology, mode="train", inference=True, global_neg=False):
        super(ComplexInferenceDataset, self).__init__()
        self.full_dataset_dir = full_dataset_dir
        self.ontology = ontology
        self.file_loader = FileLoader(full_dataset_dir=self.full_dataset_dir, dataset_name=self.ontology)
        self.entity2idx, self.idx2entity, self.rel2idx, self.idx2rel = self.file_loader.load_index_files()
        self.neg_sample_dict_path = os.path.join(self.full_dataset_dir, "neg_sample_dict.json")
        self.neg_sample_dict = json.loads(open(self.neg_sample_dict_path).read())
        self.use_global_neg = global_neg

        # if (inference):
        self.candidate_dict_path = os.path.join(self.full_dataset_dir, "candidate_dict.json")
        if os.path.exists(self.candidate_dict_path):
            self.candidate_dict = json.loads(open(self.candidate_dict_path).read())
            self.candidate_max = max([len(v) for k, v in self.candidate_dict.items()]) + 1
            print("max number of candidates is", self.candidate_max)

        self.node_value_dict_path = os.path.join(self.full_dataset_dir, "node_value_dict.json")
        if os.path.exists(self.node_value_dict_path):
            self.node_value_dict = json.loads(open(self.node_value_dict_path).read())

        self.p_stop_list = ["hasLogP", "hasDensity", "hasBoilingPoint",
                            "hasSolubility", "hasLogP", "hasLogS", "hasMolecularWeight",
                            "hasMeltingPoint"]

        self.my_extractor = HopExtractor(
            dataset_dir=full_dataset_dir,
            dataset_name=ontology)
        self.mode = mode
        self.df = df
        self.ent_num = len(self.entity2idx.keys())
        self.rel_num = len(self.rel2idx.keys())
        self.use_cached_triples = False
        cached_triple_path = f"{self.full_dataset_dir}/triple_idx_{self.mode}.json"
        if os.path.exists(cached_triple_path) and self.use_cached_triples:
            # the triples are already cached
            self.triples = json.loads(open(cached_triple_path).read())
        else:
            if self.mode == "value_node":
                self.triples = self.create_value_node_triples()
            elif self.mode == "value_node_eval":
                self.triples = self.create_value_node_evaluation_triples()
            elif self.mode == "general_train":
                self.triples = self.create_triples_for_general_train()
            elif self.mode == "general_test":
                self.triples = self.create_triples_for_general_test()
            elif self.mode == "general_train_eval":
                self.triples = self.create_triples_for_general_test()
            else:
                self.triples = self.create_test_triples()
            if self.use_cached_triples:
                with open(cached_triple_path, "w") as f:
                    f.write(json.dumps(self.triples))
                    f.close()

    def create_value_node_evaluation_triples(self):
        triples = []
        for idx, row in self.df.iterrows():
            s = self.entity2idx[row[0]]
            p = self.rel2idx[row[1]]
            o = self.entity2idx[row[2]]
            if row[2] in self.node_value_dict:
                # if row[1] in self.p_stop_list:
                tail_all = range(0, self.ent_num)
                for tail in tail_all:
                    triples.append((s, p, tail, o))
        return triples

    def create_value_node_triples(self):
        triples = []
        for idx, row in self.df.iterrows():
            s = self.entity2idx[row[0]]
            p = self.rel2idx[row[1]]
            o = self.entity2idx[row[2]]
            # if row[1] in self.p_stop_list:
            if row[2] in self.node_value_dict:
                triples.append((s, p, o))
        return triples

    def load_numerical_triples(self):
        triples = []
        for idx, row in self.df.iterrows():
            # print(f"{idx} out of {len(self.df)}")
            s = self.entity2idx[row[0]]
            p = self.rel2idx[row[1]]
            v = float(row[2])
            true_triple = (s, p, v)
            triples.append(true_triple)

        return triples

    def create_fake_triple(self, s, p, o, mode="head"):
        s_p_str = f'{s}_{p}'
        if s_p_str in self.neg_sample_dict and o not in self.node_value_dict:
            fake_candidates = self.neg_sample_dict[s_p_str]
        else:
            fake_candidates = [0]
        return fake_candidates

    def create_triples_for_general_train(self):
        counter = 0
        triples = []
        for idx, row in self.df.iterrows():
            counter += 1
            if counter % 10000 == 0:
                print(f"{counter} out of {len(self.df)}")
            s = self.entity2idx[row[0]]
            p = self.rel2idx[row[1]]
            o = self.entity2idx[row[2]]
            is_numerical_node = (row[2] in self.node_value_dict or row[2].strip() == "hasInstance")
            if is_numerical_node:
                v = float(self.node_value_dict[row[2]])
                candidates = [0]
            else:
                v = -999
                if self.use_global_neg:
                    candidates = range(0, self.ent_num)
                else:
                    candidates = self.create_fake_triple(s, p, o)

            true_triple = (s, p, o, 1, v)

            for i in candidates:
                triple_idx_string = f"{s}_{p}_{i}"
                if not self.my_extractor.check_triple_existence(triple_idx_string):
                    fake_triple = (s, p, i, 0, -999)
                    triples.append(true_triple)
                    triples.append(fake_triple)

        print(f"Total number of triples for training {len(triples)}")
        return triples

    def create_triples_for_general_test(self):
        print(f"using global neg test {self.use_global_neg}")
        triples = []
        progress_counter = 0
        for idx, row in self.df.iterrows():
            progress_counter += 1
            if progress_counter % 10000 == 0:
                print(f"{progress_counter} out of {len(self.df)}")
            s = self.entity2idx[row[0]]
            p = self.rel2idx[row[1]]
            o = self.entity2idx[row[2]]
            if row[2] not in self.node_value_dict:
                triples.append((s, p, o, o))
                # check o and remove any o with
                if self.use_global_neg:
                    candidates = range(0, self.ent_num)
                    padding_num = self.ent_num
                else:
                    candidates = list(set(self.my_extractor.extract_neighbour_from_idx(str(s))))
                    padding_num = self.ent_num

                fake_triples_list = []
                for i in candidates:
                    triple_idx_string = f"{s}_{p}_{i}"
                    if not self.my_extractor.check_triple_existence(triple_idx_string):
                        # also filter out the numerical triples
                        fake_triples_list.append((s, p, i, o))
                        # counter += 1
                triples += fake_triples_list
                length_diff = padding_num - len(fake_triples_list) - 1
                padding_list = [(s, p, -1, o)] * length_diff
                triples += padding_list
                if length_diff < 0:
                    print("We have a problem", length_diff, len(candidates))
        return triples

    def create_test_triples(self):
        triples = []
        for idx, test_row in self.df.iterrows():
            if idx % 10000 == 0:
                print(f"{idx} out of {len(self.df)}")
            s = self.entity2idx[test_row[0]]
            p = self.rel2idx[test_row[1]]
            o = self.entity2idx[test_row[2]]
            # if test_row[1] == "hasRole":
            candidates = self.candidate_dict[str(o)]
            length_diff = self.candidate_max - len(candidates)
            padding_list = [-1] * length_diff
            candidates += padding_list
            for tail in candidates:
                triple_idx_string = f"{s}_{p}_{tail}"
                if o == tail:
                    triples.append((s, p, tail, o))
                elif not self.my_extractor.check_triple_existence(triple_idx_string):
                    triples.append((s, p, tail, o))
                else:
                    triples.append((s, p, -1, o))
        return triples

    def __len__(self):
        return len(self.triples)

    def __getitem__(self, item):
        return self.triples[item]


if __name__ == "__main__":

    ontology = "ontospecies_new"
    sub_ontology = "dev_small"
    # sub_ontology = "role_with_subclass_full_attributes_with_class_flatten"
    full_dataset_dir = os.path.join(DATA_DIR, 'CrossGraph', f'{ontology}/{sub_ontology}')
    batch_size = 32
    # ===================================================================================================
    df_train = pd.read_csv(os.path.join(full_dataset_dir, f"{sub_ontology}-train-2.txt"), sep="\t", header=None)
    df_train_small = df_train.sample(frac=0.01)
    df_test = pd.read_csv(os.path.join(full_dataset_dir, f"{sub_ontology}-test.txt"), sep="\t", header=None)
    # df_numerical = pd.read_csv(os.path.join(full_dir, f"numerical_eval.tsv"), sep="\t", header=None)

    # value_node_path = os.path.join(full_dir, f"{sub_ontology}-singular-train.txt")
    # df_value_node = pd.read_csv(value_node_path, sep="\t", header=None)
    # value_node_set = TransRInferenceDataset(df=df_value_node, full_dataset_dir=full_dir,
    #                                         ontology=sub_ontology,
    #                                         mode="value_node")
    #
    # dataloader_value_node = torch.utils.data.DataLoader(value_node_set, batch_size=batch_size,
    #                                                     shuffle=True)
    #
    # for row in dataloader_value_node:
    #     print(row)
    # df_test = pd.read_csv(os.path.join(full_dataset_dir, f"{ontology}-test.txt"), sep="\t", header=None)
    # test_set = TransRInferenceDataset(df_test, full_dataset_dir=full_dataset_dir,
    #                                   ontology=ontology,
    #                                   mode="test")
    # test_dataloader = torch.utils.data.DataLoader(test_set, batch_size=test_set.candidate_max,
    #                                                    shuffle=False)
    #
    #
    # for x in test_dataloader:
    #     print
    train_set = ComplexInferenceDataset(df_train_small, full_dataset_dir=full_dataset_dir,
                                        ontology=sub_ontology,
                                        mode="general_train")

    i2e = train_set.idx2entity
    i2r = train_set.idx2rel

    train_dataloader = torch.utils.data.DataLoader(train_set,
                                                   batch_size=32,
                                                   shuffle=False)

    for triples in train_dataloader:
        numerical_idx_list = triples[3] != -999
        triples = torch.transpose(torch.stack(triples), 0, 1)
        numerical_idx_num = torch.sum(numerical_idx_list == True)
        if numerical_idx_num.item() > 0:
            pos[numerical_idx_list].to(self.device)



    # for head_set, rel_set, fake_tail_set, true_tail_set in train_dataloader_eval:
    #     # if not eval_set[0] == eval_set[-1]:
    #     h = head_set[0].item()
    #     r = rel_set[0].item()
    #     t = true_tail_set[0].item()
    #     print(i2e[h])
    #     print(i2r[r])
    #     print(i2e[t])
    #     for f_t in fake_tail_set:
    #         f_t_idx = f_t.item()
    #         if f_t_idx > 0:
    #             print(i2e[f_t_idx])
    #     print("=================")
    #     x = input()
    # ===================================================================================================
    # train_set = TransRInferenceDataset(df_train, full_dataset_dir=full_dir, ontology=sub_ontology, mode="train")
    # test_set = TransRInferenceDataset(df_test, full_dataset_dir=full_dir, ontology=sub_ontology, mode="test")
    # train_eval_set = TransRInferenceDataset(df_train_small, full_dataset_dir=full_dir, ontology=sub_ontology,
    #                                         mode="train_eval")
    # numerical_set = TransRInferenceDataset(df_numerical, full_dataset_dir=full_dir, ontology=sub_ontology,
    #                                        mode="numerical")

    # test_dataloader = torch.utils.data.DataLoader(test_set, batch_size=test_set.candidate_max, shuffle=False)
    # train_dataloader = torch.utils.data.DataLoader(train_set, batch_size=32, shuffle=True)
    # numerical_dataloader = torch.utils.data.DataLoader(numerical_set, batch_size=32, shuffle=True)
    # train_eval_set_dataloader = torch.utils.data.DataLoader(train_eval_set, batch_size=train_eval_set.candidate_max,
    #                                                         shuffle=False)

    # value_node_set = TransRInferenceDataset(df=df_train, full_dataset_dir=full_dir,
    #                                         ontology=sub_ontology,
    #                                         mode="value_node")
    # dataloader_value_node = torch.utils.data.DataLoader(value_no+          +de_set, batch_size=batch_size, shuffle=False)
    # for triple in dataloader_value_node:
    #     print(triple)

    # value_node_eval_set = TransRInferenceDataset(df=df_train, full_dataset_dir=full_dir,
    #                                              ontology=sub_ontology,
    #                                              mode="value_node_eval")
    #
    # dataloader_value_node_eval = torch.utils.data.DataLoader(value_node_eval_set,
    # batch_size=value_node_eval_set.ent_num, shuffle=False) for row in dataloader_value_node_eval: print(row)