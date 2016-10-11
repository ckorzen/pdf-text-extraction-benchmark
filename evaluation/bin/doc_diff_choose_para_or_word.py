from collections import Counter

import diff

# ------------------------------------------------------------------------------
# Ignored phrase.

def apply_ignored_phrase(phrase):
    """ Simulates the given phrase by applying para ops. Returns the number
    of operations and the related visualization. """         
    
    para = apply_para_ops_ignored_phrase(phrase)
    word = apply_word_ops_ignored_phrase(phrase)
     
    num_para_ops, vis_para_ops, force_para_ops = para
    num_word_ops, vis_word_ops, force_word_ops = word
    
    costs_para_ops = get_costs_para_ops(num_para_ops)
    costs_word_ops = get_costs_word_ops(num_word_ops)
    
    if force_para_ops or costs_para_ops <= costs_word_ops:
        return num_para_ops, vis_para_ops
    else:
        return num_word_ops, vis_word_ops
    
def apply_para_ops_ignored_phrase(phrase):
    """ Simulates the given phrase by applying para ops. Returns the number
    of operations and the related visualization. """         
    
    # Count number of operations.
    num_ops = Counter()
    
    # Create visualization.
    text    = get_unnormalized_text(phrase.words_target)
    vis     = gray(text)  
    
    # Define the position.
    pos     = phrase.pos
    
    # Apply split/merge operations.
    num_ops, vis = apply_split_merge(phrase, num_ops, vis)
        
    return num_ops, [(vis, pos)], False
    
def apply_word_ops_ignored_phrase(phrase):
    """ Simulates the given phrase by applying word ops. Returns the number
    of operations and the related visualization. """         
    
    # Count number of operations.
    num_ops = Counter()
    
    # Create visualization.
    text    = get_unnormalized_text(phrase.words_target)
    vis     = gray(text)  
    
    # Define the position.
    pos     = phrase.pos
    
    # Apply split/merge operations.
    num_ops, vis = apply_split_merge(phrase, num_ops, vis)
        
    return num_ops, [(vis, pos)], False
  
# ------------------------------------------------------------------------------  
# Common Phrase.

def apply_common_phrase(phrase):
    """ Simulates the given phrase by applying para ops. Returns the number
    of operations and the related visualization. """         
    
    para = apply_para_ops_common_phrase(phrase)
    word = apply_word_ops_common_phrase(phrase)
     
    num_para_ops, vis_para_ops, force_para_ops = para
    num_word_ops, vis_word_ops, force_word_ops = word
    
    costs_para_ops = get_costs_para_ops(num_para_ops)
    costs_word_ops = get_costs_word_ops(num_word_ops)
    
    if force_para_ops or costs_para_ops <= costs_word_ops:
        return num_para_ops, vis_para_ops
    else:
        return num_word_ops, vis_word_ops

def apply_para_ops_common_phrase(phrase):
    """ Simulates the given phrase by applying para ops. Returns the number
    of operations and the related visualization. """         
    
    # Count number of operations.
    num_ops = Counter()
    
    # Create visualization.
    text    = get_unnormalized_text(phrase.words_target)
    vis     = text 
    
    # Define the position.
    pos     = phrase.pos
    
    # Apply split/merge operations.
    num_ops, vis = apply_split_merge(phrase, num_ops, vis)
        
    return num_ops, [(vis, pos)], False
    
def apply_word_ops_common_phrase(phrase):
    """ Simulates the given phrase by applying word ops. Returns the number
    of operations and the related visualization. """         
    
    # Count number of operations.
    num_ops = Counter()
    
    # Create visualization.
    text    = get_unnormalized_text(phrase.words_target)
    vis     = text 
    
    # Define the position.
    pos     = phrase.pos
    
    # Apply split/merge operations.
    num_ops, vis = apply_split_merge(phrase, num_ops, vis)
        
    return num_ops, [(vis, pos)], False

# ------------------------------------------------------------------------------
# Rearrange phrase.

