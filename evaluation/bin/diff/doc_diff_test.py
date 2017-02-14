import unittest

from doc_diff import doc_diff_from_strings


class DocDiffTest(unittest.TestCase):
    """ Tests for doc_diff."""

    def test_doc_diff_from_strings_border_cases(self):
        in1 = None
        in2 = None
        phrases = "[]"  # NOQA
        num_ops = {}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = ""
        in2 = ""
        phrases = "[]"  # NOQA
        num_ops = {}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = None
        in2 = "A"
        phrases = "[[/ [], ['A']]]"  # NOQA
        num_ops = {'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = ""
        in2 = "A"
        phrases = "[[/ [], ['A']]]"  # NOQA
        num_ops = {'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = None
        in2 = "A B"
        phrases = "[[/ [], ['A', 'B']]]"  # NOQA
        num_ops = {'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = None
        in2 = "A\n\nB"
        phrases = "[[/ [], ['A']], [/ [], ['B']]]"  # NOQA
        num_ops = {'num_para_inserts': 2}
        self.evaluate(in1, in2, phrases, num_ops)

    def test_doc_diff_from_strings_equal_inputs(self):
        in1 = "A"
        in2 = "A"
        phrases = "[[= ['A'], ['A']]]"  # NOQA
        num_ops = {}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A B"
        in2 = "A B"
        phrases = "[[= ['A', 'B'], ['A', 'B']]]"  # NOQA
        num_ops = {}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A B C"
        in2 = "A B C"
        phrases = "[[= ['A', 'B', 'C'], ['A', 'B', 'C']]]"  # NOQA
        num_ops = {}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nB"
        in2 = "A\n\nB"
        phrases = "[[= ['A'], ['A']], [= ['B'], ['B']]]"  # NOQA
        num_ops = {}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nB\n\nC"
        in2 = "A\n\nB\n\nC"
        phrases = "[[= ['A'], ['A']], [= ['B'], ['B']], [= ['C'], ['C']]]"  # NOQA
        num_ops = {}
        self.evaluate(in1, in2, phrases, num_ops)

    def test_doc_diff_from_strings_word_deletes(self):
        in1 = "A B C"
        in2 = "A C"
        phrases = "[[= ['A'], ['A']], [/ ['B'], []], [= ['C'], ['C']]]"  # NOQA
        num_ops = {'num_word_deletes': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A B C"
        in2 = "B C"
        phrases = "[[/ ['A'], []], [= ['B', 'C'], ['B', 'C']]]"  # NOQA
        num_ops = {'num_word_deletes': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A B C"
        in2 = "A B"
        phrases = "[[= ['A', 'B'], ['A', 'B']], [/ ['C'], []]]"  # NOQA
        num_ops = {'num_word_deletes': 1}
        self.evaluate(in1, in2, phrases, num_ops)

    def test_doc_diff_from_strings_para_deletes(self):
        in1 = "A"
        in2 = None
        phrases = "[[/ ['A'], []]]"  # NOQA
        num_ops = {'num_para_deletes': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A"
        in2 = ""
        phrases = "[[/ ['A'], []]]"  # NOQA
        num_ops = {'num_para_deletes': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A B"
        in2 = None
        phrases = "[[/ ['A', 'B'], []]]"  # NOQA
        num_ops = {'num_para_deletes': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nB"
        in2 = None
        phrases = "[[/ ['A'], []], [/ ['B'], []]]"  # NOQA
        num_ops = {'num_para_deletes': 2}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A B"
        in2 = ""
        phrases = "[[/ ['A', 'B'], []]]"  # NOQA
        num_ops = {'num_para_deletes': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nB"
        in2 = ""
        phrases = "[[/ ['A'], []], [/ ['B'], []]]"  # NOQA
        num_ops = {'num_para_deletes': 2}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nB\n\nC"
        in2 = "A\n\nC"
        phrases = "[[= ['A'], ['A']], [/ ['B'], []], [= ['C'], ['C']]]"  # NOQA
        num_ops = {'num_para_deletes': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nB\n\nC"
        in2 = "B\n\nC"
        phrases = "[[/ ['A'], []], [= ['B'], ['B']], [= ['C'], ['C']]]"  # NOQA
        num_ops = {'num_para_deletes': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nB\n\nC"
        in2 = "A\n\nB"
        phrases = "[[= ['A'], ['A']], [= ['B'], ['B']], [/ ['C'], []]]"  # NOQA
        num_ops = {'num_para_deletes': 1}
        self.evaluate(in1, in2, phrases, num_ops)

    def test_doc_diff_from_strings_word_inserts(self):
        in1 = "A C"
        in2 = "A B C"
        phrases = "[[= ['A'], ['A']], [/ [], ['B']], [= ['C'], ['C']]]"  # NOQA
        num_ops = {'num_word_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "B C"
        in2 = "A B C"
        phrases = "[[/ [], ['A']], [= ['B', 'C'], ['B', 'C']]]"  # NOQA
        num_ops = {'num_word_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A B"
        in2 = "A B C"
        phrases = "[[= ['A', 'B'], ['A', 'B']], [/ [], ['C']]]"  # NOQA
        num_ops = {'num_word_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

    def test_doc_diff_from_strings_para_inserts(self):
        in1 = None
        in2 = "A"
        phrases = "[[/ [], ['A']]]"  # NOQA
        num_ops = {'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = ""
        in2 = "A"
        phrases = "[[/ [], ['A']]]"  # NOQA
        num_ops = {'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = None
        in2 = "A B"
        phrases = "[[/ [], ['A', 'B']]]"  # NOQA
        num_ops = {'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = None
        in2 = "A\n\nB"
        phrases = "[[/ [], ['A']], [/ [], ['B']]]"  # NOQA
        num_ops = {'num_para_inserts': 2}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = ""
        in2 = "A B"
        phrases = "[[/ [], ['A', 'B']]]"  # NOQA
        num_ops = {'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = ""
        in2 = "A\n\nB"
        phrases = "[[/ [], ['A']], [/ [], ['B']]]"  # NOQA
        num_ops = {'num_para_inserts': 2}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nC"
        in2 = "A\n\nB\n\nC"
        phrases = "[[= ['A'], ['A']], [/ [], ['B']], [= ['C'], ['C']]]"  # NOQA
        num_ops = {'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "B\n\nC"
        in2 = "A\n\nB\n\nC"
        phrases = "[[/ [], ['A']], [= ['B'], ['B']], [= ['C'], ['C']]]"  # NOQA
        num_ops = {'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nB"
        in2 = "A\n\nB\n\nC"
        phrases = "[[= ['A'], ['A']], [= ['B'], ['B']], [/ [], ['C']]]"  # NOQA
        num_ops = {'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

    def test_doc_diff_from_strings_word_replaces(self):
        in1 = "A B C"
        in2 = "A X C"
        phrases = "[[= ['A'], ['A']], [/ ['B'], ['X']], [= ['C'], ['C']]]"  # NOQA
        num_ops = {'num_word_replaces': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A B C"
        in2 = "X B C"
        phrases = "[[/ ['A'], ['X']], [= ['B', 'C'], ['B', 'C']]]"  # NOQA
        num_ops = {'num_word_replaces': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A B C"
        in2 = "A B X"
        phrases = "[[= ['A', 'B'], ['A', 'B']], [/ ['C'], ['X']]]"  # NOQA
        num_ops = {'num_word_replaces': 1}
        self.evaluate(in1, in2, phrases, num_ops)

    def test_doc_diff_from_strings_para_replaces(self):
        in1 = "A\n\nB\n\nC"
        in2 = "A\n\nX\n\nC"
        phrases = "[[= ['A'], ['A']], [/ ['B'], ['X']], [= ['C'], ['C']]]"  # NOQA
        num_ops = {'num_para_deletes': 1, 'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nB\n\nC"
        in2 = "X\n\nB\n\nC"
        phrases = "[[/ ['A'], ['X']], [= ['B'], ['B']], [= ['C'], ['C']]]"  # NOQA
        num_ops = {'num_para_deletes': 1, 'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nB\n\nC"
        in2 = "A\n\nB\n\nX"
        phrases = "[[= ['A'], ['A']], [= ['B'], ['B']], [/ ['C'], ['X']]]"  # NOQA
        num_ops = {'num_para_deletes': 1, 'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

    def test_doc_diff_from_strings_word_rearranges(self):
        in1 = "X A B"
        in2 = "A B X"
        phrases = "[[/ ['X'], []], [= ['A', 'B'], ['A', 'B']], [/ [], ['X']]]"  # NOQA
        num_ops = {'num_word_deletes': 1, 'num_word_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "X A B"
        in2 = "A X B"
        phrases = "[[/ ['X'], []], [= ['A'], ['A']], [/ [], ['X']], [= ['B'], ['B']]]"  # NOQA
        num_ops = {'num_word_deletes': 1, 'num_word_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A X B"
        in2 = "X A B"
        phrases = "[[/ ['A'], []], [= ['X'], ['X']], [/ [], ['A']], [= ['B'], ['B']]]"  # NOQA
        num_ops = {'num_word_deletes': 1, 'num_word_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A X B"
        in2 = "A B X"
        phrases = "[[= ['A'], ['A']], [/ ['X'], []], [= ['B'], ['B']], [/ [], ['X']]]"  # NOQA
        num_ops = {'num_word_deletes': 1, 'num_word_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A B X"
        in2 = "X A B"
        phrases = "[[/ [], ['X']], [= ['A', 'B'], ['A', 'B']], [/ ['X'], []]]"  # NOQA
        num_ops = {'num_word_deletes': 1, 'num_word_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A B X"
        in2 = "A X B"
        phrases = "[[= ['A'], ['A']], [/ ['B'], []], [= ['X'], ['X']], [/ [], ['B']]]"  # NOQA
        num_ops = {'num_word_deletes': 1, 'num_word_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        # With rearrange=True
        in1 = "X A B"
        in2 = "A B X"
        phrases = "[[/ ['X'], []], [= ['A', 'B'], ['A', 'B']], [/ [], ['X']]]"  # NOQA
        num_ops = {'num_word_deletes': 1, 'num_word_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

        in1 = "X A B"
        in2 = "A X B"
        phrases = "[[/ ['X'], []], [= ['A'], ['A']], [/ [], ['X']], [= ['B'], ['B']]]"  # NOQA
        num_ops = {'num_word_deletes': 1, 'num_word_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

        in1 = "A X B"
        in2 = "X A B"
        phrases = "[[/ ['A'], []], [= ['X'], ['X']], [/ [], ['A']], [= ['B'], ['B']]]"  # NOQA
        num_ops = {'num_word_deletes': 1, 'num_word_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

        in1 = "A X B"
        in2 = "A B X"
        phrases = "[[= ['A'], ['A']], [/ ['X'], []], [= ['B'], ['B']], [/ [], ['X']]]"  # NOQA
        num_ops = {'num_word_deletes': 1, 'num_word_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

        in1 = "A B X"
        in2 = "X A B"
        phrases = "[[/ [], ['X']], [= ['A', 'B'], ['A', 'B']], [/ ['X'], []]]"  # NOQA
        num_ops = {'num_word_deletes': 1, 'num_word_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

        in1 = "A B X"
        in2 = "A X B"
        phrases = "[[= ['A'], ['A']], [/ ['B'], []], [= ['X'], ['X']], [/ [], ['B']]]"  # NOQA
        num_ops = {'num_word_deletes': 1, 'num_word_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

    def test_doc_diff_from_strings_para_rearranges(self):
        in1 = "X\n\nA\n\nB"
        in2 = "A\n\nB\n\nX"
        phrases = "[[/ ['X'], []], [= ['A'], ['A']], [= ['B'], ['B']], [/ [], ['X']]]"  # NOQA
        num_ops = {'num_para_deletes': 1, 'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "X\n\nA\n\nB"
        in2 = "A\n\nX\n\nB"
        phrases = "[[/ ['X'], []], [= ['A'], ['A']], [/ [], ['X']], [= ['B'], ['B']]]"  # NOQA
        num_ops = {'num_para_deletes': 1, 'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nX\n\nB"
        in2 = "X\n\nA\n\nB"
        phrases = "[[/ ['A'], []], [= ['X'], ['X']], [/ [], ['A']], [= ['B'], ['B']]]"  # NOQA
        num_ops = {'num_para_deletes': 1, 'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nX\n\nB"
        in2 = "A\n\nB\n\nX"
        phrases = "[[= ['A'], ['A']], [/ ['X'], []], [= ['B'], ['B']], [/ [], ['X']]]"  # NOQA
        num_ops = {'num_para_deletes': 1, 'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nB\n\nX"
        in2 = "X\n\nA\n\nB"
        phrases = "[[/ [], ['X']], [= ['A'], ['A']], [= ['B'], ['B']], [/ ['X'], []]]"  # NOQA
        num_ops = {'num_para_deletes': 1, 'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nB\n\nX"
        in2 = "A\n\nX\n\nB"
        phrases = "[[= ['A'], ['A']], [/ ['B'], []], [= ['X'], ['X']], [/ [], ['B']]]"  # NOQA
        num_ops = {'num_para_deletes': 1, 'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        # With rearrange=True
        in1 = "X\n\nA\n\nB"
        in2 = "A\n\nB\n\nX"
        phrases = "[[= ['A'], ['A']], [= ['B'], ['B']], [~ ['X'], ['X']]]"  # NOQA
        num_ops = {'num_para_rearranges': 1}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

        in1 = "X\n\nA\n\nB"
        in2 = "A\n\nX\n\nB"
        phrases = "[[= ['A'], ['A']], [~ ['X'], ['X']], [= ['B'], ['B']]]"  # NOQA
        num_ops = {'num_para_rearranges': 1}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

        in1 = "A\n\nX\n\nB"
        in2 = "X\n\nA\n\nB"
        phrases = "[[= ['X'], ['X']], [~ ['A'], ['A']], [= ['B'], ['B']]]"  # NOQA
        num_ops = {'num_para_rearranges': 1}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

        in1 = "A\n\nX\n\nB"
        in2 = "A\n\nB\n\nX"
        phrases = "[[= ['A'], ['A']], [= ['B'], ['B']], [~ ['X'], ['X']]]"  # NOQA
        num_ops = {'num_para_rearranges': 1}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

        in1 = "A\n\nB\n\nX"
        in2 = "X\n\nA\n\nB"
        phrases = "[[~ ['X'], ['X']], [= ['A'], ['A']], [= ['B'], ['B']]]"  # NOQA
        num_ops = {'num_para_rearranges': 1}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

        in1 = "A\n\nB\n\nX"
        in2 = "A\n\nX\n\nB"
        phrases = "[[= ['A'], ['A']], [= ['X'], ['X']], [~ ['B'], ['B']]]"  # NOQA
        num_ops = {'num_para_rearranges': 1}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

        in1 = "X\n\nA B"
        in2 = "A B\n\nX"
        phrases = "[[= ['A', 'B'], ['A', 'B']], [~ ['X'], ['X']]]"  # NOQA
        num_ops = {'num_para_rearranges': 1}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

        in1 = "A B\n\nX"
        in2 = "X\n\nA B"
        phrases = "[[~ ['X'], ['X']], [= ['A', 'B'], ['A', 'B']]]"  # NOQA
        num_ops = {'num_para_rearranges': 1}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

    def test_doc_diff_from_strings_splits(self):
        in1 = "A B C"
        in2 = "A\n\nB C"
        phrases = "[[= ['A'], ['A']], S, [= ['B', 'C'], ['B', 'C']]]"  # NOQA
        num_ops = {'num_para_splits': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A B C"
        in2 = "A B\n\nC"
        phrases = "[[= ['A', 'B'], ['A', 'B']], S, [= ['C'], ['C']]]"  # NOQA
        num_ops = {'num_para_splits': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A B C"
        in2 = "A\n\nB\n\nC"
        phrases = "[[= ['A'], ['A']], S, [= ['B'], ['B']], S, [= ['C'], ['C']]]"  # NOQA
        num_ops = {'num_para_splits': 2}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nB C"
        in2 = "A\n\nB\n\nC"
        phrases = "[[= ['A'], ['A']], [= ['B'], ['B']], S, [= ['C'], ['C']]]"  # NOQA
        num_ops = {'num_para_splits': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A B\n\nC"
        in2 = "A\n\nB\n\nC"
        phrases = "[[= ['A'], ['A']], S, [= ['B'], ['B']], [= ['C'], ['C']]]"  # NOQA
        num_ops = {'num_para_splits': 1}
        self.evaluate(in1, in2, phrases, num_ops)

    def test_doc_diff_from_strings_merges(self):
        in1 = "A\n\nB C"
        in2 = "A B C"
        phrases = "[[= ['A'], ['A']], M, [= ['B', 'C'], ['B', 'C']]]"  # NOQA
        num_ops = {'num_para_merges': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A B\n\nC"
        in2 = "A B C"
        phrases = "[[= ['A', 'B'], ['A', 'B']], M, [= ['C'], ['C']]]"  # NOQA
        num_ops = {'num_para_merges': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nB\n\nC"
        in2 = "A B C"
        phrases = "[[= ['A'], ['A']], M, [= ['B'], ['B']], M, [= ['C'], ['C']]]"  # NOQA
        num_ops = {'num_para_merges': 2}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nB\n\nC"
        in2 = "A\n\nB C"
        phrases = "[[= ['A'], ['A']], [= ['B'], ['B']], M, [= ['C'], ['C']]]"  # NOQA
        num_ops = {'num_para_merges': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nB\n\nC"
        in2 = "A B\n\nC"
        phrases = "[[= ['A'], ['A']], M, [= ['B'], ['B']], [= ['C'], ['C']]]"  # NOQA
        num_ops = {'num_para_merges': 1}
        self.evaluate(in1, in2, phrases, num_ops)

    def test_doc_diff_from_strings_splits_and_merges(self):
        in1 = "A\n\nB C"
        in2 = "A B\n\nC"
        phrases = "[[= ['A'], ['A']], M, [= ['B'], ['B']], S, [= ['C'], ['C']]]"  # NOQA
        num_ops = {'num_para_merges': 1, 'num_para_splits': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A B\n\nC"
        in2 = "A\n\nB C"
        phrases = "[[= ['A'], ['A']], S, [= ['B'], ['B']], M, [= ['C'], ['C']]]"  # NOQA
        num_ops = {'num_para_merges': 1, 'num_para_splits': 1}
        self.evaluate(in1, in2, phrases, num_ops)

    def test_doc_diff_from_strings_word_insert_deletes_replaces(self):
        in1 = "A B C"
        in2 = "B C D"
        phrases = "[[/ ['A'], []], [= ['B', 'C'], ['B', 'C']], [/ [], ['D']]]"  # NOQA
        num_ops = {'num_word_deletes': 1, 'num_word_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A B C"
        in2 = "B X D"
        phrases = "[[/ ['A'], []], [= ['B'], ['B']], [/ ['C'], ['X', 'D']]]"  # NOQA
        num_ops = {'num_word_deletes': 1,
                   'num_word_inserts': 1,
                   'num_word_replaces': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "B C D"
        in2 = "B X D E"
        phrases = "[[= ['B'], ['B']], [/ ['C'], ['X']], [= ['D'], ['D']], [/ [], ['E']]]"  # NOQA
        num_ops = {'num_word_inserts': 1,
                   'num_word_replaces': 1}
        self.evaluate(in1, in2, phrases, num_ops)

    def test_doc_diff_from_strings_para_insert_deletes_replaces(self):
        in1 = "A\n\nB\n\nC"
        in2 = "B\n\nC\n\nD"
        phrases = "[[/ ['A'], []], [= ['B'], ['B']], [= ['C'], ['C']], [/ [], ['D']]]"  # NOQA
        num_ops = {'num_para_deletes': 1, 'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nB\n\nC"
        in2 = "B\n\nX\n\nD"
        phrases = "[[/ ['A'], []], [= ['B'], ['B']], [/ ['C'], ['X']], [/ [], ['D']]]"  # NOQA
        num_ops = {'num_para_deletes': 2, 'num_para_inserts': 2}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "B\n\nC\n\nD"
        in2 = "B\n\nX\n\nD\n\nE"
        phrases = "[[= ['B'], ['B']], [/ ['C'], ['X']], [= ['D'], ['D']], [/ [], ['E']]]"  # NOQA
        num_ops = {'num_para_inserts': 2,
                   'num_para_deletes': 1}
        self.evaluate(in1, in2, phrases, num_ops)

    def test_doc_diff_from_strings_splits_merges_within_replaces(self):
        in1 = "A X C D"
        in2 = "A B\n\nC D"
        phrases = "[[= ['A'], ['A']], [/ ['X'], ['B']], S, [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_para_splits': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A X C D"
        in2 = "A\n\nB C D"
        phrases = "[[= ['A'], ['A']], S, [/ ['X'], ['B']], [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_para_splits': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A X\n\nC D"
        in2 = "A B C D"
        phrases = "[[= ['A'], ['A']], [/ ['X'], ['B']], M, [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_para_merges': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nX C D"
        in2 = "A B C D"
        phrases = "[[= ['A'], ['A']], M, [/ ['X'], ['B']], [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_para_merges': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nX C D"
        in2 = "A B\n\nC D"
        phrases = "[[= ['A'], ['A']], M, [/ ['X'], ['B']], S, [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_para_splits': 1,
                   'num_para_merges': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A X\n\nC D"
        in2 = "A\n\nB C D"
        phrases = "[[= ['A'], ['A']], S, [/ ['X'], ['B']], M, [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_para_splits': 1,
                   'num_para_merges': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        ###
        in1 = "A\n\nX Y C D"
        in2 = "A B C D"
        phrases = "[[= ['A'], ['A']], M, [/ ['X', 'Y'], ['B']], [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_word_deletes': 1,
                   'num_para_merges': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A X\n\nY C D"
        in2 = "A B C D"
        phrases = "[[= ['A'], ['A']], [/ ['X'], ['B']], [/ ['Y'], []], M, [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_word_deletes': 1,
                   'num_para_merges': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A X Y\n\nC D"
        in2 = "A B C D"
        phrases = "[[= ['A'], ['A']], [/ ['X', 'Y'], ['B']], M, [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_word_deletes': 1,
                   'num_para_merges': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nX\n\nY C D"
        in2 = "A B C D"
        phrases = "[[= ['A'], ['A']], M, [/ ['X'], ['B']], [/ ['Y'], []], M, [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_word_deletes': 1,
                   'num_para_merges': 2}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A X\n\nY\n\nC D"
        in2 = "A B C D"
        phrases = "[[= ['A'], ['A']], [/ ['X'], ['B']], [/ ['Y'], []], M, [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_word_deletes': 1,
                   'num_para_merges': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nX Y\n\nC D"
        in2 = "A B C D"
        phrases = "[[= ['A'], ['A']], M, [/ ['X', 'Y'], ['B']], M, [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_word_deletes': 1,
                   'num_para_merges': 2}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nX\n\nY\n\nC D"
        in2 = "A B C D"
        phrases = "[[= ['A'], ['A']], M, [/ ['X'], ['B']], [/ ['Y'], []], M, [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_word_deletes': 1,
                   'num_para_merges': 2}
        self.evaluate(in1, in2, phrases, num_ops)

        ###
        in1 = "A\n\nX Y C D"
        in2 = "A\n\nB C D"
        phrases = "[[= ['A'], ['A']], [/ ['X', 'Y'], ['B']], [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_word_deletes': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A X\n\nY C D"
        in2 = "A\n\nB C D"
        phrases = "[[= ['A'], ['A']], S, [/ ['X'], ['B']], [/ ['Y'], []], M, [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_para_deletes': 1,
                   'num_para_inserts': 1,
                   'num_word_deletes': 1,
                   'num_para_splits': 1,
                   'num_para_merges': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A X Y\n\nC D"
        in2 = "A\n\nB C D"
        phrases = "[[= ['A'], ['A']], S, [/ ['X', 'Y'], ['B']], M, [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_word_deletes': 1,
                   'num_para_splits': 1,
                   'num_para_merges': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        ###
        in1 = "A\n\nX\n\nY C D"
        in2 = "A\n\nB C D"
        phrases = "[[= ['A'], ['A']], [/ ['X'], ['B']], [/ ['Y'], []], M, [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_para_deletes': 1,
                   'num_para_inserts': 1,
                   'num_word_deletes': 1,
                   'num_para_merges': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A X\n\nY\n\nC D"
        in2 = "A\n\nB C D"
        phrases = "[[= ['A'], ['A']], S, [/ ['X'], ['B']], [/ ['Y'], []], M, [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_para_deletes': 1,
                   'num_para_inserts': 1,
                   'num_para_merges': 1,
                   'num_para_splits': 1,
                   'num_word_deletes': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nX Y\n\nC D"
        in2 = "A\n\nB C D"
        phrases = "[[= ['A'], ['A']], [/ ['X', 'Y'], ['B']], M, [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_word_deletes': 1,
                   'num_para_merges': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nX\n\nY\n\nC D"
        in2 = "A\n\nB C D"
        phrases = "[[= ['A'], ['A']], [/ ['X'], ['B']], [/ ['Y'], []], M, [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_para_deletes': 1,
                   'num_para_inserts': 1,
                   'num_para_merges': 1,
                   'num_word_deletes': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        ###
        in1 = "A\n\nX Y C D"
        in2 = "A B\n\nC D"
        phrases = "[[= ['A'], ['A']], M, [/ ['X', 'Y'], ['B']], S, [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_word_deletes': 1,
                   'num_para_merges': 1,
                   'num_para_splits': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A X\n\nY C D"
        in2 = "A B\n\nC D"
        phrases = "[[= ['A'], ['A']], [/ ['X'], ['B']], [/ ['Y'], []], [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_word_deletes': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A X Y\n\nC D"
        in2 = "A B\n\nC D"
        phrases = "[[= ['A'], ['A']], [/ ['X', 'Y'], ['B']], [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_word_deletes': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        ###
        in1 = "A\n\nX\n\nY C D"
        in2 = "A B\n\nC D"
        phrases = "[[= ['A'], ['A']], M, [/ ['X'], ['B']], [/ ['Y'], []], [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_word_deletes': 1,
                   'num_para_merges': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A X\n\nY\n\nC D"
        in2 = "A B\n\nC D"
        phrases = "[[= ['A'], ['A']], [/ ['X'], ['B']], [/ ['Y'], []], [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_para_deletes': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nX Y\n\nC D"
        in2 = "A B\n\nC D"
        phrases = "[[= ['A'], ['A']], M, [/ ['X', 'Y'], ['B']], [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_word_replaces': 1,
                   'num_word_deletes': 1,
                   'num_para_merges': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nX\n\nY\n\nC D"
        in2 = "A B\n\nC D"
        phrases = "[[= ['A'], ['A']], M, [/ ['X'], ['B']], [/ ['Y'], []], [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_para_merges': 1,
                   'num_word_replaces': 1,
                   'num_para_deletes': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        ###
        in1 = "A\n\nX Y C D"
        in2 = "A\n\nB\n\nC D"
        phrases = "[[= ['A'], ['A']], [/ ['X', 'Y'], ['B']], S, [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_para_deletes': 1,
                   'num_para_inserts': 1,
                   'num_para_splits': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A X\n\nY C D"
        in2 = "A\n\nB\n\nC D"
        phrases = "[[= ['A'], ['A']], S, [/ ['X'], ['B']], [/ ['Y'], []], [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_para_deletes': 1,
                   'num_para_inserts': 1,
                   'num_word_deletes': 1,
                   'num_para_splits': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A X Y\n\nC D"
        in2 = "A\n\nB\n\nC D"
        phrases = "[[= ['A'], ['A']], S, [/ ['X', 'Y'], ['B']], [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_para_deletes': 1,
                   'num_para_inserts': 1,
                   'num_para_splits': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        ###
        in1 = "A\n\nX\n\nY C D"
        in2 = "A\n\nB\n\nC D"
        phrases = "[[= ['A'], ['A']], [/ ['X'], ['B']], [/ ['Y'], []], [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_para_deletes': 1,
                   'num_para_inserts': 1,
                   'num_word_deletes': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A X\n\nY\n\nC D"
        in2 = "A\n\nB\n\nC D"
        phrases = "[[= ['A'], ['A']], S, [/ ['X'], ['B']], [/ ['Y'], []], [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_para_deletes': 2,
                   'num_para_inserts': 1,
                   'num_para_splits': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nX Y\n\nC D"
        in2 = "A\n\nB\n\nC D"
        phrases = "[[= ['A'], ['A']], [/ ['X', 'Y'], ['B']], [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_para_deletes': 1,
                   'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

        in1 = "A\n\nX\n\nY\n\nC D"
        in2 = "A\n\nB\n\nC D"
        phrases = "[[= ['A'], ['A']], [/ ['X'], ['B']], [/ ['Y'], []], [= ['C', 'D'], ['C', 'D']]]"  # NOQA
        num_ops = {'num_para_deletes': 2,
                   'num_para_inserts': 1}
        self.evaluate(in1, in2, phrases, num_ops)

    def test_doc_diff_from_strings_split_merges_with_rearranges(self):
        in1 = "E F G\n\nA B C D"
        in2 = "A B C D E F G"
        phrases = "[[= ['A', 'B', 'C', 'D'], ['A', 'B', 'C', 'D']], M, [~ ['E', 'F', 'G'], ['E', 'F', 'G']]]"  # NOQA
        num_ops = {'num_para_rearranges': 1,
                   'num_para_merges': 1}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

        in1 = "E\n\nF G A B C D"
        in2 = "A B C D E F G"
        phrases = "[[/ ['F', 'G'], []], [= ['A', 'B', 'C', 'D'], ['A', 'B', 'C', 'D']], M, [~ ['E'], ['E']], M, [/ [], ['F', 'G']]]"  # NOQA
        num_ops = {'num_word_deletes': 2,
                   'num_word_inserts': 2,
                   'num_para_rearranges': 1,
                   'num_para_merges': 1}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

        in1 = "E\n\nF\n\nG\n\nA B C D"
        in2 = "A B C D E F G"
        phrases = "[[= ['A', 'B', 'C', 'D'], ['A', 'B', 'C', 'D']], M, [~ ['E'], ['E']], M, M, [~ ['F'], ['F']], M, M, [~ ['G'], ['G']]]"  # NOQA
        num_ops = {'num_para_rearranges': 3,
                   'num_para_merges': 3}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

        in1 = "F\n\nG\n\nE\n\nA B C D"
        in2 = "A B C D E F G"
        phrases = "[[= ['A', 'B', 'C', 'D'], ['A', 'B', 'C', 'D']], M, [~ ['E'], ['E']], M, M, [~ ['F'], ['F']], M, M, [~ ['G'], ['G']]]"  # NOQA
        num_ops = {'num_para_rearranges': 3,
                   'num_para_merges': 3}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

        in1 = "H I J K L M A B C D E F G"
        in2 = "A B C D E F G H I J K L M"
        phrases = "[[= ['A', 'B', 'C', 'D', 'E', 'F', 'G'], ['A', 'B', 'C', 'D', 'E', 'F', 'G']], M, [~ ['H', 'I', 'J', 'K', 'L', 'M'], ['H', 'I', 'J', 'K', 'L', 'M']]]"  # NOQA
        num_ops = {'num_para_rearranges': 1,
                   'num_para_merges': 1,
                   'num_para_splits': 1}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

        in1 = "A B V W X Y Z C D E F G H"
        in2 = "A B C D E F G V W X Y Z H"
        phrases = "[[= ['A', 'B'], ['A', 'B']], [= ['C', 'D', 'E', 'F', 'G'], ['C', 'D', 'E', 'F', 'G']], M, [~ ['V', 'W', 'X', 'Y', 'Z'], ['V', 'W', 'X', 'Y', 'Z']], M, [= ['H'], ['H']]]"  # NOQA
        num_ops = {'num_para_merges': 2,
                   'num_para_rearranges': 1,
                   'num_para_splits': 2}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

        in1 = "A B V W X Y Z C D E F G H"
        in2 = "A B C D E F G V W X Y Z H"
        phrases = "[[= ['A', 'B'], ['A', 'B']], [= ['C', 'D', 'E', 'F', 'G'], ['C', 'D', 'E', 'F', 'G']], M, [~ ['V', 'W', 'X', 'Y', 'Z'], ['V', 'W', 'X', 'Y', 'Z']], M, [= ['H'], ['H']]]"  # NOQA
        num_ops = {'num_para_merges': 2,
                   'num_para_rearranges': 1,
                   'num_para_splits': 2}
        self.evaluate(in1, in2, phrases, num_ops, rearrange=True)

    # ==========================================================================
    # Some util methods.

    def evaluate(self, in1, in2, phrases, num_ops, rearrange=False):
        result = doc_diff_from_strings(
            in1,
            in2,
            to_lower=False,
            rearrange_phrases=rearrange,
            min_rearrange_length=1)
        self.assertEqual(self.to_string(result.phrases), phrases)
        self.assertDictEqual(dict(result.num_ops), num_ops)

    def to_string(self, phrases):
        """ Creates a string representation for given list of phrases. """
        texts = []
        for phrase in phrases:
            parts = []
            if getattr(phrase, 'split_before', False):
                parts.append("S")
            if getattr(phrase, 'merge_before', False):
                parts.append("M")
            parts.append(str(phrase))
            if getattr(phrase, 'split_after', False):
                parts.append("S")
            if getattr(phrase, 'merge_after', False):
                parts.append("M")
            texts.append(", ".join(parts))
        return "[%s]" % ", ".join(texts)

if __name__ == '__main__':
    unittest.main()
