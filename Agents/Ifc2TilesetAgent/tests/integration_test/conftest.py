"""
# Author: qhouyee #

A module that provides all pytest fixtures and utility functions for all integration tests.
"""
# Standard import
import os
import json

# Third party import
import pytest
import ifcopenshell
from ifcopenshell.api import run
import yaml

# Self import
from agent import create_app
from agent.kgutils import KGClient
from . import testconsts


# ----------------------------------------------------------------------------------
# Session-scoped test fixtures
# (i.e. the fixture is destroyed at the end of the test session)
# ----------------------------------------------------------------------------------

@pytest.fixture(scope="session")
def gen_sample_ifc_file():
    """
    A test function to generate two different sample IFC file based on requirements

    Argument:
    ifc_path - File path to the IFC file
    is_complex - A boolean whether to create a complex IFC model with multiple element,\
        or a simple model consisting of only one Wall
    """
    def _gen_ifc_file(ifc_path, is_complex=False):
        # Create a blank model
        model = ifcopenshell.file()
        # All projects must have one IFC Project element
        project = run("root.create_entity", model,
                    ifc_class="IfcProject", name="My Project")
        # To generate geometry, must assign units, defaults to metric units without args
        run("unit.assign_unit", model)
        # Create a geometry modelling context for storing 3D geometries
        context = run("context.add_context", model, context_type="Model")
        body = run(
            "context.add_context", model,
            context_type="Model", context_identifier="Body", target_view="MODEL_VIEW", parent=context
        )
        # Create a site, building, and storey
        site = run("root.create_entity", model,
                ifc_class="IfcSite", name="My Site")
        building = run("root.create_entity", model,
                    ifc_class="IfcBuilding", name="Building A")
        storey = run("root.create_entity", model,
                    ifc_class="IfcBuildingStorey", name="Ground Floor")
        # Assign their relations
        run("aggregate.assign_object", model, relating_object=project, product=site)
        run("aggregate.assign_object", model, relating_object=site, product=building)
        run("aggregate.assign_object", model,
            relating_object=building, product=storey)
        # Create a wall
        wall = run("root.create_entity", model, ifc_class="IfcWall")
        # Add body geometry in meters
        representation = run("geometry.add_wall_representation",
                            model, context=body, length=5, height=3, thickness=0.2)
        # Assign body geometry to the wall
        run("geometry.assign_representation", model,
            product=wall, representation=representation)
        # Place the wall on ground floor
        run("spatial.assign_container", model,
            relating_structure=storey, product=wall)

        if is_complex:
            # Create a building element proxy for water meter and solar panel
            meter = model.create_entity("IfcBuildingElementProxy",
                                GlobalId=testconsts.meterid, Name="Water Meter")
            solarpanel = model.create_entity("IfcBuildingElementProxy",
                                GlobalId=testconsts.panelid, Name="Solar Panel")
            # Create a random furnishing element
            wirebox = model.create_entity("IfcFurnishingElement",
                                GlobalId=testconsts.boxid, Name="Electric Wire Box")
            # Assign geometries to each element
            run("geometry.assign_representation", model,
                    product=meter, representation=representation)
            run("geometry.assign_representation", model,
                    product=solarpanel, representation=representation)
            run("geometry.assign_representation", model,
                    product=wirebox, representation=representation)
        # Write out to a file
        model.write(ifc_path)
        return ifc_path
    return _gen_ifc_file

@pytest.fixture(scope="session")
def assert_asset_geometries():
    """
    A test function to assert and remove the expected generated geometries files

    Argument:
    asset_list - A list containing the expected asset names
    """
    def _setup_geom_assertions(asset_list):
        for asset in asset_list:
            glbpath = "./data/glb/" + asset + ".glb"
            gltfpath = "./data/gltf/" + asset + ".gltf"
            try: 
                assert os.path.isfile(glbpath)
                assert os.path.isfile(gltfpath)
            finally:
                os.remove(glbpath)
                os.remove(gltfpath)
        return None
    return _setup_geom_assertions

# ----------------------------------------------------------------------------------
# Module-scoped test fixtures
# (i.e. the fixture is destroyed during teardown of the last test in the module)
# ----------------------------------------------------------------------------------

