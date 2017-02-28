import models

from collections import Iterator
from collections import deque

class DFSIterator(Iterator):
    def __init__(self, document):
        self.stack = deque(document.elements)

    def __iter__(self):
        return self

    def __next__(self):
        if not self.stack:
            raise StopIteration
        element = self.stack.popleft()
        if isinstance(element, models.TeXCommand):
            if getattr(element, "is_expanded", False):
                expanded_elements = getattr(element, "expanded", [])
                if len(expanded_elements) > 0:
                    self.stack += expanded_elements
                    element = self.stack.pop()
        elif isinstance(element, models.TeXMarker):
            if getattr(element, "is_expanded", False):
                expanded_elements = getattr(element, "expanded", [])
                if len(expanded_elements) > 0:
                    self.stack += expanded_elements
                    element = self.stack.pop()
        return element

    def extend(self, elements):
        self.stack.extendleft(reversed(elements))
