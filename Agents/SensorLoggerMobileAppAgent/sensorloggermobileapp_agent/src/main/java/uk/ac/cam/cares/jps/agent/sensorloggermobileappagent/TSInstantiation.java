package uk.ac.cam.cares.jps.agent.sensorloggermobileappagent;

import com.cmclinnovations.stack.clients.postgis.PostGISClient;
import com.github.dockerjava.api.model.Device;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.postgis.Point;
import uk.ac.cam.cares.downsampling.Downsampling;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.query.RemoteRDBStoreClient;
import uk.ac.cam.cares.jps.base.query.RemoteStoreClient;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeries;
import uk.ac.cam.cares.jps.base.timeseries.TimeSeriesClient;

import java.sql.Connection;
import java.time.OffsetDateTime;
import java.util.*;



public class TSInstantiation implements Cloneable  {
    private HashMap data;

    public TSInstantiation(HashMap data) {
        this.data = data;
        this.timer=timer;
        this.addDataIntoTheLists(data);
    }

    public void setData(HashMap data) {
        this.data = data;
        this.timer=timer;
        addDataIntoTheLists(data);
    }
    // public TSInstantiation clone() {
    //     try {
    //         TSInstantiation copy = (TSInstantiation) super.clone();
    //         // Deep copy of lists
    //         copy.accel_tsList = new ArrayList<>(this.accel_tsList);
    //         copy.accelList_x = new ArrayList<>(this.accelList_x);
    //         copy.accelList_y = new ArrayList<>(this.accelList_y);
    //         copy.accelList_z = new ArrayList<>(this.accelList_z);

    //         copy.magnetometer_tsList = new ArrayList<>(this.magnetometer_tsList);
    //         copy.magnetometerList_x = new ArrayList<>(this.magnetometerList_x);
    //         copy.magnetometerList_y = new ArrayList<>(this.magnetometerList_y);
    //         copy.magnetometerList_z = new ArrayList<>(this.magnetometerList_z);

    //         copy.gravity_tsList = new ArrayList<>(this.gravity_tsList);
    //         copy.gravityList_x = new ArrayList<>(this.gravityList_x);
    //         copy.gravityList_y = new ArrayList<>(this.gravityList_y);
    //         copy.gravityList_z = new ArrayList<>(this.gravityList_z);

    //         copy.location_tsList = new ArrayList<>(this.location_tsList);
    //         copy.bearingList = new ArrayList<>(this.bearingList);
    //         copy.speedList = new ArrayList<>(this.speedList);
    //         copy.altitudeList = new ArrayList<>(this.altitudeList);
    //         copy.geomLocationList = new ArrayList<>(this.geomLocationList);

    //         copy.dBFS_tsList = new ArrayList<>(this.dBFS_tsList);
    //         copy.dBFSList = new ArrayList<>(this.dBFSList);

    //         copy.lightValue_tsList = new ArrayList<>(this.lightValue_tsList);
    //         copy.lightValueList = new ArrayList<>(this.lightValueList);

    //         copy.brightness_tsList = new ArrayList<>(this.brightness_tsList);
    //         copy.brightnessList = new ArrayList<>(this.brightnessList);
    //         return copy;
    //     } catch (CloneNotSupportedException e) {
    //         throw new AssertionError(); // should not happen
    //     }
    // }



    

