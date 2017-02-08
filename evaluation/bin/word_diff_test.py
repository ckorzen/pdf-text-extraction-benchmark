import unittest
import word_diff

word_diff_tests = [
    # (actual, target, expected output)
    (None, None, "[]"),
    ("", "", "[]"),
    ("", None, "[]"),
    (None, "A", "[[/ [], [A]]]"),
    ("A", None, "[[/ [A], []]]"),
    ("A", "", "[[/ [A], []]]"),
    ("", "A", "[[/ [], [A]]]"),
    ("A", "A", "[[= [A], [A]]]"),
    ("A", "A B C", "[[= [A], [A]], [/ [], [B, C]]]"),
    ("A B C", "A", "[[= [A], [A]], [/ [B, C], []]]"),
    ("A B C", "A B C", "[[= [A, B, C], [A, B, C]]]"),
    ("A B C", "A B D", "[[= [A, B], [A, B]], [/ [C], [D]]]"),
    ("A B C", "A D C", "[[= [A], [A]], [/ [B], [D]], [= [C], [C]]]"),
    ("A B C", "A D E", "[[= [A], [A]], [/ [B, C], [D, E]]]"),
    ("A B C", "D B C", "[[/ [A], [D]], [= [B, C], [B, C]]]"),
    ("A B C", "D B E", "[[/ [A], [D]], [= [B], [B]], [/ [C], [E]]]"),
    ("A B C", "D E C", "[[/ [A, B], [D, E]], [= [C], [C]]]"),
    ("A B C", "D E G", "[[/ [A, B, C], [D, E, G]]]")
]


