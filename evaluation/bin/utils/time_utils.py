from datetime import datetime

time_pattern = "%H:%M:%S.%f (%Y-%m-%d)"


def time_in_ms():
    return int(round(datetime.now().timestamp() * 1000))


def format_time(time_in_ms):
    """
    Formats the given time to human readable string.
    """
    return datetime.fromtimestamp(time_in_ms / 1000).strftime(time_pattern)


def format_time_delta(time_in_ms):
    """
    Formats the given time delta to human readable string.
    """
    quotient, ms = divmod(time_in_ms, 1000)
    quotient, s = divmod(quotient, 60)
    h, m = divmod(quotient, 60)

    time_parts = []
    if h > 0:
        time_parts.append("%dh" % h)
    if m > 0:
        time_parts.append("%dm" % m)
    if s > 0:
        time_parts.append("%ds" % s)
    time_parts.append("%dms" % ms)

    return " ".join(time_parts)
