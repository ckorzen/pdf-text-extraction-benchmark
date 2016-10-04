import unittest
import doc_diff

class DocDiffTest(unittest.TestCase):
    
    def assert_equal(self, input1, input2, expected):
        diff_phrases = doc_diff.doc_diff(input1, input2)
        num_ops      = doc_diff.count_num_ops(diff_phrases)
        self.assertDictEqual(dict(num_ops), expected)
    
    def test_common(self):
        input1 = ""
        input2 = ""
        expected = {}
        self.assert_equal(input1, input2, expected)
        
        input1 = "foo bar"
        input2 = "foo bar"
        expected = {}
        self.assert_equal(input1, input2, expected)
        
        input1 = """
        foo bar
        
        baz boo
        """
        input2 = """
        foo bar
        
        baz boo
        """
        expected = {}
        self.assert_equal(input1, input2, expected)
        
    def test_rearranges(self):
        # Test rearrange with single paragraph and *word* operations.
        input1 = "foo bar"
        input2 = "bar foo"
        expected = { 'num_word_inserts': 1, 'num_word_deletes': 1 }
        self.assert_equal(input1, input2, expected)
    
        # Test rearrange with single paragraph and *paragraph* operations.
        input1 = "foo bar baz boo doo goo gol bol koi foi hul xxx"
        input2 = "gol bol koi foi hul xxx foo bar baz boo doo goo"
        expected = { 'num_para_rearranges': 1 }
        self.assert_equal(input1, input2, expected)
    
        # Test rearrange with 2 paragraphs and *word* operations.
        input1 = """
        foo bar
        
        baz boo
        """
        input2 = """
        baz boo
        
        foo bar
        """
        expected = { 'num_word_inserts': 2, 'num_word_deletes': 2 }
        self.assert_equal(input1, input2, expected)
            
        # Test rearrange with 2 paragraphs and *paragraph* operations.
        input1 = """
        foo bar baz boo doo goo
        
        goo gol bol koi foi hul xxx
        """
        input2 = """
        goo gol bol koi foi hul xxx
        
        foo bar baz boo doo goo
        """
        expected = { 'num_para_rearranges': 1 }
        self.assert_equal(input1, input2, expected)
        
        # Test rearrange whithin single paragraph where the rearrange is a short inner string.    
        input1 = "foo bar baz boo doo koo"
        input2 = "foo bar doo koo baz boo"
        expected = { 'num_word_inserts': 2, 'num_word_deletes': 2 }
        self.assert_equal(input1, input2, expected)
        
        # Test paragraph rearrange whithin single paragraph where the rearrange is a long inner string.
        input1 = "foo bar baz jojo hoko baw kli blu glu doo koo doo koo doo koo doo"
        input2 = "foo bar doo koo doo koo doo koo doo baz jojo hoko baw kli blu glu"
        expected = { 'num_para_rearranges': 1 }
        self.assert_equal(input1, input2, expected)
        
    def test_substitutes(self):
        # Test short replace within single paragraph.
        input1 = "foo bar"
        input2 = "foo bal"
        expected = { 'num_word_replaces': 1 }
        self.assert_equal(input1, input2, expected)

        # Test long replace within single paragraph.
        input1 = "foo xxx yyy zzz aaa bbb ccc ddd"
        input2 = "foo 111 222 333 444 555 666 777"
        expected = { 'num_word_replaces': 7 }
        self.assert_equal(input1, input2, expected)
        
        # Test replaces with different lengths.
        input1 = "foo xxx"
        input2 = "foo 111 222 333 444 555 666 777"
        expected = { 'num_word_replaces': 1, 'num_word_inserts': 6 }
        self.assert_equal(input1, input2, expected)

        # Test replaces with different lengths.
        input1 = "foo 111 222 333 444 555 666 777"
        input2 = "foo xxx"
        expected = { 'num_word_replaces': 1, 'num_word_deletes': 6 }
        self.assert_equal(input1, input2, expected)

        # Test replace across paragraphs boundaries.
        input1 = """
        Hello World
        
        How are you
        """
        input2 = """
        Hello Word
        
        Huw are you
        """
        expected = { 'num_word_replaces': 2 }
        self.assert_equal(input1, input2, expected)
     
    def test_deletes(self):        
        # Test long delete within two paragraphs.
        input1 = """
        foo bar 
        
        baz boo baz boo baz boo baz boo
        """
        input2 = """foo bar"""
        expected = { 'num_para_deletes': 1 }
        self.assert_equal(input1, input2, expected)

        # Test two deletes within paragraphs.
        input1 = """
        foo bar 
        
        baz boo
        
        doo goo
        """
        input2 = """foo bar"""
        expected = { 'num_word_deletes': 4 }
        self.assert_equal(input1, input2, expected)
        
        # Test two deletes within paragraphs.
        input1 = """
        foo bar 
        
        baz boo baz boo baz boo baz boo
        
        doo goo
        """
        input2 = """foo bar"""
        expected = { 'num_word_deletes': 2, 'num_para_deletes': 1 }
        self.assert_equal(input1, input2, expected)
        

        # Test short inner delete.
        input1 = "foo bar baz"
        input2 = "foo baz"
        expected = { 'num_word_deletes': 1 }
        self.assert_equal(input1, input2, expected)
        
        # Test long inner delete.
        input1 = "foo bar gar koo roo woo tul bol baz"
        input2 = "foo baz"
        expected = { 'num_para_deletes': 1 }
        self.assert_equal(input1, input2, expected)
        
        # Test delete across paragraph boundaries.
        input1 = """
        foo bar baz 
        
        goo koo loo
        """
        input2 = """
        foo bar
        
        koo loo
        """
        expected = { 'num_word_deletes': 2 }
        self.assert_equal(input1, input2, expected)
    
        # Test delete across paragraph boundaries.
        input1 = """
        foo bar baz baz baz
        
        goo goo goo koo loo
        """
        input2 = """
        foo bar
        
        koo loo
        """
        expected = { 'num_word_deletes': 6 }
        self.assert_equal(input1, input2, expected)
               
    def test_inserts(self):
        # Test short insert within single paragraph.
        input1 = "foo"
        input2 = "foo bar baz"
        expected = { 'num_word_inserts': 2 }
        self.assert_equal(input1, input2, expected)
        
        # Test long insert within single paragraph.
        input1 = "foo"
        input2 = "foo bar baz boo goo loo hoo too"
        expected = { 'num_para_inserts': 1 }
        self.assert_equal(input1, input2, expected)
        
        # Test short insert within two paragraphs.
        input1 = """foo bar"""
        input2 = """
        foo bar 
        
        baz boo
        """
        expected = { 'num_word_inserts': 2 }
        self.assert_equal(input1, input2, expected)
        
        # Test long insert within two paragraphs.
        input1 = """foo bar"""
        input2 = """
        foo bar 
        
        baz boo baz boo baz boo baz boo
        """
        expected = { 'num_para_inserts': 1 }
        self.assert_equal(input1, input2, expected)

        # Test two inserts within paragraphs.
        input1 = """foo bar"""
        input2 = """
        foo bar 
        
        baz boo
        
        doo goo
        """
        expected = { 'num_word_inserts': 4 }
        self.assert_equal(input1, input2, expected)
        
        # Test two inserts within paragraphs.
        input1 = """foo bar"""
        input2 = """
        foo bar 
        
        baz boo baz boo baz boo baz boo
        
        doo goo
        """
        expected = { 'num_word_inserts': 2, 'num_para_inserts': 1 }
        self.assert_equal(input1, input2, expected)
        
        # Test short inner insert.
        input1 = "foo baz"
        input2 = "foo bar baz"
        expected = { 'num_word_inserts': 1 }
        self.assert_equal(input1, input2, expected)
        
        # Test long inner insert.
        input1 = "foo baz"
        input2 = "foo bar gar koo roo woo tul bol baz"
        expected = { 'num_para_inserts': 1 }
        self.assert_equal(input1, input2, expected)
                
        # Test delete across paragraph boundaries.
        input1 = """
        foo bar
        
        koo loo
        """
        input2 = """
        foo bar baz 
        
        goo koo loo
        """
        expected = { 'num_word_inserts': 2 }
        self.assert_equal(input1, input2, expected)
    
        # Test delete across paragraph boundaries.
        input1 = """
        foo bar
        
        koo loo
        """
        input2 = """
        foo bar baz baz baz
        
        goo goo goo koo loo
        """
        expected = { 'num_word_inserts': 6 }
        self.assert_equal(input1, input2, expected)
                   
        # Test insert of a short paragraph.
        input1 = """
        bar
        """
        input2 = """
        foo
                
        bar
        """
        expected = { 'num_word_inserts': 1 }
        self.assert_equal(input1, input2, expected)
                        
        # Test insert of a long paragraph.
        input1 = """
        bar
        """
        input2 = """
        foo foo foo foo foo foo foo
                
        bar
        """
        expected = { 'num_para_inserts': 1 }
        self.assert_equal(input1, input2, expected)
                                
if __name__ == '__main__':
    unittest.main()