def apply_rearrange_phrase(phrase):
    """ Simulates the given phrase by applying para ops. Returns the number
    of operations and the related visualization. """         
        
    para = apply_para_ops_rearrange_phrase(phrase)
    word = apply_word_ops_rearrange_phrase(phrase)
     
    num_para_ops, vis_para_ops, force_para_ops = para
    num_word_ops, vis_word_ops, force_word_ops = word
    
    costs_para_ops = get_costs_para_ops(num_para_ops)
    costs_word_ops = get_costs_word_ops(num_word_ops)
        
    if force_para_ops or costs_para_ops <= costs_word_ops:
        return num_para_ops, vis_para_ops
    else:
        return num_word_ops, vis_word_ops
    
def apply_para_ops_rearrange_phrase(phrase):
    """ Simulates the given phrase by applying para ops. Returns the number
    of operations and the related visualization. """
    
    # Count number of operations.
    num_ops = Counter({ "num_para_rearranges": 1 })
    
    vis_parts = []
    for sub_phrase in phrase.sub_phrases:
        if isinstance(sub_phrase, diff.DiffCommonPhrase):
            sub_num_ops, sub_vis = apply_common_phrase(sub_phrase)
            num_ops.update(sub_num_ops)
            vis_parts.extend(sub_vis)
        elif isinstance(sub_phrase, diff.DiffReplacePhrase):
            sub_num_ops, sub_vis = apply_replace_phrase(sub_phrase)
            num_ops.update(sub_num_ops)
            vis_parts.extend(sub_vis)
            
    # Create visualization.
    text     = "".join([v[0] for v in vis_parts])    
    vis      = blue_bg(text) 
        
    # Define the position.
    pos      = phrase.pos

    first_word_actual = phrase.words_actual[0]
    last_word_actual = phrase.words_actual[-1]    
    
    is_para_break_actual_before = is_paragraph_break_before(first_word_actual)
    is_para_break_actual_after = is_paragraph_break_after(last_word_actual)
    
    force_split_before = is_para_break_actual_before is False         
    force_split_after  = is_para_break_actual_after  is False         
    
    # TODO
    force_para_ops = (is_para_break_actual_before is None or is_para_break_actual_before is True) and (is_para_break_actual_after is None or is_para_break_actual_after is True)
        
    # Apply split/merge operations.
    num_ops, vis = apply_split_merge(phrase, num_ops, vis, 
        force_split_before, force_split_after)
                
    return num_ops, [(vis, pos)], force_para_ops
    
def apply_word_ops_rearrange_phrase(phrase):
    """ Simulates the given phrase by applying para ops. Returns the number
    of operations and the related visualization. """
    
     # Count number of operations.
    num_ops = Counter({ 
        "num_word_deletes": phrase.num_words_actual(),
        "num_word_inserts": phrase.num_words_target(),
    })
    
    # Create visualization.
    first_word_actual = phrase.words_actual[0]
    first_word_target = phrase.words_target[0]
            
    text_actual = get_unnormalized_text(phrase.words_actual)   
    vis_actual  = red(text_actual) 
    pos_actual  = first_word_actual.pos
    
    text_target = get_unnormalized_text(phrase.words_target)   
    vis_target  = green(text_target)   
    pos_target  = first_word_target.pos
    
    # Apply split/merge operations.
    num_ops, vis_actual = apply_split_merge(phrase, num_ops, vis_actual)
    
    vis = [(vis_actual, pos_actual), (vis_target, pos_target)]
                
    return num_ops, vis, False  

# ------------------------------------------------------------------------------
# Replace phrase.

def apply_replace_phrase(phrase):
    """ Decides which kind of operations to perform on applying the given 
    replace phrase. Returns the number of operations. """ 
    
    if phrase is None:
        # Nothing to do.
        return None, None
    elif phrase.is_empty():
        # Nothing to do.
        return None, None
    elif phrase.num_words_actual() == 0:
        # The replace phrase represents an insertion.
        return apply_insert_phrase(phrase)
    elif phrase.num_words_target() == 0:
        # The replace phrase represents a deletion.
        return apply_delete_phrase(phrase)
    else:
        # The replace phrase represents indeed a 'real' substitution.
        return apply_substitute_phrase(phrase)

# ------------------------------------------------------------------------------
# Insert phrase

