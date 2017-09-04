import para_diff
import para_diff_rearrange as para_diff_rearr
import word_diff
import diff_utils
import doc_diff_visualize as vis
from collections import Counter


def doc_diff_from_strings(
        actual,
        target,
        rearrange_phrases=False,
        min_rearrange_length=3,
        refuse_common_threshold=0,
        compose_characters=False,
        para_delimiter="\n\s*\n\s*",
        word_delimiter="\s+",
        to_lower=True,
        # specialchars_pattern: special chars that are not surrounded by digit.
        specialchars_pattern="(?<!\d)\W+|\W+(?!\d)",
        excludes=[],
        junk=[],
        word_pdf_positions_index=None):
    """ Does a doc diff based on given strings.
    Splits the strings actual and target to paragraphs and words based on
    given para_delimiter and word_delimiter. Normalizes the words by
    transforming it to lower cases if to_lower is True and by removing
    characters that matches the given specialchars_pattern. Words that matches
    any of pattern given in excludes will be not normalized.
    Outputs a list of DiffCommonPhrase objects and DiffReplacePhrase objects.
    """

    actual = "" if actual is None else actual
    target = "" if target is None else target

    # Transform actual to flat list of DiffWords.
    actual = diff_utils.string_to_diff_words(
        actual,
        flatten=True,
        compose_characters=compose_characters,
        para_delimiter=para_delimiter,
        word_delimiter=word_delimiter,
        to_lower=to_lower,
        specialchars_pattern=specialchars_pattern,
        excludes=excludes)

    # Transform actual to flat list of DiffWords.
    target = diff_utils.string_to_diff_words(
        target,
        flatten=True,
        compose_characters=compose_characters,
        para_delimiter=para_delimiter,
        word_delimiter=word_delimiter,
        to_lower=to_lower,
        specialchars_pattern=specialchars_pattern,
        excludes=excludes,
        word_pdf_positions_index=word_pdf_positions_index)

    return doc_diff(
        actual,
        target,
        rearrange_phrases=rearrange_phrases,
        min_rearrange_length=min_rearrange_length,
        refuse_common_threshold=refuse_common_threshold,
        junk=junk)


def doc_diff(
        actual,
        target,
        rearrange_phrases=False,
        min_rearrange_length=3,
        refuse_common_threshold=0,
        junk=[]):
    """ Does doc_diff based on flat lists of DiffWord objects. """

    # Run para_diff on given input.
    diff_result = para_diff.para_diff(
        actual,
        target,
        rearrange_phrases=rearrange_phrases,
        min_rearrange_length=min_rearrange_length,
        refuse_common_threshold=refuse_common_threshold,
        junk=junk)

    # Compute the number of operations.
    handle_diff_result(diff_result, junk)
    
    return diff_result


