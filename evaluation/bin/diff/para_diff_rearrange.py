import doc_diff
import logging

from word_diff import DiffReplacePhrase, DiffPhrase
from collections import Counter
from queue import PriorityQueue

logging.basicConfig(
    format='%(asctime)s : %(levelname)s : %(module)s : %(message)s',
    level=logging.INFO
)
log = logging.getLogger(__name__)


def rearrange(diff_result, min_rearrange_length=3, refuse_common_threshold=0,
              junk=[]):
    """
    Tries to rearrange phrases.
    """

    # A queue of matching but not settled (delete_window, insert_window) pairs
    # ordered by number of common words.
    pq = PriorityQueue()
    # The matching but not settled (delete_window, insert_window) pairs
    # grouped by insert_window.
    pairs_per_insert_window = {}

    def put_into_queue(pair):
        """
        Puts the given (delete_window, insert_window) pair into the queue if it
        is not None.
        """
        if pair is None:
            return

        pq.put(pair)

        # Register the pair by insert window.
        if pair.insert_window in pairs_per_insert_window:
            pairs_per_insert_window[pair.insert_window].append(pair)
        else:
            pairs_per_insert_window[pair.insert_window] = [pair]

    # Create index of all unmatched insert windows, grouped by insert phrases.
    insert_windows_index = {}
    for phrase in diff_result.phrases:
        if isinstance(phrase, DiffReplacePhrase):
            if phrase.num_words_target > 0:
                insert_windows_index[phrase] = {InsertWindow(phrase)}

    # Find unmatched insert window candidates for each unmatched delete window.
    for phrase in diff_result.phrases:
        if isinstance(phrase, DiffReplacePhrase):
            if phrase.num_words_actual > 0:
                # Find a matching insert window.
                pair = find_matching_insert_window(
                    DeleteWindow(phrase), insert_windows_index,
                    min_rearrange_length=min_rearrange_length)
                put_into_queue(pair)

    while not pq.empty():
        pair = pq.get()

        # Ignore the pair if it has become obsolete in the meanwhile.
        if getattr(pair, "obsolete", False):
            continue

        # Settle the matching pair.
        res = match_to_insert_window(pair)

        # The matched delete window.
        matched_delete_window = res.get("matched_delete_window")
        # The matched insert window.
        matched_insert_window = res.get("matched_insert_window")
        # The new unmatched delete windows.
        new_delete_windows = res.get("new_delete_windows")
        # The new unmatched insert windows.
        new_insert_windows = res.get("new_insert_windows")

        # Append the match to result.
        actual = matched_delete_window.words
        pos_actual = actual[0].pos_actual
        target = matched_insert_window.words
        pos_target = target[0].pos_target

        rearrange_phrase = DiffRearrangePhrase(
            pos_actual, actual, pos_target, target,
            refuse_common_threshold=refuse_common_threshold,
            junk=junk)

        rearrange_phrase.source_phrase = matched_delete_window.phrase
        rearrange_phrase.target_phrase = matched_insert_window.phrase

        source_phrase = rearrange_phrase.source_phrase
        if not hasattr(source_phrase, "rearrange_candidates"):
            source_phrase.rearrange_candidates = [rearrange_phrase]
        else:
            source_phrase.rearrange_candidates.append(rearrange_phrase)

        # Update the insert_windows_index:
        # Remove the affected insert window and append the new insert windows.
        index_set = insert_windows_index[matched_insert_window.phrase]
        index_set.remove(pair.insert_window)
        index_set.update(new_insert_windows)

        # Recompute matching insert window for new unmatched delete windows.
        for delete_window in new_delete_windows:
            new_pair = find_matching_insert_window(
                delete_window, insert_windows_index,
                min_rearrange_length=min_rearrange_length)
            put_into_queue(new_pair)

        # Recompute matching insert window for all delete windows which was
        # also mapped to the affected insert window.
        others = pairs_per_insert_window.pop(pair.insert_window, [])
        for other_pair in others:
            if pair == other_pair:
                continue
            other_pair.obsolete = True
            new_pair = find_matching_insert_window(
                other_pair.delete_window, insert_windows_index,
                min_rearrange_length=min_rearrange_length)
            put_into_queue(new_pair)


