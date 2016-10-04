from extractor import Extractor

class PdfToTextExtractor(Extractor):        
    pass
    
if __name__ == "__main__":      
    PdfToTextExtractor(Extractor.get_argument_parser().parse_args()).process()
