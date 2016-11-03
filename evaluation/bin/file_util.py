import os.path

def read_file(file_path):
    """ 
    Returns the content of given file. Returns empty string if the file doesn't
    exist.
    """
    text = ""        
    if not is_missing_or_empty_file(file_path):
        with open(file_path) as f: 
            text = f.read()
        
    return text
    
def is_missing_or_empty_file(file_path):
    """ 
    Returns true, if the given file_path doesn't exist or if the 
    content of file is empty. 
    """
    return not os.path.isfile(file_path) or os.path.getsize(file_path) == 0
    
def collect_files(root_dir=None, dir_filter=[], prefix="", suffix=""):        
    """ 
    Collects all files in the given root directory and in all 
    sub-directories that match the given directory filter that matches the given
    prefix and the given suffix.
    """    
    files = []
    # Iterate through the entries of root directory.
    for entry_name in sorted(os.listdir(root_dir)):            
        entry_path = os.path.join(root_dir, entry_name)
                        
        # Only proceed if the entry is a directory.
        if not os.path.isdir(entry_path):
            continue
            
        # Only proceed if either the dir filter is empty or the dir matches
        # the dir filter.
        if len(dir_filter) > 0 and entry_name not in dir_filter:
            continue
            
        # Collect the pdf files of current dir and put it to result.
        files_of_dir = collect_files_from_dir(entry_path, prefix, suffix)      
        files_of_dir.sort()
            
        files.extend(files_of_dir)
                  
    return files
    
def collect_files_from_dir(directory, file_prefix="", file_suffix=""):
    """
    Scans the given directory for pdf files.
    """
    files = []
    collect_files_from_dir_rec(directory, file_prefix, file_suffix, files)
    return files
        
def collect_files_from_dir_rec(directory, prefix, suffix, result_files):
    """
    Scans the given directory for pdf files recursively.
    """
        
    # Iterate through the entries of root directory to find pdf files.
    for entry_name in sorted(os.listdir(directory)):
        entry_path = os.path.join(directory, entry_name)
        if os.path.isdir(entry_path):
            collect_files_from_dir_rec(entry_path, prefix, suffix, result_files)
        elif os.path.isfile(entry_path):
            # Only proceed if the file matches the given prefix.
            if not entry_name.lower().startswith(prefix):
                continue
                
            # Only proceed if file is a pdf file.
            if not entry_name.lower().endswith(suffix):
                continue
                        
            result_files.append(entry_path)