@pytest.fixture(scope='module')
def tileset_content():
    """
    A test function to read the contents of a tileset.json

    Argument:
    json_filepath - File path to the tileset.json
    Returns:
    The tileset's contents as a Python dictionary
    """
    def _retrieve_tileset_contents(json_filepath):
        # Read the results
        json_output = open(json_filepath, "r", encoding="utf-8")
        contents = json_output.read()  # Store as string
        tileset_content = json.loads(contents)  # Convert to dictionary
        json_output.close()
        return tileset_content
    return _retrieve_tileset_contents


# ----------------------------------------------------------------------------------
# Function-scoped test fixtures
# (i.e. the fixture is destroyed at the end of each test)
# -----
@pytest.fixture(scope="function")
def initialise_client():
    """
    Retrieves all the exposed endpoints for dockerised testing services
    """
    # Create KG Client for testing
    kg_client = KGClient(testconsts.KG_ENDPOINT, testconsts.KG_ENDPOINT)
    # Returns client for the test to continue running
    yield kg_client

    # Clean up operations at the end of the test
    clear_triplestore(kg_client)
    clear_loggers()

@pytest.fixture(scope='function')
def flaskapp():
    app = create_app()
    app.config.update({
        "TESTING": True,
    })
    yield app.test_client()

@pytest.fixture(scope='function')
def sample_properties():
    """
    Generates a sample yaml file with required properties for retrieval
    """
    yaml_path = "./config/properties.yaml"
    data = dict(
        root_tile = testconsts.ROOT_TILE,
        child_tile = testconsts.CHILD_TILE,
        query_endpoint = testconsts.KG_ENDPOINT,
        update_endpoint = testconsts.KG_ENDPOINT
    )
    # Generate the file
    with open(yaml_path, 'w') as outfile:
        yaml.dump(data, outfile)
    return yaml_path

# ----------------------------------------------------------------------------------
# Helper functions
# ----------------------------------------------------------------------------------

def clear_loggers():
    """Remove handlers from all loggers. Adopted from https://github.com/pytest-dev/pytest/issues/5502#issuecomment-647157873"""
    import logging
    loggers = [logging.getLogger()] + list(logging.Logger.manager.loggerDict.values())
    for logger in loggers:
        handlers = getattr(logger, 'handlers', [])
        for handler in handlers:
            logger.removeHandler(handler)

def clear_triplestore(kgClient):
    """Delete all triples"""
    query_delete = """
        DELETE WHERE {?s ?p ?o}
        """
    kgClient.execute_update(query_delete)

def create_ifcaxis2placement(ifcfile, point, dir1, dir2):
    """Creates an IfcAxis2Placement3D from Location, Axis and RefDirection specified as Python tuples"""
    point = ifcfile.createIfcCartesianPoint(point)
    dir1 = ifcfile.createIfcDirection(dir1)
    dir2 = ifcfile.createIfcDirection(dir2)
    axis2placement = ifcfile.createIfcAxis2Placement3D(point, dir1, dir2)
    return axis2placement

def create_ifclocalplacement(ifcfile, point, dir1, dir2, relative_to=None):
    """
    Creates an IfcLocalPlacement from Location, Axis and RefDirection, 
    specified as Python tuples, and relative placement
    """
    axis2placement = create_ifcaxis2placement(ifcfile,point,dir1,dir2)
    ifclocalplacement2 = ifcfile.createIfcLocalPlacement(relative_to,axis2placement)
    return ifclocalplacement2

def create_ifcpolyline(ifcfile, point_list):
    """Creates an IfcPolyLine from a list of points, specified as Python tuples"""
    ifcpts = []
    for point in point_list:
        point = ifcfile.createIfcCartesianPoint(point)
        ifcpts.append(point)
    polyline = ifcfile.createIfcPolyLine(ifcpts)
    return polyline

def create_ifcextrudedareasolid(ifcfile, point_list, ifcaxis2placement, extrude_dir, extrusion):
    """Creates an IfcExtrudedAreaSolid from a list of points, specified as Python tuples"""
    polyline = create_ifcpolyline(ifcfile, point_list)
    ifcclosedprofile = ifcfile.createIfcArbitraryClosedProfileDef("AREA", None, polyline)
    ifcdir = ifcfile.createIfcDirection(extrude_dir)
    ifcextrudedareasolid = ifcfile.createIfcExtrudedAreaSolid(ifcclosedprofile, ifcaxis2placement, ifcdir, extrusion)
    return ifcextrudedareasolid

