from evaluator import Evaluator

class LaPdfTextEvaluator(Evaluator):        
    pass

if __name__ == "__main__":      
    LaPdfTextEvaluator(Evaluator.get_argument_parser().parse_args()).evaluate() 
