package uk.ac.cam.cares.jps.agent.sensorloggermobileappagent;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.json.JSONArray;
import uk.ac.cam.cares.jps.base.query.RemoteRDBStoreClient;
import uk.ac.cam.cares.jps.base.query.RemoteStoreClient;

public class QueryClientSPARQL {

    private RemoteStoreClient storeClient;
    // Prefixes
    static final String SLMA = "https://www.theworldavatar.com/kg/sensorloggerapp/";
    static final String OM = "http://www.ontology-of-units-of-measure.org/resource/om-2/";
    static final String SF ="http://www.opengis.net/ont/sf#";
    static final String GEO = "http://www.opengis.net/ont/geosparql#";
    static final String XSD = "https://www.w3.org/2001/XMLSchema#";
    static final String ONTODEVICE = "https://www.theworldavatar.com/kg/ontodevice/";
    static final String MON = "https://w3id.org/MON/person.owl";
    static final String SAREF="https://saref.etsi.org/core/";
    static final String RDF="http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    final static String str_s = "s";
    final static Var VAR_S = Var.alloc(str_s);
    final static String str_o = "o";
    final static Var VAR_O = Var.alloc(str_o);

    public QueryClientSPARQL(RemoteStoreClient storeClient) {
        this.storeClient = storeClient;
    }

    String getIRIfromJSONarray(JSONArray jsonArray){
        if(jsonArray.length()!=1){System.out.println("There is more than 1 data in this JSONArray, only the first one is returned");}
        return jsonArray.getJSONObject(0).get(str_o).toString();
    }


    JSONArray getPersonIRI(String smartphoneIRI)  {

        WhereBuilder wb = new WhereBuilder()
                .addPrefix("slma",SLMA)
                .addWhere(VAR_S, "slma:hasA", smartphoneIRI);

        SelectBuilder sb = new SelectBuilder()
                .addVar(VAR_S).addWhere(wb);

        JSONArray queryResult=storeClient.executeQuery(sb.buildString());
        return queryResult;
    }

    JSONArray getAccel_xIRIArray(Node smartphoneIRI)  {

        WhereBuilder wb = new WhereBuilder()
                .addPrefix("slma",SLMA)
                .addPrefix ("saref",SAREF)
                .addPrefix("ontodevice", ONTODEVICE)
                .addPrefix("rdf", RDF)
                .addPrefix("om",OM)
                .addWhere(smartphoneIRI,"saref:consistsOf","?accelerometer")
                .addWhere("?accelerometer","rdf:type","ontodevice:Accelerometer")
                .addWhere("?accelerometer","ontodevice:measures","?slma_accel_x")
                .addWhere("?slma_accel_x","rdf:type","slma:Accel_x")
                .addWhere("?slma_accel_x", "om:hasValue", VAR_O);

        SelectBuilder sb = new SelectBuilder()
                .addVar(VAR_O).addWhere(wb);

        JSONArray queryResult=storeClient.executeQuery(sb.buildString());
        return queryResult;
    }
    JSONArray getAccel_yIRIArray(Node smartphoneIRI)  {

        WhereBuilder wb = new WhereBuilder()
                .addPrefix("slma",SLMA)
                .addPrefix ("saref",SAREF)
                .addPrefix("ontodevice", ONTODEVICE)
                .addPrefix("rdf", RDF)
                .addPrefix("om",OM)
                .addWhere(smartphoneIRI,"saref:consistsOf","?accelerometer")
                .addWhere("?accelerometer","rdf:type","ontodevice:Accelerometer")
                .addWhere("?accelerometer","ontodevice:measures","?slma_accel_y")
                .addWhere("?slma_accel_y","rdf:type","slma:Accel_y")
                .addWhere("?slma_accel_y", "om:hasValue", VAR_O);

        SelectBuilder sb = new SelectBuilder()
                .addVar(VAR_O).addWhere(wb);

        JSONArray queryResult=storeClient.executeQuery(sb.buildString());
        return queryResult;
    }
    JSONArray getAccel_zIRIArray(Node smartphoneIRI)  {
        WhereBuilder wb = new WhereBuilder()
                .addPrefix("slma",SLMA)
                .addPrefix ("saref",SAREF)
                .addPrefix("ontodevice", ONTODEVICE)
                .addPrefix("rdf", RDF)
                .addPrefix("om",OM)
                .addWhere(smartphoneIRI,"saref:consistsOf","?accelerometer")
                .addWhere("?accelerometer","rdf:type","ontodevice:Accelerometer")
                .addWhere("?accelerometer","ontodevice:measures","?slma_accel_z")
                .addWhere("?slma_accel_z","rdf:type","slma:Accel_z")
                .addWhere("?slma_accel_z", "om:hasValue", VAR_O);

        SelectBuilder sb = new SelectBuilder()
                .addVar(VAR_O).addWhere(wb);

        JSONArray queryResult=storeClient.executeQuery(sb.buildString());
        return queryResult;
    }


