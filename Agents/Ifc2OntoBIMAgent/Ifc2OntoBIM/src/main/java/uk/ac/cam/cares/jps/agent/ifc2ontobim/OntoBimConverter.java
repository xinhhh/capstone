package uk.ac.cam.cares.jps.agent.ifc2ontobim;

import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifcparser.facade.BuildingStructureFacade;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifcparser.facade.ElementFacade;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifcparser.facade.ModellingOperatorFacade;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifcparser.facade.SpatialZoneFacade;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifcparser.storage.ElementStorage;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifcparser.storage.ModellingOperatorStorage;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifcparser.storage.SpatialZoneStorage;

import java.nio.file.Path;
import java.util.*;


/**
 * A converter class that reads the converted IfcOwl triples, and convert them into the OntoBIM instances as a TTL file.
 *
 * @author qhouyee
 */
public class OntoBimConverter {
    private Model owlModel;
    private static final Logger LOGGER = LogManager.getLogger(Ifc2OntoBIMAgent.class);

    /**
     * Standard Constructor
     */
    public OntoBimConverter() {
    }

    /**
     * Read the ttl file output for IfcOwl and instantiate them as OntoBIM instances.
     */
    public LinkedHashSet<Statement> convertOntoBIM(String ttlFile) {
        // Load the existing IfcOwl triples into a model
        Path ttlFilePath = Path.of(ttlFile);
        this.owlModel = RDFParser.create()
                .source(ttlFilePath)
                .toModel();
        // Reset the values from previous iterations
        SpatialZoneStorage.resetSingleton();
        ElementStorage.resetSingleton();
        ModellingOperatorStorage.resetSingleton();
        // Create a new Set to ensure statements are kept in one object and not duplicated
        LinkedHashSet<Statement> statementSet = new LinkedHashSet<>();
        genZoneAndElementStatements(statementSet);
        genGeometryContentStatements(statementSet);
        return statementSet;
    }


    /**
     * Generate the OntoBIM triples for spatial zones and elements.
     *
     * @param statementSet Stores the relevant queried statements into this set.
     */
    private void genZoneAndElementStatements(LinkedHashSet<Statement> statementSet) {
        LOGGER.info("Retrieving and generating spatial zones statements...");
        SpatialZoneFacade.genZoneTriples(this.owlModel, statementSet);
        // Create a new helper object
        BuildingStructureFacade buildingStructureHelper = new BuildingStructureFacade();
        LOGGER.info("Retrieving and generating statements related to ceiling elements...");
        buildingStructureHelper.addCeilingStatements(this.owlModel, statementSet);
        LOGGER.info("Retrieving and generating statements related to column elements...");
        buildingStructureHelper.addColumnStatements(this.owlModel, statementSet);
        LOGGER.info("Retrieving and generating statements related to floor elements...");
        buildingStructureHelper.addFloorStatements(this.owlModel, statementSet);
        LOGGER.info("Retrieving and generating statements related to roof elements...");
        buildingStructureHelper.addRoofStatements(this.owlModel, statementSet);
        LOGGER.info("Retrieving and generating statements related to wall elements...");
        buildingStructureHelper.addWallStatements(this.owlModel, statementSet);
        LOGGER.info("Retrieving and generating statements related to door elements...");
        buildingStructureHelper.addDoorStatements(this.owlModel, statementSet);
        LOGGER.info("Retrieving and generating statements related to window elements...");
        buildingStructureHelper.addWindowStatements(this.owlModel, statementSet);
        LOGGER.info("Retrieving and generating statements related to stair elements...");
        buildingStructureHelper.addStairStatements(this.owlModel, statementSet);
        LOGGER.info("Retrieving and generating statements related to all remaining elements...");
        ElementFacade.addElementStatements(this.owlModel, statementSet);
    }

    /**
     * This method performs two functions. It first retrieves the common geometry elements' IRIs in the statement list
     * containing spatial zones and element information. Second, it queries the IRI and their related content from
     * the IfcOwl model and add the subgraph constructed as a query result into the statement set.
     * Note that the statements will be written to a temporary file to prevent heap overflow.
     * <p>
     * Directly querying the common geometry contents from the IfcOwl model is problematic as it is less efficient.
     * In the Ifc schema, each instance of the same IfcClass and Family Type is linked to the same geometry resource.
     * Repeating the query will add duplicate results. Moreover, as IFC is verbose, the extension to the
     * SPARQL query syntax for including these common geometries will slow down the SPARQL query process.
     *
     * @param statementSet Stores the relevant queried statements into this set.
     */
    private void genGeometryContentStatements(LinkedHashSet<Statement> statementSet) {
        ModellingOperatorStorage operatorMappings = ModellingOperatorStorage.Singleton();
        LOGGER.info("Retrieving and generating statements related to local placement, direction, and cartesian points...");
        ModellingOperatorFacade.retrieveOperatorInstances(this.owlModel);
        operatorMappings.constructAllStatements(statementSet);
    }
}
