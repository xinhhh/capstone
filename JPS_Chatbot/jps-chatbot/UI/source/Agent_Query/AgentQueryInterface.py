if __name__ == '__main__':
    from AgentUtil.AgentQueryParser import AgentQueryParser
    from AgentUtil.AgentCaller import AgentCaller
    from AgentUtil.util.StopWords import removeStopWords
    from AgentUtil.util.ModelLoader import ModelLoader
    from AgentUtil.util.MarieLogger import MarieIOLog, MarieMessage

else:
    from .AgentUtil.AgentCaller import AgentCaller
    from .AgentUtil.AgentQueryParser import AgentQueryParser
    from .AgentUtil.util.MarieLogger import MarieIOLog, MarieMessage
    from .AgentUtil.util.StopWords import removeStopWords
    from .AgentUtil.util.ModelLoader import ModelLoader

from rasa.nlu.model import Interpreter


class AgentQueryInterface:
    def __init__(self, model_dir):
        self.agent_interpreter = Interpreter.load(model_dir)
        self.agent_query_parser = AgentQueryParser()
        self.agent_caller = AgentCaller()

    # @MarieIOLog
    def agent_query(self, question):
        rst = self.parse_question(question)
        MarieMessage('AGENT PARSE RESULT {}'.format(rst))
        _inputs, _outputs, _url = self.agent_query_parser.parse(rst)
        if _inputs is None or _outputs is None:
            return None
        MarieMessage(_inputs)
        MarieMessage(_outputs)
        MarieMessage('====================================================')

        # try:
        #     response = self.agent_caller.call(_inputs, _outputs, _url)
        #     return response
        # except:
        #     return None
        response = self.agent_caller.call(_inputs, _outputs, _url)
        return response

    # make NLP analysis on the question
    def parse_question(self, question):
        question = removeStopWords(question)
        result = self.agent_interpreter.parse(question)
        MarieMessage('question {}'.format(question))
        for e in result['entities']:
            MarieMessage('entity {}'.format(e['entity']))
            MarieMessage('value  {}'.format(e['value']))
            MarieMessage('--------------')
        return result


if __name__ == '__main__':
    ml = ModelLoader()
    agent_nlu_model = ml.AGENT_NLU_MODEL()
    aqi = AgentQueryInterface(agent_nlu_model)
    questions = ['enthalpy of inchi=1s/c2h6o/c1-2-3/h3h,2h2,1h3 at the temperature of 123 K and pressure of 1 atm',
                 'heat capacity of CO2 at 123 K and 1 Pa',
                 'internal energy of C4H10O at 901 Pascal and 3694 kelvin',
                 'gibbs egnery of carbon dioxide at the temperature of 222 degrees celsius and 232Pa']
    questions = ['geometry c=c=c', 'what is the internal energy of methane at 100 K']
    for q in questions:
        r = aqi.agent_query(q)
        print(r)