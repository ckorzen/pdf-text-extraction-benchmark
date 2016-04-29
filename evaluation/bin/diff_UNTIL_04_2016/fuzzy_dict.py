import difflib
import editdistance

class fuzzy_dict(dict):
    """ A dictionary that allows fuzzy lookups. """
    
    def __init__(self, items=None):
        """ Constructs a new instance of fuzzy_dict. """

        super(fuzzy_dict, self).__init__()
        if items:
            self.update(items)
        
    def get(self, key, max_distance=0, min_similarity=1.0, default=None):
        """ Returns the value that is associated to the closest match of the 
        given key (with respect to the given maximum distance and minimum 
        similarity). """

        # Check, if the dict contains an exact match.
        if key in self:
            return self[key]
    
        if max_distance > 0 or min_similarity < 1.0:
            close_keys = difflib.get_close_matches(key, self.keys(), 1, 0.5)
                        
            if close_keys:
                matched_key = close_keys[0]
                max_length = max(len(key), len(matched_key))
                
                # Compute both, distance and similarity.
                distance = editdistance.eval(matched_key, key)
                similarity = (max_length - distance) / max_length
                                
                if distance <= max_distance or similarity >= min_similarity:
                    return self[matched_key]
        return default
                            
if __name__ == '__main__':
    import unittest

    class fuzzy_dict_test(unittest.TestCase):
        "Perform some tests"
        test_dict = { "Monday": 1, "Tuesday": 2, "Wednesday": 3 }
        
        def test_empty_constructor(self):
            fd = fuzzy_dict()

            self.assertEqual(fd, {})

        def test_constructor_and_get(self):
            fd = fuzzy_dict(self.test_dict)
            self.assertEqual(fd, self.test_dict)
            self.assertEqual(self.test_dict['Monday'], fd['Monday'])
            with self.assertRaises(KeyError):
                fd['Sunday']

            fd3 = fuzzy_dict(self.test_dict)
            self.assertEqual(fd3, 
                self.test_dict)
            self.assertEqual(self.test_dict['Monday'], 
                fd3.get('Monday'))
            self.assertEqual(self.test_dict['Monday'], 
                fd3.get('Sunday', max_distance=2))
            self.assertEqual(self.test_dict['Monday'], 
                fd3.get('Sunday', min_similarity=0.5))
            self.assertEqual(None, fd3.get('Sunday'))
            self.assertEqual(None, fd3.get('Sunday'))
            self.assertEqual(None, fd3.get('Sunday', max_distance=1))
            self.assertEqual(None, fd3.get('Sunday', min_similarity=0.9))                
            
            fd4 = fuzzy_dict(self.test_dict)
            self.assertEqual(fd4, 
                self.test_dict)
            self.assertEqual(self.test_dict['Monday'], 
                fd4.get('Monday', default=[]))
            self.assertEqual([], 
                fd4.get('Sunday', default=[]))
            self.assertEqual([], 
                fd4.get('Sunday', max_distance=1, default=[]))
            self.assertEqual(self.test_dict['Monday'], 
                fd4.get('Sunday', max_distance=2, default=[]))
            self.assertEqual([], 
                fd4.get('Sunday', min_similarity=0.9, default=[]))
            self.assertEqual(self.test_dict['Monday'], 
                fd4.get('Sunday', min_similarity=0.5, default=[]))

    unittest.main()
