import diff
import util
import para_diff_rearrange as rearr
import doc_diff_choose_para_or_word as choose

def visualize_diff_phrases(diff_phrases, junk=[]):
    """ Visualizes the given diff phrases. """
        
    visualizations = []
    
    if diff_phrases:
        # Visualize each single phrase.
        for phrase in diff_phrases: 
            visualizations.extend(visualize_phrase(phrase))
        
        # Sort the instructions by defined positions.
        visualizations.sort(key=lambda v: v[1])
    
    return "".join([vis for vis, pos in visualizations])
    
def visualize_phrase(phrase, junk=[]):
    """ Visualizes the given diff phrases. """
                
    visualizations = []
           
    # Collect the visualization instructions per phrase.    
    if util.ignore_phrase(phrase, junk):
        num_ops, vis = choose.apply_ignored_phrase(phrase)
        visualizations.extend(vis)
    elif isinstance(phrase, diff.DiffCommonPhrase):
        num_ops, vis = choose.apply_common_phrase(phrase)
        visualizations.extend(vis)
    elif isinstance(phrase, diff.DiffReplacePhrase):
        num_ops, vis = choose.apply_replace_phrase(phrase)
        visualizations.extend(vis)
    elif isinstance(phrase, rearr.DiffRearrangePhrase):
        num_ops, vis = choose.apply_rearrange_phrase(phrase)
        visualizations.extend(vis)
        
    return visualizations
  
# ------------------------------------------------------------------------------
     
def visualize_ignored_phrase(phrase):
    """ Visualizes the given ignored phrase. Returns a tuple with 
    position where to place the visualization string and the visualization 
    string."""
        
    pos     = phrase.pos
    before  = visualize_split_and_merge_para(phrase)
    text    = get_unnormalized_text(phrase.words_target)
    vis     = gray(text)     
    return [(pos, before + vis)] 
    
def visualize_common_phrase(phrase):
    """ Visualizes the given common phrase. Returns a tuple with 
    position where to place the visualization string and the visualization 
    string."""
    pos     = phrase.pos
    before  = visualize_split_and_merge_para(phrase)
    text    = get_unnormalized_text(phrase.words_target)
    vis     = text     
    return [(pos, before + vis)] 
    
def visualize_rearrange_phrase(phrase):
    """ Visualizes the given rearrange phrase. Returns a tuple with 
    position where to place the visualization string and the visualization 
    string."""
            
    pos     = phrase.pos
    before  = visualize_split_and_merge_para(phrase)
    text    = visualize_diff_phrases(phrase.sub_phrases)
    vis     = blue_bg(text)
    return [(pos, before + vis)]  
    
def visualize_replace_phrase(phrase):
    """ Visualizes the given replace phrase. Returns a tuple with 
    position where to place the visualization string and the visualization 
    string."""
    
    if phrase is None:
        # Nothing to do.
        return None, []
    elif phrase.is_empty():
        # Nothing to do.
        return None, []
    elif phrase.num_words_actual() == 0:
        # The replace phrase represents an insertion.
        return visualize_insert_phrase(phrase)
    elif phrase.num_words_target() == 0:
        # The replace phrase represents a deletion.
        return visualize_delete_phrase(phrase)
    else:
        # The replace phrase represents indeed a 'real' substitution.
        return visualize_substitute_phrase(phrase)
        
def visualize_insert_phrase(phrase):
    """ Visualizes the given replace phrase that represents indeed an insertion.
    Returns a tuple with position where to place the visualization string and 
    the visualization string."""
    
    num_ops, ops_type = count_num_ops.count_num_ops_insert_phrase(phrase)
    
    pos     = phrase.pos
    before  = visualize_split_and_merge_para(phrase)
    text    = get_unnormalized_text(phrase.words_target)                    
    vis     = green(text) if ops_type == "word" else green_bg(text)
           
    return [(pos, before + vis)]   
    
def visualize_delete_phrase(phrase):
    """ Visualizes the given replace phrase that represents indeed a deletion.
    Returns a tuple with position where to place the visualization string and 
    the visualization string."""
    
    num_ops, ops_type = count_num_ops.count_num_ops_delete_phrase(phrase)
        
    pos    = phrase.pos
    before = visualize_split_and_merge_para(phrase)
    text   = get_unnormalized_text(phrase.words_actual)                    
    vis    = red(text) if ops_type == "word" else red_bg(text)
           
    return [(pos, before + vis)]  
    
def visualize_substitute_phrase(phrase):
    """ Visualizes the given replace phrase that represents indeed a 
    substitution. Returns a tuple with position where to place the 
    visualization string and the visualization string."""
    
    pos_actual  = phrase.pos
    before      = visualize_split_and_merge_para(phrase)
    text_actual = get_unnormalized_text(phrase.words_actual)                    
    vis_actual  = red(text_actual)
    
    pos_target  = phrase.pos + [0]
    text_target = get_unnormalized_text(phrase.words_target)                    
    vis_target  = green(text_target)
    
    return [(pos_actual, before + vis_actual), (pos_target, vis_target)]
    


