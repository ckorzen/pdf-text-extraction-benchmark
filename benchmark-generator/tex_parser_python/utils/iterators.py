from models import tex_models

from collections import Iterator
from collections import deque


class BaseIterator(Iterator):
    """
    A base iterator.
    """
    def __init__(self, elements):
        """
        Creates a new iterator for the given elements.
        """
        self.stack = deque(elements)

    def __iter__(self):
        return self

    def __next__(self):
        raise StopIteration

    def peek(self):
        if len(self.stack) > 0:
            return self.stack[0]

    def pop(self):
        if not self.stack:
            raise StopIteration
        return self.stack.popleft()

    def extend(self, elements):
        self.stack.extendleft(reversed(elements))

    def skip_to_string(self, string):
        if string is None:
            return
        element = None
        while str(element) != string:
            element = self.pop()
        return element

    def skip_to_element(self, element):
        if element is None:
            return
        el = None
        while el != element:
            el = self.pop()
        return el

#    def skip_to_element(self, element):
#        if element is None:
#            return
#        while self.peek() != element:
#            self.pop()
#        return self.peek()


class ShallowIterator(BaseIterator):
    """
    An iterator that iterates the elements of a tree-like datastructure in a
    shallow manner.
    """

    def __init__(self, elements):
        """
        Creates a new iterator for the given elements.
        """
        super(ShallowIterator, self).__init__(elements)

    def __next__(self):
        element = self.pop()
        if element.has_elements_from_macro_expansion():
            self.extend(element.get_elements_from_macro_expansion())
            element = self.pop()
        return element


class DFSIterator(BaseIterator):
    """
    An iterator that iterates the elements of a tree-like datastructure in DFS
    order.
    """
    def __init__(self, elements):
        """
        Creates a new iterator for the given elements.
        """
        super(DFSIterator, self).__init__(elements)

    def __next__(self):
        element = self.pop()
        if element.has_elements_from_macro_expansion():
            self.extend(element.get_elements_from_macro_expansion())
            element = self.pop()

        if isinstance(element, tex_models.TeXGroup):
            self.extend(element.elements)
        if isinstance(element, tex_models.TeXCommand):
            self.extend(element.opts_args)
        return element