def find_matching_insert_window(
        delete_window,
        insert_windows_index,
        min_rearrange_length=1,
        max_num=3):
    """ Finds a matching insert window for the given delete window. """
    # Abort if length of delete_window is smaller than min_rearrange_length.
    if len(delete_window.words) < min_rearrange_length:
        return

    # Find some candidates.
    index = Counter()
    for phrase in insert_windows_index:
        for insert_window in insert_windows_index[phrase]:
            # Abort if the length of insert_window < min_rearrange_length.
            if len(insert_window.words) < min_rearrange_length:
                continue
            # Count the total number of common words.
            common_grams = get_num_common_grams(
                delete_window, insert_window, q=min_rearrange_length)
            if common_grams > 0:
                index[insert_window] = common_grams
    candidates = index.most_common(max_num)

    # Choose the best matching candidate.
    max_score = 0
    max_insert_window = None
    insert_window_start, insert_window_end = None, None
    delete_window_start, delete_window_end = None, None
    for insert_window, freq in candidates:
        score, delete_pos, insert_pos = sw(delete_window, insert_window)

        if score is not None and score > max_score:
            max_score = score
            max_insert_window = insert_window
            insert_window_start, insert_window_end = insert_pos
            delete_window_start, delete_window_end = delete_pos

    if max_score == 0 or max_insert_window is None:
        return

    pair = MatchingPair(max_score, delete_window, max_insert_window)
    pair.insert_window_start = insert_window_start
    pair.insert_window_end = insert_window_end
    pair.delete_window_start = delete_window_start
    pair.delete_window_end = delete_window_end

    return pair


def match_to_insert_window(pair):
    """ Settles the given matching pair. """

    delete = pair.delete_window
    del_phrase = delete.phrase
    del_start = pair.delete_window_start
    del_end = pair.delete_window_end

    insert = pair.insert_window
    ins_phrase = insert.phrase
    ins_start = pair.insert_window_start
    ins_end = pair.insert_window_end

    # Create matching delete window.
    matched_delete = DeleteWindow(del_phrase, del_start, del_end, matched=True)
    # Create matching insert window.
    matched_insert = InsertWindow(ins_phrase, ins_start, ins_end, matched=True)

    # Create the new unmatched delete windows.
    new_delete_windows = []
    if del_start > delete.phrase_start:
        sub = delete.sub_window(delete.phrase_start, del_start)
        new_delete_windows.append(sub)
    if del_end < delete.phrase_end:
        sub = delete.sub_window(del_end, delete.phrase_end)
        new_delete_windows.append(sub)

    # Create the new unmatched insert windows.
    new_insert_windows = []
    if ins_start > insert.phrase_start:
        sub = insert.sub_window(insert.phrase_start, ins_start)
        new_insert_windows.append(sub)
    if ins_end < insert.phrase_end:
        sub = insert.sub_window(ins_end, insert.phrase_end)
        new_insert_windows.append(sub)

    return {
        "matched_delete_window": matched_delete,
        "matched_insert_window": matched_insert,
        "new_delete_windows": new_delete_windows,
        "new_insert_windows": new_insert_windows,
    }

# ==============================================================================


def sw(delete, insert, score=2, mis_score=-1):
    """
    Computes the Smith-Waterman score for the given delete and the given
    insert.
    """
    def is_match(delete_word, insert_word):
        return str(delete_word) == str(insert_word)

    x = len(delete.words) + 1
    y = len(insert.words) + 1
    m = [[0 for j in range(y)] for i in range(x)]

    # Fill the matrices and keep track of the max score.
    max_score = 0
    max_pos = None
    for i in range(1, x):
        for j in range(1, y):
            d_word = delete.words[i - 1]
            i_word = insert.words[j - 1]
            score_diag = m[i - 1][j - 1]
            score_up = m[i - 1][j]
            score_left = m[i][j - 1]

            # if i_word.exclude:
            #    score_cur = max(1, score_diag, score_up, score_left)
            #    print("  ", score_cur, (i, j), d_word, i_word)
            # else:
            score_diag += score if is_match(d_word, i_word) else mis_score
            score_up += mis_score
            score_left += mis_score
            score_cur = max(0, score_diag, score_up, score_left)
            if score_cur >= max_score:
                max_score = score_cur
                max_pos = (i, j)
            m[i][j] = score_cur

    if max_pos is None:
        return None, None, None

    # Do the traceback.
    i, j = max_pos
    while True:
        d_word = delete.words[i - 1]
        i_word = insert.words[j - 1]
        score_diag = m[i - 1][j - 1]
        score_up = m[i - 1][j]
        score_left = m[i][j - 1]
        score_cur = m[i][j]

        is_word_match = is_match(d_word, i_word)
        expected = score_cur - (score if is_word_match else mis_score)
        if score_diag > 0 and score_diag == expected:
            i -= 1
            j -= 1
            continue

        expected = score_cur - mis_score
        if score_up > 0 and score_up == expected:
            i -= 1
            continue

        expected = score_cur - mis_score
        if score_left > 0 and score_left == expected:
            j -= 1
            continue
        break

    delete_start = delete.phrase_start + i - 1
    delete_end = delete.phrase_start + max_pos[0]
    insert_start = insert.phrase_start + j - 1
    insert_end = insert.phrase_start + max_pos[1]

    return max_score, (delete_start, delete_end), (insert_start, insert_end)


