from pprint import pprint

from Chatbot.Interpretation_parser import InterpretationParser
from Chatbot.SearchEngine import SearchEngine
from Chatbot.SPARQLConstructor import SPARQLConstructor
from Chatbot.SPARQLQuery import SPARQLQuery
from pprint import pprint
from rasa.nlu.model import Interpreter
import os, sys
import tempfile
import tarfile
import nltk


# 0. get the topic model result, choose which direction it goes
# 1. get the InterpretationParse

def extract_nlu_model(extract_dir='../models/'):
    # Identify the newest trained nlu model
    path = '../models/'
    files = os.listdir(path)
    paths = [os.path.join(path, basename) for basename in files if ('.tar' in basename)]
    file_name = max(paths, key=os.path.getctime)
    # Extract the model to a temporary directory
    tf = tarfile.open(file_name)
    tf.extractall(path=extract_dir)


class CoordinateAgent():
    def __init__(self):
        # initialize interpreter
        extract_nlu_model()
        self.stopwords = ['all', 'the']
        # self.stopwords.append('all')
        self.interpreter = Interpreter.load('../models/nlu')

    def run(self, question):

        self.interpreter_parser = InterpretationParser()
        self.interpreter_parser.interpreter = self.interpreter
        self.search_engine = SearchEngine()
        self.sparql_constructor = SPARQLConstructor()
        self.sparql_query = SPARQLQuery()

        intent_and_entities = self.interpreter_parser.parse_question_interpretation(question)
        intent_and_entities_with_uris = self.search_engine.parse_entities(intent_and_entities)
        if intent_and_entities_with_uris is None:
            return None

        print('================= result with uris ================')
        pprint(intent_and_entities_with_uris)
        sparqls = self.sparql_constructor.fill_sparql_query(intent_and_entities_with_uris)
        if sparqls is None:
            return None
        if len(sparqls) >= 5:
            sparqls = sparqls[:5]

        result = self.sparql_query.start_queries(sparqls)
        print('-------------- we have a result -------------------')
        pprint(result)
        return result

ca = CoordinateAgent()
# # r = ca.run(question='what is the molecular weight of benzene')
ca.run(question='what is the heat capacity of ch4')