class WordDiffTest(unittest.TestCase):
    """ Tests for word_diff."""

    def test_word_diff_from_string(self):
        """ Tests the method word_diff_from_strings()."""

        # Test all the given test cases.
        for test in word_diff_tests:
            out = word_diff.word_diff_from_strings(test[0], test[1]).phrases
            self.assertEqual(str(out), test[2], msg="Test: {0}".format(test))

    def test_diff_phrases_and_diff_words(self):
        """ Tests some properties of DiffPhrase and DiffWord objects. """

        # Test properties of a phrase.
        phrase = word_diff.word_diff_from_strings("A B C", "").phrases[0]
        self.assertTrue(hasattr(phrase, "words_actual"))
        self.assertEqual(len(phrase.words_actual), 3)
        self.assertTrue(hasattr(phrase, "pos_actual"))
        self.assertEqual(phrase.pos_actual, 0)

        self.assertTrue(hasattr(phrase, "words_target"))
        self.assertEqual(len(phrase.words_target), 0)
        self.assertTrue(hasattr(phrase, "pos_target"))
        self.assertEqual(phrase.pos_target, 0)

        self.assertEqual(str(phrase.first_word_actual), "A")
        self.assertEqual(str(phrase.last_word_actual), "C")
        self.assertEqual(str(phrase.first_word_target), "None")
        self.assertEqual(str(phrase.last_word_target), "None")
        self.assertEqual(phrase.num_words_actual, 3)
        self.assertEqual(phrase.num_words_target, 0)

        # Test properties of some words.
        word = phrase.words_actual[0]
        self.assertTrue(hasattr(word, "word"))
        self.assertEqual(word.word, "A")
        self.assertTrue(hasattr(word, "pos_actual"))
        self.assertEqual(word.pos_actual, 0)
        self.assertTrue(hasattr(word, "pos_target"))
        self.assertEqual(word.pos_target, 0)
        self.assertTrue(hasattr(word, "phrase"))
        self.assertTrue(word.phrase is not None)

        word = phrase.words_actual[2]
        self.assertTrue(hasattr(word, "word"))
        self.assertEqual(word.word, "C")
        self.assertTrue(hasattr(word, "pos_actual"))
        self.assertEqual(word.pos_actual, 2)
        self.assertTrue(hasattr(word, "pos_target"))
        self.assertEqual(word.pos_target, 0)
        self.assertTrue(hasattr(word, "phrase"))
        self.assertTrue(word.phrase is not None)

        # ----------------------------------------------------------------------
        phrases = word_diff.word_diff_from_strings("A B C", "C").phrases
        phrase = phrases[0]
        self.assertTrue(hasattr(phrase, "words_actual"))
        self.assertEqual(len(phrase.words_actual), 2)
        self.assertTrue(hasattr(phrase, "pos_actual"))
        self.assertEqual(phrase.pos_actual, 0)

        self.assertTrue(hasattr(phrase, "words_target"))
        self.assertEqual(len(phrase.words_target), 0)
        self.assertTrue(hasattr(phrase, "pos_target"))
        self.assertEqual(phrase.pos_target, 0)

        self.assertEqual(str(phrase.first_word_actual), "A")
        self.assertEqual(str(phrase.last_word_actual), "B")
        self.assertEqual(str(phrase.first_word_target), "None")
        self.assertEqual(str(phrase.last_word_target), "None")
        self.assertEqual(phrase.num_words_actual, 2)
        self.assertEqual(phrase.num_words_target, 0)

        phrase = phrases[1]
        self.assertTrue(hasattr(phrase, "words_actual"))
        self.assertEqual(len(phrase.words_actual), 1)
        self.assertTrue(hasattr(phrase, "pos_actual"))
        self.assertEqual(phrase.pos_actual, 2)

        self.assertTrue(hasattr(phrase, "words_target"))
        self.assertEqual(len(phrase.words_target), 1)
        self.assertTrue(hasattr(phrase, "pos_target"))
        self.assertEqual(phrase.pos_target, 0)

        self.assertEqual(str(phrase.first_word_actual), "C")
        self.assertEqual(str(phrase.last_word_actual), "C")
        self.assertEqual(str(phrase.first_word_target), "C")
        self.assertEqual(str(phrase.last_word_target), "C")
        self.assertEqual(phrase.num_words_actual, 1)
        self.assertEqual(phrase.num_words_target, 1)

        # ----------------------------------------------------------------------
        phrases = word_diff.word_diff_from_strings("C", "A B C").phrases
        phrase = phrases[0]
        self.assertTrue(hasattr(phrase, "words_actual"))
        self.assertEqual(len(phrase.words_actual), 0)
        self.assertTrue(hasattr(phrase, "pos_actual"))
        self.assertEqual(phrase.pos_actual, 0)

        self.assertTrue(hasattr(phrase, "words_target"))
        self.assertEqual(len(phrase.words_target), 2)
        self.assertTrue(hasattr(phrase, "pos_target"))
        self.assertEqual(phrase.pos_target, 0)

        self.assertEqual(str(phrase.first_word_actual), "None")
        self.assertEqual(str(phrase.last_word_actual), "None")
        self.assertEqual(str(phrase.first_word_target), "A")
        self.assertEqual(str(phrase.last_word_target), "B")
        self.assertEqual(phrase.num_words_actual, 0)
        self.assertEqual(phrase.num_words_target, 2)

        # Test a word.
        word = phrase.words_target[1]
        self.assertTrue(hasattr(word, "word"))
        self.assertEqual(word.word, "B")
        self.assertTrue(hasattr(word, "pos_actual"))
        self.assertEqual(word.pos_actual, 0)
        self.assertTrue(hasattr(word, "pos_target"))
        self.assertEqual(word.pos_target, 1)
        self.assertTrue(hasattr(word, "phrase"))
        self.assertTrue(word.phrase is not None)

        phrase = phrases[1]
        self.assertTrue(hasattr(phrase, "words_actual"))
        self.assertEqual(len(phrase.words_actual), 1)
        self.assertTrue(hasattr(phrase, "pos_actual"))
        self.assertEqual(phrase.pos_actual, 0)

        self.assertTrue(hasattr(phrase, "words_target"))
        self.assertEqual(len(phrase.words_target), 1)
        self.assertTrue(hasattr(phrase, "pos_target"))
        self.assertEqual(phrase.pos_target, 2)

        self.assertEqual(str(phrase.first_word_actual), "C")
        self.assertEqual(str(phrase.last_word_actual), "C")
        self.assertEqual(str(phrase.first_word_target), "C")
        self.assertEqual(str(phrase.last_word_target), "C")
        self.assertEqual(phrase.num_words_actual, 1)
        self.assertEqual(phrase.num_words_target, 1)

        # ----------------------------------------------------------------------
        phrases = word_diff.word_diff_from_strings("A B C", "A D C").phrases
        phrase = phrases[0]
        self.assertTrue(hasattr(phrase, "words_actual"))
        self.assertEqual(len(phrase.words_actual), 1)
        self.assertTrue(hasattr(phrase, "pos_actual"))
        self.assertEqual(phrase.pos_actual, 0)

        self.assertTrue(hasattr(phrase, "words_target"))
        self.assertEqual(len(phrase.words_target), 1)
        self.assertTrue(hasattr(phrase, "pos_target"))
        self.assertEqual(phrase.pos_target, 0)

        self.assertEqual(str(phrase.first_word_actual), "A")
        self.assertEqual(str(phrase.last_word_actual), "A")
        self.assertEqual(str(phrase.first_word_target), "A")
        self.assertEqual(str(phrase.last_word_target), "A")
        self.assertEqual(phrase.num_words_actual, 1)
        self.assertEqual(phrase.num_words_target, 1)

        phrase = phrases[1]
        self.assertTrue(hasattr(phrase, "words_actual"))
        self.assertEqual(len(phrase.words_actual), 1)
        self.assertTrue(hasattr(phrase, "pos_actual"))
        self.assertEqual(phrase.pos_actual, 1)

        self.assertTrue(hasattr(phrase, "words_target"))
        self.assertEqual(len(phrase.words_target), 1)
        self.assertTrue(hasattr(phrase, "pos_target"))
        self.assertEqual(phrase.pos_target, 1)

        self.assertEqual(str(phrase.first_word_actual), "B")
        self.assertEqual(str(phrase.last_word_actual), "B")
        self.assertEqual(str(phrase.first_word_target), "D")
        self.assertEqual(str(phrase.last_word_target), "D")
        self.assertEqual(phrase.num_words_actual, 1)
        self.assertEqual(phrase.num_words_target, 1)

        phrase = phrases[2]
        self.assertTrue(hasattr(phrase, "words_actual"))
        self.assertEqual(len(phrase.words_actual), 1)
        self.assertTrue(hasattr(phrase, "pos_actual"))
        self.assertEqual(phrase.pos_actual, 2)

        self.assertTrue(hasattr(phrase, "words_target"))
        self.assertEqual(len(phrase.words_target), 1)
        self.assertTrue(hasattr(phrase, "pos_target"))
        self.assertEqual(phrase.pos_target, 2)

        self.assertEqual(str(phrase.first_word_actual), "C")
        self.assertEqual(str(phrase.last_word_actual), "C")
        self.assertEqual(str(phrase.first_word_target), "C")
        self.assertEqual(str(phrase.last_word_target), "C")
        self.assertEqual(phrase.num_words_actual, 1)
        self.assertEqual(phrase.num_words_target, 1)

if __name__ == '__main__':
    unittest.main()