def handle_diff_result(diff_result, junk=[]):
    # The total number of required operations.
    diff_result.num_ops = Counter()
    # The absolute number of required operations.
    diff_result.num_ops_abs = Counter()
    # The relative number of required operations.
    diff_result.num_ops_rel = Counter()
    # The visualization parts.
    vis_parts = []

    # Decide which of the rearrange candidates to rearrange.
    rearrange(diff_result)

    # Decide where to split and where to merge the phrases.
    split_and_merge(diff_result, junk=junk)

    # For each phrase, choose word- or para ops.
    for phrase in diff_result.phrases:
        # Compute the word operations.
        compute_word_ops(phrase, junk)
        # Compute the para ops.
        force_para_ops = compute_para_ops(phrase, junk)

        num_word_ops = phrase.num_word_ops
        num_para_ops = phrase.num_para_ops
        num_split_merge = phrase.num_para_ops_split_merge

        # Compute costs.
        costs_word_ops = compute_costs_word_ops(num_word_ops)
        costs_para_ops = compute_costs_para_ops(num_split_merge, num_para_ops)

        if force_para_ops or costs_para_ops <= costs_word_ops:
            phrase.num_ops = num_split_merge + phrase.num_para_ops
            phrase.num_ops_abs = num_split_merge + phrase.num_para_ops_abs
            phrase.vis = phrase.vis_para_ops
            phrase.op_type = "para"
        else:
            phrase.num_ops = phrase.num_word_ops
            phrase.num_ops_abs = phrase.num_word_ops_abs
            phrase.vis = phrase.vis_word_ops
            phrase.op_type = "words"

        # Add splits and merges.
        if getattr(phrase, "split_before", False):
            phrase.num_ops["num_para_splits"] += 1
            phrase.num_ops_abs["num_para_splits"] += 1
            phrase.vis = "%s%s" % (vis.visualize_split(), phrase.vis)
        if getattr(phrase, "merge_before", False):
            phrase.num_ops["num_para_merges"] += 1
            phrase.num_ops_abs["num_para_merges"] += 1
            phrase.vis = "%s%s" % (vis.visualize_merge(), phrase.vis)

        # Update visualization and num ops.
        diff_result.num_ops.update(phrase.num_ops)
        diff_result.num_ops_abs.update(phrase.num_ops_abs)
        vis_parts.append(phrase.vis)

    def compute_rel_num_ops(key, div):
        result = diff_result.num_ops_abs.get(key, 0) / max(div, 1)
        diff_result.num_ops_rel[key] = result

    # Compute the relative number of operations.
    compute_rel_num_ops("num_para_splits", diff_result.num_paras_target - 1)
    compute_rel_num_ops("num_para_merges", diff_result.num_paras_target - 1)
    compute_rel_num_ops("num_para_rearranges", diff_result.num_words_target)
    compute_rel_num_ops("num_para_inserts", diff_result.num_words_target)
    compute_rel_num_ops("num_para_deletes", diff_result.num_words_target)
    compute_rel_num_ops("num_word_inserts", diff_result.num_words_target)
    compute_rel_num_ops("num_word_deletes", diff_result.num_words_target)
    compute_rel_num_ops("num_word_replaces", diff_result.num_words_target)

    diff_result.vis = "".join(vis_parts)
    diff_result.num_ops += Counter()
    diff_result.num_ops_abs += Counter()
    diff_result.num_ops_rel += Counter()

# ==============================================================================
# Rearrange methods.


def rearrange(diff_result):
    for phrase in diff_result.phrases:
        # Decide whether to rearrange the rearrange candidates or not.
        if hasattr(phrase, "rearrange_candidates"):
            rearrange_phrase(phrase)

    # Add the rearrange phrases if any.
    rearranged_phrases = []
    for phrase in diff_result.phrases:
        phrase_parts = []
        if not phrase.is_empty():
            phrase_parts.append(phrase)
        if hasattr(phrase, "applied_rearranges"):
            # phrase.applied_rearranges.sort(key=lambda x: x.pos_target)
            phrase_parts.extend(phrase.applied_rearranges)
            phrase_parts.sort(key=lambda x: x.first_word_target.pos
                              if x.first_word_target is not None
                              else (-1, -1))
        rearranged_phrases.extend(phrase_parts)

    diff_result.phrases = rearranged_phrases


