from docopt import docopt, DocoptExit
from chemaboxwriters.ontospecies import write_abox

doc = """aboxwriter
Usage:
    ospecies <fileOrDir>  [--inp-file-type=<type>]
                          [--qc-log-ext=<ext>]
                          [--out-dir=<dir>]
                          [--out-base-name=<name>]

Options:
--inp-file-type=<type>  Types of the allowed input files
                        to the ospecies abox writer:
                         - quantum calculation log         [default: qc_log]
                         - quantum calculation json        [qc_json]
                         - ontospecies meta json           [os_json]
                         - ontospecies meta csv            [csv]
--qc-log-ext=<ext>      Extensions of the quantum
                        calculation log files, defaults
                        to ".log, .g09" if not specified
--out-dir=<dir>         Output directory to write the
                        abox files to. If not provided
                        defaults to the directory of the
                        input file
--out-base-name=<name>  Base name of the produced output
                        files. If not provided, defaults
                        to the input file base name.
"""

def start():
    try:
        args = docopt(doc)
    except DocoptExit:
        raise DocoptExit('Error: ospecies called with wrong arguments.')

    write_abox(fileOrDir=args['<fileOrDir>'], inpFileType=args['--inp-file-type'], \
                         outDir=args['--out-dir'], qcLogExt=args['--qc-log-ext'],
                         outBaseName=args['--out-base-name'])
if __name__ == '__main__':
    start()