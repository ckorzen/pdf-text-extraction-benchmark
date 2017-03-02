from json_serializer import JsonSerializer
from txt_serializer import TxtSerializer
from xml_serializer import XmlSerializer

serializers = {
    "txt": TxtSerializer,
    "xml": XmlSerializer,
    "json": JsonSerializer,
}


def serialize(doc, target, output_format="txt", roles_filter=[]):
    """
    Serializes the given TeX document to given target in given format.
    """
    if output_format not in serializers:
        raise ValueError("The format '%s' is not supported" % output_format)
    serializer = serializers[output_format](roles_filter)
    serializer.serialize(doc, target)


def get_serialization_choices():
    """
    Returns the supported serialization formats.
    """
    return sorted(list(serializers.keys()))