def rearrange_phrase(phrase):
    """ Handles a rearrange phrase. """

    phrase.rearrange_candidates.sort(key=lambda x: x.pos_actual)

    for p in phrase.rearrange_candidates:
        # Compute the word operations.
        compute_word_ops_replace_phrase(p)

        # Compute para ops.
        force_para_ops = compute_para_ops_rearrange_phrase(p)
        p.num_para_ops += p.sub_diff_result.num_ops

        # Choose between word and para operation.
        num_word_ops = p.num_word_ops
        num_para_ops = p.num_para_ops
        num_split_merge = p.num_para_ops_split_merge

        costs_word_ops = compute_costs_word_ops(num_word_ops)
        costs_para_ops = compute_costs_para_ops(num_split_merge, num_para_ops)

        if force_para_ops or costs_para_ops <= costs_word_ops:
            # Do the rearrange.
            to_remove = set(p.words_actual)
            words_actual = p.source_phrase.words_actual
            words_actual = [x for x in words_actual if x not in to_remove]
            p.source_phrase.words_actual = words_actual

            to_remove = set(p.words_target)
            words_target = p.target_phrase.words_target
            words_target = [x for x in words_target if x not in to_remove]
            p.target_phrase.words_target = words_target

            if not hasattr(p.target_phrase, "applied_rearranges"):
                p.target_phrase.applied_rearranges = [p]
            else:
                p.target_phrase.applied_rearranges.append(p)

# ==============================================================================


def split_and_merge(diff_result, junk=[]):
    """
    Identifies the positions where to split and where to merge the given
    phrases.
    """

    # The splitted and merged phrases.
    split_and_merged_phrases = []
    # Flag to indicate whether we have to add a SPLIT in front of next phrase.
    split_before_subphrase = False
    # Flag to indicate whether we have to add a MERGE in front of next phrase.
    merge_before_subphrase = False

    pre_word_actual = None
    pre_word_target = None

    # Iterate through the phrases.
    for phrase in diff_result.phrases:
        if diff_utils.ignore_phrase(phrase, junk):
            split_and_merged_phrases.append(phrase)
            continue

        if isinstance(phrase, para_diff_rearr.DiffRearrangePhrase):
            is_break_before = is_break_before_target(phrase=phrase)
            is_break_after = is_break_after_target(phrase=phrase)
            phrase.merge_before = not is_break_before
            phrase.merge_after = not is_break_after
            split_and_merged_phrases.append(phrase)
            continue

        # The position where the current phrase was interrupted last time.
        prev_phrase_break_pos = 0
        actual = phrase.words_actual
        target = phrase.words_target

        for i in range(max(len(actual), len(target))):
            word_actual = actual[i] if i < len(actual) else None
            word_target = target[i] if i < len(target) else None

            if word_actual is None or word_target is None:
                continue

            break_actual = is_break_before_actual(pre_word_actual, word_actual)
            break_target = is_break_before_target(pre_word_target, word_target)

            split = not break_actual and break_target
            merge = break_actual and not break_target

            if split or merge:
                # Take a subphrase.
                subphrase = phrase.subphrase(prev_phrase_break_pos, i)
                prev_phrase_break_pos = i
                if not subphrase.is_empty():
                    subphrase.split_before = split_before_subphrase
                    subphrase.merge_before = merge_before_subphrase
                    split_and_merged_phrases.append(subphrase)
                if split:
                    split_before_subphrase = True
                    merge_before_subphrase = False
                if merge:
                    split_before_subphrase = False
                    merge_before_subphrase = True

            pre_word_actual = word_actual
            pre_word_target = word_target

        # Don't forget the remaining part of the phrase.
        # If prev_phrase_break_pos is 0, there was no break. We can append the
        # phrase "as it is".
        if prev_phrase_break_pos != 0:
            phrase = phrase.subphrase(prev_phrase_break_pos, None)
        if not phrase.is_empty():
            phrase.split_before = split_before_subphrase
            phrase.merge_before = merge_before_subphrase
            split_and_merged_phrases.append(phrase)

        # End of phrase. Reset the split and merge flags.
        split_before_subphrase = False
        merge_before_subphrase = False
    diff_result.phrases = split_and_merged_phrases

# ==============================================================================


