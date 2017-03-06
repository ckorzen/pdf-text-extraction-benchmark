from json_serializer import JsonSerializer
from txt_serializer import TxtSerializer
from xml_serializer import XmlSerializer

# The available serializers.
serializers = {
    "txt": TxtSerializer,
    "xml": XmlSerializer,
    "json": JsonSerializer,
}


def serialize(doc, target, output_format="txt", roles_filter=[]):
    """
    Serializes the given TeX document to given target in given format.

    Args:
        doc (TeXDocument): The TeX document to serialize.
        target (str): The path to the target path. If None, the document will
            be serialized to stdout.
        output_format (str, optional): The serialization format.
        roles_filter (list of str): The roles of blocks to serialize.
    """
    if output_format not in serializers:
        raise ValueError("The format '%s' is not supported" % output_format)
    serializer = serializers[output_format](roles_filter)
    serializer.serialize(doc, target)


def get_serialization_choices():
    """
    Returns the available serialization formats.

    Returns:
        The available serialization formats.
    """
    return sorted(list(serializers.keys()))
