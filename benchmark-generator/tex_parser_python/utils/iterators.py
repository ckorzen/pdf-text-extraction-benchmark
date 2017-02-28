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

    def pop(self):
        if not self.stack:
            raise StopIteration
        return self.stack.popleft()

    def extend(self, elements):
        self.stack.extendleft(reversed(elements))


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
        expanded = element.get_expanded()
        self.extend(expanded)
        element = self.pop() if len(expanded) > 0 else element
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
        expanded = element.get_expanded()
        self.extend(expanded)
        element = self.pop() if len(expanded) > 0 else element

        if isinstance(element, tex_models.TeXGroup):
            self.extend(element.elements)
        if isinstance(element, tex_models.TeXCommand):
            self.extend(element.opts_and_args)
        return element
