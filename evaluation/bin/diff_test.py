import unittest
from diff import diff

class DiffTest(unittest.TestCase):

    def test(self):
        actual   = str(diff([], []))
        expected = str([])
        self.assertEqual(actual, expected)

        actual   = str(diff(["foo", "bar"], []))
        expected = "[(/ [foo, bar], [])]"
        self.assertEqual(actual, expected)

        actual   = str(diff([], ["foo", "bar"]))
        expected = "[(/ [], [foo, bar])]"
        self.assertEqual(actual, expected)

        actual   = str(diff(["foo", "bar"], ["foo", "bar"]))
        expected = "[(= [foo, bar], [foo, bar])]"
        self.assertEqual(actual, expected)

        actual   = str(diff(["foo", "bar"], ["foo", "baz"]))
        expected = "[(= [foo], [foo]), (/ [bar], [baz])]"
        self.assertEqual(actual, expected)

        actual   = str(diff(["foo", "bar"], ["foo", "baz", "bar"]))
        expected = "[(= [foo], [foo]), (/ [], [baz]), (= [bar], [bar])]"
        self.assertEqual(actual, expected)

        actual   = str(diff(["foo", "baz", "bar"], ["foo", "bar"]))
        expected = "[(= [foo], [foo]), (/ [baz], []), (= [bar], [bar])]"
        self.assertEqual(actual, expected)

if __name__ == '__main__':
    unittest.main()