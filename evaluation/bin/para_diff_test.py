import unittest
from para_diff import para_diff

class ParaDiffTest(unittest.TestCase):

    def assert_equal(self, input1, input2, expected_str):
        actual     = para_diff(input1, input2)
        actual_str = "[%s]" % ", ".join([str(x) for x in actual]) 
        self.assertEqual(actual_str, expected_str)

    def test_equal_paras(self):
        input1   = []
        input2   = []
        expected = "[]"
        self.assert_equal(input1, input2, expected)

        input1   = [["foo", "bar"]]
        input2   = [["foo", "bar"]]
        expected = "[[= ['foo', 'bar'], ['foo', 'bar']]]"
        self.assert_equal(input1, input2, expected)

        input1   = [["foo", "bar"], ["baz", "boo"]] 
        input2   = [["foo", "bar"], ["baz", "boo"]]
        expected = "[[= ['foo', 'bar'], ['foo', 'bar']], [= ['baz', 'boo'], ['baz', 'boo']]]"
        self.assert_equal(input1, input2, expected)

    def test_delete_para(self):
        input1 = [["foo"]]
        input2 = []
        expected = "[[/ ['foo'], []]]"
        self.assert_equal(input1, input2, expected)

        input1 = [["foo", "bar"]]
        input2 = []
        expected = "[[/ ['foo', 'bar'], []]]"
        self.assert_equal(input1, input2, expected)

        input1 = [["foo"], ["bar"]]
        input2 = []
        expected = "[[/ ['foo'], []], [/ ['bar'], []]]"
        self.assert_equal(input1, input2, expected)

        input1 = [["foo"], ["bar"]]
        input2 = [["bar"]]
        expected = "[[/ ['foo'], []], [= ['bar'], ['bar']]]"
        self.assert_equal(input1, input2, expected)

        input1 = [["foo", "bar"], ["baz"]]
        input2 = [["foo"], ["baz"]]
        expected = "[[= ['foo'], ['foo']], [/ ['bar'], []], [= ['baz'], ['baz']]]"
        self.assert_equal(input1, input2, expected)
                
    def test_insert_para(self):
        input1 = []
        input2 = [["foo"]]
        expected = "[[/ [], ['foo']]]"
        self.assert_equal(input1, input2, expected)

        input1 = []
        input2 = [["foo", "bar"]]
        expected = "[[/ [], ['foo', 'bar']]]"
        self.assert_equal(input1, input2, expected)

        input1 = []
        input2 = [["foo"], ["bar"]]        
        expected = "[[/ [], ['foo']], [/ [], ['bar']]]"
        self.assert_equal(input1, input2, expected)

        input1 = [["bar"]]
        input2 = [["foo"], ["bar"]]
        expected = "[[/ [], ['foo']], [= ['bar'], ['bar']]]"
        self.assert_equal(input1, input2, expected)

        input1 = [["foo"], ["baz"]]
        input2 = [["foo", "bar"], ["baz"]]
        expected = "[[= ['foo'], ['foo']], [/ [], ['bar']], [= ['baz'], ['baz']]]"
        self.assert_equal(input1, input2, expected)
        
    def test_replace_para(self):
        input1 = [["foo"]]
        input2 = [["bar"]]
        expected = "[[/ ['foo'], ['bar']]]"
        self.assert_equal(input1, input2, expected)

        input1 = [["foo", "bar"]]
        input2 = [["baz", "boo"]]
        expected = "[[/ ['foo', 'bar'], ['baz', 'boo']]]"
        self.assert_equal(input1, input2, expected)

        input1 = [["foo"], ["bar"]]
        input2 = [["baz"], ["boo"]]
        expected = "[[/ ['foo'], ['baz']], [/ ['bar'], ['boo']]]"
        self.assert_equal(input1, input2, expected)

        input1 = [["foo", "bar"]]
        input2 = [["baz"]]
        expected = "[[/ ['foo', 'bar'], ['baz']]]"
        self.assert_equal(input1, input2, expected)
        
        input1 = [["foo"], ["bar"]]
        input2 = [["baz"]]
        expected = "[[/ ['foo'], ['baz']], [/ ['bar'], []]]"
        self.assert_equal(input1, input2, expected)
        
        input1 = [["foo"]]
        input2 = [["baz", "boo"]]
        expected = "[[/ ['foo'], ['baz', 'boo']]]"
        self.assert_equal(input1, input2, expected)
       
        input1 = [["foo"]]
        input2 = [["baz"], ["boo"]]
        expected = "[[/ ['foo'], ['baz']], [/ [], ['boo']]]"
        self.assert_equal(input1, input2, expected)
        
    def test_split_para(self):
        input1 = [["foo", "bar"]]
        input2 = [["foo"], ["bar"]]
        expected = "[[= ['foo'], ['foo']], [split], [= ['bar'], ['bar']]]"
        self.assert_equal(input1, input2, expected)
        
        input1 = [["foo", "bar", "baz"]]
        input2 = [["foo"], ["bar"], ["baz"]]
        expected = "[[= ['foo'], ['foo']], [split], [= ['bar'], ['bar']], [split], [= ['baz'], ['baz']]]"
        self.assert_equal(input1, input2, expected)
        
        input1 = [["foo", "bar", "baz"]]
        input2 = [["foo", "bar"], ["baz"]]
        expected = "[[= ['foo', 'bar'], ['foo', 'bar']], [split], [= ['baz'], ['baz']]]"
        self.assert_equal(input1, input2, expected)
               
    def test_merge_para(self):
        input1 = [["foo"], ["bar"]]
        input2 = [["foo", "bar"]]
        expected = "[[= ['foo'], ['foo']], [merge], [= ['bar'], ['bar']]]"
        self.assert_equal(input1, input2, expected)

        input1 = [["foo"], ["bar"], ["baz"]]        
        input2 = [["foo", "bar", "baz"]]
        expected = "[[= ['foo'], ['foo']], [merge], [= ['bar'], ['bar']], [merge], [= ['baz'], ['baz']]]"
        self.assert_equal(input1, input2, expected)
        
        input1 = [["foo", "bar"], ["baz"]]
        input2 = [["foo", "bar", "baz"]]
        expected = "[[= ['foo', 'bar'], ['foo', 'bar']], [merge], [= ['baz'], ['baz']]]"
        self.assert_equal(input1, input2, expected)
     
    def test_rearrange_para(self):
        input1 = [["foo"], ["bar"]]
        input2 = [["bar"], ["foo"]]
        expected = "[[~ ['foo'], ['foo']], [= ['bar'], ['bar']]]"
        self.assert_equal(input1, input2, expected)
        
        input1 = [["foo"], ["bar"], ["baz"]]
        input2 = [["baz"], ["bar"], ["foo"]]
        expected = "[[~ ['foo'], ['foo']], [~ ['bar'], ['bar']], [= ['baz'], ['baz']]]"
        self.assert_equal(input1, input2, expected)
        
        input1 = [["foo", "bar", "baz"]]
        input2 = [["baz", "bar", "foo"]]
        expected = "[[~ ['foo'], ['foo']], [~ ['bar'], ['bar']], [= ['baz'], ['baz']]]"
        self.assert_equal(input1, input2, expected)
        
        input1 = [["foo", "bar", "baz", "boo"]]
        input2 = [["baz", "boo", "foo", "bar"]]
        expected = "[[~ ['foo', 'bar'], ['foo', 'bar']], [= ['baz', 'boo'], ['baz', 'boo']]]"
        self.assert_equal(input1, input2, expected)
        
        input1 = [["foo", "bar"], ["baz"]]
        input2 = [["baz"], ["foo", "bar"]]
        expected = "[[= ['foo', 'bar'], ['foo', 'bar']], [~ ['baz'], ['baz']]]"
        self.assert_equal(input1, input2, expected)
         
    def test_various_combinations(self):
        input1 = [["foo"], ["bar"]]
        input2 = [["foo", "baz", "bar"]]
        expected = "[[= ['foo'], ['foo']], [/ [], ['baz']], [merge], [= ['bar'], ['bar']]]"
        # self.assert_equal(input1, input2, expected)
        
        input1 = [["foo", "bar"]]
        input2 = [["foo", "baz"], ["bar"]]
        expected = "[[= ['foo'], ['foo']], [/ [], ['baz']], [split], [= ['bar'], ['bar']]]"
        # self.assert_equal(input1, input2, expected)
                       
if __name__ == '__main__':
    unittest.main()
