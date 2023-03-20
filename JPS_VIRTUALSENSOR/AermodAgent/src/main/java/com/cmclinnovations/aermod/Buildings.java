package com.cmclinnovations.aermod;

import geotrellis.proj4.CRS;
import geotrellis.proj4.Transform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import scala.Tuple2;
import uk.ac.cam.cares.jps.base.query.AccessAgentCaller;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.io.IOUtils;

import com.cmclinnovations.stack.clients.gdal.GDALClient;
import com.cmclinnovations.stack.clients.gdal.Ogr2OgrOptions;
import com.cmclinnovations.stack.clients.geoserver.GeoServerClient;
import com.cmclinnovations.stack.clients.geoserver.GeoServerVectorSettings;


public class Buildings {

    private static final Logger LOGGER = LogManager.getLogger(Buildings.class);

    //TODO: The next 4 variables need to be updated with additional entries for locations other than Jurong Island.
    public static String[] StackQueryEndpoint = {"jibusinessunits"} ;
    public static String[] GeospatialQueryEndpoint = {"jriEPSG24500"} ;
    public static String[] DatabaseCRS = {"EPSG:24500"};

    //    These values are taken from bboxfinder.com and are in EPSG:4326/WGS84 format.
    public static List<String> boundaryPolygons = new ArrayList<> (
            Arrays.asList("POLYGON ((103.650684 1.216988, 103.743038 1.216988, 103.743038 1.308804, 103.650684 1.308804, 103.650684 1.216988))")) ;


    public int locindex = -1;
    public String StackQueryIRI;
    public String GeospatialQueryIRI;


    // Coordinate reference systems used by database (DatabaseCoordSys) and AERMOD(UTMCoordSys)

    public String DatabaseCoordSys, UTMCoordSys ;

    /* Each element of StackProperties contains the (x,y) coordinates of the center of the base polygon of the stack and the stack height.
    Each element of BuildingVertices contains the coordinates of the vertices of the base polygon.
     */
    public List<String> StackProperties = new ArrayList<>()  ;
    public List<Double> StackEmissions = new ArrayList<>()  ;
    public List<Double> StackDiameter = new ArrayList<>()  ;

    /* Each element of BuildingProperties contains the (x,y) coordinates of the center of the base polygon of the building and the building height.
    Each element of BuildingVertices contains the coordinates of the vertices of the base polygon.
     */
    public List<String> BuildingVertices = new ArrayList<>() ;
    public List<String> BuildingProperties = new ArrayList<>() ;

    // Variables used to run AERMOD and its preprocessors
    public List<List<String>> BPIPPRMBuildingInput = new ArrayList<>();
    public List<String> BPIPPRMStackInput = new ArrayList<>() ;

    public Path simulationDirectory;
    public Path bpipprmDirectory;
    public Path aermodDirectory;
    public Path aermetDirectory;
    public Path aermapDirectory;
    public Polygon scope;
    public int nx;
    public int ny;
    

    public void init(Path simulationDirectory, Polygon scope, int srid, int nx, int ny) throws ParseException {


        this.simulationDirectory = simulationDirectory;
        this.bpipprmDirectory = simulationDirectory.resolve("bpipprm");
        this.bpipprmDirectory.toFile().mkdir();
        this.aermodDirectory = simulationDirectory.resolve("aermod");
        this.aermetDirectory = simulationDirectory.resolve("aermet");
        this.aermapDirectory = simulationDirectory.resolve("aermap");
        this.aermapDirectory.toFile().mkdir();
        this.scope = scope;
        this.nx = nx;
        this.ny = ny;


        // Determine namespace to query based on input polygon

        // The boundary.covers(scope) test works correctly only if boundary and scope have the same srid. Hence, srid of scope must be 4326.//


        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(),4326);
        Polygon boundary = null;
        for (int i = 0; i < boundaryPolygons.size(); i++){
            String wkt = boundaryPolygons.get(i);
            boundary = (Polygon) new WKTReader(geometryFactory).read(wkt);
            if (boundary.covers(scope)) {
                locindex = i;
                break;
            }
        }

