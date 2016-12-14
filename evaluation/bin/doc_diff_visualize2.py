import diff
import util
import para_diff_rearrange as rearr
import doc_diff_choose_para_or_word as choose

def visualize_diff_phrases(evaluation_result, junk=[]):
    """ Visualizes the given diff phrases. """
            
    diff_phrases = evaluation_result.get("phrases", None)
    
    if diff_phrases is None:
        return
    
    for phrase in diff_phrases: 
        # Decide if we apply the phrase by word operations or by paragraph
        # operations.
    
        if isinstance(phrase, rearr.DiffRearrangePhrase):    
            op_type, _, _, _ = choose.apply_rearrange_phrase(phrase, junk)
            phrase.op_type = op_type
        elif util.ignore_phrase(phrase, junk):
            op_type, _, _, _ = choose.apply_ignored_phrase(phrase)
            phrase.op_type = op_type
        elif isinstance(phrase, diff.DiffCommonPhrase):
            op_type, _, _, _ = choose.apply_ignored_phrase(phrase)
            phrase.op_type = op_type
        elif isinstance(phrase, diff.DiffReplacePhrase):
            op_type, _, _, _ = choose.apply_replace_phrase(phrase)
            phrase.op_type = op_type
    
        # Obtain the start- and end line and column numbers in tex file.
        tex_line_num_start   = -1
        tex_line_num_end     = -1
        tex_column_num_start = -1
        tex_column_num_end   = -1
        
        words_target = phrase.words_target
        
        if len(words_target) > 0:
            first_diff_word_target = words_target[0]
            last_diff_word_target = words_target[-1]
            
            # Line and column numbers are placed in DocWords.
            # The hierarchy is as follows: DiffWord > ParaWord > DocWord
            
            if first_diff_word_target is None or last_diff_word_target is None:
                continue
                
            first_para_word_target = first_diff_word_target.wrapped
            last_para_word_target = last_diff_word_target.wrapped
            
            if first_para_word_target is None or last_para_word_target is None:
                continue
            
            first_doc_word_target = first_para_word_target.wrapped
            last_doc_word_target = last_para_word_target.wrapped
                         
            if first_doc_word_target is None or last_doc_word_target is None:
                continue
                                    
            tex_line_num_start   = first_doc_word_target.line_num
            tex_column_num_start = first_doc_word_target.column_num
            tex_line_num_end     = last_doc_word_target.line_num
            tex_column_num_end   = last_doc_word_target.column_num         
            
        phrase.tex_line_num_start   = tex_line_num_start
        phrase.tex_column_num_start = tex_column_num_start
        phrase.tex_line_num_end     = tex_line_num_end
        phrase.tex_column_num_end   = tex_column_num_end       
    
    visualize(diff_phrases)
    
def visualize(diff_phrases):
    for diff_phrase in diff_phrases:
        print("---------------------------------------------------------------")
        print(type(diff_phrase))
        print("---------------------------------------------------------------")
        for key in diff_phrase.__dict__:
            print(key, ": ", diff_phrase.__dict__[key])
    