def apply_insert_phrase(phrase):
    """ Simulates the given phrase by applying para ops. Returns the number
    of operations and the related visualization. """         
    
    para = apply_para_ops_insert_phrase(phrase)
    word = apply_word_ops_insert_phrase(phrase)
     
    num_para_ops, vis_para_ops, force_para_ops = para
    num_word_ops, vis_word_ops, force_word_ops = word
    
    costs_para_ops = get_costs_para_ops(num_para_ops)
    costs_word_ops = get_costs_word_ops(num_word_ops)
    
    if force_para_ops or costs_para_ops <= costs_word_ops:
        return num_para_ops, vis_para_ops
    else:
        return num_word_ops, vis_word_ops

def apply_para_ops_insert_phrase(phrase):
    """ Simulates the given phrase by applying para ops. Returns the number
    of operations and the related visualization. """
    
    # Count number of operations.
    num_ops = Counter({ "num_para_inserts": 1 })
    
    # Create visualization.
    text     = get_unnormalized_text(phrase.words_target)
    vis      = green_bg(text) 
    
    # Define the position.
    pos      = phrase.pos
    
    # Check if the phrase covers a whole paragraph (and hence, if we have to 
    # force para ops.
    first_word_target = phrase.words_target[0]
    last_word_target = phrase.words_target[-1]    
    
    is_para_break_target_before = is_paragraph_break_before(first_word_target)
    is_para_break_target_after = is_paragraph_break_after(last_word_target)
    
    # TODO
    force_para_ops = (is_para_break_target_before is None or is_para_break_target_before is True) and (is_para_break_target_after is None or is_para_break_target_after is True)
                
    # Apply split/merge operations.
    num_ops, vis = apply_split_merge(phrase, num_ops, vis)
    
    return num_ops, [(vis, pos)], force_para_ops
    
def apply_word_ops_insert_phrase(phrase):
    """ Simulates the given phrase by applying para ops. Returns the number
    of operations and the related visualization. """
    
    # Count number of operations.
    num_ops = Counter({ "num_word_inserts": phrase.num_words_target() })
    
    # Create visualization.
    text     = get_unnormalized_text(phrase.words_target)
    vis      = green(text) 
    
    # Define the position.
    pos      = phrase.pos
    
    # Apply split/merge operations.
    num_ops, vis = apply_split_merge(phrase, num_ops, vis)
        
    return num_ops, [(vis, pos)], False
   
# ------------------------------------------------------------------------------
# Delete phrase.

def apply_delete_phrase(phrase):
    """ Simulates the given phrase by applying para ops. Returns the number
    of operations and the related visualization. """         
        
    para = apply_para_ops_delete_phrase(phrase)
    word = apply_word_ops_delete_phrase(phrase)
     
    num_para_ops, vis_para_ops, force_para_ops = para
    num_word_ops, vis_word_ops, force_word_ops = word
    
    costs_para_ops = get_costs_para_ops(num_para_ops)
    costs_word_ops = get_costs_word_ops(num_word_ops)
    
    if force_para_ops or costs_para_ops <= costs_word_ops:
        return num_para_ops, vis_para_ops
    else:
        return num_word_ops, vis_word_ops

def apply_para_ops_delete_phrase(phrase):
    """ Simulates the given phrase by applying para ops. Returns the number
    of operations and the related visualization. """
    
    # Count number of operations.
    num_ops = Counter({ "num_para_deletes": 1 })
    
    # Create visualization.
    text     = get_unnormalized_text(phrase.words_actual)
    vis      = red_bg(text) 
    
    # Define the position.
    pos      = phrase.pos
    
    # Check if the phrase covers a whole paragraph (and hence, if we have to 
    # force para ops.
    first_word_actual = phrase.words_actual[0]
    last_word_actual = phrase.words_actual[-1]    
    
    is_para_break_actual_before = is_paragraph_break_before(first_word_actual)
    is_para_break_actual_after = is_paragraph_break_after(last_word_actual)
    
    # TODO
    force_para_ops = (is_para_break_actual_before is None or is_para_break_actual_before is True) and (is_para_break_actual_after is None or is_para_break_actual_after is True)
        
    # Apply split/merge operations.
    num_ops, vis = apply_split_merge(phrase, num_ops, vis)
    
    return num_ops, [(vis, pos)], force_para_ops
    
