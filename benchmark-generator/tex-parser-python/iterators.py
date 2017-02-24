import models

from collections import Iterator


class DFSIterator(Iterator):
    def __init__(self, document):
        self.stack = list(document.elements)

    def __iter__(self):
        return self

    def __next__(self):
        if not self.stack:
            raise StopIteration
        element = self.stack.pop()
        if isinstance(element, models.TeXGroup):
            self.stack += element.elements
        elif isinstance(element, models.TeXCommand):
            if getattr(element, "is_expanded", False):
                self.stack += getattr(element, "expanded", [])
            else:
                for opt_or_arg in element.opts_and_args:
                    self.stack += opt_or_arg.elements
        elif isinstance(element, models.TeXMarker):
            if getattr(element, "is_expanded", False):
                self.stack += getattr(element, "expanded", [])
        return element