def compute_word_ops(phrase, junk):
    if phrase.is_empty():
        return
    elif isinstance(phrase, para_diff_rearr.DiffRearrangePhrase):
        return compute_word_ops_rearrange_phrase(phrase)
    elif diff_utils.ignore_phrase(phrase, junk):
        return compute_word_ops_ignore_phrase(phrase)
    elif isinstance(phrase, word_diff.DiffCommonPhrase):
        return compute_word_ops_common_phrase(phrase)
    elif isinstance(phrase, word_diff.DiffReplacePhrase):
        if phrase.num_words_actual == 0:
            return compute_word_ops_insert_phrase(phrase)
        elif phrase.num_words_target == 0:
            return compute_word_ops_delete_phrase(phrase)
        else:
            return compute_word_ops_replace_phrase(phrase)


def compute_para_ops(phrase, junk):
    if phrase.is_empty():
        return
    elif isinstance(phrase, para_diff_rearr.DiffRearrangePhrase):
        return compute_para_ops_rearrange_phrase(phrase)
    elif diff_utils.ignore_phrase(phrase, junk):
        return compute_para_ops_ignore_phrase(phrase)
    elif isinstance(phrase, word_diff.DiffCommonPhrase):
        return compute_para_ops_common_phrase(phrase)
    elif isinstance(phrase, word_diff.DiffReplacePhrase):
        if phrase.num_words_actual == 0:
            return compute_para_ops_insert_phrase(phrase)
        elif phrase.num_words_target == 0:
            return compute_para_ops_delete_phrase(phrase)
        else:
            return compute_para_ops_replace_phrase(phrase)

# ==============================================================================

# The multiplication factor on computing the costs for paragraph operations.
COST_FACTOR_PARA_OPS = 1.1
# The multiplication factor on computing the costs of word operations.
COST_FACTOR_WORD_OPS = 1


def compute_costs_para_ops(split_merge_ops, para_ops):
    """ Returns the total costs for the given para operations. """
    if split_merge_ops is None or para_ops is None:
        return float("inf")
    else:
        sum_split_merge = sum(split_merge_ops[op] for op in split_merge_ops)
        sum_para_ops = sum(para_ops[op] for op in para_ops)
        return COST_FACTOR_PARA_OPS * sum_split_merge + sum_para_ops


def compute_costs_word_ops(word_ops):
    """ Returns the total costs for the given word operations. """
    if word_ops is None:
        return float("inf")
    else:
        return COST_FACTOR_WORD_OPS * sum(word_ops[op] for op in word_ops)

# ==============================================================================
# Word operations.


def compute_word_ops_ignore_phrase(phrase):
    phrase.num_word_ops = Counter()
    phrase.num_word_ops_abs = Counter()
    phrase.vis_word_ops = vis.gray(get_text(phrase.words_target))


def compute_word_ops_common_phrase(phrase):
    phrase.num_word_ops = Counter()
    phrase.num_word_ops_abs = Counter()
    phrase.vis_word_ops = get_text(phrase.words_target)


def compute_word_ops_rearrange_phrase(phrase):
    phrase.num_word_ops = None
    phrase.num_word_ops_abs = None
    phrase.vis_word_ops = ""


def compute_word_ops_insert_phrase(phrase):
    """ Returns the required word operations for given insert phrase. """

    phrase.num_word_ops = Counter({
        "num_word_inserts": len(phrase.words_target)
    })
    phrase.num_word_ops_abs = Counter(phrase.num_word_ops)
    phrase.vis_word_ops = vis.green(get_text(phrase.words_target))


def compute_word_ops_delete_phrase(phrase):
    """ Returns the required word operations for given delete phrase. """

    phrase.num_word_ops = Counter({
        "num_word_deletes": len(phrase.words_actual)
    })
    phrase.num_word_ops_abs = Counter(phrase.num_word_ops)
    phrase.vis_word_ops = vis.red(get_text(phrase.words_actual))


