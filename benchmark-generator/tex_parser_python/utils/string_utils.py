def split(string, delim, num, default=None):
    """
    Splits the given string on given delimiter using str.split() and ensures
    that exactly num-many substrings are returned.
    If str.split() returns more than num-many substrings, the result list will
    be truncated.
    If str.split() returns less than num-many substrings, the result list will
    be padded with the given default value, such that its length is equal to
    num.
    
    Args:
        string (str): The string to split.
        delim (str): The delimiter.
        num (int): The expected length of substrings to return.
        default (str, optional): The default value to use when the result list
            need to be padded.
    Returns:
        A list of substrings with length <num>.
    """
    fragments = string.split(delim)
    if len(fragments) > num:
        fragments = fragments[0:num]
    elif len(fragments) < num:
        num_missing = num - len(fragments)
        fragments += num_missing * [default]
    return fragments
