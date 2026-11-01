import fhirspec_pb2
import sys
import os
import json
from fhir_spec_extractor import FhirSpecExtractor, HC_SUPPORTED_RESOURCE_SET
from google.protobuf import text_format
from typing import List, Mapping

_TEXTPROTO_EXT = "textproto"
_BINARY_PB_EXT = "binarypb"

if __name__ == '__main__':
    output_file_name = sys.argv[1]
    input_file_names = sys.argv[2:]

    output_file_extension = output_file_name.split('.')[-1]
    if output_file_extension != _TEXTPROTO_EXT and output_file_extension != _BINARY_PB_EXT:
        raise ValueError(f"Output file extension must be '{_TEXTPROTO_EXT}' or '{_BINARY_PB_EXT}'")

    resource_definitions_file_name = None
    type_definitions_file_name = None

    for path in input_file_names:
        base_file_name = os.path.basename(path)
        if base_file_name == "profiles-resources.json":
            resource_definitions_file_name = path
        elif base_file_name == "profiles-types.json":
            type_definitions_file_name = path

    if not resource_definitions_file_name:
        raise ValueError("Expected profiles-resources.json")

    if not type_definitions_file_name:
        raise ValueError("Expected profiles-types.json")

    with open(resource_definitions_file_name, 'r') as resources_file, open(
            type_definitions_file_name, 'r') as types_file:
        profiles_resources_json = json.load(resources_file)
        profiles_types_json = json.load(types_file)

        fhirSpecExtractor = FhirSpecExtractor(profiles_resources_json, HC_SUPPORTED_RESOURCE_SET)

        r4_resource_spec = fhirSpecExtractor.generate_r4_fhir_spec_proto_message(
            profiles_types_json)

    if output_file_extension == _TEXTPROTO_EXT:
        with open(output_file_name, 'w') as f:
            f.write(text_format.MessageToString(r4_resource_spec))
    else:
        with open(output_file_name, 'wb') as f:
            f.write(r4_resource_spec.SerializeToString())
