# Author: Sujit Rokka Chhetri
# License: MIT

import sys


from pykg2vec.common import KGEArgParser
from pykg2vec.utils.bayesian_optimizer import BaysOptimizer


def main():
    # getting the customized configurations from the command-line arguments.
    args = KGEArgParser().get_args(sys.argv[1:])

    # initializing bayesian optimizer and prepare data.
    bays_opt = BaysOptimizer(args=args)

    # perform the golden hyperparameter tuning.
    bays_opt.optimize()


if __name__ == "__main__":
    main()