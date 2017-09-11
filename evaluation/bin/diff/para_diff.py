import word_diff
from para_diff_rearrange import rearrange

import diff_utils
import logging

logging.basicConfig(
    format='%(asctime)s : %(levelname)s : %(module)s : %(message)s',
    level=logging.INFO
)
log = logging.getLogger(__name__)


def para_diff_from_strings(
        actual,
        target,
        rearrange_phrases=False,
        min_rearrange_length=3,
        refuse_common_threshold=0,
        compose_characters=False,
        para_delimiter="\n\s*\n\s*",
        word_delimiter="\s+",
        to_lower=False,
        specialchars_pattern="\W+",
        excludes=[],
        junk=[],
        pdf_path=None):
    """ Does a para diff based on given strings.
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

    # Transform target to flat list of DiffWords.
    target = diff_utils.string_to_diff_words(
        target,
        flatten=True,
        compose_characters=compose_characters,
        para_delimiter=para_delimiter,
        word_delimiter=word_delimiter,
        to_lower=to_lower,
        specialchars_pattern=specialchars_pattern,
        excludes=excludes)

    return para_diff(
        actual,
        target,
        rearrange_phrases=rearrange_phrases,
        min_rearrange_length=min_rearrange_length,
        refuse_common_threshold=refuse_common_threshold,
        junk=junk,
        pdf_path=pdf_path)


def para_diff(
        actual,
        target,
        rearrange_phrases=False,
        min_rearrange_length=3,
        refuse_common_threshold=0,
        junk=[],
        pdf_path=None):
    """ Does a para_diff based on flat lists of DiffWord objects. """

    actual = [] if actual is None else actual
    target = [] if target is None else target

    # Compute the phrases.
    result = word_diff.word_diff(
        actual, target, refuse_common_threshold=refuse_common_threshold)

    break_phrases(result, junk=junk)

    # Provide some metadata.
    result.num_paras_actual = actual[-1].para + 1 if len(actual) > 0 else 0
    result.num_paras_target = target[-1].para + 1 if len(target) > 0 else 0

    if rearrange_phrases:
        # Rearrange the phrases.
        rearrange(
            result, min_rearrange_length=min_rearrange_length,
            refuse_common_threshold=refuse_common_threshold,
            junk=junk, pdf_path=pdf_path)

    # Split and merge the phrases to meet paragraph boundaries.
    # split_and_merge(result)

    return result


def break_phrases(diff_result, junk=[]):
    result = []

    for phrase in diff_result.phrases:
        if diff_utils.ignore_phrase(phrase, junk):
            # Don't break phrases to ignore.
            result.append(phrase)
        elif isinstance(phrase, word_diff.DiffCommonPhrase):
            result.extend(break_common_phrase(phrase))
        elif isinstance(phrase, word_diff.DiffReplacePhrase):
            result.extend(break_replace_phrase(phrase))

    diff_result.phrases = result


def break_common_phrase(phrase):
    """ Breaks the given common phrase into actual paras. """
    result = []
    prev_break = 0
    pos_actual = phrase.pos_actual
    pos_target = phrase.pos_target
    for i in range(1, len(phrase.words_actual)):
        prev_word_actual = phrase.words_actual[i - 1]
        word_actual = phrase.words_actual[i]

        if prev_word_actual.para != word_actual.para:
            sub_words_actual = phrase.words_actual[prev_break: i]
            sub_words_target = phrase.words_target[prev_break: i]
            sub_phrase = type(phrase)(pos_actual, sub_words_actual,
                                      pos_target, sub_words_target)
            if not sub_phrase.is_empty():
                result.append(sub_phrase)
            pos_actual += len(sub_words_actual)
            pos_target += len(sub_words_target)
            prev_break = i

    sub_words_actual = phrase.words_actual[prev_break:]
    sub_words_target = phrase.words_target[prev_break:]
    sub_phrase = type(phrase)(pos_actual, sub_words_actual,
                              pos_target, sub_words_target)
    if not sub_phrase.is_empty():
        result.append(sub_phrase)

    return result


def break_replace_phrase(phrase):
    """ Breaks the given common phrase into actual paras. """

    result = []
    pos_actual = phrase.pos_actual
    pos_target = phrase.pos_target
    words_actual = phrase.words_actual
    words_target = phrase.words_target
    paras_actual = break_down_into_paras(words_actual)
    paras_target = break_down_into_paras(words_target)

    for i in range(0, max(len(paras_actual), len(paras_target))):
        para_actual = paras_actual[i] if i < len(paras_actual) else []
        para_target = paras_target[i] if i < len(paras_target) else []

        p = type(phrase)(pos_actual, para_actual, pos_target, para_target)
        pos_actual += len(para_actual)
        pos_target += len(para_target)

        result.append(p)

    return result


def break_down_into_paras(words):
    if words is None or len(words) == 0:
        return []

    paras = []
    prev_break = 0
    for i in range(1, len(words)):
        prev_word = words[i - 1]
        word = words[i]
        if prev_word.para != word.para:
            paras.append(words[prev_break:i])
            prev_break = i
    paras.append(words[prev_break:])
    return paras


if __name__ == "__main__":
    x = """X Y Z"""
    y = """A\n\nB C"""
    result = para_diff_from_strings(x, y)
