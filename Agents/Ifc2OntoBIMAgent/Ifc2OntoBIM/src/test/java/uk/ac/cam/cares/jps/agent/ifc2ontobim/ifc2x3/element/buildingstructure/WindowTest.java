package uk.ac.cam.cares.jps.agent.ifc2ontobim.ifc2x3.element.buildingstructure;

import org.apache.jena.rdf.model.Statement;
import org.junit.jupiter.api.Test;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.JunitTestUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WindowTest {
    private static final String testBaseUri1 = "http://www.example.org/";
    private static final String testIri1 = testBaseUri1 + "IfcWindow_332";
    private static final String testName1 = "Curtain Window Panel";
    private static final String testUID1 = "01294ha";
    private static final String testPlacementIri1 = testBaseUri1 + "LocalPlacement_515";
    private static final String testBaseUri2 = "http://www.example.org/test#";
    private static final String testIri2 = testBaseUri2 + "IfcWindow_322";
    private static final String testName2 = "Glass Windows";
    private static final String testUID2 = "9sdg781";
    private static final String testPlacementIri2 = testBaseUri2 + "LocalPlacement_8172";
    private static final String testHostZoneIRI = testBaseUri1 + "Storey_715";
    private static final String testAssemblyIRI = testBaseUri1 + "Wall_6135";
    private static final String testGeomModelIRI = testBaseUri1 + "ModelRepresentation3D_715";
    private static final String testClassName = "IfcModelRepresentation";
    private static final String testClass = JunitTestUtils.bimUri + testClassName;

    @Test
    void testConstructor() {
        Window sample = new Window(testIri1, testName1, testUID1, testPlacementIri1, testHostZoneIRI, testAssemblyIRI, testGeomModelIRI);
        // Test that the sample fields are correct
        assertEquals(testBaseUri1, sample.getPrefix());
        assertNotEquals(testIri1, sample.getIfcRepIri());
        assertTrue(sample.getIfcRepIri().contains(testBaseUri1 + testClassName + "_"));
        assertEquals(testName1, sample.getName());
        assertEquals(testUID1, sample.getUid());
        assertEquals(testPlacementIri1, sample.getPlacementIri());

        Window sample2 = new Window(testIri2, testName2, testUID2, testPlacementIri2, testHostZoneIRI, testAssemblyIRI, testGeomModelIRI);
        // Test that the sample fields are correct
        assertEquals(testBaseUri2, sample2.getPrefix());
        assertNotEquals(testIri2, sample2.getIfcRepIri());
        assertTrue(sample2.getIfcRepIri().contains(testBaseUri2 + testClassName + "_"));
        assertEquals(testName2, sample2.getName());
        assertEquals(testUID2, sample2.getUid());
        assertEquals(testPlacementIri2, sample2.getPlacementIri());
    }

    @Test
    void testConstructStatements() {
        // Set up
        LinkedHashSet<Statement> sampleSet = new LinkedHashSet<>();
        Window sample = new Window(testIri1, testName1, testUID1, testPlacementIri1, testHostZoneIRI, testAssemblyIRI, testGeomModelIRI);
        // Execute method
        sample.constructStatements(sampleSet);
        // Clean up results as one string
        String result = JunitTestUtils.appendStatementsAsString(sampleSet);
        // Generated expected statement lists and verify their existence
        JunitTestUtils.doesExpectedListExist(genExpectedCommonStatements(), result);
    }

    private List<String> genExpectedCommonStatements() {
        List<String> expected = new ArrayList<>();
        expected.add(testHostZoneIRI + ", https://w3id.org/bot#containsElement, " + testBaseUri1 + "Window_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
        expected.add(testBaseUri1 + "Window_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.theworldavatar.com/kg/ontobuildingstructure/Window");
        expected.add(testBaseUri1 + "Window_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.theworldavatar.com/kg/ontobim/hasIfcRepresentation, " + testBaseUri1 + "IfcModelRepresentation_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
        expected.add(testBaseUri1 + "IfcModelRepresentation_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, " + testClass);
        expected.add(testBaseUri1 + "IfcModelRepresentation_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.w3.org/2000/01/rdf-schema#label, \"" + testName1);
        expected.add(testBaseUri1 + "IfcModelRepresentation_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.theworldavatar.com/kg/ontobim/hasIfcId, \"" + testUID1);
        expected.add(testBaseUri1 + "IfcModelRepresentation_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.theworldavatar.com/kg/ontobim/hasLocalPosition, " + testPlacementIri1);
        expected.add(testPlacementIri1 + ", http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.theworldavatar.com/kg/ontobim/LocalPlacement");
        expected.add(testBaseUri1 + "IfcModelRepresentation_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}, http://www.theworldavatar.com/kg/ontobim/hasGeometricRepresentation, " + testGeomModelIRI);
        expected.add(testAssemblyIRI + ", http://www.theworldavatar.com/kg/ontobuildingstructure/consistsOf, " + testBaseUri1 + "Window_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");
        return expected;
    }
}