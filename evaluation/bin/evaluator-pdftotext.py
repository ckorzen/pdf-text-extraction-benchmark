import logging

from evaluator import Evaluator

logging.basicConfig(
    format='%(asctime)s : %(levelname)s : %(module)s : %(message)s',
    level=logging.DEBUG,
)
logger = logging.getLogger(__name__)

class PdfToTextEvaluator(Evaluator):
    pass
    
if __name__ == "__main__":      
    PdfToTextEvaluator(Evaluator.get_argument_parser().parse_args()).process()
