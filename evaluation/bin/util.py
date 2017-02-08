import unicodedata
import re
import string
import os.path

import file_util

from time import time

def time_in_ms():
    return int(round(time() * 1000))

def remove_control_characters(text):
    """ Removes all control characters from given text."""
    
    if text is None:
        return
    
    # Create a dict with all the control characters.
    ctrl_chars = dict.fromkeys(range(32))
    
    return text.translate(ctrl_chars)

def have_elements_in_common(list1, list2):   
    return len(list(set(list1) & set(list2))) > 0

def update_file_extension(path, new_file_extension):
    ''' Returns the given path where the actual file extension is replaced by 
    the given new file extension.''' 
    
    # Find the last dot in the path.
    index_last_dot = path.rfind('.')
    
    if index_last_dot < 0:
        basename = path
    else:
        basename = path[ : index_last_dot]
    
    return basename + new_file_extension    

def to_str(arg, default):
    ''' Parses the given arg as string. If parsing fails, returns the given 
    default value. '''
    try: 
        return str(arg)
    except:
        return default

def to_float(arg, default):
    ''' Parses the given arg as int. If parsing fails, returns the given 
    default value. '''
    try: 
        return float(arg)
    except:
        return default

def to_int(arg, default):
    ''' Parses the given arg as int. If parsing fails, returns the given 
    default value. '''
    try: 
        return int(arg)
    except:
        return default

def to_bool(arg, default):
    ''' Parses the given arg as bool. If parsing fails, returns the given 
    default value. '''
    arg_int = to_int(arg, None)
    if arg_int is not None:
        return arg_int != 0
    else:
        arg_str = to_str(arg, None)
        if arg_str is not None:
            return arg_str.lower() != "false" 
        else:
            return default
        
def to_list(arg, default, separator=" "):
    ''' Splits the given arg using the given separator as delimiter and wraps 
    all elements in a list. '''
    arg_str = to_str(arg, None)
    if arg_str:
        return arg_str.split(sep=separator)
    else:
        return default

# ------------------------------------------------------------------------------
# Reading tool info file.

def read_tool_info(tool_dir):
    """ Reads the external tool info file and appends the key/value pairs 
    to given args dictionary. The given args dictionary must contain the 
    path to the root directory of tool. """
    
    args = {}
    tool_info_file_path = get_tool_info_file_path(tool_dir)
    
    # Only proceed if the the tool info file exists.
    if file_util.is_missing_or_empty_file(tool_info_file_path):
        return args
                
    with open(tool_info_file_path) as tool_info_file:
        # Each line of file is of form <key> <TAB> <value>    
        for line in tool_info_file:
            key, value = line.strip().split("\t")
            args[key] = value    
    
    return args

def get_tool_info_file_path(tool_dir):
    return os.path.join(tool_dir, "info.txt")    