    JSONArray getGravity_xIRIArray(Node smartphoneIRI)  {


        WhereBuilder wb = new WhereBuilder()
                .addPrefix("slma",SLMA)
                .addPrefix ("saref",SAREF)
                .addPrefix("ontodevice", ONTODEVICE)
                .addPrefix("rdf", RDF)
                .addPrefix("om",OM)
                .addWhere(smartphoneIRI,"saref:consistsOf","?gravitySensor")
                .addWhere("?gravitySensor","rdf:type","ontodevice:GravitySensor")
                .addWhere("?gravitySensor","ontodevice:measures","?slma_gravity_x")
                .addWhere("?slma_gravity_x","rdf:type","slma:Gravity_x")
                .addWhere("?slma_gravity_x", "om:hasValue", VAR_O);

        SelectBuilder sb = new SelectBuilder()
                .addVar(VAR_O).addWhere(wb);

        JSONArray queryResult=storeClient.executeQuery(sb.buildString());
        return queryResult;
    }
    JSONArray getGravity_yIRIArray(Node smartphoneIRI)  {


        WhereBuilder wb = new WhereBuilder()
                .addPrefix("slma",SLMA)
                .addPrefix ("saref",SAREF)
                .addPrefix("ontodevice", ONTODEVICE)
                .addPrefix("rdf", RDF)
                .addPrefix("om",OM)
                .addWhere(smartphoneIRI,"saref:consistsOf","?gravitySensor")
                .addWhere("?gravitySensor","rdf:type","ontodevice:GravitySensor")
                .addWhere("?gravitySensor","ontodevice:measures","?slma_gravity_y")
                .addWhere("?slma_gravity_y","rdf:type","slma:Gravity_y")
                .addWhere("?slma_gravity_y", "om:hasValue", VAR_O);

        SelectBuilder sb = new SelectBuilder()
                .addVar(VAR_O).addWhere(wb);

        JSONArray queryResult=storeClient.executeQuery(sb.buildString());
        return queryResult;
    }
    JSONArray getGravity_zIRIArray(Node smartphoneIRI)  {

        WhereBuilder wb = new WhereBuilder()
                .addPrefix("slma",SLMA)
                .addPrefix ("saref",SAREF)
                .addPrefix("ontodevice", ONTODEVICE)
                .addPrefix("rdf", RDF)
                .addPrefix("om",OM)
                .addWhere(smartphoneIRI,"saref:consistsOf","?gravitySensor")
                .addWhere("?gravitySensor","rdf:type","ontodevice:GravitySensor")
                .addWhere("?gravitySensor","ontodevice:measures","?slma_gravity_z")
                .addWhere("?slma_gravity_z","rdf:type","slma:Gravity_z")
                .addWhere("?slma_gravity_z", "om:hasValue", VAR_O);

        SelectBuilder sb = new SelectBuilder()
                .addVar(VAR_O).addWhere(wb);

        JSONArray queryResult=storeClient.executeQuery(sb.buildString());
        return queryResult;
    }

