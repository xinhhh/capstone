"""
# Author: qhouyee #

A test suite for the agent.ifc2tileset submodule.
"""

# Standard import
import os

# Third party import
import numpy as np
import pandas as pd
import trimesh

# Self import
from agent.ifc2tileset import gen_tilesets
from . import testconsts as C
from .testutils import read_json, gen_sample_asset_df, gen_sample_asset_contents, z_up_to_y_up


def test_gen_tilesets_solarpanel():
    """
    Tests gen_tilesets() for generating only solarpanel tileset
    """
    # Create sample glb file
    solarpanel_glb = os.path.join("data", "glb", "solarpanel.glb")
    m = C.sample_box_gen()
    m.export(solarpanel_glb)

    # JSON output path
    bim_json_filepath = os.path.join("data", "tileset_bim.json")
    solar_json_filepath = os.path.join("data", "tileset_solarpanel.json")
    sewage_json_filepath = os.path.join("data", "tileset_sewage.json")

    # Execute method
    gen_tilesets(pd.DataFrame(), "building_iri")

    # Test that only relevant tileset.json are created
    assert os.path.exists(solar_json_filepath)
    assert not os.path.exists(bim_json_filepath)
    assert not os.path.exists(sewage_json_filepath)

    # Read the tileset
    solar_tileset = read_json(solar_json_filepath)
    # Test that the tileset contents are equivalent to the dictionary
    assert solar_tileset["root"]["content"] == {"uri": "./glb/solarpanel.glb"}
    # Test that the bbox is correctly computed
    assert np.allclose(solar_tileset["root"]["boundingVolume"]["box"], C.sample_box_bbox)


def test_gen_tilesets_sewage():
    """
    Tests gen_tilesets() for generating only sewage tileset
    """
    # Create sample glb file
    sewage_glb = os.path.join("data", "glb", "sewagenetwork.glb")
    m = C.sample_cone_gen()
    m.export(sewage_glb)

    # JSON output path
    bim_json_filepath = os.path.join("data", "tileset_bim.json")
    solar_json_filepath = os.path.join("data", "tileset_solarpanel.json")
    sewage_json_filepath = os.path.join("data", "tileset_sewage.json")

    # Execute method
    gen_tilesets(pd.DataFrame(), "building_iri")

    # Test that only relevant tileset.json are created
    assert os.path.exists(sewage_json_filepath)
    assert not os.path.exists(bim_json_filepath)
    assert not os.path.exists(solar_json_filepath)

    # Read the tileset
    sewage_tileset = read_json(sewage_json_filepath)
    # Test that the tileset contents are equivalent to the dictionary
    assert sewage_tileset["root"]["content"] == {"uri": "./glb/sewagenetwork.glb"}
    # Test that the bbox is correctly computed
    assert np.allclose(sewage_tileset["root"]["boundingVolume"]["box"], C.sample_cone_bbox)


def assert_tileset_metadata(tileset_content: dict):
    assert "schema" in tileset_content
    assert "classes" in tileset_content["schema"]
    assert "TilesetMetaData" in tileset_content["schema"]["classes"]
    assert tileset_content["schema"]["classes"]["TilesetMetaData"] == {
        "name": "Tileset metadata",
        "description": "A metadata class for the tileset",
        "properties": {
            "buildingIri": {
                "description": "Data IRI of the building",
                "type": "STRING"
            }
        }
    }

    assert "metadata" in tileset_content
    assert tileset_content["metadata"] == {
        "class": "TilesetMetaData",
        "properties": {
            "buildingIri": "building_iri"
        }
    }


def test_gen_tilesets_building():
    """
    Tests gen_tilesets() for generating only the bim tileset without asset data
    """
    # Create sample glb file
    building_glb = os.path.join("data", "glb", "building.glb")
    m = C.sample_box_gen()
    m.export(building_glb)

    # JSON output path
    bim_json_filepath = os.path.join("data", "tileset_bim.json")
    solar_json_filepath = os.path.join("data", "tileset_solarpanel.json")
    sewage_json_filepath = os.path.join("data", "tileset_sewage.json")

    # Execute method
    gen_tilesets(pd.DataFrame(), "building_iri")

    # Test that only relevant tileset.json are created
    assert os.path.exists(bim_json_filepath)
    assert not os.path.exists(sewage_json_filepath)
    assert not os.path.exists(solar_json_filepath)

    # Read the tileset
    tileset_content = read_json(bim_json_filepath)
    # Test that the tileset contents are equivalent to the dictionary
    assert tileset_content["root"]["content"] == {"uri": "./glb/building.glb"}
    assert np.allclose(tileset_content["root"]["boundingVolume"]["box"], C.sample_box_bbox)
    assert "children" not in tileset_content["root"]
    assert_tileset_metadata(tileset_content)


# TODO: add more scenarios (assets > 6) and assertions (bbox)
def test_gen_tilesets_asset():
    """
    Tests gen_tilesets() for generating the bim tileset with only asset data
    """
    # Initialise test case
    test_range = 3

    sample_asset_df = gen_sample_asset_df(test_range)

    glb_files = [os.path.join("data", "glb", f"asset{i}.glb") for i in range(test_range)]
    for i, file in enumerate(glb_files):
        z_up_coords = -10, -10, i, 10, 10, i + 1
        y_up_coords = z_up_to_y_up(*z_up_coords)
        m = trimesh.creation.box(bounds=[y_up_coords[:3], y_up_coords[3:]])
        m.export(file)

    # JSON output path
    bim_json_filepath = os.path.join("data", "tileset_bim.json")

    expected_child_node = {
        "boundingVolume": {"box": [0, 0, 1.5, 10, 0, 0, 0, 10, 0, 0, 0, 1.5]},
        "geometricError": 50,
        "contents": gen_sample_asset_contents(test_range)
    }

    # Execute method
    gen_tilesets(sample_asset_df, "building_iri")

    # Test that only relevant tileset.json are created
    assert os.path.exists(bim_json_filepath)

    # Read the tileset
    tileset_content = read_json(bim_json_filepath)

    # Test for metadata fields
    assert_tileset_metadata(tileset_content)

    # Test root tile
    root_tile = tileset_content["root"]
    assert "content" not in root_tile
    assert "contents" not in root_tile

    assert "children" in root_tile
    assert len(root_tile["children"]) == 1
    assert root_tile["children"][0] == expected_child_node
