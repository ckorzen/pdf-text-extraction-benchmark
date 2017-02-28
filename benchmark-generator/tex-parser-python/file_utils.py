import os.path


def is_missing_or_empty_file(path):
    """
    Returns true, if the given file_path does not exist or if the content of
    the file is empty.
    """
    if path is None or len(path) == 0:
        return True
    return not os.path.isfile(path) or os.path.getsize(path) == 0


def read_file(path):
    """
    Reads the given file.
    """
    if is_missing_or_empty_file(path):
        return None

    with open(path) as f:
        return f.read()
