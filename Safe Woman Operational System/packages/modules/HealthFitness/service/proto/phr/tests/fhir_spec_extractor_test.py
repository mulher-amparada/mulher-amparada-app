import unittest
import json
import fhirspec_pb2
from fhir_spec_extractor import FhirSpecExtractor
from google.protobuf import text_format


class FhirSpecExtractorTest(unittest.TestCase):
    BUNDLE_WITH_IMMUNIZATION_AND_PATIENT_STRUCTURE_DEFINITION = json.loads("""
    {
        "resourceType" : "Bundle",
        "id" : "resources",
        "meta" : {
          "lastUpdated" : "2019-11-01T09:29:23.356+11:00"
        },
        "type" : "collection",
        "entry" : [
          {
            "fullUrl" : "http://hl7.org/fhir/CompartmentDefinition/relatedPerson"
          },
          {
            "fullUrl" : "http://hl7.org/fhir/StructureDefinition/Immunization",
            "resource" : {
              "resourceType" : "StructureDefinition",
              "id" : "Immunization",
              "meta" : {
                "lastUpdated" : "2019-11-01T09:29:23.356+11:00"
              },
              "fhirVersion" : "4.0.1",
              "kind" : "resource",
              "type" : "Immunization",
              "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/DomainResource",
              "snapshot" : {
                "element" : [{
                  "id" : "Immunization",
                  "path" : "Immunization",
                  "min" : 0,
                  "max" : "*",
                  "base" : {
                    "path" : "Immunization",
                    "min" : 0,
                    "max" : "*"
                  }
                },
                {
                  "id" : "Immunization.id",
                  "path" : "Immunization.id",
                  "min" : 0,
                  "max" : "1",
                  "base" : {
                    "path" : "Resource.id",
                    "min" : 0,
                    "max" : "1"
                  },
                  "type" : [{
                    "extension" : [{
                      "url" : "http://hl7.org/fhir/StructureDefinition/structuredefinition-fhir-type",
                      "valueUrl" : "string"
                    }],
                    "code" : "http://hl7.org/fhirpath/System.String"
                  }]
                },
                {
                  "id" : "Immunization.status",
                  "path" : "Immunization.status",
                  "min" : 1,
                  "max" : "1",
                  "base" : {
                    "path" : "Immunization.status",
                    "min" : 1,
                    "max" : "1"
                  },
                  "type" : [{
                    "code" : "code"
                  }]
                },
                {
                  "id" : "Immunization.vaccineCode",
                  "path" : "Immunization.vaccineCode",
                  "min" : 1,
                  "max" : "1",
                  "base" : {
                    "path" : "Immunization.vaccineCode",
                    "min" : 1,
                    "max" : "1"
                  },
                  "type" : [{
                    "code" : "CodeableConcept"
                  }]
                },
                {
                  "id" : "Immunization.exampleFieldToTestOneToMany",
                  "path" : "Immunization.exampleFieldToTestOneToMany",
                  "min" : 1,
                  "max" : "*",
                  "base" : {
                    "path" : "Immunization.exampleFieldToTestOneToMany",
                    "min" : 1,
                    "max" : "*"
                  },
                  "type" : [{
                    "code" : "CodeableConcept"
                  }]
                },
                {
                  "id" : "Immunization.occurrence[x]",
                  "path" : "Immunization.occurrence[x]",
                  "min" : 1,
                  "max" : "1",
                  "base" : {
                    "path" : "Immunization.occurrence[x]",
                    "min" : 1,
                    "max" : "1"
                  },
                  "type" : [{
                    "code" : "dateTime"
                  },
                  {
                    "code" : "string"
                  }]
                },
                {
                  "id" : "Immunization.performer",
                  "path" : "Immunization.performer",
                  "min" : 0,
                  "max" : "*",
                  "base" : {
                    "path" : "Immunization.performer",
                    "min" : 0,
                    "max" : "*"
                  },
                  "type" : [{
                    "code" : "BackboneElement"
                  }]
                },
                {
                  "id" : "Immunization.performer.id",
                  "path" : "Immunization.performer.id",
                  "min" : 0,
                  "max" : "1",
                  "base" : {
                    "path" : "Element.id",
                    "min" : 0,
                    "max" : "1"
                  },
                  "type" : [{
                    "extension" : [{
                      "url" : "http://hl7.org/fhir/StructureDefinition/structuredefinition-fhir-type",
                      "valueUrl" : "string"
                    }],
                    "code" : "http://hl7.org/fhirpath/System.String"
                  }]
                },
                {
                  "id" : "Immunization.performer.extension",
                  "path" : "Immunization.performer.extension",
                  "min" : 0,
                  "max" : "*",
                  "base" : {
                    "path" : "Element.extension",
                    "min" : 0,
                    "max" : "*"
                  },
                  "type" : [{
                    "code" : "Extension"
                  }]
                },
                {
                  "id" : "Immunization.performer.actor",
                  "path" : "Immunization.performer.actor",
                  "min" : 1,
                  "max" : "1",
                  "base" : {
                    "path" : "Immunization.performer.actor",
                    "min" : 1,
                    "max" : "1"
                  },
                  "type" : [{
                    "code" : "Reference"
                  }]
                },
                {
                  "id" : "Immunization.contained",
                  "path" : "Immunization.contained",
                  "min" : 0,
                  "max" : "*",
                  "base" : {
                    "path" : "DomainResource.contained",
                    "min" : 0,
                    "max" : "*"
                  },
                  "type" : [{
                    "code" : "Resource"
                  }]
                }
              ]
              }
            }
          },
          {
            "fullUrl" : "http://hl7.org/fhir/StructureDefinition/Patient",
            "resource" : {
              "resourceType" : "StructureDefinition",
              "id" : "Patient",
              "meta" : {
                "lastUpdated" : "2019-11-01T09:29:23.356+11:00"
              },
              "url" : "http://hl7.org/fhir/StructureDefinition/Patient",
              "fhirVersion" : "4.0.1",
              "kind" : "resource",
              "type" : "Patient",
              "snapshot" : {
                "element" : [{
                  "id" : "Patient.id",
                  "path" : "Patient.id",
                  "min" : 0,
                  "max" : "1",
                  "base" : {
                    "path" : "Resource.id",
                    "min" : 0,
                    "max" : "1"
                  },
                  "type" : [{
                    "extension" : [{
                      "url" : "http://hl7.org/fhir/StructureDefinition/structuredefinition-fhir-type",
                      "valueUrl" : "string"
                    }],
                    "code" : "http://hl7.org/fhirpath/System.String"
                  }]
                }]
              }
            }
          }
        ]
    }
    """)

    BUNDLE_WITH_OBSERVATION_STRUCTURE_DEFINITION = json.loads("""
    {
        "resourceType" : "Bundle",
        "id" : "resources",
        "meta" : {
          "lastUpdated" : "2024-12-06T22:01:38.605+00:00"
        },
        "type" : "collection",
        "entry" : [
          {
            "fullUrl" : "http://hl7.org/fhir/CompartmentDefinition/relatedPerson"
          },
          {
            "fullUrl" : "http://hl7.org/fhir/StructureDefinition/Observation",
            "resource" : {
              "resourceType" : "StructureDefinition",
              "id" : "Observation",
              "meta" : {
                "lastUpdated" : "2024-12-06T22:01:38.605+00:00"
              },
              "fhirVersion" : "4.0.1",
              "kind" : "resource",
              "type" : "Observation",
              "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/DomainResource",
              "snapshot" : {
                "element" : [{
                  "id" : "Observation",
                  "path" : "Observation",
                  "min" : 0,
                  "max" : "*",
                  "base" : {
                    "path" : "Observation",
                    "min" : 0,
                    "max" : "*"
                  }
                },
                {
                  "id" : "Observation.id",
                  "path" : "Observation.id",
                  "min" : 0,
                  "max" : "1",
                  "base" : {
                    "path" : "Resource.id",
                    "min" : 0,
                    "max" : "1"
                  },
                  "type" : [{
                    "extension" : [{
                      "url" : "http://hl7.org/fhir/StructureDefinition/structuredefinition-fhir-type",
                      "valueUrl" : "id"
                    }],
                    "code" : "http://hl7.org/fhirpath/System.String"
                  }]
                },
                {
                  "id" : "Observation.status",
                  "path" : "Observation.status",
                  "min" : 1,
                  "max" : "1",
                  "base" : {
                    "path" : "Observation.status",
                    "min" : 1,
                    "max" : "1"
                  },
                  "type" : [{
                    "code" : "code"
                  }]
                },
                {
                  "id" : "Observation.code",
                  "path" : "Observation.code",
                  "min" : 1,
                  "max" : "1",
                  "base" : {
                    "path" : "Observation.code",
                    "min" : 1,
                    "max" : "1"
                  },
                  "type" : [{
                    "code" : "CodeableConcept"
                  }]
                },
                {
                  "id" : "Observation.value[x]",
                  "path" : "Observation.value[x]",
                  "min" : 0,
                  "max" : "1",
                  "base" : {
                    "path" : "Observation.value[x]",
                    "min" : 1,
                    "max" : "1"
                  },
                  "type" : [{
                    "code": "Quantity"
                  },
                  {
                    "code": "CodeableConcept"
                  },
                  {
                    "code": "string"
                  },
                  {
                    "code": "boolean"
                  },
                  {
                    "code": "integer"
                  },
                  {
                    "code": "SampledData"
                  },
                  {
                    "code": "dateTime"
                  }]
                }
              ]
              }
            }
          }
        ]
    }
    """)

    BUNDLE_WITH_TYPE_STRUCTURE_DEFINITIONS = json.loads("""
    {
  "resourceType": "Bundle",
  "id": "types",
  "meta": {
    "lastUpdated": "2019-11-01T09:29:23.356+11:00"
  },
  "type": "collection",
  "entry": [
    {
      "fullUrl": "http://hl7.org/fhir/StructureDefinition/boolean",
      "resource": {
        "resourceType": "StructureDefinition",
        "id": "boolean",
        "url": "http://hl7.org/fhir/StructureDefinition/boolean",
        "name": "boolean",
        "fhirVersion": "4.0.1",
        "kind": "primitive-type",
        "abstract": false,
        "type": "boolean",
        "baseDefinition": "http://hl7.org/fhir/StructureDefinition/Element",
        "snapshot": {
          "element": [
            {
              "id": "boolean",
              "path": "boolean",
              "min": 0,
              "max": "*",
              "base": {
                "path": "boolean",
                "min": 0,
                "max": "*"
              },
              "isModifier": false,
              "isSummary": false
            },
            {
              "id": "boolean.id",
              "path": "boolean.id",
              "min": 0,
              "max": "1",
              "base": {
                "path": "Element.id",
                "min": 0,
                "max": "1"
              },
              "type": [
                {
                  "extension": [
                    {
                      "url": "http://hl7.org/fhir/StructureDefinition/structuredefinition-fhir-type",
                      "valueUrl": "string"
                    }
                  ],
                  "code": "http://hl7.org/fhirpath/System.String"
                }
              ],
              "isModifier": false,
              "isSummary": false
            },
            {
              "id": "boolean.extension",
              "path": "boolean.extension",
              "min": 0,
              "max": "*",
              "base": {
                "path": "Element.extension",
                "min": 0,
                "max": "*"
              },
              "type": [
                {
                  "code": "Extension"
                }
              ],
              "isModifier": false,
              "isSummary": false
            },
            {
              "id": "boolean.value",
              "path": "boolean.value",
              "representation": [
                "xmlAttr"
              ],
              "short": "Primitive value for boolean",
              "definition": "The actual value",
              "min": 0,
              "max": "1",
              "base": {
                "path": "boolean.value",
                "min": 0,
                "max": "1"
              },
              "type": [
                {
                  "extension": [
                    {
                      "url": "http://hl7.org/fhir/StructureDefinition/structuredefinition-fhir-type",
                      "valueUrl": "boolean"
                    },
                    {
                      "url": "http://hl7.org/fhir/StructureDefinition/regex",
                      "valueString": "true|false"
                    }
                  ],
                  "code": "http://hl7.org/fhirpath/System.Boolean"
                }
              ],
              "isModifier": false,
              "isSummary": false
            }
          ]
        }
      }
    },
    {
      "fullUrl": "http://hl7.org/fhir/StructureDefinition/integer",
      "resource": {
        "resourceType": "StructureDefinition",
        "id": "integer",
        "url": "http://hl7.org/fhir/StructureDefinition/integer",
        "version": "4.0.1",
        "name": "integer",
        "status": "active",
        "fhirVersion": "4.0.1",
        "kind": "primitive-type",
        "type": "integer"
      }
    },
    {
      "fullUrl": "http://hl7.org/fhir/StructureDefinition/id",
      "resource": {
        "resourceType": "StructureDefinition",
        "id": "id",
        "url": "http://hl7.org/fhir/StructureDefinition/id",
        "version": "4.0.1",
        "name": "id",
        "status": "active",
        "fhirVersion": "4.0.1",
        "kind": "primitive-type",
        "type": "id"
      }
    },
    {
      "fullUrl": "http://hl7.org/fhir/StructureDefinition/string",
      "resource": {
        "resourceType": "StructureDefinition",
        "id": "string",
        "url": "http://hl7.org/fhir/StructureDefinition/string",
        "version": "4.0.1",
        "name": "string",
        "status": "active",
        "fhirVersion": "4.0.1",
        "kind": "primitive-type",
        "abstract": false,
        "type": "string"
      }
    },
    {
      "fullUrl": "http://hl7.org/fhir/StructureDefinition/code",
      "resource": {
        "resourceType": "StructureDefinition",
        "id": "code",
        "url": "http://hl7.org/fhir/StructureDefinition/code",
        "version": "4.0.1",
        "name": "code",
        "status": "active",
        "fhirVersion": "4.0.1",
        "kind": "primitive-type",
        "abstract": false,
        "type": "code",
        "baseDefinition": "http://hl7.org/fhir/StructureDefinition/string"
      }
    },
    {
      "fullUrl": "http://hl7.org/fhir/StructureDefinition/dateTime",
      "resource": {
        "resourceType": "StructureDefinition",
        "id": "dateTime",
        "url": "http://hl7.org/fhir/StructureDefinition/dateTime",
        "version": "4.0.1",
        "name": "dateTime",
        "status": "active",
        "fhirVersion": "4.0.1",
        "kind": "primitive-type",
        "abstract": false,
        "type": "dateTime",
        "baseDefinition": "http://hl7.org/fhir/StructureDefinition/Element"
      }
    },
    {
      "fullUrl": "http://hl7.org/fhir/StructureDefinition/uri",
      "resource": {
        "resourceType": "StructureDefinition",
        "id": "uri",
        "url": "http://hl7.org/fhir/StructureDefinition/uri",
        "version": "4.0.1",
        "name": "uri",
        "status": "active",
        "fhirVersion": "4.0.1",
        "kind": "primitive-type",
        "abstract": false,
        "type": "uri",
        "baseDefinition": "http://hl7.org/fhir/StructureDefinition/Element"
      }
    },
    {
      "fullUrl": "http://hl7.org/fhir/StructureDefinition/CodeableConcept",
      "resource": {
        "resourceType": "StructureDefinition",
        "id": "CodeableConcept",
        "url": "http://hl7.org/fhir/StructureDefinition/CodeableConcept",
        "version": "4.0.1",
        "name": "CodeableConcept",
        "contact": [
          {
            "telecom": [
              {
                "system": "url",
                "value": "http://hl7.org/fhir"
              }
            ]
          }
        ],
        "fhirVersion": "4.0.1",
        "kind": "complex-type",
        "abstract": false,
        "type": "CodeableConcept",
        "baseDefinition": "http://hl7.org/fhir/StructureDefinition/Element",
        "derivation": "specialization",
        "snapshot": {
          "element": [
            {
              "id": "CodeableConcept",
              "path": "CodeableConcept",
              "min": 0,
              "max": "*",
              "base": {
                "path": "CodeableConcept",
                "min": 0,
                "max": "*"
              },
              "isModifier": false
            },
            {
              "id": "CodeableConcept.id",
              "path": "CodeableConcept.id",
              "min": 0,
              "max": "1",
              "base": {
                "path": "Element.id",
                "min": 0,
                "max": "1"
              },
              "type": [
                {
                  "extension": [
                    {
                      "url": "http://hl7.org/fhir/StructureDefinition/structuredefinition-fhir-type",
                      "valueUrl": "string"
                    }
                  ],
                  "code": "http://hl7.org/fhirpath/System.String"
                }
              ],
              "isModifier": false
            },
            {
              "id": "CodeableConcept.extension",
              "path": "CodeableConcept.extension",
              "min": 0,
              "max": "*",
              "base": {
                "path": "Element.extension",
                "min": 0,
                "max": "*"
              },
              "type": [
                {
                  "code": "Extension"
                }
              ],
              "isModifier": false
            },
            {
              "id": "CodeableConcept.coding",
              "path": "CodeableConcept.coding",
              "min": 0,
              "max": "*",
              "base": {
                "path": "CodeableConcept.coding",
                "min": 0,
                "max": "*"
              },
              "type": [
                {
                  "code": "Coding"
                }
              ],
              "isModifier": false
            },
            {
              "id": "CodeableConcept.text",
              "path": "CodeableConcept.text",
              "min": 0,
              "max": "1",
              "base": {
                "path": "CodeableConcept.text",
                "min": 0,
                "max": "1"
              },
              "type": [
                {
                  "code": "string"
                }
              ],
              "isModifier": false,
              "isSummary": true
            }
          ]
        }
      }
    },
    {
      "fullUrl": "http://hl7.org/fhir/StructureDefinition/Element",
      "resource": {
        "resourceType": "StructureDefinition",
        "id": "Element",
        "url": "http://hl7.org/fhir/StructureDefinition/Element",
        "version": "4.0.1",
        "name": "BElement",
        "status": "active",
        "fhirVersion": "4.0.1",
        "kind": "complex-type",
        "abstract": true,
        "type": "Element",
        "baseDefinition": "http://hl7.org/fhir/StructureDefinition/Element",
        "snapshot": {
          "element": [
            {
              "id": "Element",
              "path": "Element",
              "min": 0,
              "max": "*",
              "base": {
                "path": "Element",
                "min": 0,
                "max": "*"
              },
              "isModifier": false
            },
            {
              "id": "Element.id",
              "path": "Element.id",
              "min": 0,
              "max": "1",
              "base": {
                "path": "Element.id",
                "min": 0,
                "max": "1"
              },
              "type": [
                {
                  "extension": [
                    {
                      "url": "http://hl7.org/fhir/StructureDefinition/structuredefinition-fhir-type",
                      "valueUrl": "string"
                    }
                  ],
                  "code": "http://hl7.org/fhirpath/System.String"
                }
              ],
              "isModifier": false,
              "isSummary": false
            },
            {
              "id": "Element.extension",
              "path": "Element.extension",
              "min": 0,
              "max": "*",
              "base": {
                "path": "Element.extension",
                "min": 0,
                "max": "*"
              },
              "type": [
                {
                  "code": "Extension"
                }
              ],
              "isModifier": false,
              "isSummary": false
            }
          ]
        }
      }
    },
    {
      "fullUrl": "http://hl7.org/fhir/StructureDefinition/Quantity",
      "resource": {
        "resourceType": "StructureDefinition",
        "id": "Quantity",
        "url": "http://hl7.org/fhir/StructureDefinition/Quantity",
        "version": "4.0.1",
        "name": "Quantity",
        "fhirVersion": "4.0.1",
        "kind": "complex-type",
        "type": "Quantity",
        "snapshot": {
          "element": []
        }
      }
    },
    {
      "fullUrl": "http://hl7.org/fhir/StructureDefinition/SampledData",
      "resource": {
        "resourceType": "StructureDefinition",
        "id": "SampledData",
        "url": "http://hl7.org/fhir/StructureDefinition/SampledData",
        "version": "4.0.1",
        "name": "SampledData",
        "fhirVersion": "4.0.1",
        "kind": "complex-type",
        "type": "SampledData",
        "snapshot": {
          "element": []
        }
      }
    },
    {
      "fullUrl" : "http://hl7.org/fhir/StructureDefinition/Extension",
      "resource" : {
        "resourceType" : "StructureDefinition",
        "id" : "Extension",
        "url" : "http://hl7.org/fhir/StructureDefinition/Extension",
        "version" : "4.0.1",
        "name" : "Extension",
        "fhirVersion" : "4.0.1",
        "kind" : "complex-type",
        "type" : "Extension",
        "snapshot" : {
          "element" : [{
            "id" : "Extension",
            "path" : "Extension",
            "min" : 0,
            "max" : "*",
            "base" : {
              "path" : "Extension",
              "min" : 0,
              "max" : "*"
            }
          },
          {
            "id" : "Extension.id",
            "path" : "Extension.id",
            "min" : 0,
            "max" : "1",
            "base" : {
              "path" : "Element.id",
              "min" : 0,
              "max" : "1"
            },
            "type" : [{
              "extension" : [{
                "url" : "http://hl7.org/fhir/StructureDefinition/structuredefinition-fhir-type",
                "valueUrl" : "string"
              }],
              "code" : "http://hl7.org/fhirpath/System.String"
            }]
          },
          {
            "id" : "Extension.extension",
            "path" : "Extension.extension",
            "min" : 0,
            "max" : "*",
            "base" : {
              "path" : "Element.extension",
              "min" : 0,
              "max" : "*"
            },
            "type" : [{
              "code" : "Extension"
            }]
          },
          {
            "id" : "Extension.url",
            "path" : "Extension.url",
            "min" : 1,
            "max" : "1",
            "base" : {
              "path" : "Extension.url",
              "min" : 1,
              "max" : "1"
            },
            "type" : [{
              "extension" : [{
                "url" : "http://hl7.org/fhir/StructureDefinition/structuredefinition-fhir-type",
                "valueUrl" : "uri"
              }],
              "code" : "http://hl7.org/fhirpath/System.String"
            }]
          },
          {
            "id" : "Extension.value[x]",
            "path" : "Extension.value[x]",
            "min" : 0,
            "max" : "1",
            "base" : {
              "path" : "Extension.value[x]",
              "min" : 0,
              "max" : "1"
            },
            "type" : [{
              "code" : "boolean"
            },
            {
              "code" : "code"
            },
            {
              "code" : "CodeableConcept"
            },
            {
              "code" : "Coding"
            }]
          }]
        }
      }
    },
    {
      "fullUrl" : "http://hl7.org/fhir/StructureDefinition/Coding",
      "resource" : {
        "resourceType" : "StructureDefinition",
        "id" : "Coding",
        "url" : "http://hl7.org/fhir/StructureDefinition/Coding",
        "version" : "4.0.1",
        "name" : "Coding",
        "fhirVersion" : "4.0.1",
        "kind" : "complex-type",
        "type" : "Coding",
        "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/Element",
        "snapshot" : {
          "element" : [
            {
            "id" : "Coding.system",
            "path" : "Coding.system",
            "min" : 0,
            "max" : "1",
            "base" : {
              "path" : "Coding.system",
              "min" : 0,
              "max" : "1"
            },
            "type" : [{
              "code" : "uri"
            }]
          },
          {
            "id" : "Coding.code",
            "path" : "Coding.code",
            "min" : 0,
            "max" : "1",
            "base" : {
              "path" : "Coding.code",
              "min" : 0,
              "max" : "1"
            },
            "type" : [{
              "code" : "code"
            }]
          }
        ]
        }
      }
    },
    {
      "fullUrl" : "http://hl7.org/fhir/StructureDefinition/Reference",
      "resource" : {
        "resourceType" : "StructureDefinition",
        "id" : "Reference",
        "url" : "http://hl7.org/fhir/StructureDefinition/Reference",
        "version" : "4.0.1",
        "name" : "Reference",
        "fhirVersion" : "4.0.1",
        "kind" : "complex-type",
        "type" : "Reference",
        "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/Element",
        "snapshot" : {
          "element" : [
            {
            "id" : "Reference.reference",
            "path" : "Reference.reference",
            "min" : 0,
            "max" : "1",
            "base" : {
              "path" : "Reference.reference",
              "min" : 0,
              "max" : "1"
            },
            "type" : [{
              "code" : "string"
            }]
          }
        ]
        }
      }
    },
    {
      "fullUrl": "http://hl7.org/fhir/StructureDefinition/OtherType",
      "resource": {
        "resourceType": "StructureDefinition",
        "id": "OtherType",
        "url": "http://hl7.org/fhir/StructureDefinition/OtherType",
        "version": "4.0.1",
        "name": "OtherType",
        "status": "active",
        "fhirVersion": "4.0.1",
        "kind": "complex-type",
        "abstract": false,
        "type": "OtherType",
        "baseDefinition": "http://hl7.org/fhir/StructureDefinition/Element",
        "snapshot": {
          "element": [
            {
              "id": "CodeableConcept.id",
              "path": "CodeableConcept.id",
              "representation": [
                "xmlAttr"
              ],
              "min": 0,
              "max": "1",
              "base": {
                "path": "Element.id",
                "min": 0,
                "max": "1"
              },
              "type": [
                {
                  "extension": [
                    {
                      "url": "http://hl7.org/fhir/StructureDefinition/structuredefinition-fhir-type",
                      "valueUrl": "string"
                    }
                  ],
                  "code": "http://hl7.org/fhirpath/System.String"
                }
              ],
              "isModifier": false,
              "isSummary": false
            }
          ]
        }
      }
    }
  ]
}
    """)

    IMMUNIZATION_RESOURCE_TYPE_INT = 1

    OBSERVATION_RESOURCE_TYPE_INT = 3

    PATIENT_RESOURCE_TYPE_INT = 9

    def test_fhir_spec_extractor_immunization_resource_produces_expected_resource_config(self):
        fhir_spec_extractor = FhirSpecExtractor(
            self.BUNDLE_WITH_IMMUNIZATION_AND_PATIENT_STRUCTURE_DEFINITION,
            {"Immunization"})
        # we expect each top level field to be present, and one ofs that are represented in the spec
        # as e.g. occurrence[x] should be expanded into the individual fields such as
        # occurrenceDateTime and occurrenceString. Fields with a cardinality of 0..* or 1..* should
        # have is_array = true set in their config.
        expected_required_fields = {"status", "vaccineCode", "exampleFieldToTestOneToMany"}
        expected_multi_type_config = fhirspec_pb2.MultiTypeFieldConfig(
            name="occurrence[x]",
            typed_field_names=["occurrenceDateTime", "occurrenceString"],
            is_required=True
        )
        expected_field_names_to_config = {
            "resourceType": fhirspec_pb2.FhirFieldConfig(
                is_array=False,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_STRING
            ),
            "id": fhirspec_pb2.FhirFieldConfig(
                is_array=False,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_ID
            ),
            "status": fhirspec_pb2.FhirFieldConfig(
                is_array=False,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_CODE
            ),
            "vaccineCode": fhirspec_pb2.FhirFieldConfig(
                is_array=False,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_CODEABLE_CONCEPT
            ),
            "exampleFieldToTestOneToMany": fhirspec_pb2.FhirFieldConfig(
                is_array=True,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_CODEABLE_CONCEPT
            ),
            "occurrenceDateTime": fhirspec_pb2.FhirFieldConfig(
                is_array=False,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_DATE_TIME
            ),
            "occurrenceString": fhirspec_pb2.FhirFieldConfig(
                is_array=False,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_STRING
            ),
            "performer": fhirspec_pb2.FhirFieldConfig(
                is_array=True,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_IMMUNIZATION_PERFORMER
            ),
            "contained": fhirspec_pb2.FhirFieldConfig(
                is_array=True,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_RESOURCE
            ),
        }

        generated_spec = fhir_spec_extractor.generate_r4_fhir_spec_proto_message(
            self.BUNDLE_WITH_TYPE_STRUCTURE_DEFINITIONS
        )

        # Check that exactly one Immunization config is present
        self.assertEqual(len(generated_spec.resource_type_to_config.keys()), 1)
        immunization_config = (
            generated_spec.resource_type_to_config[self.IMMUNIZATION_RESOURCE_TYPE_INT])
        # Check that the list of required fields is as expected
        self.assertEqual(set(immunization_config.required_fields), expected_required_fields)
        # Check that the list of multi type configs is as expected
        self.assertEqual(len(immunization_config.multi_type_fields), 1)
        received_multi_type_config = immunization_config.multi_type_fields[0]
        self.assertEqual(received_multi_type_config.name, expected_multi_type_config.name)
        self.assertEqual(received_multi_type_config.typed_field_names,
                         expected_multi_type_config.typed_field_names)
        self.assertEqual(received_multi_type_config.is_required,
                         expected_multi_type_config.is_required)
        # Check that the field names to config map is as expected
        self.assertEqual(set(expected_field_names_to_config.keys()),
                         set(immunization_config.allowed_field_names_to_config.keys()))
        for expected_field, expected_config in expected_field_names_to_config.items():
            self.assertEqual(
                immunization_config.allowed_field_names_to_config[expected_field],
                expected_config,
                "Mismatching config for field " + expected_field
            )

    def test_fhir_spec_extractor_immunization_and_patient_contains_two_entries(self):
        fhir_spec_extractor = FhirSpecExtractor(
            self.BUNDLE_WITH_IMMUNIZATION_AND_PATIENT_STRUCTURE_DEFINITION,
            {"Immunization", "Patient"})

        generated_spec = fhir_spec_extractor.generate_r4_fhir_spec_proto_message(
            self.BUNDLE_WITH_TYPE_STRUCTURE_DEFINITIONS
        )

        self.assertEqual(len(generated_spec.resource_type_to_config), 2)

    def test_fhir_spec_extractor_unsupported_resource_raises_exception(self):
        with self.assertRaises(ValueError):
            FhirSpecExtractor(
                self.BUNDLE_WITH_IMMUNIZATION_AND_PATIENT_STRUCTURE_DEFINITION,
                {"UnsupportedResource"})

    def test_fhir_spec_extractor_missing_resource_raises_exception(self):
        with self.assertRaises(ValueError):
            FhirSpecExtractor(
                self.BUNDLE_WITH_IMMUNIZATION_AND_PATIENT_STRUCTURE_DEFINITION,
                {"Observation"})

    def test_fhir_spec_extractor_observation_resource_produces_expected_resource_config(self):
        fhir_spec_extractor = FhirSpecExtractor(
            self.BUNDLE_WITH_OBSERVATION_STRUCTURE_DEFINITION,
            {"Observation"})
        # we expect each top level field to be present, and one ofs that are represented in the spec
        # as e.g. value[x] should be expanded into the individual fields such as
        # valueString and valueInteger. Fields with a cardinality of 0..* or 1..* should
        # have is_array = true set in their config.
        expected_required_fields = {"status", "code"}
        expected_multi_type_config = fhirspec_pb2.MultiTypeFieldConfig(
            name="value[x]",
            typed_field_names=[
                "valueQuantity",
                "valueCodeableConcept",
                "valueString",
                "valueBoolean",
                "valueInteger",
                "valueSampledData",
                "valueDateTime"],
            is_required=False
        )
        expected_field_names_to_config = {
            "resourceType": fhirspec_pb2.FhirFieldConfig(
                is_array=False,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_STRING
            ),
            "id": fhirspec_pb2.FhirFieldConfig(
                is_array=False,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_ID
            ),
            "status": fhirspec_pb2.FhirFieldConfig(
                is_array=False,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_CODE
            ),
            "code": fhirspec_pb2.FhirFieldConfig(
                is_array=False,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_CODEABLE_CONCEPT
            ),
            "valueQuantity": fhirspec_pb2.FhirFieldConfig(
                is_array=False,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_QUANTITY
            ),
            "valueCodeableConcept": fhirspec_pb2.FhirFieldConfig(
                is_array=False,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_CODEABLE_CONCEPT
            ),
            "valueString": fhirspec_pb2.FhirFieldConfig(
                is_array=False,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_STRING
            ),
            "valueBoolean": fhirspec_pb2.FhirFieldConfig(
                is_array=False,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_BOOLEAN
            ),
            "valueInteger": fhirspec_pb2.FhirFieldConfig(
                is_array=False,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_INTEGER
            ),
            "valueSampledData": fhirspec_pb2.FhirFieldConfig(
                is_array=False,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_SAMPLED_DATA
            ),
            "valueDateTime": fhirspec_pb2.FhirFieldConfig(
                is_array=False,
                r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_DATE_TIME
            ),
        }

        generated_spec = fhir_spec_extractor.generate_r4_fhir_spec_proto_message(
            self.BUNDLE_WITH_TYPE_STRUCTURE_DEFINITIONS
        )

        # Check that exactly one Observation config is present
        self.assertEqual(len(generated_spec.resource_type_to_config.keys()), 1)
        observation_config = (
            generated_spec.resource_type_to_config[self.OBSERVATION_RESOURCE_TYPE_INT])
        # Check that the list of required fields is as expected
        self.assertEqual(set(observation_config.required_fields), expected_required_fields)
        # Check that the list of multi type configs is as expected
        self.assertEqual(len(observation_config.multi_type_fields), 1)
        received_multi_type_config = observation_config.multi_type_fields[0]
        self.assertEqual(received_multi_type_config.name, expected_multi_type_config.name)
        self.assertEqual(received_multi_type_config.typed_field_names,
                         expected_multi_type_config.typed_field_names)
        self.assertEqual(received_multi_type_config.is_required,
                         expected_multi_type_config.is_required)
        # Check that the field names to config map is as expected
        self.assertEqual(set(expected_field_names_to_config.keys()),
                         set(observation_config.allowed_field_names_to_config.keys()))
        for expected_field, expected_config in expected_field_names_to_config.items():
            self.assertEqual(
                observation_config.allowed_field_names_to_config[expected_field],
                expected_config,
                "Mismatching config for field " + expected_field
            )

    def test_fhir_spec_extractor_immunization_produces_expected_type_configs(self):
        fhir_spec_extractor = FhirSpecExtractor(
            self.BUNDLE_WITH_IMMUNIZATION_AND_PATIENT_STRUCTURE_DEFINITION, {"Immunization"})
        expected_complex_type_configs = [
            fhirspec_pb2.FhirDataType(
                fhir_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_RESOURCE,
                kind=fhirspec_pb2.Kind.KIND_COMPLEX_TYPE,
            ),
            fhirspec_pb2.FhirDataType(
                fhir_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_CODE,
                kind=fhirspec_pb2.Kind.KIND_PRIMITIVE_TYPE,
            ),
            fhirspec_pb2.FhirDataType(
                fhir_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_CODEABLE_CONCEPT,
                kind=fhirspec_pb2.Kind.KIND_COMPLEX_TYPE,
                fhir_complex_type_config=fhirspec_pb2.FhirComplexTypeConfig(
                    allowed_field_names_to_config={
                        "id": fhirspec_pb2.FhirFieldConfig(
                            is_array=False,
                            r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_STRING
                        ),
                        "extension": fhirspec_pb2.FhirFieldConfig(
                            is_array=True,
                            r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_EXTENSION
                        ),
                        "coding": fhirspec_pb2.FhirFieldConfig(
                            is_array=True,
                            r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_CODING
                        ),
                        "text": fhirspec_pb2.FhirFieldConfig(
                            is_array=False,
                            r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_STRING
                        )
                    }),
            ),
            # Expected because this is added for primitive type extension validation
            fhirspec_pb2.FhirDataType(
                fhir_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_ELEMENT,
                kind=fhirspec_pb2.Kind.KIND_COMPLEX_TYPE,
                fhir_complex_type_config=fhirspec_pb2.FhirComplexTypeConfig(
                    allowed_field_names_to_config={
                        "id": fhirspec_pb2.FhirFieldConfig(
                            is_array=False,
                            r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_STRING
                        ),
                        "extension": fhirspec_pb2.FhirFieldConfig(
                            is_array=True,
                            r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_EXTENSION
                        ),
                    }),
            ),
            # Expected because Coding is a nested type used in CodeableConcept
            fhirspec_pb2.FhirDataType(
                fhir_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_CODING,
                kind=fhirspec_pb2.Kind.KIND_COMPLEX_TYPE,
                fhir_complex_type_config=fhirspec_pb2.FhirComplexTypeConfig(
                    allowed_field_names_to_config={
                        "system": fhirspec_pb2.FhirFieldConfig(
                            is_array=False,
                            r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_URI
                        ),
                        "code": fhirspec_pb2.FhirFieldConfig(
                            is_array=False,
                            r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_CODE
                        )
                    }),
            ),
            # Expected because Extension is a nested type used in CodeableConcept
            fhirspec_pb2.FhirDataType(
                fhir_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_EXTENSION,
                kind=fhirspec_pb2.Kind.KIND_COMPLEX_TYPE,
                fhir_complex_type_config=fhirspec_pb2.FhirComplexTypeConfig(
                    allowed_field_names_to_config={
                        "id": fhirspec_pb2.FhirFieldConfig(
                            is_array=False,
                            r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_STRING
                        ),
                        "extension": fhirspec_pb2.FhirFieldConfig(
                            is_array=True,
                            r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_EXTENSION
                        ),
                        "url": fhirspec_pb2.FhirFieldConfig(
                            is_array=False,
                            r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_URI
                        ),
                        "valueBoolean": fhirspec_pb2.FhirFieldConfig(
                            is_array=False,
                            r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_BOOLEAN
                        ),
                        "valueCode": fhirspec_pb2.FhirFieldConfig(
                            is_array=False,
                            r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_CODE
                        ),
                        "valueCodeableConcept": fhirspec_pb2.FhirFieldConfig(
                            is_array=False,
                            r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_CODEABLE_CONCEPT
                        ),
                        "valueCoding": fhirspec_pb2.FhirFieldConfig(
                            is_array=False,
                            r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_CODING
                        ),
                    },
                    multi_type_fields=[fhirspec_pb2.MultiTypeFieldConfig(
                        name="value[x]",
                        typed_field_names=[
                            "valueBoolean", "valueCode", "valueCodeableConcept", "valueCoding"],
                        is_required=False,
                    )],
                    required_fields=["url"]),
            ),
            fhirspec_pb2.FhirDataType(
                fhir_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_DATE_TIME,
                kind=fhirspec_pb2.Kind.KIND_PRIMITIVE_TYPE,
            ),
            fhirspec_pb2.FhirDataType(
                fhir_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_ID,
                kind=fhirspec_pb2.Kind.KIND_PRIMITIVE_TYPE,
            ),
            fhirspec_pb2.FhirDataType(
                fhir_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_STRING,
                kind=fhirspec_pb2.Kind.KIND_PRIMITIVE_TYPE,
            ),
            # Expected because it is used in the Extension definition
            fhirspec_pb2.FhirDataType(
                fhir_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_BOOLEAN,
                kind=fhirspec_pb2.Kind.KIND_PRIMITIVE_TYPE,
            ),
            # Expected because URI is the type of "Coding.system"
            fhirspec_pb2.FhirDataType(
                fhir_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_URI,
                kind=fhirspec_pb2.Kind.KIND_PRIMITIVE_TYPE,
            ),
            # Expected because this is the definition of the BackboneElement child type, defined in
            # the Immunization resource "performer" field.
            fhirspec_pb2.FhirDataType(
                fhir_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_IMMUNIZATION_PERFORMER,
                kind=fhirspec_pb2.Kind.KIND_COMPLEX_TYPE,
                fhir_complex_type_config=fhirspec_pb2.FhirComplexTypeConfig(
                    allowed_field_names_to_config={
                        "id": fhirspec_pb2.FhirFieldConfig(
                            is_array=False,
                            r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_STRING
                        ),
                        "extension": fhirspec_pb2.FhirFieldConfig(
                            is_array=True,
                            r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_EXTENSION
                        ),
                        "actor": fhirspec_pb2.FhirFieldConfig(
                            is_array=False,
                            r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_REFERENCE
                        ),
                    },
                    required_fields=["actor"]),
            ),
            # Expected because this is a type required by the IMMUNIZATION_PERFORMER child type.
            fhirspec_pb2.FhirDataType(
                fhir_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_REFERENCE,
                kind=fhirspec_pb2.Kind.KIND_COMPLEX_TYPE,
                fhir_complex_type_config=fhirspec_pb2.FhirComplexTypeConfig(
                    allowed_field_names_to_config={
                        "reference": fhirspec_pb2.FhirFieldConfig(
                            is_array=False,
                            r4_type=fhirspec_pb2.R4FhirType.R4_FHIR_TYPE_STRING
                        )
                    }),
            ),
        ]

        generated_spec = fhir_spec_extractor.generate_r4_fhir_spec_proto_message(
            self.BUNDLE_WITH_TYPE_STRUCTURE_DEFINITIONS)

        self.assertEqual(set(config.fhir_type for config in expected_complex_type_configs),
                         set(config.fhir_type for config in generated_spec.fhir_data_type_configs))
        self.assertEqual(len(expected_complex_type_configs),
                         len(generated_spec.fhir_data_type_configs))
        self.assertEqual(set(str(config) for config in expected_complex_type_configs),
                         set(str(config) for config in generated_spec.fhir_data_type_configs))

    def test_fhir_spec_extractor_missing_type_structure_definition_raises_exception(self):
        fhir_spec_extractor = FhirSpecExtractor(
            self.BUNDLE_WITH_IMMUNIZATION_AND_PATIENT_STRUCTURE_DEFINITION, {"Immunization"})
        type_definitions_bundle_without_any_definitions = json.loads("""
        {
            "resourceType": "Bundle",
            "id": "types",
            "meta": {
            "lastUpdated": "2019-11-01T09:29:23.356+11:00"
            },
            "type": "collection",
            "entry": []
        }
        """)

        with self.assertRaises(ValueError):
            fhir_spec_extractor.generate_r4_fhir_spec_proto_message(
                type_definitions_bundle_without_any_definitions)


if __name__ == '__main__':
    unittest.main()