def apply_word_ops_delete_phrase(phrase):
    """ Simulates the given phrase by applying para ops. Returns the number
    of operations and the related visualization. """
    
    # Count number of operations.
    num_ops = Counter({ "num_word_deletes": phrase.num_words_actual() })
    
    # Create visualization.
    text     = get_unnormalized_text(phrase.words_actual)
    vis      = red(text) 
    
    # Define the position.
    pos      = phrase.pos
    
    # Apply split/merge operations.
    num_ops, vis = apply_split_merge(phrase, num_ops, vis)
        
    return num_ops, [(vis, pos)], False
    
# ------------------------------------------------------------------------------
# Substitute phrase.

def apply_substitute_phrase(phrase):
    """ Simulates the given phrase by applying para ops. Returns the number
    of operations and the related visualization. """         
    
    para = apply_para_ops_substitute_phrase(phrase)
    word = apply_word_ops_substitute_phrase(phrase)
     
    num_para_ops, vis_para_ops, force_para_ops = para
    num_word_ops, vis_word_ops, force_word_ops = word
    
    costs_para_ops = get_costs_para_ops(num_para_ops)
    costs_word_ops = get_costs_word_ops(num_word_ops)
        
    if force_para_ops or costs_para_ops <= costs_word_ops:
        return num_para_ops, vis_para_ops
    else:
        return num_word_ops, vis_word_ops

def apply_para_ops_substitute_phrase(phrase):
    """ Simulates the given phrase by applying para ops. Returns the number
    of operations and the related visualization. """
    
    # Count number of operations.
    num_ops = Counter({ "num_para_replaces": 1 })
    
    # Create visualization.
    text_actual = get_unnormalized_text(phrase.words_actual)
    vis_actual  = red_bg(text_actual) 
    text_target = get_unnormalized_text(phrase.words_target)
    vis_target  = green_bg(text_target)
    vis         = vis_actual + vis_target
    
    # Define the position.
    pos         = phrase.pos
    
    # Check if the phrase covers a whole paragraph (and hence, if we have to 
    # force para ops.
    first_word_actual = phrase.words_actual[0]
    last_word_actual = phrase.words_actual[-1]    
    
    is_para_break_actual_before = is_paragraph_break_before(first_word_actual)
    is_para_break_actual_after = is_paragraph_break_after(last_word_actual)
    
    # TODO
    force_para_ops = (is_para_break_actual_before is None or is_para_break_actual_before is True) and (is_para_break_actual_after is None or is_para_break_actual_after is True)
    
    # Apply split/merge operations.
    num_ops, vis = apply_split_merge(phrase, num_ops, vis)
    
    return num_ops, [(vis, pos)], force_para_ops
    
def apply_word_ops_substitute_phrase(phrase):
    """ Simulates the given phrase by applying para ops. Returns the number
    of operations and the related visualization. """
    
    # Count number of operations.
    num_words_actual = phrase.num_words_actual()
    num_words_target = phrase.num_words_target()
    min_num_words = min(num_words_actual, num_words_target)
    
    num_ops = Counter({
        "num_word_replaces": min_num_words,
        "num_word_inserts":  num_words_target - min_num_words,
        "num_word_deletes":  num_words_actual - min_num_words
    })
    
    # Create visualization.
    text_actual = get_unnormalized_text(phrase.words_actual)
    vis_actual  = red(text_actual) 
    text_target = get_unnormalized_text(phrase.words_target)
    vis_target  = green(text_target)
    vis         = vis_actual + vis_target
    
    # Define the position.
    pos      = phrase.pos
        
    # Apply split/merge operations.
    num_ops, vis = apply_split_merge(phrase, num_ops, vis)
    
    return num_ops, [(vis, pos)], False
       
# ------------------------------------------------------------------------------

def apply_split_merge(phrase, counter, vis, force_split_before=False, 
        force_split_after=False):
    """ Checks if the given phrase must be splitted or merge and counts the
    number of needed operations. """         

    has_split_before = hasattr(phrase, "split_before") and phrase.split_before
    if has_split_before or force_split_before:
        counter.update({ "num_para_splits": 1 })
        vis = "%s%s" % (red("‖ "), vis)
    
    has_split_after = hasattr(phrase, "split_after") and phrase.split_after
    if has_split_after or force_split_after:
        counter.update({ "num_para_splits": 1 })
        vis = "%s%s" % (vis, red(" ‖"))
        
    has_merge_before = hasattr(phrase, "merge_before") and phrase.merge_before
    if has_merge_before:
        counter.update({ "num_para_merges": 1 })
        vis = "%s%s" % (red("== "), vis)
    
    has_merge_after = hasattr(phrase, "merge_after") and phrase.merge_after
    if has_merge_after:
        counter.update({ "num_para_merges": 1 })
        vis = "%s%s" % (vis, red(" =="))
        
    return counter, vis
    