    public void addDataIntoTheLists(HashMap data){
        DEVICEID= (String) data.get("deviceID");
        
        accel_tsList.addAll((List<OffsetDateTime>) data.get("accel_tsList"));
        accelList_x.addAll((List<Double>) data.get("accelList_x"));
        accelList_y.addAll((List<Double>) data.get("accelList_y"));
        accelList_z.addAll((List<Double>) data.get("accelList_z"));

        magnetometer_tsList.addAll((List<OffsetDateTime>) data.get("magnetometer_tsList"));
        magnetometerList_x.addAll((List<Double>) data.get("magnetometerList_x"));
        magnetometerList_y.addAll((List<Double>) data.get("magnetometerList_y"));
        magnetometerList_z.addAll((List<Double>) data.get("magnetometerList_z"));

        gravity_tsList.addAll((List<OffsetDateTime>) data.get("gravity_tsList"));
        gravityList_x.addAll((List<Double>) data.get("gravityList_x"));
        gravityList_y.addAll((List<Double>) data.get("gravityList_y"));
        gravityList_z.addAll((List<Double>) data.get("gravityList_z"));

        location_tsList.addAll((List<OffsetDateTime>) data.get("location_tsList"));
        bearingList.addAll((List<Double>) data.get("bearingList"));
        speedList.addAll((List<Double>) data.get("speedList"));
        altitudeList.addAll((List<Double>) data.get("altitudeList"));
        geomLocationList.addAll((List<Point>) data.get("geomLocationList"));

        dBFS_tsList.addAll((List<OffsetDateTime>) data.get("dBFS_tsList"));
        dBFSList.addAll((List<Double>) data.get("dBFSList"));

        lightValue_tsList.addAll((List<OffsetDateTime>) data.get("lightValue_tsList"));
        lightValueList.addAll((List<Double>) data.get("lightValueList"));

        brightness_tsList.addAll((List<OffsetDateTime>) data.get("brightness_tsList"));
        brightnessList.addAll((List<Double>) data.get("brightnessList"));
    }



    //Accelerometer list
    private static ArrayList<OffsetDateTime> accel_tsList = new ArrayList<>();
    private static List<Double> accelList_x = new ArrayList<>();
    private static List<Double> accelList_y = new ArrayList<>();
    private static List<Double> accelList_z = new ArrayList<>();
    private static List<List<?>> accel_lolValues= Arrays.asList(accelList_x,accelList_y,accelList_z);

    //Magnetometer list
    private static ArrayList<OffsetDateTime> magnetometer_tsList = new ArrayList<>();
    private static List<Double> magnetometerList_x = new ArrayList<>();
    private static List<Double> magnetometerList_y = new ArrayList<>();
    private static List<Double> magnetometerList_z = new ArrayList<>();
    private static List<List<?>> magnetometer_lolValues= Arrays.asList(magnetometerList_x,magnetometerList_y,magnetometerList_z);

    //Gravity sensor list
    private static ArrayList<OffsetDateTime> gravity_tsList = new ArrayList<>();
    private static List<Double> gravityList_x = new ArrayList<>();
    private static List<Double> gravityList_y = new ArrayList<>();
    private static List<Double> gravityList_z = new ArrayList<>();
    private static List<List<?>> gravity_lolValues= Arrays.asList(gravityList_x,gravityList_y,gravityList_z);

    //Location list
    private static ArrayList<OffsetDateTime> location_tsList = new ArrayList<>();
    private static List<Double> bearingList = new ArrayList<>();
    private static List<Double> speedList = new ArrayList<>();
    private static List<Double> altitudeList = new ArrayList<>();
    private static List<Point> geomLocationList = new ArrayList<>();
    private static List<List<?>> location_lolValues= Arrays.asList(bearingList,speedList,altitudeList,geomLocationList);

    //Microphone lists
    private static ArrayList<OffsetDateTime> dBFS_tsList = new ArrayList<>();
    private static List<Double> dBFSList = new ArrayList<>();
    private static List<List<?>> dBFS_lolValues= Arrays.asList(dBFSList);

    //Light value lists
    private static ArrayList<OffsetDateTime> lightValue_tsList = new ArrayList<>();
    private static List<Double> lightValueList = new ArrayList<>();
    private static List<List<?>> lightValue_lolValues= Arrays.asList(lightValueList);

    private static ArrayList<OffsetDateTime> brightness_tsList = new ArrayList<>();
    private static List<Double> brightnessList = new ArrayList<>();
    private static List<List<?>> brightness_lolValues= Arrays.asList(brightnessList);

    private static int maxSize = accel_lolValues.size()+magnetometer_lolValues.size()+gravity_lolValues.size()+location_lolValues.size()+dBFS_lolValues.size()+lightValue_lolValues.size()+brightness_lolValues.size();
    private static String DEVICEID;
    private static final long serialVersionUID = 1L;
    private static final String BASEURI = "https://www.theworldavatar.com/kg/sensorloggerapp/";

