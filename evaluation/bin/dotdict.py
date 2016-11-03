class dotdict(dict):
    """ A dict that allows to access attributes by dot.notation. """
    
    def __init__(self, *args, **kwargs):
        """ Creates a new dotdict."""
                
        for arg in args:
            if isinstance(arg, dict):
                for key, value in arg.items():
                    self[key] = value
            if hasattr(arg, "__dict__"):
                for key, value in arg.__dict__.items():
                    self[key] = value

        if kwargs:
            for key, value in kwargs.items():
                self[key] = value
    
    def __getattr__(self, key):
        """ Returns the value associated with the given key. Returns None if
        the key doesn't exist in the dict. """
        return self.get(key, None)
        
    __setattr__ = dict.__setitem__
    __delattr__ = dict.__delitem__ 
    
    # Needed for multiprocessing.
    def __getstate__(self):
        return self.__dict__
  
    # Needed for multiprocessing.
    def __setstate__(self, d):
        self.__dict__ = d