        if (locindex >= 0 && scope.getSRID() == 4326) {
            StackQueryIRI = StackQueryEndpoint[locindex];
            GeospatialQueryIRI = GeospatialQueryEndpoint[locindex];
            DatabaseCoordSys = DatabaseCRS[locindex];
        } else if (locindex == -1) {
            LOGGER.info("Input polygon not found in any namespace." + 
            "AERMOD will be run for ships only without buildings and plant items.");
        } else if (scope.getSRID() != 4326) {
            LOGGER.info("Input scope does not have 4326 as its srid." + 
            "AERMOD will be run for ships only without buildings and plant items.");
        }  
        UTMCoordSys = "EPSG:" + srid;




    }

    public int run() {
        try {
            getProperties();

            if (createPlantItemsGeoServerLayer() != 0) {
                LOGGER.error("Failed to create GeoServer Layer for plant items, terminating");
                return 1;
            }

            /* Currently unable to compile AERMAP code and copy executable in the Docker environment.  
            if (createAERMAPInputFile() != 0) {
                LOGGER.error("Failed to create AERMAP input file, terminating");
                return 1;
            }

            if (createAERMAPSourceInput() != 0) {
                LOGGER.error("Failed to create AERMAP source input, terminating");
                return 1;
            }
            if (createAERMAPReceptorInput(nx, ny) != 0) {
                LOGGER.error("Failed to create AERMAP receptor input, terminating");
                return 1;
            }

            if (runAERMAP() != 0 ) {
                LOGGER.error("Failed to run AERMAP, terminating");
                return 1;
            }
            if (processAERMAPOutput() != 0) {
                LOGGER.error("Failed to process AERMAP output, terminating");
                return 1;
            }
            */
            if (updateElevationData() != 0) {
                LOGGER.error("Failed to update elevation data, terminating");
                return 1;
            }
            if (createBPIPPRMInput() != 0) {
                LOGGER.error("Failed to create BPIPPRM input, terminating");
                return 1;
            }

            if (runBPIPPRM() != 0) {
                LOGGER.error("Failed to run BPIPPRM, terminating");
                return 1;
            }

            if (createAERMODBuildingsInput() != 0) {
                LOGGER.error("Failed to create AERMOD buildings input file, terminating");
                return 1;
            }
            if (createAERMODSourceInput() != 0) {
                LOGGER.error("Failed to create AERMOD sources input file, terminating");
                return 1;
            }
            if (createAERMODReceptorInput(nx,ny) != 0) {
                LOGGER.error("Failed to create AERMOD receptor input file, terminating");
                return 1;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return 1;
        }

        return 0;
    }

    //    The String inputs must be of a format similar to "EPSG:4326".
    public static List<List<Double>> convertCoordinates
    (List<List<Double>> inputcoordinates, String inputCRS, String outputCRS) {

        List<List<Double>> outputcoordinates = new ArrayList<>();


        String inputSys = inputCRS.split(":")[1];
        int inputCode = Integer.valueOf(inputSys);
        String outputSys = outputCRS.split(":")[1];
        int outputCode = Integer.valueOf(outputSys);

        CRS sourceCRS = CRS.fromEpsgCode(inputCode);
        CRS targetCRS = CRS.fromEpsgCode(outputCode);

        var convert = Transform.apply(sourceCRS,targetCRS);


        for (int i = 0; i < inputcoordinates.size(); i++) {
            double xi = inputcoordinates.get(i).get(0);
            double yi = inputcoordinates.get(i).get(1);
            Tuple2<Object, Object> res;
            res = convert.apply(xi,yi);
            double xt = (double) res._1();
            double yt = (double) res._2();
            outputcoordinates.add(Arrays.asList(xt,yt));
        }

        return outputcoordinates;
    }

    /* Get geometrical and geospatial properties of stacks and buildings */
    public void getProperties() {

        JSONArray StackIRIQueryResult = QueryClient.StackQuery(StackQueryIRI);
        List<String> StackIRIString = IntStream
                .range(0,StackIRIQueryResult.length())
                .mapToObj(i -> StackIRIQueryResult.getJSONObject(i).getString("IRI"))
                .collect(Collectors.toList());
        JSONArray StackGeometricQueryResult = QueryClient.StackGeometricQuery(GeospatialQueryIRI,StackIRIString);

        String objectIRIPrev = StackGeometricQueryResult.getJSONObject(0).getString("objectIRI");
//        double minZ, maxZ;
        int numberStacks = 0;


        // Determine indices at which data for a new object starts
        List<Integer> resultIndices = new ArrayList<>();
        resultIndices.add(0);

        for (int i = 0; i < StackGeometricQueryResult.length(); i++) {
            String objectIRI = StackGeometricQueryResult.getJSONObject(i).getString("objectIRI");
            if (!objectIRI.equals(objectIRIPrev) ) {
                resultIndices.add(i);
                objectIRIPrev = objectIRI;
            }
        }

        for (int i = 0; i < resultIndices.size(); i++){
            // Determine range of indices for each object
            int firstIndex = resultIndices.get(i);
            int lastIndex;
            if (i == resultIndices.size()-1){
                lastIndex = StackGeometricQueryResult.length();
            } else {
                lastIndex = resultIndices.get(i+1);
            }

            // Process results for each object;
            double minZ = 0.0;
            double maxZ = 0.0;
            double StackEastUTM = 0.0;
            double StackNorthUTM = 0.0;
            boolean includeObject = true;
            String objectIRI = "";
            double radius = 0.0;
            for (int k = firstIndex; k < lastIndex; k++) {
                JSONObject result = StackGeometricQueryResult.getJSONObject(k);
                String polygonVertex = result.getString("polygonData");
                objectIRI = result.getString("objectIRI");
                if (!polygonVertex.contains("#")){
                    continue;
                }
                String[] vertexCoordinates = polygonVertex.split("#");
                List<Double> xcoord = new ArrayList<>();
                List<Double> ycoord = new ArrayList<>();
                List<Double> zcoord = new ArrayList<>();
                for (int j = 0; j < vertexCoordinates.length; j+=3){
                    xcoord.add(Double.parseDouble(vertexCoordinates[j]));
                    ycoord.add(Double.parseDouble(vertexCoordinates[j+1]));
                    zcoord.add(Double.parseDouble(vertexCoordinates[j+2]));
                }
                double polyMinZ = Collections.min(zcoord);
                double polyMaxZ = Collections.max(zcoord);

                if (k == firstIndex ) {
                    minZ = polyMinZ;
                    maxZ = polyMaxZ;
                } else {
                    minZ = Math.min(minZ,polyMinZ);
                    maxZ = Math.max(maxZ,polyMaxZ);
                }


                if (polyMinZ == polyMaxZ && minZ == polyMinZ  ) {
                    double aveX = xcoord.stream().mapToDouble(d->d).average().orElse(0.0);
                    double aveY = ycoord.stream().mapToDouble(d->d).average().orElse(0.0);

                    List<List<Double>> inputcoordinates = new ArrayList<> () ;
                    List<Double> inputcoords = new ArrayList<>(Arrays.asList(aveX,aveY));
                    inputcoordinates.add(inputcoords);
                    List<List<Double>> outputCoordinates = convertCoordinates(inputcoordinates,DatabaseCoordSys,"EPSG:4326");

                    Geometry point = new GeometryFactory().createPoint(new Coordinate(outputCoordinates.get(0).get(0),
                            outputCoordinates.get(0).get(1)));

                    if (!scope.covers(point)) {
                        System.out.println(aveX+ ", " + aveY);
                        includeObject = false;
                        break;
                    }

                    radius = 0.0;
                    for (int j = 0; j < xcoord.size();j++){
                        double dx = xcoord.get(j) - aveX;
                        double dy = ycoord.get(j) - aveY;
                        double dist = Math.sqrt(dx*dx + dy*dy);
                        radius = radius + dist;
                    }
                    radius /= xcoord.size();


                    outputCoordinates.clear();
                    outputCoordinates = convertCoordinates(inputcoordinates,DatabaseCoordSys,UTMCoordSys);

                    StackEastUTM = outputCoordinates.get(0).get(0);
                    StackNorthUTM = outputCoordinates.get(0).get(1);
                }

            }

            if (includeObject){
                numberStacks++;
                String InputLine = "\'Stk" + numberStacks + "\'" + " " + "BASE_ELEVATION " +
                        maxZ + " " + StackEastUTM + " " + StackNorthUTM + " \n" ;
                BPIPPRMStackInput.add(InputLine);
                StringBuffer averageCoordinate = new StringBuffer();
                averageCoordinate.append(StackEastUTM).append("#").append(StackNorthUTM).append("#").append(maxZ);
                StackProperties.add(averageCoordinate.toString());
                // Search for IRI in StackIRIString
                int ind = StackIRIString.indexOf(objectIRI);
                Double emission = StackIRIQueryResult.getJSONObject(ind).getDouble("emission");
                StackEmissions.add(emission);
                StackDiameter.add(2*radius);
            }

        }


        JSONArray BuildingIRIQueryResult = QueryClient.BuildingQuery(StackQueryIRI);
        List<String> BuildingIRIString = IntStream
                .range(0,BuildingIRIQueryResult.length())
                .mapToObj(i -> BuildingIRIQueryResult.getJSONObject(i).getString("IRI"))
                .collect(Collectors.toList());
        JSONArray BuildingGeometricQueryResult = QueryClient.BuildingGeometricQuery(GeospatialQueryIRI,BuildingIRIString);

        objectIRIPrev = BuildingGeometricQueryResult.getJSONObject(0).getString("objectIRI");
        int numberBuildings = 0;


        // Determine indices at which data for a new object starts
        resultIndices.clear();
        resultIndices.add(0);

        for (int i = 0; i < BuildingGeometricQueryResult.length(); i++) {
            String objectIRI = BuildingGeometricQueryResult.getJSONObject(i).getString("objectIRI");
            if (!objectIRI.equals(objectIRIPrev) ) {
                resultIndices.add(i);
                objectIRIPrev = objectIRI;
            }
        }

        for (int i = 0; i < resultIndices.size(); i++){
            // Determine range of indices for each object
            int firstIndex = resultIndices.get(i);
            int lastIndex;
            if (i == resultIndices.size()-1){
                lastIndex = BuildingGeometricQueryResult.length();
            } else {
                lastIndex = resultIndices.get(i+1);
            }

            // Process results for each object;
            double minZ = 0.0;
            double maxZ = 0.0;
            double BuildingEastUTM = 0.0;
            double BuildingNorthUTM = 0.0;
            boolean includeObject = true;
            String objectIRI = "";
            double radius = 0.0;
            int basePolygonIndex = -1;
            for (int k = firstIndex; k < lastIndex; k++) {
                JSONObject result = BuildingGeometricQueryResult.getJSONObject(k);
                String polygonVertex = result.getString("polygonData");
                objectIRI = result.getString("objectIRI");
                if (!polygonVertex.contains("#")){
                    continue;
                }
                String[] vertexCoordinates = polygonVertex.split("#");
                List<Double> xcoord = new ArrayList<>();
                List<Double> ycoord = new ArrayList<>();
                List<Double> zcoord = new ArrayList<>();
                for (int j = 0; j < vertexCoordinates.length; j+=3){
                    xcoord.add(Double.parseDouble(vertexCoordinates[j]));
                    ycoord.add(Double.parseDouble(vertexCoordinates[j+1]));
                    zcoord.add(Double.parseDouble(vertexCoordinates[j+2]));
                }
                double polyMinZ = Collections.min(zcoord);
                double polyMaxZ = Collections.max(zcoord);

                if (k == firstIndex ) {
                    minZ = polyMinZ;
                    maxZ = polyMaxZ;
                } else {
                    minZ = Math.min(minZ,polyMinZ);
                    maxZ = Math.max(maxZ,polyMaxZ);
                }


                if (polyMinZ == polyMaxZ && minZ == polyMinZ  ) {
                    double aveX = xcoord.stream().mapToDouble(d->d).average().orElse(0.0);
                    double aveY = ycoord.stream().mapToDouble(d->d).average().orElse(0.0);

                    List<List<Double>> inputcoordinates = new ArrayList<> () ;
                    List<Double> inputcoords = new ArrayList<>(Arrays.asList(aveX,aveY));
                    inputcoordinates.add(inputcoords);
                    List<List<Double>> outputCoordinates = convertCoordinates(inputcoordinates,DatabaseCoordSys,"EPSG:4326");

                    Geometry point = new GeometryFactory().createPoint(new Coordinate(outputCoordinates.get(0).get(0),
                            outputCoordinates.get(0).get(1)));

                    if (!scope.covers(point)) {
                        System.out.println(aveX+ ", " + aveY);
                        includeObject = false;
                        break;
                    }

                    outputCoordinates.clear();
                    outputCoordinates = convertCoordinates(inputcoordinates,DatabaseCoordSys,UTMCoordSys);

                    BuildingEastUTM = outputCoordinates.get(0).get(0);
                    BuildingNorthUTM = outputCoordinates.get(0).get(1);
                    basePolygonIndex = k;
                }

            }

            if (includeObject){
                numberBuildings++;
                StringBuffer averageCoordinate = new StringBuffer();
                averageCoordinate.append(BuildingEastUTM).append("#").append(BuildingNorthUTM).append("#").append(maxZ);
                BuildingProperties.add(averageCoordinate.toString());

                List<String> buildInfo = new ArrayList<>();

                String InputLine = "\'Build" + numberBuildings + "\' " + "1 " + "BASE_ELEVATION" + " \n" ;
                buildInfo.add(InputLine);
                String BasePolygonVertices = BuildingGeometricQueryResult.getJSONObject(basePolygonIndex).getString("polygonData");

                String [] BaseVertices = BasePolygonVertices.split("#");
                int numCorners = BaseVertices.length/3;
                InputLine = numCorners + " " + maxZ + " \n" ;
                buildInfo.add(InputLine);
                List<List<Double>> inputcoordinates = new ArrayList<> () ;

                for (int j = 0; j < BaseVertices.length; j+=3 ){
                    List<Double> inputcoords = new ArrayList<>(Arrays.asList(Double.parseDouble(BaseVertices[j]), Double.parseDouble(BaseVertices[j+1]))) ;
                    inputcoordinates.add(inputcoords);
                }

                List<List<Double>> outputCoordinates = convertCoordinates(inputcoordinates,DatabaseCoordSys,UTMCoordSys);
                for (int j = 0; j < outputCoordinates.size(); j++ ) {
                    Double VertexEastUTM = outputCoordinates.get(j).get(0);
                    Double VertexNorthUTM = outputCoordinates.get(j).get(1);
                    InputLine = VertexEastUTM + " " + VertexNorthUTM + " \n" ;
                    buildInfo.add(InputLine);
                }
                BuildingVertices.add(BasePolygonVertices);
                BPIPPRMBuildingInput.add(buildInfo);

            }

        }

    }

    public int createPlantItemsGeoServerLayer() {

        // define a list of (longitude, latitude) coordinates
        List<List<Double>> utmCoordinates = new ArrayList<>();

        for (int i = 0; i < StackProperties.size(); i++) {
            String[] avecoord = StackProperties.get(i).split("#");
            double StackEastUTM = Double.parseDouble(avecoord[0]);
            double StackNorthUTM = Double.parseDouble(avecoord[1]);
            List<Double> tmp = Arrays.asList(StackEastUTM, StackNorthUTM);
            utmCoordinates.add(tmp);  
        }
        List<List<Double>> LonLatCoords = convertCoordinates(utmCoordinates, UTMCoordSys, "ESPG:4326");


        // create a JSONObject that represents a GeoJSON Feature Collection
        JSONObject featureCollection = new JSONObject();
        featureCollection.put("type", "FeatureCollection");
        JSONArray features = new JSONArray();

        // loop through the coordinates and add them as GeoJSON Points to the Feature Collection
        for (List<Double> coordinate : LonLatCoords) {
            JSONObject geometry = new JSONObject();
            geometry.put("type", "Point");
            geometry.put("coordinates", new JSONArray(coordinate));
            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");
            feature.put("geometry", geometry);
            features.put(feature);
        }
        featureCollection.put("features", features);

        LOGGER.info("Uploading plant items GeoJSON to PostGIS");
		GDALClient gdalclient = new GDALClient();
		gdalclient.uploadVectorStringToPostGIS(EnvConfig.DATABASE, EnvConfig.SOURCE_LAYER, featureCollection.toString(), new Ogr2OgrOptions(), true);

		LOGGER.info("Creating plant items layer in Geoserver");
		GeoServerClient geoserverclient = new GeoServerClient();
		geoserverclient.createWorkspace(EnvConfig.GEOSERVER_WORKSPACE);
		geoserverclient.createPostGISLayer(null, EnvConfig.GEOSERVER_WORKSPACE, EnvConfig.DATABASE, EnvConfig.SOURCE_LAYER, new GeoServerVectorSettings());

        // convert the Feature Collection to a JSON string
        // String geojsonString = featureCollection.toString();
        // System.out.println(geojsonString);

        return 0;

    } 

    public int createAERMAPInputFile() {

        int centreZoneNumber = (int) Math.ceil((scope.getCentroid().getCoordinate().getX() + 180)/6);
        String templateContent;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("aermap.inp")) {
            templateContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            String simGrid = String.format("%f %f %f %f %d %d", 0.0, 0.0, 0.0, 0.0, centreZoneNumber, 0);
            templateContent = templateContent.replace("REPLACED_BY_AERMOD_AGENT", simGrid);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            LOGGER.error("Failed to read aermap.inp file");
            return 1;
        }
        return writeToFile(aermapDirectory.resolve("aermap.inp"), templateContent);
    }


    public int createAERMAPSourceInput() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < StackProperties.size(); i++) {
            String[] avecoord = StackProperties.get(i).split("#");
            List<Double> inputcoords = Arrays.asList(Double.parseDouble(avecoord[0]), Double.parseDouble(avecoord[1]));
            List<List<Double>> inputcoordinates = Arrays.asList(inputcoords);
            List<List<Double>> outputcoordinates = convertCoordinates(inputcoordinates, DatabaseCoordSys, UTMCoordSys);
            double StackEastUTM = outputcoordinates.get(0).get(0);
            double StackNorthUTM = outputcoordinates.get(0).get(1);
            String stkId = "Stk" + (i + 1);
            sb.append(String.format("SO LOCATION %s POINT %f %f %f \n", stkId, StackEastUTM, StackNorthUTM, 0.0));
        }

        for (int i = 0; i < BuildingProperties.size(); i++) {
            String[] avecoord = BuildingProperties.get(i).split("#");
            List<Double> inputcoords = Arrays.asList(Double.parseDouble(avecoord[0]), Double.parseDouble(avecoord[1]));
            List<List<Double>> inputcoordinates = Arrays.asList(inputcoords);
            List<List<Double>> outputcoordinates = convertCoordinates(inputcoordinates, DatabaseCoordSys, UTMCoordSys);
            double BuildEastUTM = outputcoordinates.get(0).get(0);
            double BuildNorthUTM = outputcoordinates.get(0).get(1);
            String buildId = "Build" + (i + 1);
            sb.append(String.format("SO LOCATION %s POINT %f %f %f \n", buildId, BuildEastUTM, BuildNorthUTM, 0.0));
        }

        return writeToFile(aermapDirectory.resolve("aermapSources.dat"), sb.toString());
    }

    public int createAERMAPReceptorInput(int nx, int ny) {

        List<Double> xDoubles = new ArrayList<>();
        List<Double> yDoubles = new ArrayList<>();

        for (int i = 0; i < scope.getCoordinates().length; i++) {

            double xc = scope.getCoordinates()[i].x;
            double yc = scope.getCoordinates()[i].y;
            List<Double> inputcoords = Arrays.asList(xc, yc);
            List<List<Double>> inputcoordinates = Arrays.asList(inputcoords);
            List<List<Double>> outputcoordinates = convertCoordinates(inputcoordinates, "EPSG:4326", UTMCoordSys);
            xDoubles.add(outputcoordinates.get(0).get(0));
            yDoubles.add(outputcoordinates.get(0).get(1));
        }

        double xlo = Collections.min(xDoubles);
        double xhi = Collections.max(xDoubles);
        double ylo = Collections.min(yDoubles);
        double yhi = Collections.max(yDoubles);

        double dx = (xhi - xlo)/nx;
        double dy = (yhi - ylo)/ny;

        StringBuilder sb = new StringBuilder("RE GRIDCART POL1 STA \n");
        String rec = String.format("                 XYINC %f %d %f %f %d %f",xlo, nx, dx, ylo, ny, dy);
        sb.append(rec + " \n");
        sb.append("RE GRIDCART POL1 END \n");

        return writeToFile(aermapDirectory.resolve("aermapReceptors.dat"),sb.toString());
    }

    public int runAERMAP() {

        // Read data file names from aermap input file. Assuming that there is no "CO" before the "DATAFILE" keyword.
        List<String> dataFiles = new ArrayList<>();
        Path filepath = aermapDirectory.resolve("aermap.inp");

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath.toString()));
            String line = reader.readLine();
            while (line != null) {
                if (line.contains("DATAFILE")) {
                    String [] linesplit = line.split("\\s+");
                    dataFiles.add(linesplit[1]);

                }
                if (line.contains("CO FINISHED")) break; 
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return 1;
        }

        if (dataFiles.size() == 0) {
            LOGGER.error("No elevation data files specified in aermap.inp");
            return 1;
        }

        for (int i = 0; i < dataFiles.size(); i++) {
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(dataFiles.get(i))) {
                Files.copy(inputStream, aermapDirectory.resolve(dataFiles.get(i)));
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                LOGGER.error("Failed to copy the data file" + dataFiles.get(i));
                return 1;
            }
        }


        try {
            Process process = Runtime.getRuntime().exec(new String[]{EnvConfig.AERMAP_EXE, "aermap.inp"}, null, aermapDirectory.toFile());
            if (process.waitFor() != 0) {
                return 1;
            }
        } catch (IOException e) {
            LOGGER.error("Error executing aermap");
            LOGGER.error(e.getMessage());
            return 1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Error executing aermap");
            LOGGER.error(e.getMessage());
            return 1;
        }

        return 0;
    }

    public int processAERMAPOutput() {
        // Update BPIPPRMBuildingInput and BPIPPRMStackInput
        Path filepath = aermapDirectory.resolve("buildingSources.dat");

        StringBuilder sb = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath.toString()));
            String line = reader.readLine();
            while (line != null) {
                if (line.isBlank() || line.substring(0,2).equals("**")) continue;
                if (line.contains("ELEVUNIT") ) sb.append(line + "\n");
                else if (line.contains("STK")) {
                    sb.append(line + "\n");
                    String [] StackInfo = line.split("\\s+");
                    String StackElevation = StackInfo[StackInfo.length - 1];
                    int StackNum = Integer.parseInt(StackInfo[2].substring(3,4));
                    String StackLine = BPIPPRMStackInput.get(StackNum-1);
                    StackLine.replace("BASE_ELEVATION", StackElevation);
                    BPIPPRMStackInput.set(StackNum-1, StackLine);
                    String stackProp = StackProperties.get(StackNum-1);
                    String propElevation = "#" + StackElevation;
                    stackProp += propElevation;
                    StackProperties.set(StackNum-1, stackProp);
                }
                else if (line.contains("BUILD")) {
                    String [] buildInfo = line.split("\\s+");
                    String buildElevation = buildInfo[buildInfo.length - 1] ;
                    int buildNum = Integer.parseInt(buildInfo[2].substring(5,6));
                    String BuildLine = BPIPPRMBuildingInput.get(buildNum-1).get(0);
                    BuildLine.replace("BASE_ELEVATION", buildElevation);
                    BPIPPRMBuildingInput.get(buildNum-1).set(0,BuildLine);
                }
                line = reader.readLine();
            }
            reader.close();
            LOGGER.info(sb.toString());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return 1;
        }
        return 0;
    }

    // This method should be called only if AERMAP was not run.
    public int updateElevationData() {
        
        int numberBuildings = BPIPPRMBuildingInput.size();
        for (int i = 0; i < numberBuildings; i++) {
            String BuildLine = BPIPPRMBuildingInput.get(i).get(0);
            BuildLine = BuildLine.replace("BASE_ELEVATION", "0.0");
            BPIPPRMBuildingInput.get(i).set(0,BuildLine);
        }
        
        int numberStacks = BPIPPRMStackInput.size() ;
        for (int i = 0; i < numberStacks; i++) {
            String StackLine = BPIPPRMStackInput.get(i);
            StackLine = StackLine.replace("BASE_ELEVATION", "0.0");
            BPIPPRMStackInput.set(i, StackLine);
            String stackProp = StackProperties.get(i);
            String propElevation = "#0.0" ;
            stackProp += propElevation;
            StackProperties.set(i, stackProp);

        }
        return 0;
    }


    /* Write out data to BPIPPRM input file and run this program. */
    public int createBPIPPRMInput() {

        List<String> frontmatter = new ArrayList<>();
        frontmatter.add("\'BPIPPRM test run\' \n");
        frontmatter.add("\'p\' \n");
        frontmatter.add("\' METERS    \'  1.0  \n");
        frontmatter.add("\'UTMY \'  0.0 \n");

        StringBuilder sb = new StringBuilder();

        for (String st:frontmatter) {
            sb.append(st);
        }

        int numberBuildings = BPIPPRMBuildingInput.size() ;
        sb.append(numberBuildings + " \n");
        for (int i = 0; i < numberBuildings; i++) {
            for (int j = 0; j < BPIPPRMBuildingInput.get(i).size(); j++) {
                sb.append(BPIPPRMBuildingInput.get(i).get(j));
            }
        }
        int numberStacks = BPIPPRMStackInput.size() ;
        sb.append(numberStacks + " \n");
        for (int i = 0; i < numberStacks; i++) {
            sb.append(BPIPPRMStackInput.get(i));
        }
        return writeToFile(bpipprmDirectory.resolve("bpipprm.inp"), sb.toString());

    }

    private int writeToFile(Path path, String content ) {

        try {
            boolean res = Files.deleteIfExists(path);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            LOGGER.info("Writing file: {}", path);
            writer.write(content);
            return 0;
        } catch (IOException e) {
            String errmsg = "Failed to write " + path.getFileName();
            LOGGER.error(errmsg);
            LOGGER.error(e.getMessage());
            return 1;
        }
    }


    public int runBPIPPRM() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{EnvConfig.BPIPPRM_EXE, "bpipprm.inp","building.dat","buildings_summary.dat"}, null, bpipprmDirectory.toFile());
            if (process.waitFor() != 0) {
                return 1;
            }
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // Read the output from the command
            LOGGER.info("Here is the standard output of the command:\n");
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                LOGGER.info(s);
            }

            // Read any errors from the attempted command
            LOGGER.info("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                LOGGER.info(s);
            }
        } catch (IOException e) {
            return 0;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        }
        return 0;
    }

    public int createAERMODBuildingsInput() {

        
        StringBuilder sb = new StringBuilder();
        if (locindex == -1) return writeToFile(aermodDirectory.resolve("buildings.dat"), sb.toString());

        Path filepath = bpipprmDirectory.resolve("building.dat");

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath.toString()));
            String line = reader.readLine();
            while (line != null) {
                line = line.stripLeading();
                if (line.length() > 2 && line.substring(0,2).equals("SO")) sb.append(line + "\n");
                line = reader.readLine();
            }
            reader.close();
            LOGGER.info(sb.toString());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        return writeToFile(aermodDirectory.resolve("buildings.dat"), sb.toString());
    }

    public int createAERMODSourceInput() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < StackProperties.size(); i++) {
            String[] avecoord = StackProperties.get(i).split("#");
            double StackEastUTM = Double.parseDouble(avecoord[0]);
            double StackNorthUTM = Double.parseDouble(avecoord[1]);
            double StackHeight = Double.parseDouble(avecoord[2]);
            double StackBaseElevation = Double.parseDouble(avecoord[3]);            
            double massFlowrateInTonYr = StackEmissions.get(i);
            double massFlowrateInGs = massFlowrateInTonYr * 1000 * 1000 / (365 * 24 * 60 * 60);
            double gasTemperatureKelvin = 890.0;
            double atmosphericPressurePa = 101325;
            double gasConstantJoulemolKelvin = 8.314;
            double molarMassCO2gmol = 44.01;
            double volumetricFlowRatem3s = (massFlowrateInGs / molarMassCO2gmol) * gasConstantJoulemolKelvin * gasTemperatureKelvin / atmosphericPressurePa;
            double Diameter = StackDiameter.get(i);
            double stackAream2 = (Math.PI / 4) * Diameter * Diameter;
            double velocityms = volumetricFlowRatem3s / stackAream2;

            String stkId = "Stk" + (i + 1);
            sb.append(String.format("SO LOCATION %s POINT %f %f %f \n", stkId, StackEastUTM, StackNorthUTM, StackBaseElevation));
            sb.append(String.format("SO SRCPARAM %s %f %f %f %f %f \n", stkId,
                    massFlowrateInGs, StackHeight, gasTemperatureKelvin, velocityms, Diameter));
        }

        return writeToFile(aermodDirectory.resolve("plantSources.dat"),sb.toString());

    }

    public int createAERMODReceptorInput(int nx, int ny) {

        List<Double> xDoubles = new ArrayList<>();
        List<Double> yDoubles = new ArrayList<>();

        for (int i = 0; i < scope.getCoordinates().length; i++) {

            double xc = scope.getCoordinates()[i].x;
            double yc = scope.getCoordinates()[i].y;


            List<Double> inputcoords = Arrays.asList(xc, yc);
            List<List<Double>> inputcoordinates = Arrays.asList(inputcoords);
            List<List<Double>> outputcoordinates = convertCoordinates(inputcoordinates, "EPSG:4326", UTMCoordSys);

            xDoubles.add(outputcoordinates.get(0).get(0));
            yDoubles.add(outputcoordinates.get(0).get(1));
        }

        double xlo = Collections.min(xDoubles);
        double xhi = Collections.max(xDoubles);
        double ylo = Collections.min(yDoubles);
        double yhi = Collections.max(yDoubles);

        double dx = (xhi - xlo)/nx;
        double dy = (yhi - ylo)/ny;

        StringBuilder sb = new StringBuilder("RE GRIDCART POL1 STA \n");
        String rec = String.format("                 XYINC %f %d %f %f %d %f",xlo, nx, dx, ylo, ny, dy);
        sb.append(rec + " \n");
        sb.append("RE GRIDCART POL1 END \n");

        return writeToFile(aermodDirectory.resolve("receptor.dat"),sb.toString());
    }

}

  // The following methods are deprecated.


    /* Query stacks and buildings 
    public static void getStacksBuildings () {


        JSONArray StackOCGMLIRI = QueryClient.StackQuery(StackQueryIRI) ;
        JSONArray BuildingOCGMLIRI = QueryClient.BuildingQuery(StackQueryIRI) ;

        int numberStacks = 0;
        int numberBuildings = 0;




        for (int i = 0; i < StackOCGMLIRI.length(); i++) {
            Double emission = StackOCGMLIRI.getJSONObject(i).getDouble("emission");
            String IRI = StackOCGMLIRI.getJSONObject(i).getString("IRI");
            StringBuffer coordinateQuery = new StringBuffer("PREFIX ocgml: <http://www.theworldavatar.com/ontology/ontocitygml/citieskg/OntoCityGML.owl#>\n");
            coordinateQuery.append("SELECT ?geometricIRI ?polygonData WHERE {\n");
            coordinateQuery.append("?geometricIRI ocgml:GeometryType ?polygonData.\n") ;
            coordinateQuery.append("?geometricIRI ocgml:cityObjectId <").append(IRI).append(">.}");
            JSONArray coordinateQueryResult = AccessAgentCaller.queryStore(GeospatialQueryIRI, coordinateQuery.toString());
            String StackX = "0";
            String StackY = "0";
            String StackZ = "0";

            int basePolygonIndex = -1;

            for (int ip = 0; ip < coordinateQueryResult.length(); ip++) {
                JSONObject coordiS = coordinateQueryResult.getJSONObject(ip);
                String coordiData = coordiS.getString("polygonData");
                List<String> z_values = new ArrayList<>();


                String[] coordinates = coordiData.split("#");
                double sum_x = 0; double sum_y = 0;
                double sum_z = 0; double min_z = 0;

                for(int j = 1; j <= coordinates.length; j++) {
                    if( j%3==0 ){
                        z_values.add(coordinates[j-1]);
                        sum_x = sum_x + Double.parseDouble(coordinates[j-3]);
                        sum_y = sum_y + Double.parseDouble(coordinates[j-2]);
                        sum_z = sum_z + Double.parseDouble(coordinates[j-1]);
                        min_z = min(min_z,Double.parseDouble(coordinates[j-1]));
                    }
                }
                if (min_z == sum_z/(coordinates.length/3) && !z_values.isEmpty()) {
                    StackX = String.valueOf(sum_x/(coordinates.length/3));
                    StackY = String.valueOf(sum_y/(coordinates.length/3));
                    basePolygonIndex = ip;
                }
                if (!z_values.isEmpty() && Double.parseDouble(StackZ) < Double.parseDouble(Collections.max(z_values))) {
                    StackZ = Collections.max(z_values);
                }
            }
            StringBuffer averageCoordinate = new StringBuffer();

            averageCoordinate.append(StackX).append("#").append(StackY).append("#").append(StackZ);

            List<List<Double>> inputcoordinates = new ArrayList<> () ;
            List<Double> inputcoords = new ArrayList<>(Arrays.asList(Double.parseDouble(StackX),Double.parseDouble(StackY)));
            inputcoordinates.add(inputcoords);
            List<List<Double>> outputCoordinates = convertCoordinates(inputcoordinates,DatabaseCoordSys,"EPSG:4326");

            Geometry point = new GeometryFactory().createPoint(new Coordinate(outputCoordinates.get(0).get(0),
                    outputCoordinates.get(0).get(1)));

            if (!scope.covers(point)) {
                System.out.println(StackX+ ", " + StackY);
                continue;
//                throw new RuntimeException("Stack outside poylgon");
            }


            numberStacks++;
            inputcoordinates.clear();
            inputcoords.clear();
            outputCoordinates.clear();

            StackEmissions.add(emission);
            StackProperties.add(averageCoordinate.toString());

            inputcoords =
                    new ArrayList<>(Arrays.asList(Double.parseDouble(StackX), Double.parseDouble(StackY))) ;
            inputcoordinates = new ArrayList<>(Arrays.asList(inputcoords)) ;
            // convert coordinates from Database coordinates to UTM
            outputCoordinates = convertCoordinates(inputcoordinates,DatabaseCoordSys,UTMCoordSys);

            Double StackEastUTM = outputCoordinates.get(0).get(0);
            Double StackNorthUTM = outputCoordinates.get(0).get(1);
            String InputLine = "\'Stk" + numberStacks + "\'" + " " + "0.0 " +
                    StackZ + " " + StackEastUTM + " " + StackNorthUTM + " \n" ;
            BPIPPRMStackInput.add(InputLine);

            // Calculate stack diameter from base polygon data
            JSONObject coordiS = coordinateQueryResult.getJSONObject(basePolygonIndex);
            String coordiData = coordiS.getString("polygonData");
            String[] coordinates = coordiData.split("#");
            Double StackDoubleX = Double.parseDouble(StackX);
            Double StackDoubleY = Double.parseDouble(StackY);
            Double radius = 0.0;
            for (int j = 0; j < coordinates.length;j+=3){
                Double dx = StackDoubleX - Double.parseDouble(coordinates[j]);
                Double dy = StackDoubleY - Double.parseDouble(coordinates[j+1]);
                Double dist = Math.sqrt(dx*dx + dy*dy);
                radius = radius +dist;
            }
            radius = radius/(coordinates.length/3);
            StackDiameter.add(2*radius);
        }

        for (int i = 0; i < BuildingOCGMLIRI.length(); i++) {

            String IRI = BuildingOCGMLIRI.getJSONObject(i).getString("IRI");
            StringBuffer coordinateQuery = new StringBuffer("PREFIX ocgml: <http://www.theworldavatar.com/ontology/ontocitygml/citieskg/OntoCityGML.owl#>\n");
            coordinateQuery.append("SELECT ?polygondata WHERE {\n");
            coordinateQuery.append("?surfaceIRI ocgml:GeometryType ?polygondata.");
            coordinateQuery.append("?geometricIRI ocgml:lod2MultiSurfaceId ?surfaceIRI.");
            coordinateQuery.append("?geometricIRI ocgml:buildingId <").append(IRI).append(">.}");
            JSONArray coordinateQueryResult = AccessAgentCaller.queryStore(GeospatialQueryIRI, coordinateQuery.toString());
            String BuildingX = "0";
            String BuildingY = "0";
            String BuildingZ = "0";

            for (int ip = 0; ip < coordinateQueryResult.length(); ip++) {
                JSONObject coordiS = coordinateQueryResult.getJSONObject(ip);
                String coordiData = coordiS.getString("polygondata");
                List<String> z_values = new ArrayList<>();
                String[] coordinates = coordiData.split("#");
                double sum_x = 0; double sum_y = 0;
                double sum_z = 0; double min_z = 0;

                for (int j = 1; j <= coordinates.length; j++) {
                    if( j%3==0 ){
                        z_values.add(coordinates[j-1]);
                        sum_x = sum_x + Double.parseDouble(coordinates[j-3]);
                        sum_y = sum_y + Double.parseDouble(coordinates[j-2]);
                        sum_z = sum_z + Double.parseDouble(coordinates[j-1]);
                        min_z = min(min_z,Double.parseDouble(coordinates[j-1]));
                    }
                }
                if (min_z == sum_z/(coordinates.length/3) && !z_values.isEmpty()) {
                    BuildingX = String.valueOf(sum_x/(coordinates.length/3));
                    BuildingY = String.valueOf(sum_y/(coordinates.length/3));
                    BuildingVertices.add(coordiData);
                }
                if (!z_values.isEmpty() && Double.parseDouble(BuildingZ) < Double.parseDouble(Collections.max(z_values))) {
                    BuildingZ = Collections.max(z_values);
                }
            }

            StringBuffer averageCoordinate = new StringBuffer();
            averageCoordinate.append(BuildingX).append("#").append(BuildingY).append("#").append(BuildingZ);

            List<List<Double>> inputcoordinates = new ArrayList<> () ;
            List<Double> inputcoords = new ArrayList<>(Arrays.asList(Double.parseDouble(BuildingX),Double.parseDouble(BuildingY)));
            inputcoordinates.add(inputcoords);
            List<List<Double>> outputCoordinates = convertCoordinates(inputcoordinates,DatabaseCoordSys,"EPSG:4326");

            Geometry point = new GeometryFactory().createPoint(new Coordinate(outputCoordinates.get(0).get(0),
                    outputCoordinates.get(0).get(1)));

            if (!scope.covers(point)) {
                int index = BuildingVertices.size() - 1;
                BuildingVertices.remove(index);
                System.out.println(BuildingX+ ", " + BuildingY);
                continue;
//                throw new RuntimeException("Building outside poylgon");
            }
            numberBuildings++;
            inputcoordinates.clear();
            inputcoords.clear();
            outputCoordinates.clear();

            BuildingProperties.add(averageCoordinate.toString());

            String InputLine = "\'Build" + numberBuildings + "\' " + "1 " + "0.0" + " \n" ;
            BPIPPRMBuildingInput.add(InputLine);
            String BasePolygonVertices = BuildingVertices.get(i);
            String [] BaseVertices = BasePolygonVertices.split("#");
            int numCorners = BaseVertices.length/3;
            InputLine = numCorners + " " + BuildingZ + " \n" ;
            BPIPPRMBuildingInput.add(InputLine);

            inputcoordinates = new ArrayList<> () ;

            for (int j = 0; j < BaseVertices.length; j+=3 ){
                inputcoords = new ArrayList<>(Arrays.asList(Double.parseDouble(BaseVertices[j]), Double.parseDouble(BaseVertices[j+1]))) ;
                inputcoordinates.add(inputcoords);
            }

            // convert coordinates from Database coordinates to UTM
            outputCoordinates = convertCoordinates(inputcoordinates,DatabaseCoordSys,UTMCoordSys);
            for (int j = 0; j < outputCoordinates.size(); j++ ) {
                Double VertexEastUTM = outputCoordinates.get(j).get(0);
                Double VertexNorthUTM = outputCoordinates.get(j).get(1);
                InputLine = VertexEastUTM + " " + VertexNorthUTM + " \n" ;
                BPIPPRMBuildingInput.add(InputLine);
            }
        }


        // Add the numbers of buildings and stacks as the last elements of the BPIPPRMStackInput and
        // BPIPPRMBuildingInput arrays.However, this information must be written to the BPIPPRM input file first.
        String StackLine = numberStacks + " \n" ;
        String BuildingsLine = numberBuildings + " \n" ;
        BPIPPRMStackInput.add(StackLine);
        BPIPPRMBuildingInput.add(BuildingsLine);


    }




    public static JSONArray StackQuery (String StackQueryIRI) {
        StringBuffer StackIRIQuery = new StringBuffer("PREFIX ns2: <https://www.theworldavatar.com/kg/ontobuiltenv/>\n");
        StackIRIQuery.append("PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n");
        StackIRIQuery.append("PREFIX kb: <http://www.theworldavatar.com/kb/ontochemplant/>\n");
        StackIRIQuery.append("PREFIX ocp: <http://theworldavatar.com/ontology/ontochemplant/OntoChemPlant.owl#>\n");
        StackIRIQuery.append("PREFIX om:  <http://www.ontology-of-units-of-measure.org/resource/om-2/>\n");
        StackIRIQuery.append("PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        StackIRIQuery.append("SELECT ?IRI ?emission WHERE {");
        StackIRIQuery.append("?chemical_plant rdf:type <http://theworldavatar.com/ontology/ontochemplant/OntoChemPlant.owl#ChemicalPlant>.");
        StackIRIQuery.append("?chemical_plant geo:ehContains ?plant_item .");
        StackIRIQuery.append("?plant_item rdf:type <http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_realization/plant.owl#PlantItem>.");
        StackIRIQuery.append("?plant_item ns2:hasOntoCityGMLRepresentation ?IRI .");
        StackIRIQuery.append("?plant_item ocp:hasIndividualCO2Emission ?CO2 .");
        StackIRIQuery.append("?CO2 om:hasNumericalValue ?emission .}");
//        StackIRIQuery.append("LIMIT 100");
        JSONArray StackIRIQueryResult = AccessAgentCaller.queryStore(StackQueryIRI, StackIRIQuery.toString());
        return StackIRIQueryResult;
    }

    public static JSONArray BuildingQuery (String StackQueryIRI) {
        StringBuffer BuildingIRIQuery = new StringBuffer("PREFIX ns2: <https://www.theworldavatar.com/kg/ontobuiltenv/>\n");
        BuildingIRIQuery.append("PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n");
        BuildingIRIQuery.append("PREFIX kb: <http://www.theworldavatar.com/kb/ontochemplant/>\n");
        BuildingIRIQuery.append("PREFIX ocp: <http://theworldavatar.com/ontology/ontochemplant/OntoChemPlant.owl#>\n");
        BuildingIRIQuery.append("PREFIX om:  <http://www.ontology-of-units-of-measure.org/resource/om-2/>\n");
        BuildingIRIQuery.append("PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        BuildingIRIQuery.append("SELECT ?IRI WHERE {");
        BuildingIRIQuery.append("?chemical_plant rdf:type <http://theworldavatar.com/ontology/ontochemplant/OntoChemPlant.owl#ChemicalPlant>.");
        BuildingIRIQuery.append("?chemical_plant geo:ehContains ?building .");
        BuildingIRIQuery.append("?building rdf:type <http://www.purl.org/oema/infrastructure/Building>.");
        BuildingIRIQuery.append("?building ns2:hasOntoCityGMLRepresentation ?IRI .}");
        BuildingIRIQuery.append("LIMIT 10");
        JSONArray BuildingIRIQueryResult = AccessAgentCaller.queryStore(StackQueryIRI, BuildingIRIQuery.toString());
        return BuildingIRIQueryResult;
    }


    */