def get_num_common_grams(delete, insert, q=5):
    """ Returns the number of common words in given delete and given insert."""
    delete_grams_freqs = delete.get_q_grams_freq(q)
    insert_grams_freqs = insert.get_q_grams_freq(q)
    common_grams_freqs = delete_grams_freqs & insert_grams_freqs
    return sum(common_grams_freqs.values())

# ==============================================================================


class Window:
    """ An excerpt of a phrase. """
    def __init__(self, phrase, phrase_start=0, phrase_end=None, matched=False,
                 min_rearrange_length=1):
        self.phrase = phrase
        self.phrase_start = phrase_start
        self.phrase_end = phrase_end
        if phrase_end is None:
            self.phrase_end = len(self.get_phrase_words())
        self.words = self.get_phrase_words()[phrase_start:phrase_end]
        self.matched = matched
        for word in self.words:
            word.is_rearranged = matched
        self.q_grams_freq_index = {}
        self.q_words = [str(x) for x in self.words if not x.exclude]

    def get_q_grams_freq(self, q=5):
        if q not in self.q_grams_freq_index:
            self.q_grams_freq_index[q] = self.compute_q_grams_freq(q)
        return self.q_grams_freq_index[q]

    def compute_q_grams_freq(self, q=5):
        return Counter(["".join(x) for x in self.compute_q_grams(q)])

    def compute_q_grams(self, q=5):
        return [self.q_words[i:i+q] for i in range(len(self.q_words)-q+1)]

    def sub_window(self, phrase_start, phrase_end):
        return type(self)(self.phrase, phrase_start, phrase_end)

    def __str__(self):
        return "(phrase: %s, phrase_start: %s, phrase_end: %s, words: %s)" \
            % ("", self.phrase_start, self.phrase_end, self.words)

    def __repr__(self):
        return self.__str__()


class DeleteWindow(Window):
    def get_phrase_words(self):
        return self.phrase.words_actual


class InsertWindow(Window):
    def get_phrase_words(self):
        return self.phrase.words_target


class MatchingPair:
    """
    A pair of matching delete window and insert window.
    """
    def __init__(self, score, delete_window, insert_window):
        self.score = score
        self.delete_window = delete_window
        self.insert_window = insert_window

    def __lt__(self, other):
        """ Define an order to be able to put it into a queue. """
        return self.score > other.score

    def __str__(self):
        return "(id: %s, score: %s, DeleteWindow: %s, InsertWindows: %s)" \
            % (id(self), self.score, self.delete_window.words,
               self.insert_window.words)

# ==============================================================================


class DiffRearrangePhrase(DiffPhrase):
    """ A phrase of rearranged words. """

    def __init__(self, pos_actual, words_actual, pos_target, words_target,
                 refuse_common_threshold=0, junk=[]):
        super(DiffRearrangePhrase, self).__init__(
            pos_actual, words_actual, pos_target, words_target)
        self.refuse_common_threshold = refuse_common_threshold
        self.junk = junk
        self.sub_diff_result = doc_diff.doc_diff(
            self.words_actual, self.words_target,
            refuse_common_threshold=refuse_common_threshold,
            junk=junk)

    def get_str_pattern(self):
        """ Returns the pattern to use on creating string representation. """
        return "[~ %s, %s]"

    # Override
    def subphrase(self, start, end):
        """
        Creates a subphrase from this phrase, starting at given start index
        (inclusive) and ending at given end index (exclusively).
        This method is needed in para_diff to divide a phrase in order to meet
        paragraph boundaries.
        """
        words_actual = self.words_actual[start:end]
        words_target = self.words_target[start:end]
        pos_actual = self.pos_actual
        if len(words_actual) > 0:
            pos_actual = words_actual[0].pos_actual
        pos_target = self.pos_target
        if len(words_target) > 0:
            pos_target = words_target[0].pos_target
        # Call the constructor of the concrete subtype.
        return type(self)(pos_actual, words_actual, pos_target, words_target,
                          refuse_common_threshold=self.refuse_common_threshold,
                          junk=self.junk)