    JSONArray getMagnetometer_xIRIArray(Node smartphoneIRI)  {

        WhereBuilder wb = new WhereBuilder()
                .addPrefix("slma",SLMA)
                .addPrefix ("saref",SAREF)
                .addPrefix("ontodevice", ONTODEVICE)
                .addPrefix("rdf", RDF)
                .addPrefix("om",OM)
                .addWhere(smartphoneIRI,"saref:consistsOf","?magnetometer")
                .addWhere("?magnetometer","rdf:type","ontodevice:Magnetometer")
                .addWhere("?magnetometer","ontodevice:measures","?slma_magnetometer_x")
                .addWhere("?slma_magnetometer_x","rdf:type","slma:Magnetometer_x")
                .addWhere("?slma_magnetometer_x", "om:hasValue", VAR_O);

        SelectBuilder sb = new SelectBuilder()
                .addVar(VAR_O).addWhere(wb);

        JSONArray queryResult=storeClient.executeQuery(sb.buildString());
        return queryResult;
    }
    JSONArray getMagnetometer_yIRIArray(Node smartphoneIRI)  {
        WhereBuilder wb = new WhereBuilder()
                .addPrefix("slma",SLMA)
                .addPrefix ("saref",SAREF)
                .addPrefix("ontodevice", ONTODEVICE)
                .addPrefix("rdf", RDF)
                .addPrefix("om",OM)
                .addWhere(smartphoneIRI,"saref:consistsOf","?magnetometer")
                .addWhere("?magnetometer","rdf:type","ontodevice:Magnetometer")
                .addWhere("?magnetometer","ontodevice:measures","?slma_magnetometer_y")
                .addWhere("?slma_magnetometer_y","rdf:type","slma:Magnetometer_y")
                .addWhere("?slma_magnetometer_y", "om:hasValue", VAR_O);

        SelectBuilder sb = new SelectBuilder()
                .addVar(VAR_O).addWhere(wb);

        JSONArray queryResult=storeClient.executeQuery(sb.buildString());
        return queryResult;
    }
    JSONArray getMagnetometer_zIRIArray(Node smartphoneIRI)  {

        WhereBuilder wb = new WhereBuilder()
                .addPrefix("slma",SLMA)
                .addPrefix ("saref",SAREF)
                .addPrefix("ontodevice", ONTODEVICE)
                .addPrefix("rdf", RDF)
                .addPrefix("om",OM)
                .addWhere(smartphoneIRI,"saref:consistsOf","?magnetometer")
                .addWhere("?magnetometer","rdf:type","ontodevice:Magnetometer")
                .addWhere("?magnetometer","ontodevice:measures","?slma_magnetometer_z")
                .addWhere("?slma_magnetometer_z","rdf:type","slma:Magnetometer_z")
                .addWhere("?slma_magnetometer_z", "om:hasValue", VAR_O);

        SelectBuilder sb = new SelectBuilder()
                .addVar(VAR_O).addWhere(wb);

        JSONArray queryResult=storeClient.executeQuery(sb.buildString());
        return queryResult;
    }

    JSONArray getBearingIRIArray(Node smartphoneIRI)  {

        WhereBuilder wb = new WhereBuilder()
                .addPrefix("slma",SLMA)
                .addPrefix ("saref",SAREF)
                .addPrefix("ontodevice", ONTODEVICE)
                .addPrefix("rdf", RDF)
                .addPrefix("om",OM)
                .addWhere(smartphoneIRI,"saref:consistsOf","?GPSDevice")
                .addWhere("?GPSDevice","rdf:type","ontodevice:GPSDevice")
                .addWhere("?GPSDevice","ontodevice:measures","?Bearing")
                .addWhere("?Bearing","rdf:type","slma:Bearing")
                .addWhere("?Bearing", "om:hasValue", VAR_O);

        SelectBuilder sb = new SelectBuilder()
                .addVar(VAR_O).addWhere(wb);

        JSONArray queryResult=storeClient.executeQuery(sb.buildString());
        return queryResult;
    }
    JSONArray getAltitudeIRIArray(Node smartphoneIRI)  {

        WhereBuilder wb = new WhereBuilder()
                .addPrefix("slma",SLMA)
                .addPrefix ("saref",SAREF)
                .addPrefix("ontodevice", ONTODEVICE)
                .addPrefix("rdf", RDF)
                .addPrefix("om",OM)
                .addWhere(smartphoneIRI,"saref:consistsOf","?GPSDevice")
                .addWhere("?GPSDevice","rdf:type","ontodevice:GPSDevice")
                .addWhere("?GPSDevice","ontodevice:measures","?Altitude")
                .addWhere("?Altitude","rdf:type","slma:Altitude")
                .addWhere("?Altitude", "om:hasValue", VAR_O);

        SelectBuilder sb = new SelectBuilder()
                .addVar(VAR_O).addWhere(wb);

        JSONArray queryResult=storeClient.executeQuery(sb.buildString());
        return queryResult;
    }

