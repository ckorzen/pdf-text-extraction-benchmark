import unittest
from align_documents import align_strings
from align_documents import count_ops

def para(*strings):
    return " \n \n ".join(strings)

class align_documents_test(unittest.TestCase):

    def check(self, x, y, expected_ops):
        self.assertEqual(count_ops(align_strings(x, y)), expected_ops)

    def test_single_paragraph(self):
        # Check "" and "" (COMMON)
        self.check("", 
                   "", 
                   {})
        
        # Check X and X (COMMON)
        self.check("foo bar", 
                   "foo bar",
                   {})

        # Check "" and X (INSERT)
        self.check("", 
                   "foo bar", 
                   { 'num_para_inserts': 1 })

        # Check "" and X (INSERT), many words
        self.check("", 
                   "foo bar foo bar foo bar", 
                   { 'num_para_inserts': 1 })

        # Check X and "" (DELETE)
        self.check("foo bar", 
                   "", 
                   { 'num_para_deletes': 1 })

        # Check X and "" (DELETE), many words
        self.check("foo bar foo bar foo bar", 
                   "", 
                   { 'num_para_deletes': 1 })

        # Check X and Y (REPLACE)
        # TODO: { 'num_word_rearranges': 2 } ?
        self.check("foo bar", 
                   "baz boo", 
                   { 'num_para_inserts': 1, 
                     'num_para_deletes': 1 })

        # Check X and Y (REPLACE), many words
        # TODO: { 'num_para_rearranges': 1 } ?
        self.check("foo bar foo bar foo bar", 
                   "doo dar doo dar doo dar", 
                   { 'num_para_deletes': 1, 
                     'num_para_inserts': 1 })

    def test_two_paragraphs(self):
        # Check X1 X2 and X1 X2 (COMMON COMMON)
        self.check(para("foo bar", "baz boo"), 
                   para("foo bar", "baz boo"), 
                   {})

        # Check X1 X2 and X1 (COMMON DELETE)
        self.check(para("foo bar", "baz boo"), 
                   para("foo bar"), 
                   { 'num_para_deletes': 1 })

        # Check X1 X2 and X1 Y1 (COMMON REPLACE)
        self.check(para("foo bar", "baz boo"), 
                   para("foo bar", "doo"), 
                   { 'num_para_deletes': 1,
                     'num_para_inserts': 1 })

        # Check X1 X2 and Y1 (COMMON INSERT)
        self.check(para("foo bar"), 
                   para("foo bar", "doo"), 
                   { 'num_para_inserts': 1 })

        # Check X1 X2 and X2 X1 (COMMON REARRANGE / REARRANGE COMMON)
        self.check(para("foo bar", "baz boo"), 
                   para("baz boo", "foo bar"), 
                   { 'num_para_rearranges': 1 })

        # Check X1 X2 and X2 X1 (COMMON REARRANGE / REARRANGE COMMON), many words
        self.check(para("foo bar foo bar foo bar", "baz boo baz boo baz boo"), 
                   para("baz boo baz boo baz boo", "foo bar foo bar foo bar"), 
                   { 'num_para_rearranges': 1 })

        # ______________________________________________________________________

        # Check X1 and Y1 X1 (INSERT COMMON)
        self.check(para("baz boo"), 
                   para("foo bar", "baz boo"), 
                   { 'num_para_inserts': 1 })

        # Check X1 and Y1 Y2 (INSERT REPLACE / REPLACE INSERT)
        self.check(para("baz boo"), 
                   para("foo bar", "doo"), 
                   { 'num_para_deletes': 1,
                     'num_para_inserts': 2 })

        # Check "" and X1 X2 (INSERT INSERT)
        self.check(para(""), 
                   para("foo bar", "doo"), 
                   { 'num_para_inserts': 2 })

        # Check X1 X2 and X2 X1 (INSERT REARRANGE / REARRANGE INSERT)
        self.check(para("foo bar"), 
                   para("baz boo", "foo bar"), 
                   { 'num_para_inserts': 1 })

        # Check X1 X2 and X2 X1 (INSERT REARRANGE / INSERT COMMON), many words
        self.check(para("foo bar foo bar foo bar"), 
                   para("baz boo baz boo baz boo", "foo bar foo bar foo bar"), 
                   { 'num_para_inserts': 1 })

        # ______________________________________________________________________

        # Check X1 and Y1 X1 (DELETE COMMON)
        self.check(para("foo bar", "baz boo"), 
                   para("baz boo"), 
                   { 'num_para_deletes': 1 })

        # Check X1 X2 and "" (DELETE DELETE)
        self.check(para("foo bar", "baz boo"), 
                   para(""), 
                   { 'num_para_deletes': 2 })

        # Check X1 X2 and Y1 (DELETE REPLACE)
        self.check(para("foo bar", "baz boo"), 
                   para("doo"), 
                   { 'num_para_deletes': 2,
                     'num_para_inserts': 1 })

        # ______________________________________________________________________

        # Check X1 X2 and Y1 X2 (REPLACE COMMON)
        self.check(para("doo goo", "baz boo"), 
                   para("foo bar", "baz boo"), 
                   { 'num_para_deletes': 1,
                     'num_para_inserts': 1 })

        # Check X1 X2 and Y1 (REPLACE DELETE)
        self.check(para("foo bar", "baz boo"), 
                   para("doo goo"), 
                   { 'num_para_deletes': 2,
                     'num_para_inserts': 1 })

        # Check X1 X2 and Y1 Y2 (REPLACE REPLACE)
        self.check(para("foo bar", "baz boo"), 
                   para("doo dar", "daz doo"), 
                   { 'num_para_deletes': 2,
                     'num_para_inserts': 2 })

        # Check X1 and Y1 Y2 (REPLACE INSERT)
        self.check(para("foo bar"), 
                   para("doo dar", "doo"), 
                   { 'num_para_deletes': 1,
                     'num_para_inserts': 2 })

        # Check X1 X2 and Y1 X1 (REPLACE REARRANGE / REARRANGE REPLACE)
        self.check(para("foo bar", "daz doo"), 
                   para("baz boo", "foo bar"), 
                   { 'num_para_deletes': 1,
                     'num_para_inserts': 1 })

        # Check X1 X2 and Y1 X1 (REPLACE REARRANGE / REARRANGE REPLACE), many words
        self.check(para("foo bar foo bar foo bar", "foo goo foo goo foo goo"), 
                   para("doo roo doo roo doo roo", "foo bar foo bar foo bar"), 
                   { 'num_para_deletes': 1,
                     'num_para_inserts': 1 })

        # ______________________________________________________________________

        # Check (COMMON COMMON REARRANGE)
        self.check(para("foo bar", "baz boo", "doo goo"), 
                   para("foo bar", "doo goo", "baz boo"), 
                   { 'num_para_rearranges': 1 })

        # Check (COMMON COMMON REARRANGE), manx words
        self.check(para("foo bar", "baz boo baz boo baz boo", "doo goo doo goo doo goo"), 
                   para("foo bar", "doo goo doo goo doo goo", "baz boo baz boo baz boo"), 
                   { 'num_para_rearranges': 1 })

        # Check (REARRANGE COMMON REARRANGE)
        self.check(para("foo bar", "baz boo", "doo goo"), 
                   para("doo goo", "baz boo", "foo bar"), 
                   { 'num_para_rearranges': 2 })

        # Check (REARRANGE COMMON REARRANGE)
        self.check(para("foo bar foo bar foo bar foo bar", "baz boo baz boo baz boo", "doo goo doo goo doo goo"), 
                   para("doo goo doo goo doo goo", "baz boo baz boo baz boo", "foo bar foo bar foo bar foo bar"), 
                   { 'num_para_rearranges': 2 })

        # Check (COMMON INSERT INSERT)
        self.check(para("foo bar"), 
                   para("foo bar", "bar baz", "dre dru"), 
                   { 'num_para_inserts': 2 })

        # ______________________________________________________________________

        # CHECK "A B C D" and "D C B A". 
        self.check(para("foo bar baz boo"),
                   para("boo baz bar foo"),
                   { 'num_word_rearranges': 3 }) # TODO

        # CHECK "A B C D" and "A / B / C / D". 
        self.check(para("foo bar baz boo"),
                   para("foo", "bar", "baz", "boo"),
                   { 'num_para_splits': 3 })

        # CHECK "A / B / C / D" and "A B C D". 
        self.check(para("foo", "bar", "baz", "boo"),
                   para("foo bar baz boo"),
                   { 'num_para_merges': 3 })

        # CHECK "A B C D" and "D / C / B / A". 
        self.check(para("foo bar baz boo"),
                   para("boo", "baz", "bar", "foo"),
                   { 'num_para_splits': 3, 
                     'num_para_rearranges': 3 })

        # CHECK "D / C / B / A" and "A B C D". 
        self.check(para("boo", "baz", "bar", "foo"),
                   para("foo bar baz boo"),
                   { 'num_para_merges': 3, 
                     'num_para_rearranges': 3 })

        # CHECK "A B C D" and "A B / C / D". 
        self.check(para("foo bar baz boo"),
                   para("foo bar", "baz", "boo"),
                   { 'num_para_splits': 2 })

        # CHECK "A B C D" and "C / D / A / B". 
        self.check(para("foo bar baz boo"),
                   para("baz", "boo", "foo", "bar"),
                   { 'num_para_splits':   3,
                     'num_para_rearranges': 2 })

        # CHECK "A B C D" and "A B C".
        self.check(para("foo bar baz boo"),
                   para("foo bar baz"),
                   { 'num_word_deletes': 1})

        # CHECK "A B C D" and "A B C".
        self.check(para("foo bar baz boo foo bar baz boo foo bar baz boo"),
                   para("foo bar baz"),
                   { 'num_para_splits': 1,
                     'num_para_deletes': 1 })

        # CHECK "A B C" and "A B C D".
        self.check(para("foo bar baz"),
                   para("foo bar baz boo"),
                   { 'num_word_inserts': 1})

        # CHECK "A B C" and "A B C XXX".
        self.check(para("foo bar baz"),
                   para("foo bar baz abc def ghi jkl mno pqr stu vwx yz"),
                   { 'num_para_merges': 1,
                     'num_para_inserts': 1 })

        # CHECK "A B C" and "XXX A B".
        self.check(para("abc def ghi"),
                   para("foo bar baz boo foo bar baz boo abc def"),
                   { 'num_word_deletes': 1,
                     'num_para_merges':  1,
                     'num_para_inserts': 1})

        # CHECK "A B C" and "X A B".
        self.check(para("abc def ghi"),
                   para("foo abc def"),
                   { 'num_word_deletes': 1,
                     'num_word_inserts': 1 })

        # CHECK "A B C D" and "A C D".
        self.check(para("foo bar baz boo"),
                   para("foo baz boo"),
                   { 'num_word_deletes': 1 })

        # CHECK "A XXX C D" and "A C D".
        self.check(para("foo bar abc def ghi jkl mno pqr stu vwx baz boo"),
                   para("foo baz boo"),
                   { 'num_para_splits': 2,
                     'num_para_deletes': 1,
                     'num_para_merges': 1 })

        # CHECK "A C D" and "A XXX C D".
        self.check(para("foo baz boo"),
                   para("foo bar abc def ghi jkl mno pqr stu vwx baz boo"),
                   { 'num_para_splits': 1,
                     'num_para_inserts': 1,
                     'num_para_merges': 2 })

        # CHECK "A XXX YYY B" and "A YYY XXX D".
        self.check(para("foo abc def ghi jkl mno pqr stu xxx yyy zzz aaa bbb ccc ddd bar"),
                   para("foo xxx yyy zzz aaa bbb ccc ddd abc def ghi jkl mno pqr stu bar"),
                   { 'num_para_splits': 3,
                     'num_para_rearranges': 1,
                     'num_para_merges': 3 })

        # CHECK "A B C D" and "A C B D".
        self.check(para("foo bar baz boo"),
                   para("foo baz bar boo"),
                   { 'num_word_rearranges': 1 })

if __name__ == "__main__": 
    unittest.main()