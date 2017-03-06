from models import tex_models

from collections import Iterator
from collections import deque


class BaseIterator(Iterator):
    """
    The super class for any iterators.
    """
    def __init__(self, elements):
        """
        Creates a new iterator for the given elements.

        Args:
            elements (list of TeXElement): The elements to iterate.
        """
        self.stack = deque(elements)

    # Override
    def __iter__(self):
        return self

    # Override
    def __next__(self):
        raise StopIteration

    def peek(self):
        """
        Returns the topmost element of the inner stack without popping it.

        Returns:
            The topmost element in the stack.
        """
        if len(self.stack) > 0:
            return self.stack[0]

    def pop(self):
        """
        Pops the topmost element from inner stack and returns it.

        Returns:
            The topmost element in the stack.
        """
        if not self.stack:
            raise StopIteration
        return self.stack.popleft()

    def extend(self, elements):
        """
        Extends the inner stack by the given elements.

        Args:
            elements (list of TeXElement): The elements to insert into the
                stack.
        """
        self.stack.extendleft(reversed(elements))

    def skip_to_string(self, string):
        """
        Advances to the element given by string.

        Args:
            string (str): The string of elements to advance to.
        Returns:
            The last popped element on advancing to the given element.
        """
        if string is None:
            return
        element = None
        while str(element) != string:
            element = self.pop()
        return element

    def skip_to_element(self, element):
        """
        Advances to the given element given.

        Args:
            element (TeXElement): The element to advance to.
        Returns:
            The last popped element on advancing to the given element.
        """
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

        Args:
            elements (list of TeXElement): the elements to iterate.
        """
        super(ShallowIterator, self).__init__(elements)

    # Override
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

        Args:
            elements (list of TeXElement): the elements to iterate.
        """
        super(DFSIterator, self).__init__(elements)

    # Override
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
