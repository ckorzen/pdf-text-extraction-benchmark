from multiprocessing import Pool
from time import time

def evaluate_parallel():
    p = Pool(24)        
    
    start_time = time()        
    y_parallel = p.map(f, x)
    end_time = time()
