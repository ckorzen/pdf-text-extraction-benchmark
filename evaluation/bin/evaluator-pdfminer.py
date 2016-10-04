from evaluator import Evaluator

class PdfMinerEvaluator(Evaluator):
    pass

if __name__ == "__main__":
    PdfMinerEvaluator(Evaluator.get_argument_parser().parse_args()).evaluate() 
