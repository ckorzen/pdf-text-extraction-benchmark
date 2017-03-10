from json_serializer import JsonSerializer
from txt_serializer import TxtSerializer
from xml_serializer import XmlSerializer

# Define the available serializers.
serializers = {
    "txt": TxtSerializer,
    "xml": XmlSerializer,
    "json": JsonSerializer,
}


def serialize(doc, output_format="txt", roles_filter=[]):
    """
    Serializes the given TeX document to given format. If the given roles
    filter is non-empty, only the LTBs with matching semantic roles are
    serialized.

    Args:
        doc (TeXDocument): The TeX document to serialize.
        output_format (str, optional): The serialization format.
        roles_filter (list of str): The roles of blocks to serialize.
    Returns:
        The serialization string.
    """
    if output_format not in serializers:
        raise ValueError("The format '%s' is not supported" % output_format)
    serializer = serializers[output_format](roles_filter)
    return serializer.serialize(doc)


def get_serialization_choices():
    """
    Returns the available serialization formats.

    Returns:
        A list of the available serialization formats.
    """
    return sorted(list(serializers.keys()))