def compute_word_ops_replace_phrase(phrase):
    """ Returns the required word operations for given substitute phrase. """

    num_words_target = len(phrase.words_target)

    phrase.num_word_ops = Counter({
        "num_word_replaces": num_words_target
    })
    phrase.num_word_ops_abs = Counter(phrase.num_word_ops)

    vis_word_ops = []
    vis_word_ops.append(vis.gray("["))
    vis_word_ops.append(vis.red(get_text(phrase.words_actual).strip()))
    vis_word_ops.append(vis.gray("/"))
    vis_word_ops.append(vis.green(get_text(phrase.words_target).strip()))
    vis_word_ops.append(vis.gray("]"))
    vis_word_ops.append(phrase.last_word_target.whitespaces)
    phrase.vis_word_ops = "".join(vis_word_ops)

# ==============================================================================
# Para ops.


def compute_para_ops_ignore_phrase(phrase):
    phrase.num_para_ops = None
    phrase.num_para_ops_abs = None
    phrase.num_para_ops_split_merge = None
    phrase.vis_para_ops = ""


def compute_para_ops_common_phrase(phrase):
    phrase.num_para_ops = None
    phrase.num_para_ops_abs = None
    phrase.num_para_ops_split_merge = None
    phrase.vis_para_ops = ""


def compute_para_ops_rearrange_phrase(phrase):
    """ Returns the required para operations for given insert phrase. """

    sm = phrase.num_para_ops_split_merge = Counter()

    # break_before = is_break_before(phrase.first_word_target)
    # break_after = is_break_after(phrase.last_word_target)
    # sm["num_para_splits"] += (not break_before and not break_after)
    # sm["num_para_merges"] += (not break_before)
    # sm["num_para_merges"] += (not break_after)

    break_before = is_break_before_actual(phrase=phrase)
    break_after = is_break_after_actual(phrase=phrase)
    sm["num_para_splits"] += (not break_before)
    sm["num_para_splits"] += (not break_after)
    sm["num_para_merges"] += (not break_before and not break_after)

    sub_num_ops = phrase.sub_diff_result.num_ops
    phrase.num_para_ops = sub_num_ops + Counter({
        "num_para_rearranges": 1
    })
    phrase.num_para_ops_abs = Counter({
        "num_para_rearranges": len(phrase.words_actual)
    })

    # Create visualization.
    sm_str = "(#S:%s,#M:%s)" % (sm["num_para_splits"], sm["num_para_merges"])
    sm_vis = vis.gray(sm_str)
    vis_str = "%s%s" % (phrase.sub_diff_result.vis, sm_vis)
    phrase.vis_para_ops = vis.blue_bg(vis_str)

    return break_before and break_after


def compute_para_ops_insert_phrase(phrase):
    """ Returns the required para operations for given insert phrase. """

    sm = phrase.num_para_ops_split_merge = Counter()

    break_before = is_break_before_target(phrase=phrase)
    break_after = is_break_after_target(phrase=phrase)
    sm["num_para_splits"] += (not break_before and not break_after)
    sm["num_para_merges"] += (not break_before)
    sm["num_para_merges"] += (not break_after)

    phrase.num_para_ops = Counter({
        "num_para_inserts": 1
    })
    phrase.num_para_ops_abs = Counter({
        "num_para_inserts": len(phrase.words_target)
    })

    # Compute visualization.
    sm_str = "(#S:%s,#M:%s)" % (sm["num_para_splits"], sm["num_para_merges"])
    sm_vis = vis.gray(sm_str)
    vis_str = "%s%s" % (get_text(phrase.words_target).strip(), sm_vis)
    vis_str += phrase.last_word_target.whitespaces
    phrase.vis_para_ops = vis.green_bg(vis_str)

    return break_before and break_after