    private static String smartphoneString;
    private Timer timer;
    private boolean isTimerStarted = false;

    private static Long accelDSResolution;
    private static Downsampling.Type accelDSType;

    private static Long gravityDSResolution;
    private static Downsampling.Type gravityDSType;

    private static Long magnetometerDSResolution;
    private static Downsampling.Type magnetometerDSType;

    private static Long dbfsDSResolution;
    private static Downsampling.Type dbfsDSType;

    private static Long rbDSResolution;
    private static Downsampling.Type rbDSType;

    private static Long lightValueDSResolution;
    private static Downsampling.Type lightValueDSType;


    public void startTimer() {
        //Readconfig
        readConfig();


        //Create POSTGIS Database in the stack
        PostGISClient postGISClient = PostGISClient.getInstance();
        postGISClient.createDatabase(EnvConfig.DATABASE);
        
        //Initialiase timseries
        initSensorTimeseries();

        if (!isTimerStarted) {
            isTimerStarted = true;
            // Start the timer
            timer = new Timer();
            System.out.println("Timer started for device:"+DEVICEID +"; Delay:" +timerDelay+"seconds ; Frequency:"+timerFrequency+"seconds ");
            timer.schedule(new instantiationTask(), timerDelay*1000,timerFrequency*1000); // 5000 milliseconds = 5 seconds
        }
    }


    class instantiationTask extends TimerTask {
        public void run() {
            // This code will be executed when the timer expires
            try{
                    tsInstantiate();
                    System.out.println("Timer task ran succesfully for device:"+DEVICEID);
            }catch (Exception e){
                System.out.println("Timeseries not instantiated for device:"+DEVICEID+", exception caught");
            }
        }
    }

    /**
     * Stack server
     */

    private static int timerDelay;
    private static int timerFrequency;
    private static RemoteRDBStoreClient rdbStoreClient;
    private static RemoteStoreClient storeClient;
    private static QueryClientSPARQL queryClientSPARQL;

    private static final Logger LOGGER = LogManager.getLogger(SensorLoggerMobileAppAgent.class);

    private static String accel_xIRI;
    private static String accel_yIRI;
    private static String accel_zIRI;
    private static String gravity_xIRI;
    private static String gravity_yIRI;
    private static String gravity_zIRI;
    private static String magnetometer_xIRI;
    private static String magnetometer_yIRI;
    private static String magnetometer_zIRI;
    private static String bearingIRI;
    private static String speedIRI;
    private static String altitudeIRI;
    private static String pointIRI;
    private static String dbfsIRI;
    private static String relativeBrightnessIRI;
    private static String light_valueIRI;
    private static ArrayList<OffsetDateTime> timesList;
    private static List<List<?>> lolvalues;
    private static final String timeUnit = OffsetDateTime.class.getSimpleName();
    private HashMap iriHashmap = new HashMap();

    private static void readConfig() {
        ResourceBundle config = ResourceBundle.getBundle("config");

        accelDSResolution=Long.valueOf(config.getString("accelDSResolution"));
        accelDSType=Downsampling.Type.valueOf(config.getString("accelDSType"));

        gravityDSResolution=Long.valueOf(config.getString("gravityDSResolution"));
        gravityDSType=Downsampling.Type.valueOf(config.getString("gravityDSType"));

        magnetometerDSResolution= Long.valueOf(config.getString("magnetometerDSResolution"));
        magnetometerDSType=Downsampling.Type.valueOf(config.getString("magnetometerDSType"));

        dbfsDSResolution=Long.valueOf(config.getString("dbfsDSResolution"));
        dbfsDSType=Downsampling.Type.valueOf(config.getString("dbfsDSType"));

        rbDSResolution=Long.valueOf(config.getString("rbDSResolution"));
        rbDSType=Downsampling.Type.valueOf(config.getString("rbDSType"));

        lightValueDSResolution= Long.valueOf(config.getString("lightValueDSResolution"));
        lightValueDSType=Downsampling.Type.valueOf(config.getString("lightValueDSType"));

        timerDelay= Integer.valueOf(config.getString("timerDelay"));
        timerFrequency= Integer.valueOf(config.getString("timerFrequency"));


        EndpointConfig endpointConfig = new EndpointConfig();
        rdbStoreClient = new RemoteRDBStoreClient(endpointConfig.getDburl(), endpointConfig.getDbuser(), endpointConfig.getDbpassword());
        storeClient = new RemoteStoreClient(endpointConfig.getKgurl(), endpointConfig.getKgurl());
        queryClientSPARQL = new QueryClientSPARQL(storeClient);
    }



