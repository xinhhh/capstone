package com.cmclinnovations.stack.clients.core.datasets;

import java.nio.file.Path;

import com.cmclinnovations.stack.clients.gdal.GDALClient;
import com.cmclinnovations.stack.clients.gdal.GDALTranslateOptions;
import com.cmclinnovations.stack.clients.geoserver.GeoServerClient;
import com.cmclinnovations.stack.clients.geoserver.GeoServerRasterSettings;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Raster extends GeoServerDataSubset {

    @JsonProperty
    private GDALTranslateOptions gdalTranslateOptions = new GDALTranslateOptions();

    @JsonProperty
    private GeoServerRasterSettings geoServerSettings = new GeoServerRasterSettings();

    @Override
    public void loadData(Path dirPath, String database) {
        GDALClient.getInstance()
                .uploadRasterFilesToPostGIS(database, getTable(), dirPath.toString(), gdalTranslateOptions, false);
    }

    @Override
    public void createLayer(String workspaceName, String database) {
        GeoServerClient.getInstance()
                .createGeoTiffLayer(workspaceName, getName(), "geoserver_raster_indices", "public", geoServerSettings);
    }

}
