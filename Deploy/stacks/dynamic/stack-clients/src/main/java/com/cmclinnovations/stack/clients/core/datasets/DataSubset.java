package com.cmclinnovations.stack.clients.core.datasets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(Tabular.class), @Type(Vector.class), @Type(Raster.class) })
public abstract class DataSubset {

    private String name;
    private String subdirectory;

    @JsonProperty(defaultValue = "public")
    private String schema;
    private String table;

    @JsonProperty(defaultValue = "false")
    private boolean skip;

    public String getName() {
        return (null != name) ? name : table;
    }

    public String getSubdirectory() {
        return null != subdirectory ? subdirectory : "";
    }

    public String getSchema() {
        return schema;
    }

    public String getTable() {
        return (null != table) ? table : name;
    }

    public boolean getSkip() {
        return skip;
    }

    public abstract void loadData(String datasetDir, String database);

}