    public void tsInstantiate() throws Exception {
        smartphoneString=BASEURI+"Smartphone_"+DEVICEID;
        Node smartphoneIRI= NodeFactory.createURI(smartphoneString);
        List tsList = new ArrayList();


        if (!accel_tsList.isEmpty() && !accelList_x.isEmpty() && !accelList_y.isEmpty() && !accelList_z.isEmpty()){
        //Accelerometer
        List <String> dataIRIList = new ArrayList<>();
        accel_xIRI=queryClientSPARQL.getIRIfromJSONarray(queryClientSPARQL.getAccel_xIRIArray(smartphoneIRI));
        accel_yIRI=queryClientSPARQL.getIRIfromJSONarray(queryClientSPARQL.getAccel_yIRIArray(smartphoneIRI));
        accel_zIRI=queryClientSPARQL.getIRIfromJSONarray(queryClientSPARQL.getAccel_zIRIArray(smartphoneIRI));
        dataIRIList.add(accel_xIRI);
        dataIRIList.add(accel_yIRI);
        dataIRIList.add(accel_zIRI);


        accel_lolValues= Arrays.asList(accelList_x,accelList_y,accelList_z);
        timesList= accel_tsList;
        lolvalues=accel_lolValues;

        //Reset List by declaring empty list
        accel_tsList =  new ArrayList<>();
        accelList_x =  new ArrayList<>();
        accelList_y=  new ArrayList<>();
        accelList_z=  new ArrayList<>();
        accel_lolValues= new ArrayList<>();

        TimeSeries accelTS = new TimeSeries(timesList, dataIRIList, lolvalues);

        //Carry out downsampling here
        accelTS = Downsampling.downsampleTS(accelTS,accelDSResolution, accelDSType);
        

        tsList.add(accelTS);
        }

        //GravitySensor
        if (!gravity_tsList.isEmpty() && !gravityList_x.isEmpty() && !gravityList_y.isEmpty() && !gravityList_z.isEmpty()){
        List <String> dataIRIList = new ArrayList<>();
        gravity_xIRI=queryClientSPARQL.getIRIfromJSONarray(queryClientSPARQL.getGravity_xIRIArray(smartphoneIRI));
        gravity_yIRI=queryClientSPARQL.getIRIfromJSONarray(queryClientSPARQL.getGravity_yIRIArray(smartphoneIRI));
        gravity_zIRI=queryClientSPARQL.getIRIfromJSONarray(queryClientSPARQL.getGravity_zIRIArray(smartphoneIRI));
        dataIRIList.add(gravity_xIRI);
        dataIRIList.add(gravity_yIRI);
        dataIRIList.add(gravity_zIRI);

        gravity_lolValues= Arrays.asList(gravityList_x,gravityList_y,gravityList_z);
        timesList= gravity_tsList;
        lolvalues= gravity_lolValues;

        //Reset List
        gravity_tsList =  new ArrayList<>();
        gravityList_x =  new ArrayList<>();
        gravityList_y=  new ArrayList<>();
        gravityList_z=  new ArrayList<>();
        gravity_lolValues= new ArrayList<>();


        TimeSeries gravityTS = new TimeSeries(timesList, dataIRIList, lolvalues);

        //Carry out downsampling here
        gravityTS = Downsampling.downsampleTS(gravityTS,gravityDSResolution, gravityDSType);

        tsList.add(gravityTS);
        }
        

        //Magnetometer
        if (!magnetometer_tsList.isEmpty() && !magnetometerList_x.isEmpty() && !magnetometerList_y.isEmpty() && !magnetometerList_z.isEmpty()) {
            List<String> dataIRIList = new ArrayList<>();
            magnetometer_xIRI = queryClientSPARQL.getIRIfromJSONarray(queryClientSPARQL.getMagnetometer_xIRIArray(smartphoneIRI));
            magnetometer_yIRI = queryClientSPARQL.getIRIfromJSONarray(queryClientSPARQL.getMagnetometer_yIRIArray(smartphoneIRI));
            magnetometer_zIRI = queryClientSPARQL.getIRIfromJSONarray(queryClientSPARQL.getMagnetometer_zIRIArray(smartphoneIRI));
            dataIRIList.add(magnetometer_xIRI);
            dataIRIList.add(magnetometer_yIRI);
            dataIRIList.add(magnetometer_zIRI);

            magnetometer_lolValues = Arrays.asList(magnetometerList_x, magnetometerList_y, magnetometerList_z);
            timesList = magnetometer_tsList;
            lolvalues = magnetometer_lolValues;

            //Reset List
            magnetometer_tsList = new ArrayList<>();
            magnetometerList_x = new ArrayList<>();
            magnetometerList_y = new ArrayList<>();
            magnetometerList_z = new ArrayList<>();
            magnetometer_lolValues = new ArrayList<>();

            TimeSeries magnetometerTS = new TimeSeries(timesList, dataIRIList, lolvalues);

            //Carry out downsampling here
            magnetometerTS = Downsampling.downsampleTS(magnetometerTS, magnetometerDSResolution, magnetometerDSType);

            tsList.add(magnetometerTS);
        }


        //GPSDevice
        if (!location_tsList.isEmpty() && !bearingList.isEmpty() && !speedList.isEmpty() && !altitudeList.isEmpty() && !geomLocationList.isEmpty()) {
            List<String> dataIRIList = new ArrayList<>();
            bearingIRI = queryClientSPARQL.getIRIfromJSONarray(queryClientSPARQL.getBearingIRIArray(smartphoneIRI));
            speedIRI = queryClientSPARQL.getIRIfromJSONarray(queryClientSPARQL.getSpeedIRIArray(smartphoneIRI));
            altitudeIRI = queryClientSPARQL.getIRIfromJSONarray(queryClientSPARQL.getAltitudeIRIArray(smartphoneIRI));
            pointIRI = queryClientSPARQL.getIRIfromJSONarray(queryClientSPARQL.getPointIRIArray(smartphoneIRI));
            dataIRIList.add(bearingIRI);
            dataIRIList.add(speedIRI);
            dataIRIList.add(altitudeIRI);
            dataIRIList.add(pointIRI);

            location_lolValues = Arrays.asList(bearingList, speedList, altitudeList, geomLocationList);
            timesList = location_tsList;
            lolvalues = location_lolValues;

            //Reset List
            location_tsList = new ArrayList<>();
            bearingList = new ArrayList<>();
            speedList = new ArrayList<>();
            altitudeList = new ArrayList<>();
            geomLocationList = new ArrayList<>();
            location_lolValues = new ArrayList<>();

            TimeSeries locationTS = new TimeSeries(timesList, dataIRIList, lolvalues);

            //Downsampling is not applicable to locationTS

            tsList.add(locationTS);
        }
        

        //Microphone
        if (!dBFS_tsList.isEmpty() && !dBFSList.isEmpty()) {
            List<String> dataIRIList = new ArrayList<>();
            dbfsIRI = queryClientSPARQL.getIRIfromJSONarray(queryClientSPARQL.getSoundPressureLevelIRIArray(smartphoneIRI));
            dataIRIList.add(dbfsIRI);

            dBFS_lolValues = Arrays.asList(dBFSList);
            timesList = dBFS_tsList;
            lolvalues = dBFS_lolValues;

            //Reset List
            dBFS_tsList = new ArrayList<>();
            dBFSList = new ArrayList<>();
            dBFS_lolValues = new ArrayList<>();

            TimeSeries dbfsTS = new TimeSeries(timesList, dataIRIList, lolvalues);
            dbfsTS = Downsampling.downsampleTS(dbfsTS, dbfsDSResolution, dbfsDSType);

            tsList.add(dbfsTS);
        }

        //RelativeBrightness
        if (!brightnessList.isEmpty() && !brightness_tsList.isEmpty()) {
            List<String> dataIRIList = new ArrayList<>();
            relativeBrightnessIRI = queryClientSPARQL.getIRIfromJSONarray(queryClientSPARQL.getRelativeBrightnessIRIArray(smartphoneIRI));
            dataIRIList.add(relativeBrightnessIRI);

            brightness_lolValues = Arrays.asList(brightnessList);
            timesList = brightness_tsList;
            lolvalues = brightness_lolValues;
            //Reset List
            brightness_tsList = new ArrayList<>();
            brightnessList = new ArrayList<>();
            brightness_lolValues = new ArrayList<>();

            TimeSeries relativeBrightnessTS = new TimeSeries(timesList, dataIRIList, lolvalues);

            //Carry out downsampling here
            relativeBrightnessTS = Downsampling.downsampleTS(relativeBrightnessTS, rbDSResolution, rbDSType);


            tsList.add(relativeBrightnessTS);
        }

        //Camera
        if (!lightValueList.isEmpty() && !lightValue_tsList.isEmpty()) {
            List<String> dataIRIList = new ArrayList<>();
            light_valueIRI = queryClientSPARQL.getIRIfromJSONarray(queryClientSPARQL.getIlluminanceIRIArray(smartphoneIRI));
            dataIRIList.add(light_valueIRI);

            lightValue_lolValues = Arrays.asList(lightValueList);
            timesList = lightValue_tsList;
            lolvalues = lightValue_lolValues;

            //Reset list
            lightValue_tsList = new ArrayList<>();
            lightValueList = new ArrayList<>();
            lightValue_lolValues = new ArrayList<>();

            TimeSeries lightValueTS = new TimeSeries(timesList, dataIRIList, lolvalues);

            //Carry out downsampling here
            lightValueTS = Downsampling.downsampleTS(lightValueTS, lightValueDSResolution, lightValueDSType);

            tsList.add(lightValueTS);
        }

        if (!tsList.isEmpty()){
            System.out.println(tsList.size()+" timeseries data has been added for device:"+DEVICEID);
            bulkAddTimeSeriesData(tsList);
        }else if (tsList.isEmpty()){
            System.out.println("No data has been provieded, therefore non added for device:"+DEVICEID);
        }else{
            System.out.println("Error, timeseries data has not been added for device:"+DEVICEID);
        }
    }