# ------------------------------------------------------------------------------
# Some util methods.

# The multiplication factor on computing the costs for paragraph operations. 
COST_FACTOR_PARA_OPS = 3
# The multiplication factor on computing the costs of word operations.
COST_FACTOR_WORD_OPS = 1
       
def get_costs_para_ops(para_ops):
    """ Returns the total costs for the given operations. """ 
    return COST_FACTOR_PARA_OPS * sum(para_ops[op] for op in para_ops)

def get_costs_word_ops(word_ops):
    """ Returns the total costs for the given operations. """
    return COST_FACTOR_WORD_OPS * sum(word_ops[op] for op in word_ops)

def get_unnormalized_text(words):
    """ Returns the (unnormalized) text composed from the given words."""    
    return "".join([x.wrapped.wrapped.unnormalized for x in words])
    
# ------------------------------------------------------------------------------
# Methods to colorize.

def red(text):
    return colorize(text, "\033[31m")

def green(text):
    return colorize(text, "\033[32m")

def blue(text):
    return colorize(text, "\033[34m")

def gray(text):
    return colorize(text, "\033[90m")

def red_bg(text):
    return colorize(text, "\033[41m")

def green_bg(text):
    return colorize(text, "\033[42m")

def blue_bg(text):
    return colorize(text, "\033[44m")

def gray_bg(text):
    return colorize(text, "\033[100m")

def colorize(text, color_code):
    """ Applies the given color code to the *trunk* of the given text, that
    is the stripped version of the text. All leading and trailing whitespaces
    won't be colorized."""
    
    if len(text) == 0:
        return ""
    
    # Allow the combinations of color codes. For that append the color code to
    # any reset markers ("\033[0m") in the text, because they reset *all* color 
    # codes. Need to renew the color code to apply.       
    text = text.replace("\033[0m", "\033[0m%s" % color_code)
                        
    # The tool 'less' has issues on displaying color codes if colred text 
    # contains newlines ("\n"): Only the first line is colored. 
    # As a workaround, enrich each line with given color code.
    lines = text.split("\n")
    colored_lines = []
    for line in lines:
        # Color only the "trunk" (the line without leading/trailing whitespaces.
        lstripped = line.lstrip()
        leading_ws = line[ : - len(lstripped)]

        lrstripped = lstripped.rstrip()
        trailing_ws = lstripped[len(lrstripped) : ]
    
        # Wrap the trunk in color code.
        colored_line = "%s%s%s" % (color_code, lrstripped, "\033[0m")
        
        # Reinsert leading and trailing whitespaces.
        colored_line = "%s%s%s" % (leading_ws, colored_line, trailing_ws)
        
        colored_lines.append(colored_line if len(line) > 0 else "")
        
    return "\n".join(colored_lines)
    
def is_paragraph_break_before(diff_word):
    """ Checks if there is a paragraph break between the given words.
    Returns True if both words are not None and there is a paragraph break in 
    between.
    Returns False if both words are not None and there is no paragraph break in 
    between.
    Returns None if at least one of both words is None.
    """
        
    if diff_word is None:
        return
    
    para_word = diff_word.wrapped
    
    if para_word is None:
        return
        
    prev_para_word = para_word.prev
        
    if prev_para_word is None:
        return   
            
    return prev_para_word.get_para_num() != para_word.get_para_num()
    
def is_paragraph_break_after(diff_word):
    """ Checks if there is a paragraph break between the given words.
    Returns True if both words are not None and there is a paragraph break in 
    between.
    Returns False if both words are not None and there is no paragraph break in 
    between.
    Returns None if at least one of both words is None.
    """
        
    if diff_word is None:
        return
    
    para_word = diff_word.wrapped
    
    if para_word is None:
        return
        
    next_para_word = para_word.next
        
    if next_para_word is None:
        return    
            
    return next_para_word.get_para_num() != para_word.get_para_num()
