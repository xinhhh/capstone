from ontologyWrapper import *
import sys
import pickle
import matchers.valueMatcher
import traceback
if __name__ == '__main__':

    fileUrl = 'D:/workwork/jpslatest/JParkSimulator-git/JPS_ONTOMATCH/tmp/jpsppbatch7ptsparqlptVALUE.ttl'#sys.argv[1]
    srcUrl = 'D:/workwork/jpslatest/JParkSimulator-git/JPS_ONTOMATCH/tmp/sparqlpt.pkl'#sys.argv[2]
    tgtUrl = 'D:/workwork/jpslatest/JParkSimulator-git/JPS_ONTOMATCH/tmp/jpsppbatch7pt.pkl'#sys.argv[3]
    match_method = 'matchIndividuals'#sys.argv[4]

    try:
        srcPi = open(srcUrl, 'rb')
        srcOnto = pickle.load(srcPi)
        tgtPi = open(tgtUrl, 'rb')
        tgtOnto = pickle.load(tgtPi)
        matcher = matchers.ValueMatcher((srcOnto, tgtOnto))
        mm = getattr(matcher, match_method)
        # matchSerial for T, matchIndividual for I
        a = mm()
        a.id2Entity(srcOnto,tgtOnto,'individualList')
        a.render(srcUrl,tgtUrl,fileUrl)
        print("success")
    except Exception:
        print(repr(Exception))
        print(traceback.print_exc())

