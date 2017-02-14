import unittest
import para_diff

para_diff_tests = [
    # (actual, target, expected output)
    ("A B C", "A B C",
        "[[= ['A', 'B', 'C'], ['A', 'B', 'C']]]"),
    ("", "A B C",
        "[[/ [], ['A', 'B', 'C']]]"),
    ("A B C", "",
        "[[/ ['A', 'B', 'C'], []]]"),
    ("", "A \n\n B \n\n C",
        "[[/ [], ['A']], S, [/ [], ['B']], S, [/ [], ['C']]]"),
    ("A \n\n B \n\n C", "",
        "[[/ ['A'], []], M, [/ ['B'], []], M, [/ ['C'], []]]"),
    ("A B C", "A",
        "[[= ['A'], ['A']], [/ ['B', 'C'], []]]"),
    ("A", "A B C",
        "[[= ['A'], ['A']], [/ [], ['B', 'C']]]"),
    ("X A B C", "A B C",
        "[[/ ['X'], []], [= ['A', 'B', 'C'], ['A', 'B', 'C']]]"),
    ("A B C", "X A B C",
        "[[/ [], ['X']], [= ['A', 'B', 'C'], ['A', 'B', 'C']]]"),
    ("A X B C", "A B C",
        "[[= ['A'], ['A']], [/ ['X'], []], [= ['B', 'C'], ['B', 'C']]]"),
    ("A B C", "A X B C",
        "[[= ['A'], ['A']], [/ [], ['X']], [= ['B', 'C'], ['B', 'C']]]"),
    ("A B C X", "A B C",
        "[[= ['A', 'B', 'C'], ['A', 'B', 'C']], [/ ['X'], []]]"),
    ("A B C", "A B C X",
        "[[= ['A', 'B', 'C'], ['A', 'B', 'C']], [/ [], ['X']]]"),
    ("A \n\n B \n\n C", "A \n\n B \n\n C",
        "[[= ['A', 'B', 'C'], ['A', 'B', 'C']]]"),
    ("A B C", "A \n\n B \n\n C",
        "[[= ['A'], ['A']], S, [= ['B'], ['B']], S, [= ['C'], ['C']]]"),
    ("A \n\n B \n\n C", "A B C",
        "[[= ['A'], ['A']], M, [= ['B'], ['B']], M, [= ['C'], ['C']]]"),
    ("A \n\n B \n\n C", "A B C D",
        "[[= ['A'], ['A']], M, [= ['B'], ['B']], M, [= ['C'], ['C']], "
        "[/ [], ['D']]]"),
    ("A B C D", "A \n\n B \n\n C",
        "[[= ['A'], ['A']], S, [= ['B'], ['B']], S, [= ['C'], ['C']], "
        "[/ ['D'], []]]"),
    ("A \n\n B \n\n C", "A B C \n\n D",
        "[[= ['A'], ['A']], M, [= ['B'], ['B']], M, [= ['C'], ['C']], S, "
        "[/ [], ['D']]]"),
    ("A B C \n\n D", "A \n\n B \n\n C",
        "[[= ['A'], ['A']], S, [= ['B'], ['B']], S, [= ['C'], ['C']], M, "
        "[/ ['D'], []]]"),
    ("A \n\n B \n\n C", "A B \n\n X",
        "[[= ['A'], ['A']], M, [= ['B'], ['B']], [/ ['C'], ['X']]]"),
    ("A B \n\n X", "A \n\n B \n\n C",
        "[[= ['A'], ['A']], S, [= ['B'], ['B']], [/ ['X'], ['C']]]"),
    ("A B C", "A \n\n X B C",
        "[[= ['A'], ['A']], S, [/ [], ['X']], [= ['B', 'C'], ['B', 'C']]]"),
    ("A \n\n X B C", "A B C",
        "[[= ['A'], ['A']], M, [/ ['X'], []], [= ['B', 'C'], ['B', 'C']]]"),
    ("A B C", "A X \n\n B C",
        "[[= ['A'], ['A']], [/ [], ['X']], S, [= ['B', 'C'], ['B', 'C']]]"),
    ("A X \n\n B C", "A B C",
        "[[= ['A'], ['A']], [/ ['X'], []], M, [= ['B', 'C'], ['B', 'C']]]"),
    ("A B C", "A \n\n X \n\n B \n\n C",
        "[[= ['A'], ['A']], S, [/ [], ['X']], S, [= ['B'], ['B']], S, "
        "[= ['C'], ['C']]]"),
    ("A \n\n X \n\n B \n\n C", "A B C",
        "[[= ['A'], ['A']], M, [/ ['X'], []], M, [= ['B'], ['B']], M, "
        "[= ['C'], ['C']]]"),
    ("A B C", "X \n\n A \n\n B \n\n C",
        "[[/ [], ['X']], S, [= ['A'], ['A']], S, [= ['B'], ['B']], S, "
        "[= ['C'], ['C']]]"),
    ("X \n\n A \n\n B \n\n C", "A B C",
        "[[/ ['X'], []], M, [= ['A'], ['A']], M, [= ['B'], ['B']], M, "
        "[= ['C'], ['C']]]"),
    ("A B C", "A \n\n B \n\n C \n\n X",
        "[[= ['A'], ['A']], S, [= ['B'], ['B']], S, [= ['C'], ['C']], S, "
        "[/ [], ['X']]]"),
    ("A \n\n B \n\n C \n\n X", "A B C",
        "[[= ['A'], ['A']], M, [= ['B'], ['B']], M, [= ['C'], ['C']], M, "
        "[/ ['X'], []]]"),
    ("A \n\n B \n\n C", "A \n\n X B C",
        "[[= ['A'], ['A']], [/ [], ['X']], [= ['B'], ['B']], M, "
        "[= ['C'], ['C']]]"),
    ("A \n\n X B C", "A \n\n B \n\n C",
        "[[= ['A'], ['A']], [/ ['X'], []], [= ['B'], ['B']], S, "
        "[= ['C'], ['C']]]"),
    ("A \n\n B \n\n C", "A X \n\n B C",
        "[[= ['A'], ['A']], [/ [], ['X']], [= ['B'], ['B']], M, "
        "[= ['C'], ['C']]]"),
    ("A X \n\n B C", "A \n\n B \n\n C",
        "[[= ['A'], ['A']], [/ ['X'], []], [= ['B'], ['B']], S, "
        "[= ['C'], ['C']]]"),
    ("A \n\n B \n\n C", "A \n\n X \n\n B \n\n C",
        "[[= ['A'], ['A']], [/ [], ['X']], S, [= ['B', 'C'], ['B', 'C']]]"),
    ("A \n\n X \n\n B \n\n C", "A \n\n B \n\n C",
        "[[= ['A'], ['A']], [/ ['X'], []], M, [= ['B', 'C'], ['B', 'C']]]"),
    ("A \n\n B \n\n C", "X \n\n A \n\n B \n\n C",
        "[[/ [], ['X']], S, [= ['A', 'B', 'C'], ['A', 'B', 'C']]]"),
    ("X \n\n A \n\n B \n\n C", "A \n\n B \n\n C",
        "[[/ ['X'], []], M, [= ['A', 'B', 'C'], ['A', 'B', 'C']]]"),
    ("A \n\n B \n\n C", "A \n\n B \n\n C \n\n X",
        "[[= ['A', 'B', 'C'], ['A', 'B', 'C']], S, [/ [], ['X']]]"),
    ("A \n\n B \n\n C \n\n X", "A \n\n B \n\n C",
        "[[= ['A', 'B', 'C'], ['A', 'B', 'C']], M, [/ ['X'], []]]"),
]


class ParaDiffTest(unittest.TestCase):
    """ Tests for para_diff."""

    def test_para_diff_from_strings(self):
        """ Tests the method para_diff_from_strings(). """
        # Test None values.
        res = para_diff.para_diff_from_strings(None, None).phrases
        self.assertEqual(res, [])

        # Test empty values.
        res = para_diff.para_diff_from_strings([], []).phrases
        self.assertEqual(res, [])

        # Test all the given test cases.
        for test in para_diff_tests:
            out = para_diff.para_diff_from_strings(test[0], test[1])
            out_str = self.phrases_to_string(out.phrases)
            expected = test[2]
            self.assertEqual(out_str, expected, msg="Test: {0}".format(test))

    # ==========================================================================
    # Some util methods.

    def phrases_to_string(self, phrases):
        """ Creates a string representation for given list of phrases. """
        texts = []
        for phrase in phrases:
            parts = []
            if getattr(phrase, 'split_before', False):
                parts.append("S")
            if getattr(phrase, 'merge_before', False):
                parts.append("M")
            parts.append(str(phrase))
            texts.append(", ".join(parts))
        return "[%s]" % ", ".join(texts)

if __name__ == '__main__':
    unittest.main()
