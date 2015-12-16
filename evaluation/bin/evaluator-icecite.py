import os
from evaluator import Evaluator

class IceciteEvaluator(Evaluator):        
    pass
    
if __name__ == "__main__":      
    IceciteEvaluator(Evaluator.get_argument_parser().parse_args()).process()