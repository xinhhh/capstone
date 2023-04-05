import torch
import transformers
import re
SPECIAL_SYM = "[,\'\[\]\{\}\(\)\-\]\*\+\-\_\.\/\:\>]"
stopwords = ['the', 'which', 'in', 'that', 'what']


class NerSMILE():
    def __init__(self, config):
    #config parameters
    #read model
        model = transformers.BertForTokenClassification.from_pretrained('bert-base-cased',  num_labels = 7)
        model = torch.nn.DataParallel(model)
        model.load_state_dict(torch.load(config['model_path']),strict=False)
        self.model = model
        self.config = config

    def remove_stwords(self, text):
        for st in stopwords:
            text = text.replace(st, '')
            text = text.lstrip().rstrip()
        return text


    def extractSMILE(self, sentence_list):
        results = self.get_predictions(self.model, sentence_list)
        return results

    def prediction_fn(self, model, tokenized_sub_sentence):

        tkns = tokenized_sub_sentence
        indexed_tokens = self.config['tokenizer'].convert_tokens_to_ids(tkns)
        segments_ids = [1] * len(indexed_tokens)

        tokens_tensor = torch.tensor([indexed_tokens]).to(self.config['device'])
        segments_tensors = torch.tensor([segments_ids]).to(self.config['device'])

        model.eval()
        with torch.no_grad():
            logit = model(tokens_tensor,
                          token_type_ids=None,
                          attention_mask=segments_tensors)

            logit_new = logit[0].argmax(2).detach().cpu().numpy().tolist()
            prediction = logit_new[0]

            kword = ''
            kword_list = []
            label_list = []

            for k, j in enumerate(prediction):
                if j == 2:
                    j = 0
                if (len(prediction)>1):#The sentence is more than one token long

                    if (j!=0) & (k==0):
                        #if it's the first word in the first position
                        #print('At begin first word')
                        begin = tkns[k]
                        kword = begin

                    elif (j!=0) & (k>=1) & (prediction[k-1]==0):
                        #begin word is in the middle of the sentence
                        begin = tkns[k]
                        previous = tkns[k-1]

                        if begin.startswith('##'):
                            kword = previous + begin[2:]
                        else:
                            kword = begin

                        if k == (len(prediction) - 1):#end of sentence
                            #print('begin and end word is the last word of the sentence')
                            kword_list.append(kword.rstrip().lstrip())
                            label_list.append(j)

                    elif (j!=0) & (k>=1) & (prediction[k-1]!=0):
                        # Or first word of a keyword of another category
                        if prediction[k-1] != j and not tkns[k].startswith('##'):#Not same category
                            #Add last keyword to list
                            kword_list.append(kword.rstrip().lstrip())
                            label_list.append(prediction[k-1])
                            #Restart the word counting
                            begin = tkns[k]
                            kword = begin

                        else:
                            # intermediate word of the same keyword
                            inter = tkns[k]


                            if inter.startswith('##'):
                                kword = kword + "" + inter[2:]
                            elif re.match(SPECIAL_SYM, inter) or re.match(SPECIAL_SYM, tkns[k-1]):
                                kword = kword + "" + inter
                            else:
                                kword = kword + " " + inter


                        if k == (len(prediction) - 1):#end of sentence
                            #print('begin and end')
                            kword_list.append(kword.rstrip().lstrip())
                            label_list.append(j)

                    elif (j==0) & (k>=1) & (prediction[k-1] !=0):
                        # End of a keywords but not end of sentence.
                        kword_list.append(kword.rstrip().lstrip())
                        label_list.append(prediction[k-1])
                        kword = ''
                        inter = ''
                else:
                    if (j!=0):
                        begin = tkns[k]
                        kword = begin
                        kword_list.append(kword.rstrip().lstrip())
                        label_list.append(j)

        print(kword_list)
        print(label_list)
        return kword_list,label_list

    #data format?
    def get_predictions(self, model, sentence_list):
        results = {}
        types = {}
        logits = {}
        for id, sentence in enumerate(sentence_list):
            current_id_predictions = []
            tokenized_sub_sentence = self.config['tokenizer'].tokenize(sentence)

            if len(tokenized_sub_sentence) == 0:
                # If the tokenized sentence are empty
                sub_sentence_prediction_kword_list,sub_type_list = [],[]

            elif len(tokenized_sub_sentence) <= 512:#TODO:ignore sentences that are too long for now
                # If the tokenized sentence are less than 512
                sub_sentence_prediction_kword_list,sub_type_list = self.prediction_fn(model, tokenized_sub_sentence)

            if len(sub_sentence_prediction_kword_list) !=0:#found some keywords
                current_id_predictions = current_id_predictions + sub_sentence_prediction_kword_list

            results[id] = [self.remove_stwords(x) for x in current_id_predictions] #[x.upper() for x in list(set(current_id_predictions))]
            types[id] = sub_type_list
        return results,types

    def kword2original(self, sentence, kwords):
        orginal = []
        for kword in kwords:
            start = sentence.lower().find(kword)
            end = start + len(kword)
            ori = sentence[start:end]
            orginal.append(ori)
        return orginal