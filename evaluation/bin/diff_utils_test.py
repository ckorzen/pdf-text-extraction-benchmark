import unittest
import word_diff

from diff_utils import string_to_diff_words
from diff_utils import filter_special_chars
from diff_utils import compose_characters
from diff_utils import split_into_paras_and_words
from diff_utils import split
from diff_utils import is_special_character
from diff_utils import flatten_list


class DiffUtilsTest(unittest.TestCase):
    """ Tests for diff_utils."""

    def test_string_to_diff_words_1(self):
        """ Tests the method string_to_diff_words(). """
        words = string_to_diff_words(None)
        self.assertEqual(words, [])

        words = string_to_diff_words("")
        self.assertEqual(words, [])

        words = string_to_diff_words("A")
        words_str = self.diff_words_to_string(words)
        self.assertEqual(
            words_str,
            "[(a,A,A,None,None)]")

        words = string_to_diff_words("A B")
        words_str = self.diff_words_to_string(words)
        self.assertEqual(
            words_str,
            "[(a,A,A ,None,b),(b,B,B,a,None)]")

        words = string_to_diff_words("A B C")
        words_str = self.diff_words_to_string(words)
        self.assertEqual(
            words_str,
            "[(a,A,A ,None,b),(b,B,B ,a,c),(c,C,C,b,None)]")

        words = string_to_diff_words("A \n\n B \n\n C")
        words_str = self.diff_words_to_string(words)
        self.assertEqual(
            words_str,
            "[(a,A,A \n\n ,None,b),(b,B,B \n\n ,a,c),(c,C,C,b,None)]")

    def test_string_to_diff_words_2(self):
        """ Tests the method string_to_diff_words() with flatten=False. """
        words = string_to_diff_words(None, flatten=False)
        self.assertEqual(words, [])

        words = string_to_diff_words("", flatten=False)
        self.assertEqual(words, [])

        words = string_to_diff_words("A", flatten=False)
        words_str = self.diff_words_to_string(words)
        self.assertEqual(
            words_str,
            "[[(a,A,A,None,None)]]")

        words = string_to_diff_words("A B", flatten=False)
        words_str = self.diff_words_to_string(words)
        self.assertEqual(
            words_str,
            "[[(a,A,A ,None,b),(b,B,B,a,None)]]")

        words = string_to_diff_words("A B C", flatten=False)
        words_str = self.diff_words_to_string(words)
        self.assertEqual(
            words_str,
            "[[(a,A,A ,None,b),(b,B,B ,a,c),(c,C,C,b,None)]]")

        words = string_to_diff_words("A \n\n B \n\n C", flatten=False)
        words_str = self.diff_words_to_string(words)
        self.assertEqual(
            words_str,
            "[[(a,A,A \n\n ,None,b)],[(b,B,B \n\n ,a,c)],[(c,C,C,b,None)]]")

    def test_string_to_diff_words_3(self):
        """ Tests the method string_to_diff_words() with to_lower=False. """
        words = string_to_diff_words(None, to_lower=False)
        self.assertEqual(words, [])

        words = string_to_diff_words("", to_lower=False)
        self.assertEqual(words, [])

        words = string_to_diff_words("A", to_lower=False)
        words_str = self.diff_words_to_string(words)
        self.assertEqual(
            words_str,
            "[(A,A,A,None,None)]")

        words = string_to_diff_words("A B", to_lower=False)
        words_str = self.diff_words_to_string(words)
        self.assertEqual(
            words_str,
            "[(A,A,A ,None,B),(B,B,B,A,None)]")

        words = string_to_diff_words("A B C", to_lower=False)
        words_str = self.diff_words_to_string(words)
        self.assertEqual(
            words_str,
            "[(A,A,A ,None,B),(B,B,B ,A,C),(C,C,C,B,None)]")

        words = string_to_diff_words("A \n\n B \n\n C", to_lower=False)
        words_str = self.diff_words_to_string(words)
        self.assertEqual(
            words_str,
            "[(A,A,A \n\n ,None,B),(B,B,B \n\n ,A,C),(C,C,C,B,None)]")

    def test_string_to_diff_words_4(self):
        """
        Tests the method string_to_diff_words() with specialchars_pattern="a".
        """
        res = string_to_diff_words(None, specialchars_pattern="a")
        self.assertEqual(res, [])

        res = string_to_diff_words("", specialchars_pattern="a")
        self.assertEqual(res, [])

        res = string_to_diff_words("A", specialchars_pattern="a")
        res_str = self.diff_words_to_string(res)
        self.assertEqual(res_str, "[]")

        res = string_to_diff_words("A B", specialchars_pattern="a")
        res_str = self.diff_words_to_string(res)
        self.assertEqual(res_str, "[(b,B,B,None,None)]")

        res = string_to_diff_words("A B C", specialchars_pattern="a")
        res_str = self.diff_words_to_string(res)
        self.assertEqual(res_str, "[(b,B,B ,None,c),(c,C,C,b,None)]")

        res = string_to_diff_words("A \n\n B \n\n C", specialchars_pattern="a")
        res_str = self.diff_words_to_string(res)
        self.assertEqual(res_str, "[(b,B,B \n\n ,None,c),(c,C,C,b,None)]")

    def test_string_to_diff_words_5(self):
        """
        Tests the method string_to_diff_words() with excludes=["\[A\]"]".
        """
        res = string_to_diff_words(None, excludes=["\[A\]"])
        self.assertEqual(res, [])

        res = string_to_diff_words("", excludes=["\[A\]"])
        self.assertEqual(res, [])

        res = string_to_diff_words("[A]", excludes=["\[A\]"])
        res_str = self.diff_words_to_string(res)
        self.assertEqual(res_str, "[([A],[A],[A],None,None)]")

        res = string_to_diff_words("[A] B", excludes=["\[A\]"])
        res_str = self.diff_words_to_string(res)
        self.assertEqual(
            res_str,
            "[([A],[A],[A] ,None,b),(b,B,B,[A],None)]")

        res = string_to_diff_words("[A] B C", excludes=["\[A\]"])
        res_str = self.diff_words_to_string(res)
        self.assertEqual(
            res_str,
            "[([A],[A],[A] ,None,b),(b,B,B ,[A],c),(c,C,C,b,None)]")

        res = string_to_diff_words("[A] \n\n B \n\n C", excludes=["\[A\]"])
        res_str = self.diff_words_to_string(res)
        self.assertEqual(
            res_str,
            "[([A],[A],[A] \n\n ,None,b),(b,B,B \n\n ,[A],c),(c,C,C,b,None)]")

    def test_compose_characters(self):
        """ Tests the method compose_characters(). """
        res = compose_characters(None)
        self.assertEqual(res, None)

        res = compose_characters("")
        self.assertEqual(res, "")

        res = compose_characters("A B C")
        self.assertEqual(res, "A B C")

        res = compose_characters(u"A\u0300 B\u0342 C\u0302")
        self.assertEqual(res, "À B͂ Ĉ")

        res = compose_characters(u"A\u0300 \n\n B\u0342 \n\n C\u0302")
        self.assertEqual(res, "À \n\n B͂ \n\n Ĉ")

    def test_split_into_paras_and_words(self):
        """ Tests the method split_into_paras_and_words(). """
        res = split_into_paras_and_words(None)
        self.assertEqual(res, [])

        res = split_into_paras_and_words("")
        self.assertEqual(res, [])

        res = split_into_paras_and_words("A")
        self.assertEqual(str(res), "[[a]]")

        res = split_into_paras_and_words("A B")
        self.assertEqual(str(res), "[[a, b]]")

        res = split_into_paras_and_words("A B C")
        self.assertEqual(str(res), "[[a, b, c]]")

        res = split_into_paras_and_words("A \n\n B \n\n C")
        self.assertEqual(str(res), "[[a], [b], [c]]")

    def test_split(self):
        """ Tests the method split(). """
        res = split(None, None)
        self.assertEqual(res, None)

        res = split(None, "")
        self.assertEqual(res, None)

        res = split("", "")
        self.assertEqual(res, ["", ""])

        res = split("", None)
        self.assertEqual(res, ["", ""])

        res = split("A B C", "X")
        self.assertEqual(res, ["A B C", ""])

        res = split("A B C", "\s")
        self.assertEqual(res, ["A", " ", "B", " ", "C", ""])

        res = split("A   B  C", "\s+")
        self.assertEqual(res, ["A", "   ", "B", "  ", "C", ""])

        res = split("A   B  C ", "\s+")
        self.assertEqual(res, ["A", "   ", "B", "  ", "C", " ", "", ""])

    def test_is_special_character(self):
        """ Tests the method is_special_character(). """
        res = is_special_character(None, 0, None)
        self.assertEqual(res, False)

        res = is_special_character(None, 0, "X")
        self.assertEqual(res, False)

        res = is_special_character("A", 0, None)
        self.assertEqual(res, False)

        res = is_special_character("", 0, "X")
        self.assertEqual(res, False)

        res = is_special_character("A", -1, "X")
        self.assertEqual(res, False)

        res = is_special_character("", 1, "X")
        self.assertEqual(res, False)

        res = is_special_character("A", -1, "A")
        self.assertEqual(res, False)

        res = is_special_character("", 1, "A")
        self.assertEqual(res, False)

        res = is_special_character("A", 0, "A")
        self.assertEqual(res, True)

        res = is_special_character("ABA", 0, "A")
        self.assertEqual(res, True)

        res = is_special_character("ABA", 1, "A")
        self.assertEqual(res, False)

        res = is_special_character("ABA", 2, "A")
        self.assertEqual(res, True)

    def test_filter_special_chars(self):
        """ Tests the method filter_special_chars(). """
        text = filter_special_chars(None, None)
        self.assertEqual(text, None)

        text = filter_special_chars("", None)
        self.assertEqual(text, "")

        text = filter_special_chars("ABC", None)
        self.assertEqual(text, "ABC")

        text = filter_special_chars("ABC", "X")
        self.assertEqual(text, "ABC")

        text = filter_special_chars("AXBC", "X")
        self.assertEqual(text, "ABC")

        text = filter_special_chars("XXX", "X")
        self.assertEqual(text, "")

    def test_flatten_list(self):
        """ Tests the method flatten_string(). """
        a = word_diff.DiffWord("A")
        b = word_diff.DiffWord("B")
        c = word_diff.DiffWord("C")
        d = word_diff.DiffWord("D")
        e = word_diff.DiffWord("E")
        f = word_diff.DiffWord("F")

        flat1 = flatten_list([a, b, c])
        self.assert_flatten_list1(flat1)

        flat2 = flatten_list([[a, b, c], [d, e, f]])
        self.assert_flatten_list2(flat2)

    def assert_flatten_list1(self, flat1):
        """ Tests the first flattened list. """
        self.assertEqual(len(flat1), 3)

        word0 = flat1[0]
        self.assertEqual(type(word0), word_diff.DiffWord)
        self.assertEqual(word0.word, "A")
        self.assertEqual(word0.para, 0)
        self.assertEqual(word0.pos_in_para, 0)
        self.assertEqual(word0.pos_in_text, 0)

        word1 = flat1[1]
        self.assertEqual(type(word1), word_diff.DiffWord)
        self.assertEqual(word1.word, "B")
        self.assertEqual(word1.para, 0)
        self.assertEqual(word1.pos_in_para, 1)
        self.assertEqual(word1.pos_in_text, 1)

        word2 = flat1[2]
        self.assertEqual(type(word2), word_diff.DiffWord)
        self.assertEqual(word2.word, "C")
        self.assertEqual(word2.para, 0)
        self.assertEqual(word2.pos_in_para, 2)
        self.assertEqual(word2.pos_in_text, 2)

    def assert_flatten_list2(self, flat2):
        """ Tests the second flattened list. """
        self.assertEqual(len(flat2), 6)

        word0 = flat2[0]
        self.assertEqual(type(word0), word_diff.DiffWord)
        self.assertEqual(word0.word, "A")
        self.assertEqual(word0.para, 0)
        self.assertEqual(word0.pos_in_para, 0)
        self.assertEqual(word0.pos_in_text, 0)

        word1 = flat2[1]
        self.assertEqual(type(word1), word_diff.DiffWord)
        self.assertEqual(word1.word, "B")
        self.assertEqual(word1.para, 0)
        self.assertEqual(word1.pos_in_para, 1)
        self.assertEqual(word1.pos_in_text, 1)

        word2 = flat2[2]
        self.assertEqual(type(word2), word_diff.DiffWord)
        self.assertEqual(word2.word, "C")
        self.assertEqual(word2.para, 0)
        self.assertEqual(word2.pos_in_para, 2)
        self.assertEqual(word2.pos_in_text, 2)

        word3 = flat2[3]
        self.assertEqual(type(word3), word_diff.DiffWord)
        self.assertEqual(word3.word, "D")
        self.assertEqual(word3.para, 1)
        self.assertEqual(word3.pos_in_para, 0)
        self.assertEqual(word3.pos_in_text, 3)

        word4 = flat2[4]
        self.assertEqual(type(word4), word_diff.DiffWord)
        self.assertEqual(word4.word, "E")
        self.assertEqual(word4.para, 1)
        self.assertEqual(word4.pos_in_para, 1)
        self.assertEqual(word4.pos_in_text, 4)

        word5 = flat2[5]
        self.assertEqual(type(word5), word_diff.DiffWord)
        self.assertEqual(word5.word, "F")
        self.assertEqual(word5.para, 1)
        self.assertEqual(word5.pos_in_para, 2)
        self.assertEqual(word5.pos_in_text, 5)

    # ==========================================================================
    # Some util methods.

    def diff_words_to_string(self, diff_words):
        """ Creates a string representation for given diff words. """
        parts = []
        for word in diff_words:
            if type(word) is list:
                parts.append(self.diff_words_to_string(word))
            else:
                parts.append(self.diff_word_to_string(word))
        return "[%s]" % ",".join(parts)

    def diff_word_to_string(self, word):
        """ Creates a string representation for given diff word. """
        word_parts = []
        word_parts.append(word.word)
        word_parts.append(word.unnormalized)
        word_parts.append(word.unnormalized_with_whitespaces)
        word_parts.append(str(word.prev))
        word_parts.append(str(word.next))
        return "(%s)" % ",".join(word_parts)

if __name__ == '__main__':
    unittest.main()
