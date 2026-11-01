import unittest
import fhirspec_pb2
from google.protobuf import text_format


class GenerateFhirSpecR4BinaryPbTest(unittest.TestCase):

    def test_generate_fhir_spec_r4_binarypb_output_as_expected(self):
        with open('fhirspec-r4.binarypb', 'rb') as generated_proto:
            generated_fhir_spec = fhirspec_pb2.FhirResourceSpec.FromString(generated_proto.read())

        with (open('expected_fhir_spec_r4_for_test.textproto', 'r') as expected_text_proto):
            expected_fhir_spec = fhirspec_pb2.FhirResourceSpec()
            text_format.Parse(expected_text_proto.read(), expected_fhir_spec)

        self.assertEqual(
            str(generated_fhir_spec), str(expected_fhir_spec),
            "If the diff is expected, regenerate expected_fhir_spec_r4_for_test.textproto")


if __name__ == '__main__':
    unittest.main()
