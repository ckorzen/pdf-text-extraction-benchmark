import unittest
import env  # NOQA

from parse import parser
from models import tex_elements


class TeXParserTest(unittest.TestCase):
    """
    Tests for TeXParser.
    """

    def test_none_input(self):
        """
        Tests parse() method with None input.
        """
        with self.assertRaises(ValueError):
            parser.parse(None, expand_macro_calls=False)
        with self.assertRaises(ValueError):
            parser.parse(None, expand_macro_calls=True)

    def test_empty_input(self):
        """
        Tests parse() method with empty input.
        """
        with self.assertRaises(ValueError):
            parser.parse("", expand_macro_calls=False)
        with self.assertRaises(ValueError):
            parser.parse("", expand_macro_calls=True)

    def test_group(self):
        """
        Tests parse() method with a group as input.
        """
        tex = "{\it Hello World}"
        doc = parser.parse(string=tex, expand_macro_calls=False)
        group = doc.elements[0]
        self.assertEqual(len(doc.elements), 1)
        self.assertEqual(type(group), tex_elements.TeXGroup)
        self.assertEqual(len(group.elements), 2)
        self.assertEqual(type(group.elements[0]), tex_elements.TeXControlCommand)
        self.assertEqual(type(group.elements[1]), tex_elements.TeXText)

        doc = parser.parse(string=tex, expand_macro_calls=True)
        group = doc.elements[0]
        self.assertEqual(len(doc.elements), 1)
        self.assertEqual(type(group), tex_elements.TeXGroup)
        self.assertEqual(len(group.elements), 2)
        self.assertEqual(type(group.elements[0]), tex_elements.TeXControlCommand)
        self.assertEqual(type(group.elements[1]), tex_elements.TeXText)

if __name__ == '__main__':
    unittest.main()
