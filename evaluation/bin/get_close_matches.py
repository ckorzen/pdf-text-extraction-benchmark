import unicodedata

def get_matches(word, possibilities, allow_distance_one=False):
    result = []
                
    for x in possibilities:        
        if not allow_distance_one and word == x:
            result.append(x)
        elif allow_distance_one and is_distance_at_most_one(word, x):
            result.append(x)
    return result

def is_distance_at_most_one(x, y):
    ''' Returns true, if the edit distance between the strings x and y is at
    most 1. '''
    
    # Return true, if both strings are the same.
    if x == y:
        return True
    
    # Return false, if the length of both strings differs by more than 1.
    if abs(len(x) - len(y)) > 1:
        return False
        
    # Return false, if the number of unique characters in both strings differs 
    # by more than 1.
    if abs(len(set(x)) - len(set(y))) > 1:
        return False 
    
    distance = 0
    i = 0
    j = 0
    while i < len(x) and j < len(y):
        if x[i] == y[j]:
            # The characters match. Advance in both strings.
            i += 1
            j += 1
        else:
            # The characters don't match.
            distance += 1
            
            if distance > 1:
                return False
            
            # If one string is longer than the other then only possible edit
            # is to remove a character              
            if len(x) > len(y):
                i += 1
            elif len(x) < len(y):
                j += 1
            else:
                i += 1
                j += 1
                        
    if i < len(x) or j < len(y):
        distance += 1
            
    return distance <= 1 
