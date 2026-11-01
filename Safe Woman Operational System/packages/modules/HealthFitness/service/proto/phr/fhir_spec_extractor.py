import fhirspec_pb2
from fhir_spec_utils import *
from typing import Collection, Mapping, Iterable

R4_FHIR_TYPE_PREFIX = "R4_FHIR_TYPE_"


class FhirSpecExtractor:
    """Extractor for getting information for HC FHIR validation from official FHIR spec json files.

    Typical usage example:
        extractor = new FhirSpecExtractor(profile_resources_json, profiles_types_json,
        {"Immunization", "Observation"})
        fhir_spec_message = extractor.generate_r4_fhir_spec_proto_message(profile_types_json)
    """

    def __init__(self, profile_resources_json: Mapping, resource_names: set[str]):
        """Extracts StructureDefinitions for the requested resources from the provided fhir spec.

        Args:
            profile_resources_json: The contents of the profile-resources.json fhir spec file, which
            is in the structure of a https://hl7.org/fhir/Bundle.html, parsed to dict. The
            Bundle.entry will contain the list of https://hl7.org/fhir/StructureDefinition.html that
            contain the resource definitions we are interested in.
            resource_names: The set of FHIR resources to extract FHIR spec information for.

        Raises:
            ValueError: If a requested resource is not present in the spec, if it's not supported by
            Health Connect, or if any spec values are not as expected.
        """
        if not resource_names.issubset(HC_SUPPORTED_RESOURCE_SET):
            raise ValueError("Provided resource set was not a subset of supported resources")

        # A mapping from the resource name to the list of field definitions, which are in the
        # structure of https://hl7.org/fhir/ElementDefinition.html
        self._resource_to_element_definitions = (
            self._extract_element_definitions_by_resource_from_spec(
                profile_resources_json, resource_names))

    def generate_r4_fhir_spec_proto_message(
            self, profile_types_json: Mapping) -> fhirspec_pb2.FhirResourceSpec:
        """Generates a FhirResourceSpec message from the fhir json spec.

        Args:
            profile_types_json: The contents of the profile-types.json fhir spec file, which
            is in the structure of a https://hl7.org/fhir/Bundle.html, parsed to dict. The
            Bundle.entry will contain the list of https://hl7.org/fhir/StructureDefinition.html that
            contain the data type definitions we are interested in.

        Returns:
            The FhirResourceSpec message, with an entry for each requested resource, and the data
            type configs for the required types.
        """
        r4_resource_spec = fhirspec_pb2.FhirResourceSpec()

        # Add Element type manually, as this is the type of primitive type extension fields.
        data_types_set = {fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_ELEMENT}
        data_type_configs = []

        for resource, element_definitions in self._resource_to_element_definitions.items():
            resource_type_int = RESOURCE_TYPE_STRING_TO_HC_INT_MAPPING[resource]

            resource_complex_type_config, child_types = (
                self._generate_complex_type_config_and_child_types_from_element_definitions(
                    element_definitions, is_resource_type=True))
            data_type_configs.extend(child_types)

            child_type_enums = set(child_type.fhir_type for child_type in child_types)
            all_complex_type_configs = [child_type.fhir_complex_type_config for child_type in
                                        child_types] + [resource_complex_type_config]

            for complex_type_config in all_complex_type_configs:
                new_sub_types = self._extract_subtypes_from_complex_type_config(complex_type_config)
                # Add sub types to data_types_set but skip child types, since we have already
                # extracted their definitions.
                data_types_set.update(new_sub_types - child_type_enums)

            r4_resource_spec.resource_type_to_config[
                resource_type_int].CopyFrom(resource_complex_type_config)

        data_type_configs.extend(self._get_fhir_data_type_configs_for_types_and_nested_types(
            profile_types_json, data_types_set))

        # Sort list by fhir_type before adding to make sure the script output is deterministic
        data_type_configs = sorted(data_type_configs, key=lambda x: x.fhir_type)
        r4_resource_spec.fhir_data_type_configs.extend(data_type_configs)

        return r4_resource_spec

    def _get_fhir_data_type_configs_for_types_and_nested_types(
            self,
            profile_types_json: Mapping,
            type_names: set[fhirspec_pb2.R4FhirType]) -> Iterable[fhirspec_pb2.FhirDataType]:
        # All structure definitions that have a matching enum value. If one is missing, this will
        # cause an exception when extracting the data type configs.
        all_type_to_structure_definition = {}
        for type_string, structure_definition in extract_type_to_structure_definitions_from_spec(
                profile_types_json, None).items():
            try:
                type_enum = self._get_type_enum_from_type_code(type_string)
                all_type_to_structure_definition[type_enum] = structure_definition
            except ValueError:
                print(f"Type {type_string} did not have an enum value.")

        return self._recursively_extract_data_type_configs_and_sub_types_by_type(
            all_type_to_structure_definition, type_names).values()

    def _recursively_extract_data_type_configs_and_sub_types_by_type(
            self,
            all_type_to_structure_definition: Mapping,
            fhir_types_to_extract: set[fhirspec_pb2.R4FhirType],
            already_extracted_types=set()) -> Mapping:
        new_types_to_extract = set()
        type_to_data_type_config_map = {}

        for fhir_type in fhir_types_to_extract:
            if fhir_type == fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_RESOURCE:
                # The Resource type definition does not exist in the profile types file. As we don't
                # support contained resources yet, we don't need a config for this type for now.
                type_to_data_type_config_map[fhir_type] = fhirspec_pb2.FhirDataType(
                    fhir_type=fhir_type,
                    kind=fhirspec_pb2.Kind.KIND_COMPLEX_TYPE)
                continue

            if fhir_type not in all_type_to_structure_definition:
                raise ValueError(
                    f"Type {fhir_type} was missing from the list of structure definitions.")

            structure_definition = all_type_to_structure_definition[fhir_type]
            kind = self._get_kind_enum_from_kind(structure_definition["kind"])
            complex_type_config = None

            if kind != fhirspec_pb2.Kind.KIND_PRIMITIVE_TYPE:
                complex_type_config, child_types = (
                    self._generate_complex_type_config_and_child_types_from_element_definitions(
                        extract_element_definitions_from_structure_def(structure_definition)))

                sub_types = self._extract_subtypes_from_complex_type_config(complex_type_config)
                child_type_enums = [child_type.fhir_type for child_type in child_types]
                for child_type in child_types:
                    type_to_data_type_config_map[child_type.fhir_type] = child_type
                    sub_types.update(self._extract_subtypes_from_complex_type_config(
                        child_type.fhir_complex_type_config))

                for sub_type in sub_types:
                    if sub_type not in already_extracted_types and \
                            sub_type not in fhir_types_to_extract and \
                            sub_type not in child_type_enums:
                        new_types_to_extract.add(sub_type)

            type_to_data_type_config_map[fhir_type] = fhirspec_pb2.FhirDataType(
                fhir_type=fhir_type,
                kind=kind,
                fhir_complex_type_config=complex_type_config)

        if new_types_to_extract:
            type_to_data_type_config_map.update(
                self._recursively_extract_data_type_configs_and_sub_types_by_type(
                    all_type_to_structure_definition, new_types_to_extract,
                    already_extracted_types.union(fhir_types_to_extract)))

        return type_to_data_type_config_map

    def _extract_subtypes_from_complex_type_config(
            self, complex_type_config: fhirspec_pb2.FhirComplexTypeConfig) -> Collection[
        fhirspec_pb2.R4FhirType]:
        return set(field_config.r4_type
                   for field_config in complex_type_config.allowed_field_names_to_config.values())

    def _extract_element_definitions_by_resource_from_spec(
            self, profile_resources_json: Mapping, resource_names: set[str]) -> Mapping:
        resource_to_element_definitions = {}

        for resource, structure_definition in extract_type_to_structure_definitions_from_spec(
                profile_resources_json, resource_names).items():
            if structure_definition["kind"] != "resource":
                raise ValueError(
                    "Unexpected kind field in structure definition. Expected resource.")
            resource_to_element_definitions[resource] = (
                extract_element_definitions_from_structure_def(structure_definition))

        return resource_to_element_definitions

    def _generate_complex_type_config_and_child_types_from_element_definitions(
            self,
            element_definitions: Collection[Mapping],
            is_resource_type=False) -> (
            fhirspec_pb2.FhirComplexTypeConfig, Collection[fhirspec_pb2.FhirDataType]):
        # Element definitions that have 2 field parts, and are therefore fields present in this type
        field_element_definitions = []
        # Element definitions with more than 2 field parts, which are used to define child types of
        # this type, so need to be handled separately.
        child_type_to_element_definitions = {}

        for element in element_definitions:
            field_id = element["id"]
            if field_id != element["path"]:
                raise ValueError("Expected id and path field to be the same")
            field_parts = field_id.split(".")
            field_parts_length = len(field_parts)

            if field_parts_length == 1:
                # This is the path to the element itself. For example for the Observation resource,
                # There will be an ElementDefinition with id "Observation"
                continue

            elif field_parts_length == 2:
                # This is a "regular" nested field, e.g. Immunization.status.
                field_element_definitions.append(element)

            elif field_parts_length == 3 or field_parts_length == 4:
                # This means the field is part of type BackBoneElement or Element, which are the
                # types of child types defined by Resource and Type definitions respectively.
                # For an example see the https://hl7.org/fhir/Immunization.html "reaction" field.
                # They need to be handled separately, as those fields don't have a type defined, but
                # have a child type definition instead.
                child_type_path = '.'.join(field_parts[:-1])
                if child_type_path not in child_type_to_element_definitions:
                    child_type_to_element_definitions[child_type_path] = []
                child_type_to_element_definitions[child_type_path].append(element)

            else:
                raise ValueError(
                    "This should not happen, unless there are more deeply nested child types")

        complex_type_definition = self._generate_complex_type_config_from_element_definitions(
            field_element_definitions,
            is_resource_type
        )

        child_data_types = []
        for path, element_definitions in child_type_to_element_definitions.items():
            child_data_types.append(
                fhirspec_pb2.FhirDataType(
                    fhir_type=self._get_type_enum_from_type_code(
                        generate_fhir_type_string_for_child_type_path(path)),
                    kind=fhirspec_pb2.Kind.KIND_COMPLEX_TYPE,
                    fhir_complex_type_config=
                    self._generate_complex_type_config_from_element_definitions(
                        element_definitions,
                        False
                    ))
            )

        return complex_type_definition, child_data_types

    def _generate_complex_type_config_from_element_definitions(
            self,
            element_definitions: Collection[Mapping],
            is_resource_type: bool) -> fhirspec_pb2.FhirComplexTypeConfig:
        """Generates a FhirComplexTypeDefinition from element definitions.

         The element definitions are expected to be pre filtered to contain only fields that should
         be part of the complex type config. This is done in
         _generate_complex_type_config_and_child_types_from_element_definitions().
        """
        required_fields = set()

        multi_type_configs = []

        field_configs_by_name = {}

        if is_resource_type:
            # Manually add resourceType field, as this is not present in the spec
            field_configs_by_name["resourceType"] = fhirspec_pb2.FhirFieldConfig(
                is_array=False,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_STRING
            )

        for element in element_definitions:
            field_name = element["id"].split(".")[-1]

            field_configs_to_add, multi_type_config = (
                self._generate_field_configs_and_multi_type_config_from_field_element(
                    element, field_name, is_resource_type))
            for name in field_configs_to_add:
                if name in field_configs_by_name: raise ValueError("Field name already exists")

            field_configs_by_name.update(field_configs_to_add)
            if self._field_name_is_multi_type_field(field_name):
                multi_type_configs.append(multi_type_config)
            elif self._field_is_required(element):
                required_fields.add(field_name)

        return fhirspec_pb2.FhirComplexTypeConfig(
            allowed_field_names_to_config=field_configs_by_name,
            # Sort the list of required fields alphabetically, as the output of this script is part
            # of the build, which needs to be deterministic. The required_fields come from a set,
            # which does not have ordering guarantees.
            required_fields=sorted(required_fields),
            multi_type_fields=multi_type_configs
        )

    def _generate_field_configs_and_multi_type_config_from_field_element(
            self, element_definition, field_name, is_resource_field: bool) -> (
            Mapping[str, fhirspec_pb2.FhirFieldConfig], list[fhirspec_pb2.MultiTypeFieldConfig]):
        field_is_array = self._field_is_array(element_definition)

        field_configs_by_name = {}

        multi_type_config = None

        if "contentReference" in element_definition:
            # If an element definition has a contentReference, it doesn't have a type, but instead
            # references a child type defined in a different field and we can assume the type from
            # the referenced path.
            # Since this only happens for one field currently, raise Error if it's a new field.
            if element_definition["path"] != "Observation.component.referenceRange":
                raise ValueError("Found unexpected field with contentReference")

            content_reference_string = element_definition["contentReference"]
            if content_reference_string.startswith('#'):
                content_reference_string = content_reference_string[1:]
            field_configs_by_name[field_name] = fhirspec_pb2.FhirFieldConfig(
                is_array=field_is_array,
                r4_type=self._get_type_enum_from_type_code(
                    generate_fhir_type_string_for_child_type_path(content_reference_string))
            )

        # If the field is a multi type field, it means one of several types can be set. An example
        # is the field Immunization.occurrence, which has types "string" and "dateTime" and
        # therefore means the fields "occurrenceString" and "occurrenceDateTime" are allowed. We
        # therefore expand the field name with each defined type.
        elif self._field_name_is_multi_type_field(field_name):
            if field_is_array:
                raise ValueError(
                    "Unexpected cardinality for type choice field. Did not expect array.")

            multi_type_fields = []
            for data_type in element_definition["type"]:
                field_with_type = self._get_multi_type_name_for_type(field_name, data_type["code"])
                type_enum = self._extract_type_enum_from_type(data_type, element_definition["path"])
                field_configs_by_name[field_with_type] = fhirspec_pb2.FhirFieldConfig(
                    is_array=False,
                    r4_type=type_enum
                )
                multi_type_fields.append(field_with_type)

            multi_type_config = fhirspec_pb2.MultiTypeFieldConfig(
                name=field_name,
                typed_field_names=multi_type_fields,
                is_required=self._field_is_required(element_definition)
            )

        else:
            if len(element_definition["type"]) != 1:
                raise ValueError("Expected exactly one type")
            fhir_type = element_definition["type"][0]
            # If the field is the resource "id" field manually set the type to be ID, since the spec
            # just uses System.String extension with value "string"
            type_enum = fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_ID \
                if is_resource_field and field_name == "id" \
                else self._extract_type_enum_from_type(fhir_type, element_definition["path"])
            field_configs_by_name[field_name] = fhirspec_pb2.FhirFieldConfig(
                is_array=field_is_array,
                r4_type=type_enum
            )

        return field_configs_by_name, multi_type_config

    def _field_name_is_multi_type_field(self, field_name) -> bool:
        """Returns true if the field is a oneof / type choice field, which can be contains several
        data types.

        This is the case if the field name ends with "[x]" and means that one of several types can
        be set.
        """

        return field_name.endswith("[x]")

    def _get_multi_type_name_for_type(self, field_name, type_code) -> bool:
        """Returns the one of field name for a specific type.

        For example for the field name "occurrence[x]" and type "dateTime" this will return
        "occurrenceDateTime".
        """

        return field_name[:-3] + type_code[0].upper() + type_code[1:]

    def _field_is_required(self, element_definition) -> bool:
        """Returns true if the field is required

        FHIR fields can have the following cardinalities:
        - 0..1, meaning the field is optional
        - 1..1, meaning the field is required
        - 0..*, meaning the field is an optional array
        - 1..*, meaning the field is a required array
        """

        min = element_definition["min"]

        if min not in [0, 1]:
            raise ValueError("Unexpected min cardinality value: " + min)

        return min

    def _field_is_array(self, element_definition) -> bool:
        """Returns true if the field should be an array

        FHIR fields can have the following cardinalities:
        - 0..1, meaning the field is optional
        - 1..1, meaning the field is required
        - 0..*, meaning the field is an optional array
        - 1..*, meaning the field is a required array
        """

        max = element_definition["max"]

        if max == "1":
            return False
        elif max == "*":
            return True
        else:
            raise ValueError("Unexpected max cardinality value: " + max)

    def _extract_type_enum_from_type(self,
                                     fhir_type: Mapping,
                                     field_path: str) -> fhirspec_pb2.R4FhirType:
        type_code = fhir_type["code"]

        # Many fields of type Quantity have a profile SimpleQuantity specified. This adds one fhir
        # constraint, which we don't validate, so we can ignore it.
        if "profile" in fhir_type and fhir_type["profile"] \
                != ["http://hl7.org/fhir/StructureDefinition/SimpleQuantity"]:
            raise ValueError(f"Unexpected profile {type['profile']} for type {type_code}")
        # TODO:b/385115510 - Consider validating targetProfile on "Reference" and "canonical" types.
        #  A Reference field definition for example usually specifies which type of resource can be
        #  referenced (e.g. reference to Encounter).

        # These types are used for child types defined in a resource or complex type, so we use
        # a separate type for them, derived from their path.
        if type_code in ["Element", "BackboneElement"]:
            return self._get_type_enum_from_type_code(
                generate_fhir_type_string_for_child_type_path(field_path))

        # System.String code is used in cases such as Resource.id field, Element.id field,
        # primitive type value field, and Extension.url field.
        # The code is used with the extension
        # https://hl7.org/fhir/extensions/StructureDefinition-structuredefinition-fhir-type.html.
        # For value fields (which we don't read), the "valueUrl" can take different values, but for
        # Resource.id fields and Element.id fields the valueUrl is "string", so we manually set it
        # to string.
        # The Resource.id field, should actually be of type id, but this isn't specified in the
        # r4 spec, so this is changed manually when extracting the resource configs.
        if type_code == "http://hl7.org/fhirpath/System.String":
            extension = fhir_type["extension"][0]
            if extension["url"] != \
                    "http://hl7.org/fhir/StructureDefinition/structuredefinition-fhir-type":
                raise ValueError("Unexpected extension value for code System.String")
            if extension["valueUrl"] == "uri":
                # Used by Extension.url field
                return fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_URI
            if extension["valueUrl"] == "string":
                # Used by Resource.id and Element.id field
                return fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_STRING
            else:
                raise ValueError(
                    f"Unexpected extension valueUrl in type extension: {str(fhir_type)}")

        return self._get_type_enum_from_type_code(type_code)

    def _get_type_enum_from_type_code(self, type_code: str) -> fhirspec_pb2.R4FhirType:
        if not type_code.isalnum():
            raise ValueError("Unexpected characters found in type_string: " + type_code)

        return fhirspec_pb2.R4FhirType.Value(
            R4_FHIR_TYPE_PREFIX + to_upper_snake_case(type_code))

    def _get_kind_enum_from_kind(self, kind_string: str) -> fhirspec_pb2.Kind:
        return fhirspec_pb2.Kind.Value("KIND_" + to_upper_snake_case(kind_string))