    JSONArray getSpeedIRIArray(Node smartphoneIRI)  {

        WhereBuilder wb = new WhereBuilder()
                .addPrefix("slma",SLMA)
                .addPrefix ("saref",SAREF)
                .addPrefix("ontodevice", ONTODEVICE)
                .addPrefix("rdf", RDF)
                .addPrefix("om",OM)
                .addWhere(smartphoneIRI,"saref:consistsOf","?GPSDevice")
                .addWhere("?GPSDevice","rdf:type","ontodevice:GPSDevice")
                .addWhere("?GPSDevice","ontodevice:measures","?Speed")
                .addWhere("?Speed","rdf:type","om:Speed")
                .addWhere("?Speed", "om:hasValue", VAR_O);

        SelectBuilder sb = new SelectBuilder()
                .addVar(VAR_O).addWhere(wb);

        JSONArray queryResult=storeClient.executeQuery(sb.buildString());
        return queryResult;
    }
    JSONArray getPointIRIArray(Node smartphoneIRI)  {

        WhereBuilder wb = new WhereBuilder()
                .addPrefix("slma",SLMA)
                .addPrefix ("saref",SAREF)
                .addPrefix("ontodevice", ONTODEVICE)
                .addPrefix("rdf", RDF)
                .addPrefix("om",OM)
                .addPrefix("sf",SF)
                .addWhere(smartphoneIRI,"saref:consistsOf","?GPSDevice")
                .addWhere("?GPSDevice","rdf:type","ontodevice:GPSDevice")
                .addWhere("?GPSDevice","ontodevice:hasGeoLocation",VAR_O)
                .addWhere(VAR_O,"rdf:type","sf:Point");

        SelectBuilder sb = new SelectBuilder()
                .addVar(VAR_O).addWhere(wb);

        JSONArray queryResult=storeClient.executeQuery(sb.buildString());
        return queryResult;
    }

    JSONArray getSoundPressureLevelIRIArray(Node smartphoneIRI)  {

        WhereBuilder wb = new WhereBuilder()
                .addPrefix("slma",SLMA)
                .addPrefix ("saref",SAREF)
                .addPrefix("ontodevice", ONTODEVICE)
                .addPrefix("rdf", RDF)
                .addPrefix("om",OM)
                .addWhere(smartphoneIRI,"saref:consistsOf","?microphone")
                .addWhere("?microphone","rdf:type","ontodevice:Microphone")
                .addWhere("?microphone","ontodevice:measures","?om_soundPressureLevel")
                .addWhere("?om_soundPressureLevel", "om:hasValue", VAR_O);

        SelectBuilder sb = new SelectBuilder()
                .addVar(VAR_O).addWhere(wb);

        JSONArray queryResult=storeClient.executeQuery(sb.buildString());
        return queryResult;
    }

    JSONArray getIlluminanceIRIArray(Node smartphoneIRI)  {

        WhereBuilder wb = new WhereBuilder()
                .addPrefix("slma",SLMA)
                .addPrefix ("saref",SAREF)
                .addPrefix("ontodevice", ONTODEVICE)
                .addPrefix("rdf", RDF)
                .addPrefix("om",OM)
                .addWhere(smartphoneIRI,"saref:consistsOf","?camera")
                .addWhere("?camera","rdf:type","ontodevice:Camera")
                .addWhere("?camera","ontodevice:measures","?om_illuminance")
                .addWhere("?om_illuminance", "om:hasValue", VAR_O);

        SelectBuilder sb = new SelectBuilder()
                .addVar(VAR_O).addWhere(wb);

        JSONArray queryResult=storeClient.executeQuery(sb.buildString());
        return queryResult;
    }

    JSONArray getRelativeBrightnessIRIArray(Node smartphoneIRI)  {

        WhereBuilder wb = new WhereBuilder()
                .addPrefix("slma",SLMA)
                .addPrefix ("saref",SAREF)
                .addPrefix("ontodevice", ONTODEVICE)
                .addPrefix("rdf", RDF)
                .addPrefix("om",OM)
                .addWhere(smartphoneIRI,"ontodevice:hasScreenBrightness","?relativeBrightness")
                .addWhere("?relativeBrightness","rdf:type","ontodevice:RelativeBrightness")
                .addWhere("?relativeBrightness","rdf:type","?om_ratio")
                .addWhere("?om_ratio", "rdf:type", "om:Ratio")
                .addWhere("?om_ratio","om:hasValue", VAR_O);

        SelectBuilder sb = new SelectBuilder()
                .addVar(VAR_O).addWhere(wb);

        JSONArray queryResult=storeClient.executeQuery(sb.buildString());
        return queryResult;
    }
}
