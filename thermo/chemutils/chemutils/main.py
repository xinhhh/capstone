from chemutils.mainutils import xyzReorderToxyz, \
                                xyzToAtomsPositionsWrapper, \
                                xyzReorderToxyzFlexBond
from chemutils.obabelutils import obConvertWrapper
from docopt import docopt, DocoptExit

doc = """chemutils
Usage:
    chemutils atomspos <xyzFileOrStr> [--outFile=<out-file-path>
                                       --silent]
    chemutils convert <moleculeFileOrStr> <from> <to> [--convOptions=<conv_opt>
                                                       --outFile=<out-file-path>
                                                       --silent]
    chemutils xyz2xyz <xyzTargetFileOrStr> <xyzRefFileOrStr> [--outFile=<out-file-path>
                                                    --silent]
"""

#    chemutils xyz2xyzFlexBond <xyzTargetFileOrStr> <xyzRefFileOrStr>
#                                                   <refAtomId1>
#                                                   <refAtomId2>

def start():
    try:
        args = docopt(doc)
    except DocoptExit:
        raise DocoptExit('Error: chemutils called with wrong arguments.')

    if args["atomspos"]:
        output = xyzToAtomsPositionsWrapper(xyzFileOrStr=args['<xyzFileOrStr>'], \
                            silent=args['--silent'], outFile=args['--outFile'])
    elif args["convert"]:
        output = obConvertWrapper(inputMol=args['<moleculeFileOrStr>'], inputMolFormat=args['<from>'], \
            outputMolFormat=args['<to>'], convOptions=args['--convOptions'], outFile=args['--outFile'], \
            silent=args['--silent'])
    else:
        output = xyzReorderToxyz(args['<xyzTargetFileOrStr>'], args['<xyzRefFileOrStr>'], \
                                   outFile=args['--outFile'])
    #else:
    #    output = xyzReorderToxyzFlexBond(args['<xyzTargetFileOrStr>'], args['<xyzRefFileOrStr>'], \
    #                               args['<refAtomId1>'], args['<refAtomId2>'])
if __name__ == '__main__':
    start()