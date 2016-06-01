import unittest
import doc_diff

class DocDiffTest(unittest.TestCase):
    
    def evaluate(self, str1, str2, expected, junk=[]):
        actual = doc_diff.doc_diff(str1, str2, junk)
        actual = doc_diff.visualize_diff_result_debug(actual)
        self.assertEqual(actual, str(expected))

    def test_common(self):
        self.evaluate("", "", "")
        
        self.evaluate("foo bar", "foo bar", 
            "foo bar")

        self.evaluate("foo bar\n\nbaz boo", 
            "foo bar\n\nbaz boo", "foo bar\n\nbaz boo")

    def test_rearranges(self):
        # Test word rearrange within single paragraph.
        self.evaluate("foo bar", "bar foo", 
            "bar (==) [<><> foo, foo] (‖)")

        # Test paragraph rearrange within single paragraph.
        self.evaluate("foo bar baz boo doo goo gol bol koi foi hul xxx", "gol bol koi foi hul xxx foo bar baz boo doo goo", 
            "gol bol koi foi hul xxx (==) [<><> foo bar baz boo doo goo, foo bar baz boo doo goo] (‖)")

        # Test paragraph rearrange within two (short) paragraphs.
        self.evaluate("foo bar\n\nbaz boo", "baz boo\n\nfoo bar", 
            "baz boo\n\n[<><> foo bar, foo bar]")
        
        # Test paragraph rearrange within two (longer) paragraphs.
        self.evaluate("foo bar baz boo doo goo\n\ngol bol koi foi hul xxx", "gol bol koi foi hul xxx\n\nfoo bar baz boo doo goo", 
            "gol bol koi foi hul xxx\n\n[<><> foo bar baz boo doo goo, foo bar baz boo doo goo]")

        # Test paragraph rearrange whithin single paragraph where the rearrange is a short inner string.
        self.evaluate("foo bar baz boo doo koo", "foo bar doo koo baz boo", 
            "foo bar doo koo (==) (‖) [<><> baz boo, baz boo] (‖)")

        # Test paragraph rearrange whithin single paragraph where the rearrange is a long inner string.
        self.evaluate("foo bar baz jojo hoko baw kli blu glu doo koo doo koo doo koo doo", "foo bar doo koo doo koo doo koo doo baz jojo hoko baw kli blu glu", 
            "foo bar doo koo doo koo doo koo doo (==) (‖) [<><> baz jojo hoko baw kli blu glu, baz jojo hoko baw kli blu glu] (‖)")

    def test_replaces(self):
        # Test short replace within single paragraph.
        self.evaluate("foo bar", "foo bal", "foo [/ bar, bal]")

        # Test long replace within single paragraph.
        self.evaluate("foo xxx yyy zzz aaa bbb ccc ddd", "foo 111 222 333 444 555 666 777", 
            "foo (==) [// xxx yyy zzz aaa bbb ccc ddd, 111 222 333 444 555 666 777]")

        # Test replace across paragraphs boundaries.
        self.evaluate("Hello World\n\nHow are you", "Hello Word\n\nHuw are you", 
            "hello [/ world, word]\n\n[/ how, huw] are you")

        # Test replace and insert.
        self.evaluate("Hello World", "Hello Word How are you", 
            "hello [/ world, word how are you]")

        # Test replace and delete.
        self.evaluate("foo bar baz", "foo bal", 
            "foo [/ bar baz, bal]")

    def test_deletes(self):
        # Test short delete within single paragraph.
        self.evaluate("foo bar", "foo", 
            "[- bar]\n\nfoo")

        # Test long delete within single paragraph.
        self.evaluate("foo bar baz boo goo loo hoo too", "foo", 
            "(‖) [-- bar baz boo goo loo hoo too]\n\nfoo")

        # Test single delete within two paragraph.
        self.evaluate("foo bar\n\nbaz boo", "foo bar", 
            "[-- baz boo]\n\nfoo bar")

        # Test two deletes within paragraphs.
        self.evaluate("foo bar\n\nbaz boo\n\ndoo goo", "foo bar", 
            "[-- baz boo]\n\n[-- doo goo]\n\nfoo bar")

        # Test short inner delete.
        self.evaluate("foo bar baz", "foo baz", 
            "[- bar]\n\nfoo baz")

        # Test long inner delete.
        self.evaluate("foo bar gar koo roo woo tul bol baz", "foo baz", 
            "(‖) [-- bar gar koo roo woo tul bol] (‖)\n\nfoo baz")

        # Test delete across paragraph boundaries.
        self.evaluate("foo bar baz\n\ngoo koo loo", "foo bar\n\nkoo loo", 
            "[- baz]\n\n[- goo]\n\nfoo bar\n\nkoo loo")

    def test_insert(self):
        # Test short insert within single paragraph.
        self.evaluate("foo", "foo bar", 
            "foo [+ bar]")

        # Test long insert within single paragraph.
        self.evaluate("foo", "foo  bar baz boo goo loo hoo too", 
            "foo (==) [++ bar baz boo goo loo hoo too]")

         # Test single insert within two paragraph.
        self.evaluate("foo bar", "foo bar\n\nbaz boo", 
            "foo bar\n\n[++ baz boo]")

        # Test two inserts within paragraphs.
        self.evaluate("foo bar", "foo bar\n\nbaz boo\n\ndoo goo", 
            "foo bar\n\n[++ baz boo]\n\n[++ doo goo]")

        # Test short inner insert.
        self.evaluate("foo baz", "foo bar baz", 
            "foo [+ bar] baz")

        # Test long inner insert.
        self.evaluate("foo baz", "foo bar gar koo roo woo tul bol baz",
            "foo (==) [++ bar gar koo roo woo tul bol] (==) baz")

        # Test insert across paragraph boundaries.
        self.evaluate("foo bar\n\nkoo loo", "foo bar baz\n\ngoo koo loo", 
            "foo bar [+ baz]\n\n[+ goo] koo loo")

    def test_junk(self):
        self.evaluate("foo bar baz goo hoo\n\nzoo koo loo 1 2 3 4 zar to tei 4 zaz", "zoo koo loo [formula] zar to tei [formula] zaz\n\nfoo bar baz goo hoo",
            "[<><> zoo koo loo zar to tei zaz, zoo koo loo zar to tei zaz]\n\nfoo bar baz goo hoo", ["\[formula\]"])

if __name__ == '__main__':
    unittest.main()