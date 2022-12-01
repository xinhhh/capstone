"""
# Author: qhouyee #

This is the submodule's entry function to
generate the tilesets for the IFC file and its glTF models.
"""

# Self imports
from ifc2tileset.tile_helper import gen_solarpanel_tileset, jsonwriter
from ifc2tileset.root_tile import gen_root_content
from ifc2tileset.asset_tiles import gen_tileset_assets

def gen_tilesets(hashmapping):
    """
    Generates the tileset in json format

    Argument:
        hashmappings - A hashtable to match assets to their IFC ID
    """
    # Create and write a separate tileset for solar panels
    gen_solarpanel_tileset()

    # If there are assets, generate tilesets with asset information
    if hashmapping:
        jsonwriter(gen_tileset_assets(hashmapping), "tileset_bim")
    # Else, generate only the root building contents
    else:
        jsonwriter(gen_root_content(), "tileset_bim")