    private void initSensorTimeseries(){
        smartphoneString=BASEURI+"Smartphone_"+DEVICEID;
        Node smartphoneIRI= NodeFactory.createURI(smartphoneString);

        //Accelerometer
        if(queryClientSPARQL.getAccel_xIRIArray(smartphoneIRI).isEmpty() && queryClientSPARQL.getAccel_yIRIArray(smartphoneIRI).isEmpty() && queryClientSPARQL.getAccel_zIRIArray(smartphoneIRI).isEmpty()) {
            List<String> dataIRIList = new ArrayList<>();
            accel_xIRI = "https://www.theworldavatar.com/kg/sensorloggerapp/measure_accel_x_" + UUID.randomUUID();
            accel_yIRI = "https://www.theworldavatar.com/kg/sensorloggerapp/measure_accel_y_" + UUID.randomUUID();
            accel_zIRI = "https://www.theworldavatar.com/kg/sensorloggerapp/measure_accel_z_" + UUID.randomUUID();

            dataIRIList.add(accel_xIRI);
            dataIRIList.add(accel_yIRI);
            dataIRIList.add(accel_zIRI);
            iriHashmap.put("accel_x", accel_xIRI);
            iriHashmap.put("accel_y", accel_yIRI);
            iriHashmap.put("accel_z", accel_zIRI);

            List<Class> dataClass = Collections.nCopies(dataIRIList.size(), Double.class);
            initTimeSeries(dataIRIList, dataClass, timeUnit);
        }

        //GravitySensor
        if(queryClientSPARQL.getGravity_xIRIArray(smartphoneIRI).isEmpty() && queryClientSPARQL.getGravity_yIRIArray(smartphoneIRI).isEmpty() && queryClientSPARQL.getGravity_zIRIArray(smartphoneIRI).isEmpty())
        {
            List<String> dataIRIList = new ArrayList<>();
            gravity_xIRI ="https://www.theworldavatar.com/kg/sensorloggerapp/measure_gravity_x_"+ UUID.randomUUID();
            gravity_yIRI ="https://www.theworldavatar.com/kg/sensorloggerapp/measure_gravity_y_"+ UUID.randomUUID();
            gravity_zIRI ="https://www.theworldavatar.com/kg/sensorloggerapp/measure_gravity_z_"+ UUID.randomUUID();

            dataIRIList.add(gravity_xIRI);
            dataIRIList.add(gravity_yIRI);
            dataIRIList.add(gravity_zIRI);
            iriHashmap.put("gravity_x", gravity_xIRI);
            iriHashmap.put("gravity_y", gravity_yIRI);
            iriHashmap.put("gravity_z", gravity_zIRI);


            List<Class> dataClass = Collections.nCopies(dataIRIList.size(),Double.class);
            initTimeSeries(dataIRIList,dataClass,timeUnit);
        }

        //Magnetometer
        if(queryClientSPARQL.getMagnetometer_xIRIArray(smartphoneIRI).isEmpty() && queryClientSPARQL.getMagnetometer_yIRIArray(smartphoneIRI).isEmpty() && queryClientSPARQL.getMagnetometer_zIRIArray(smartphoneIRI).isEmpty())
        {
            List<String> dataIRIList = new ArrayList<>();
            magnetometer_xIRI ="https://www.theworldavatar.com/kg/sensorloggerapp/measure_magnetometer_x_"+ UUID.randomUUID();
            magnetometer_yIRI ="https://www.theworldavatar.com/kg/sensorloggerapp/measure_magnetometer_y_"+ UUID.randomUUID();
            magnetometer_zIRI ="https://www.theworldavatar.com/kg/sensorloggerapp/measure_magnetometer_z_"+ UUID.randomUUID();

            dataIRIList.add(magnetometer_xIRI);
            dataIRIList.add(magnetometer_yIRI);
            dataIRIList.add(magnetometer_zIRI);
            iriHashmap.put("magnetometer_x", magnetometer_xIRI);
            iriHashmap.put("magnetometer_y", magnetometer_yIRI);
            iriHashmap.put("magnetometer_z", magnetometer_zIRI);

            List<Class> dataClass = Collections.nCopies(dataIRIList.size(),Double.class);
            initTimeSeries(dataIRIList,dataClass,timeUnit);
        }

        //GPSDevice
        if(queryClientSPARQL.getBearingIRIArray(smartphoneIRI).isEmpty() && queryClientSPARQL.getSpeedIRIArray(smartphoneIRI).isEmpty() && queryClientSPARQL.getAltitudeIRIArray(smartphoneIRI).isEmpty() && queryClientSPARQL.getPointIRIArray(smartphoneIRI).isEmpty())
        {
            List<String> dataIRIList = new ArrayList<>();
            bearingIRI ="https://www.theworldavatar.com/kg/sensorloggerapp/measure_bearing_"+ UUID.randomUUID();
            speedIRI ="https://www.theworldavatar.com/kg/sensorloggerapp/measure_speed_"+ UUID.randomUUID();
            altitudeIRI ="https://www.theworldavatar.com/kg/sensorloggerapp/measure_altitude_"+ UUID.randomUUID();
            pointIRI="https://www.theworldavatar.com/kg/sensorloggerapp/point_"+ UUID.randomUUID();
            dataIRIList.add(bearingIRI);
            dataIRIList.add(speedIRI);
            dataIRIList.add(altitudeIRI);
            dataIRIList.add(pointIRI);
            iriHashmap.put("bearing", bearingIRI);
            iriHashmap.put("speed", speedIRI);
            iriHashmap.put("altitude", altitudeIRI);
            iriHashmap.put("point",pointIRI);

            List<Class> dataClass = Collections.nCopies(dataIRIList.size()-1,Double.class);
            List <Class> dataClassWithPoint= new ArrayList<>(dataClass);
            dataClassWithPoint.add(Point.class);
            initTimeSeries(dataIRIList,dataClassWithPoint,timeUnit);
        }
        
        //Microphone
        if(queryClientSPARQL.getSoundPressureLevelIRIArray(smartphoneIRI).isEmpty())
        {
            List<String> dataIRIList = new ArrayList<>();
            dbfsIRI ="https://www.theworldavatar.com/kg/sensorloggerapp/measure_dbfs_"+ UUID.randomUUID();
            dataIRIList.add(dbfsIRI);
            iriHashmap.put("dbfs", dbfsIRI);

            List<Class> dataClass = Collections.nCopies(dataIRIList.size(),Double.class);
            initTimeSeries(dataIRIList,dataClass,timeUnit);
        }
        
        //RelativeBrightness
        if(queryClientSPARQL.getRelativeBrightnessIRIArray(smartphoneIRI).isEmpty())
        {
            List<String> dataIRIList = new ArrayList<>();
            relativeBrightnessIRI ="https://www.theworldavatar.com/kg/sensorloggerapp/relativeBrightness_"+ UUID.randomUUID();
            dataIRIList.add(relativeBrightnessIRI);
            iriHashmap.put("relativeBrightness", relativeBrightnessIRI);

            List<Class> dataClass = Collections.nCopies(dataIRIList.size(),Double.class);
            initTimeSeries(dataIRIList,dataClass,timeUnit);
        }

        //Camera
        if(queryClientSPARQL.getIlluminanceIRIArray(smartphoneIRI).isEmpty())
        {
            List<String> dataIRIList = new ArrayList<>();
            light_valueIRI ="https://www.theworldavatar.com/kg/sensorloggerapp/light_value_"+ UUID.randomUUID();
            dataIRIList.add(light_valueIRI);
            iriHashmap.put("light_value", light_valueIRI);

            List<Class> dataClass = Collections.nCopies(dataIRIList.size(),Double.class);
            initTimeSeries(dataIRIList,dataClass,timeUnit);
        }
        
        if (iriHashmap.size()==maxSize){
            iriHashmap.put("deviceID", DEVICEID);
            InstantiationClient.instantiationMethod(iriHashmap);
            LOGGER.info(String.format("Units is now instantiated"));
        }
        else if (iriHashmap.size()!=maxSize)
        { LOGGER.debug(String.format("Not all measuresIRIs are collected, InstantiationClient did not run"));}
        else
        {LOGGER.debug(String.format("Static relations was not instantiated"));}
    }

    private void bulkAddTimeSeriesData(List<TimeSeries> tsList){
        try (Connection conn = rdbStoreClient.getConnection()) {
            TimeSeriesClient tsClient = new TimeSeriesClient(storeClient, OffsetDateTime.class);
            tsClient.bulkaddTimeSeriesData(tsList, conn);
        } catch (Exception e) {
            e.printStackTrace();
            throw new JPSRuntimeException(e);
        }
    }

    private void initTimeSeries(List<String> dataIRIList, List dataClass, String timeUnit){

        try (Connection conn = rdbStoreClient.getConnection()) {
            TimeSeriesClient tsClient = new TimeSeriesClient(storeClient, OffsetDateTime.class);
            tsClient.initTimeSeries(dataIRIList, dataClass, timeUnit, conn);
        } catch (Exception e) {
            e.printStackTrace();
            throw new JPSRuntimeException(e);
        }
    }
}