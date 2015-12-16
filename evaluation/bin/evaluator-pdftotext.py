import os
from evaluator import Evaluator

class PdfToTextEvaluator(Evaluator):
    pass
    
if __name__ == "__main__":      
    PdfToTextEvaluator(Evaluator.get_argument_parser().parse_args()).process()