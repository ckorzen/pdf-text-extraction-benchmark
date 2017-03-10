import os.path


def is_missing_or_empty_file(path):
    """
    Checks if the given path does not exist or if the content of the file is
    empty.

    Args:
        path (str): The path to the file to check.
    Returns:
        True if the given path does not exist or if the content of the file is
        empty; False otherwise.
    """
    if path is None or len(path) == 0:
        return True
    return not os.path.isfile(path) or os.path.getsize(path) == 0


def read_file(path):
    """
    Reads the file given by path.

    Args:
        path (str): The path to the file to read.
    Returns:
        The content of the file or None if the path does not exist or the file
        is empty.
    """
    if is_missing_or_empty_file(path):
        return None

    with open(path) as f:
        return f.read()
