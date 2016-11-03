time_pattern = "%H:%M:%S.%f (%Y-%m-%d)"

def format_time(time):
    """ 
    Formats the given time to human readable string.
    """
    return time.strftime(time_pattern)
    
def format_time_delta(time_delta):
    """ 
    Formats the given time delta to human readable string.
    """
    seconds = time_delta.seconds
    microseconds = time_delta.microseconds
        
    hours, remainder = divmod(seconds, 3600)
    minutes, seconds = divmod(remainder, 60)
    milliseconds, remainder = divmod(microseconds, 1000)
        
    time_parts = []
    if hours > 0:
        time_parts.append("%dh" % hours)
    if minutes > 0:
        time_parts.append("%dm" % minutes)
    if seconds > 0:
        time_parts.append("%ds" % seconds)
    time_parts.append("%dms" % milliseconds)
        
    return " ".join(time_parts)  
