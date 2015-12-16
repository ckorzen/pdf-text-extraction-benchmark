import collections
     
def similar(seq1, seq2):
    return difflib.SequenceMatcher(a=seq1.lower(), b=seq2.lower()).ratio()
     
if __name__ == "__main__":
    c = collections.Counter({"A": 1, "B": 1})
    c += collections.Counter({"X": -1})
    print(c)
    