import diff_match_patch

def diff_lineMode(text1, text2):
    dmp = diff_match_patch.diff_match_patch()
    a = dmp.diff_linesToChars(text1, text2)
    lineText1 = a[0]
    lineText2 = a[1]
    lineArray = a[2]

    print("text1: " + lineText1)
    print("text2: " + lineText2)
    print("array: " + str(lineArray))
    
    diffs = dmp.diff_main(lineText1, lineText2, False)

    dmp.diff_charsToLines(diffs, lineArray)
    
    print(diffs)
    
    return diffs
    
    
s1 = "Hello beautiful World"
s2 = "Hello beaut if ul World fsdfs sdsd"
diff_lineMode(s1, s2)