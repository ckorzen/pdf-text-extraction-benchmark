import unittest
from para_diff import para_diff

class ParaDiffTest(unittest.TestCase):

    def test_equal_paras(self):
        actual   = str(para_diff([], []))
        expected = str([])
        self.assertEqual(actual, expected)

        actual   = str(para_diff([["foo", "bar"]], [["foo", "bar"]]))
        expected = "[(= [foo, bar])]"
        self.assertEqual(actual, expected)

        actual   = str(para_diff([["foo", "bar"], ["baz", "boo"]], [["foo", "bar"], ["baz", "boo"]]))
        expected = "[(= [foo, bar]), (= [baz, boo])]"
        self.assertEqual(actual, expected)

    def test_delete_para(self):
        actual   = str(para_diff([["foo"]], []))
        expected = "[(- [foo])]"
        self.assertEqual(actual, expected)

        actual   = str(para_diff([["foo", "bar"]], []))
        expected = "[(- [foo, bar])]"
        self.assertEqual(actual, expected)

        actual   = str(para_diff([["foo"], ["bar"]], []))
        expected = "[(- [foo]), (- [bar])]"
        self.assertEqual(actual, expected)

        actual   = str(para_diff([["foo"], ["bar"]], [["bar"]]))
        expected = "[(- [foo]), (= [bar])]"
        self.assertEqual(actual, expected)

        actual   = str(para_diff([["foo", "bar"], ["baz"]], [["foo"], ["baz"]]))
        expected = "[(= [foo]), (- [bar]), (= [baz])]"
        self.assertEqual(actual, expected)

    def test_replace_para(self):
        actual   = str(para_diff([["foo"]], [["bar"]]))
        expected = "[(/ [foo], [bar])]"
        self.assertEqual(actual, expected)

        actual   = str(para_diff([["foo", "bar"]], [["baz", "boo"]]))
        expected = "[(/ [foo, bar], [baz, boo])]"
        self.assertEqual(actual, expected)

        actual   = str(para_diff([["foo"], ["bar"]], [["baz"], ["boo"]]))
        expected = "[(/ [foo], [baz]), (/ [bar], [boo])]"
        self.assertEqual(actual, expected)

        actual   = str(para_diff([["foo", "bar"]], [["baz"]]))
        expected = "[(/ [foo, bar], [baz])]"
        self.assertEqual(actual, expected)

        actual   = str(para_diff([["foo"], ["bar"]], [["baz"]]))
        expected = "[(/ [foo], [baz]), (/ [bar], [])]"
        self.assertEqual(actual, expected)

        actual   = str(para_diff(["foo"], [["baz", "boo"]]))
        expected = "[(/ [foo], [baz, boo])]"
        self.assertEqual(actual, expected)

        actual   = str(para_diff(["foo"], [["baz"], ["boo"]]))
        expected = "[(/ [foo], [baz]), (/ [], [boo])]"
        self.assertEqual(actual, expected)

    def test_insert_para(self):
        actual   = str(para_diff([], [["foo"]]))
        expected = "[(+ [foo])]"
        self.assertEqual(actual, expected)

        actual   = str(para_diff([], [["foo", "bar"]]))
        expected = "[(+ [foo, bar])]"
        self.assertEqual(actual, expected)

        actual   = str(para_diff([], [["foo"], ["bar"]]))
        expected = "[(+ [foo]), (+ [bar])]"
        self.assertEqual(actual, expected)

        actual   = str(para_diff([["bar"]], [["foo"], ["bar"]]))
        expected = "[(+ [foo]), (= [bar])]"
        self.assertEqual(actual, expected)

        actual   = str(para_diff([["foo"], ["baz"]], [["foo", "bar"], ["baz"]]))
        expected = "[(= [foo]), (+ [bar]), (= [baz])]"
        self.assertEqual(actual, expected)

    def test_split_para(self):
        actual   = str(para_diff([["foo", "bar"]], [["foo"], ["bar"]]))
        expected = "[(= [foo]), (‖), (= [bar])]"
        self.assertEqual(actual, expected)

        actual   = str(para_diff([["foo", "bar", "baz"]], [["foo"], ["bar"], ["baz"]]))
        expected = "[(= [foo]), (‖), (= [bar]), (‖), (= [baz])]"
        self.assertEqual(actual, expected)

        actual   = str(para_diff([["foo", "bar", "baz"]], [["foo", "bar"], ["baz"]]))
        expected = "[(= [foo, bar]), (‖), (= [baz])]"
        self.assertEqual(actual, expected)

    def test_merge_para(self):
        actual   = str(para_diff([["foo"], ["bar"]], [["foo", "bar"]]))
        expected = "[(= [foo]), (==), (= [bar])]"
        self.assertEqual(actual, expected)

        actual   = str(para_diff([["foo"], ["bar"], ["baz"]], [["foo", "bar", "baz"]]))
        expected = "[(= [foo]), (==), (= [bar]), (==), (= [baz])]"
        self.assertEqual(actual, expected)

        actual   = str(para_diff([["foo", "bar"], ["baz"]], [["foo", "bar", "baz"]]))
        expected = "[(= [foo, bar]), (==), (= [baz])]"
        self.assertEqual(actual, expected)

    def test_rearrange_para(self):
        #actual   = str(para_diff([["foo"], ["bar"]], [["bar"], ["foo"]]))
        #expected = "[(<> [foo]), (= [bar])]"
        #self.assertEqual(actual, expected)

        #actual   = str(para_diff([["foo"], ["bar"], ["baz"]], [["baz"], ["bar"], ["foo"]]))
        #expected = "[(<> [foo]), (<> [bar]), (= [baz])]"
        #self.assertEqual(actual, expected)

        #actual   = str(para_diff([["foo", "bar", "baz"]], [["baz", "bar", "foo"]]))
        #expected = "[(<> [foo]), (<> [bar]), (= [baz])]"
        #self.assertEqual(actual, expected)

        #actual   = str(para_diff([["foo", "bar", "baz", "boo"]], [["baz", "boo", "foo", "bar"]]))
        #expected = "[(<> [foo, bar]), (= [baz, boo])]"
        #self.assertEqual(actual, expected)

        actual   = str(para_diff([["foo", "bar", "baz"]], [["baz", "foo", "bar"]]))
        expected = "[(= [foo, bar]), (<> [baz])]"
        self.assertEqual(actual, expected)

    def test_various_combinations(self):
        actual   = str(para_diff([["foo"], ["bar"]], [["foo", "baz", "bar"]]))
        expected = "[(= [foo]), (+ [baz]), (==), (= [bar])]"
        self.assertEqual(actual, expected)

        actual   = str(para_diff([["foo", "bar"]], [["foo", "baz"], ["bar"]]))
        expected = "[(= [foo]), (+ [baz]), (‖), (= [bar])]"
        self.assertEqual(actual, expected)

if __name__ == '__main__':
    unittest.main()