def compute_para_ops_delete_phrase(phrase):
    """ Returns the required para operations for given delete phrase. """

    sm = phrase.num_para_ops_split_merge = Counter()

    break_before = is_break_before_actual(phrase=phrase)
    break_after = is_break_after_actual(phrase=phrase)
    sm["num_para_splits"] += (not break_before)
    sm["num_para_splits"] += (not break_after)
    sm["num_para_merges"] += (not break_before and not break_after)

    phrase.num_para_ops = Counter({
        "num_para_deletes": 1
    })
    phrase.num_para_ops_abs = Counter({
        "num_para_deletes": len(phrase.words_actual)
    })

    # Compute visualization.
    sm_str = "(#S:%s,#M:%s)" % (sm["num_para_splits"], sm["num_para_merges"])
    sm_vis = vis.gray(sm_str)
    vis_str = "%s%s" % (get_text(phrase.words_actual).strip(), sm_vis)
    vis_str += phrase.last_word_actual.whitespaces
    phrase.vis_para_ops = vis.red_bg(vis_str)

    return break_before and break_after


def compute_para_ops_replace_phrase(phrase):
    """ Returns the required para operations for given substitute phrase. """

    sm = Counter()
    break_before = is_break_before_actual(phrase=phrase)
    break_after = is_break_after_actual(phrase=phrase)
    sm["num_para_splits"] += (not break_before)
    sm["num_para_splits"] += (not break_after)
    sm["num_para_merges"] += (not break_before and not break_after)
    phrase.num_para_ops_split_merge = sm

    phrase.num_para_ops = Counter({
        "num_para_deletes": 1,
        "num_para_inserts": 1
    })
    phrase.num_para_ops_abs = Counter({
        "num_para_deletes": len(phrase.words_actual),
        "num_para_inserts": len(phrase.words_target)
    })

    # Compute visualization.
    sm_str = "(#S:%s,#M:%s)" % (sm["num_para_splits"], sm["num_para_merges"])
    sm_vis = vis.gray(sm_str)
    vis2_str = "%s%s" % (get_text(phrase.words_actual).strip(), sm_vis)
    vis2_str += phrase.last_word_actual.whitespaces

    vis1_str = get_text(phrase.words_target).strip()
    vis1_str += phrase.last_word_target.whitespaces

    phrase.vis_para_ops = vis.red_bg(vis2_str) + vis.green_bg(vis1_str)

    return break_before and break_after

# ==============================================================================
# Some util methods.


def is_break_before_actual(prev_word=False, word=False, phrase=False):
    if phrase is False and word is False:
        return False
    if word is False:
        word = phrase.first_word_actual
    if word is None:
        return False
    if prev_word is False:
        prev_word = word.prev
    if prev_word is None:
        return True
    return is_break_between_words(prev_word, word)


def is_break_after_actual(word=False, next_word=False, phrase=False):
    if phrase is False and word is False:
        return False
    if word is False:
        word = phrase.last_word_actual
    if word is None:
        return False
    if next_word is False:
        next_word = word.next
    if next_word is None:
        return True
    return is_break_between_words(word, next_word)


def is_break_before_target(prev_word=False, word=False, phrase=False):
    if phrase is False and word is False:
        return False
    if word is False:
        word = phrase.first_word_target
    if word is None:
        return False
    if prev_word is False:
        prev_word = word.prev
    if prev_word is None:
        return True
    return is_break_between_words(prev_word, word)


def is_break_after_target(word=False, next_word=False, phrase=False):
    if phrase is False and word is False:
        return False
    if word is False:
        word = phrase.last_word_target
    if word is None:
        return False
    if next_word is False:
        next_word = word.next
    if next_word is None:
        return True
    return is_break_between_words(word, next_word)


def is_break_between_words(word1, word2, respect_split_merges=True):
    if word1 is None or word2 is None:
        return False

    if respect_split_merges and word1.phrase != word2.phrase:
        if getattr(word1.phrase, "split_after", False):
            return True
        if getattr(word2.phrase, "split_before", False):
            return True
        if getattr(word1.phrase, "merge_after", False):
            return False
        if getattr(word2.phrase, "merge_before", False):
            return False
    return word1.para != word2.para


def get_text(words):
    """ Returns the (unnormalized) text composed from the given words."""
    return "".join([x.unnormalized_with_whitespaces for x in words])
