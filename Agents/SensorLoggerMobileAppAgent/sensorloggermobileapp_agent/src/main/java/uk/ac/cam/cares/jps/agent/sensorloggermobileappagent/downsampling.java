package uk.ac.cam.cares.jps.agent.sensorloggermobileappagent;

import uk.ac.cam.cares.jps.base.timeseries.TimeSeries;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class downsampling {

    /** Instantaneous
     *  Time series where the data is the measurement value at each time step
     */


    public static TimeSeries instantaneous(TimeSeries ts, Long resolution, int type) throws Exception {

        List TSDataIRIS = ts.getDataIRIs();
        List<List<Double>> TSlolValues = new ArrayList<>();

        for (Object TSDataIRI : TSDataIRIS) {
            TSlolValues.add(ts.getValues(TSDataIRI.toString()));
        }
        List<OffsetDateTime> originalTimeList = ts.getTimes();

        List ResampledList;
        ResampledList = instantaneousMethod(originalTimeList, TSlolValues, resolution,type);

        TimeSeries resampledTS = new TimeSeries((List) ResampledList.get(0), (List<String>) TSDataIRIS, (List<List<?>>) ResampledList.get(1));

        return resampledTS;
    }

    public static List instantaneousMethod(List<OffsetDateTime> originalTimeList, List<List<Double>> originalValueLists, long intervalInSeconds, int type) throws Exception {
        List<List<Double>> resampledValueLists = new ArrayList<>();

        //Initiliaze size of resampledValueLists for iterator purpose
        for (int i = 0; i < originalValueLists.size(); i++) {
            resampledValueLists.add(new ArrayList<>());
        }
        List<OffsetDateTime> resampledTimeList = new ArrayList<>();

        OffsetDateTime startTime = originalTimeList.get(0).truncatedTo(ChronoUnit.SECONDS);
        OffsetDateTime endTime = originalTimeList.get(originalTimeList.size() - 1).truncatedTo(ChronoUnit.SECONDS);
        if(startTime==endTime){throw new Exception("The start time and endtime is the same, timeseries is called too often than it can downsample");}

        for (OffsetDateTime currentTime = startTime; currentTime.isBefore(endTime); currentTime = currentTime.plusSeconds(intervalInSeconds)) {
            OffsetDateTime intervalEndTime = currentTime.plusSeconds(intervalInSeconds);

            Iterator<List<Double>> it1 = originalValueLists.iterator();
            Iterator<List<Double>> it2 = resampledValueLists.iterator();

            //Max
            if(type == 1){
                while (it1.hasNext() && it2.hasNext()) {
                    List<Double> originalValueList = it1.next();
                    List<Double> resampledValueList = it2.next();
                    double maxValue = Double.NEGATIVE_INFINITY;
                    for (int i = 0; i < originalTimeList.size(); i++) {
                        if (originalTimeList.get(i).isAfter(intervalEndTime)) {
                            break;
                        }
                        if (originalTimeList.get(i).isAfter(currentTime) && originalValueList.get(i) > maxValue) {
                            maxValue = originalValueList.get(i);
                        }
                    }
                    resampledValueList.add(maxValue);
                }
            }
            //Median
            else if (type == 2){
                    while (it1.hasNext() && it2.hasNext()) {
                    List<Double> originalValueList = it1.next();
                    List<Double> resampledValueList = it2.next();
                    List<Double> valuesInInterval = new ArrayList<>();
                    for (int i = 0; i < originalTimeList.size(); i++) {
                        if (originalTimeList.get(i).isAfter(intervalEndTime)) {
                            break;
                        }
                        if (originalTimeList.get(i).isAfter(currentTime)) {
                            valuesInInterval.add(originalValueList.get(i));
                        }
                    }
                    if (!valuesInInterval.isEmpty()) {
                        Collections.sort(valuesInInterval);
                        double medianValue;
                        int size = valuesInInterval.size();
                        if (size % 2 == 0) {
                            int mid = size / 2;
                            medianValue = (valuesInInterval.get(mid - 1) + valuesInInterval.get(mid)) / 2;
                        } else {
                            medianValue = valuesInInterval.get(size / 2);
                        }
                        resampledValueList.add(medianValue);
                    } else {
                        resampledValueList.add(null);
                    }
                }

            }

            //Min
            else if (type==3 ){
                    while (it1.hasNext() && it2.hasNext()) {
                    List<Double> originalValueList = it1.next();
                    List<Double> resampledValueList = it2.next();
                    double minValue = Double.POSITIVE_INFINITY;  // initialize minValue to positive infinity
                    for (int i = 0; i < originalTimeList.size(); i++) {
                        if (originalTimeList.get(i).isAfter(intervalEndTime)) {
                            break;
                        }
                        if (originalTimeList.get(i).isAfter(currentTime) && originalValueList.get(i) < minValue) {
                            minValue = originalValueList.get(i);  // update minValue
                        }
                    }
                    if (minValue == Double.POSITIVE_INFINITY) {throw new Exception("Something went wrong here");}
                    resampledValueList.add(minValue);  // add minValue to the resampledValueList

                    }
                }
            resampledTimeList.add(currentTime);
        }

        List result = new ArrayList();
        result.add(resampledTimeList);
        result.add(resampledValueLists);

        return result;
    }

//    public static List instantaneousMax(List<OffsetDateTime> originalTimeList, List<List<Double>> originalValueLists, long intervalInSeconds) throws Exception {
//        List<List<Double>> resampledValueLists = new ArrayList<>();
//
//        //Initiliaze size of resampledValueLists for iterator purpose
//        for (int i = 0; i < originalValueLists.size(); i++) {
//            resampledValueLists.add(new ArrayList<>());
//        }
//        List<OffsetDateTime> resampledTimeList = new ArrayList<>();
//
//        OffsetDateTime startTime = originalTimeList.get(0).truncatedTo(ChronoUnit.SECONDS);
//        OffsetDateTime endTime = originalTimeList.get(originalTimeList.size() - 1).truncatedTo(ChronoUnit.SECONDS);
//        if(startTime==endTime){throw new Exception("The start time and endtime is the same, timeseries is called too often than it can downsample");}
//
//        for (OffsetDateTime currentTime = startTime; currentTime.isBefore(endTime); currentTime = currentTime.plusSeconds(intervalInSeconds)) {
//            OffsetDateTime intervalEndTime = currentTime.plusSeconds(intervalInSeconds);
//
//            Iterator<List<Double>> it1 = originalValueLists.iterator();
//            Iterator<List<Double>> it2 = resampledValueLists.iterator();
//
//
//            while (it1.hasNext() && it2.hasNext()) {
//                List<Double> originalValueList = it1.next();
//                List<Double> resampledValueList = it2.next();
//                double maxValue = Double.NEGATIVE_INFINITY;
//                for (int i = 0; i < originalTimeList.size(); i++) {
//                    if (originalTimeList.get(i).isAfter(intervalEndTime)) {
//                        break;
//                    }
//                    if (originalTimeList.get(i).isAfter(currentTime) && originalValueList.get(i) > maxValue) {
//                        maxValue = originalValueList.get(i);
//                    }
//                }
//                resampledValueList.add(maxValue);
//            }
//            resampledTimeList.add(currentTime);
//
//        }
//        List result = new ArrayList();
//        result.add(resampledTimeList);
//        result.add(resampledValueLists);
//
//        return result;
//    }
//
//    public static List instantaneousMedian(List<OffsetDateTime> originalTimeList, List<List<Double>> originalValueLists, long intervalInSeconds) throws Exception {
//        List<List<Double>> resampledValueLists = new ArrayList<>();
//
//        //Initialize size of resampledValueLists for iterator purpose
//        for (int i = 0; i < originalValueLists.size(); i++) {
//            resampledValueLists.add(new ArrayList<>());
//        }
//        List<OffsetDateTime> resampledTimeList = new ArrayList<>();
//
//        OffsetDateTime startTime = originalTimeList.get(0).truncatedTo(ChronoUnit.SECONDS);
//        OffsetDateTime endTime = originalTimeList.get(originalTimeList.size() - 1).truncatedTo(ChronoUnit.SECONDS);
//        if(startTime==endTime){throw new Exception("The start time and endtime is the same, timeseries is called too often than it can downsample");}
//
//        for (OffsetDateTime currentTime = startTime; currentTime.isBefore(endTime); currentTime = currentTime.plusSeconds(intervalInSeconds)) {
//            OffsetDateTime intervalEndTime = currentTime.plusSeconds(intervalInSeconds);
//
//            Iterator<List<Double>> it1 = originalValueLists.iterator();
//            Iterator<List<Double>> it2 = resampledValueLists.iterator();
//
//            while (it1.hasNext() && it2.hasNext()) {
//                List<Double> originalValueList = it1.next();
//                List<Double> resampledValueList = it2.next();
//                List<Double> valuesInInterval = new ArrayList<>();
//                for (int i = 0; i < originalTimeList.size(); i++) {
//                    if (originalTimeList.get(i).isAfter(intervalEndTime)) {
//                        break;
//                    }
//                    if (originalTimeList.get(i).isAfter(currentTime)) {
//                        valuesInInterval.add(originalValueList.get(i));
//                    }
//                }
//                if (!valuesInInterval.isEmpty()) {
//                    Collections.sort(valuesInInterval);
//                    double medianValue;
//                    int size = valuesInInterval.size();
//                    if (size % 2 == 0) {
//                        int mid = size / 2;
//                        medianValue = (valuesInInterval.get(mid - 1) + valuesInInterval.get(mid)) / 2;
//                    } else {
//                        medianValue = valuesInInterval.get(size / 2);
//                    }
//                    resampledValueList.add(medianValue);
//                } else {
//                    resampledValueList.add(null);
//                }
//            }
//            resampledTimeList.add(currentTime);
//        }
//        List result = new ArrayList();
//        result.add(resampledTimeList);
//        result.add(resampledValueLists);
//
//        return result;
//    }


    /** StepwiseCumulative
     *  Time series where data is cumulative between time steps
     */
    public static TimeSeries stepwiseCumulative(TimeSeries ts, Duration timesteps) {

        return ts;
    }


    /**
     *     Time series of running sum
     */
    public static TimeSeries cumulativeTotal(TimeSeries ts) {


        return ts;
    }

    /**
     * Time series where the data is the calculated average
     */
    public static TimeSeries average(TimeSeries ts) {

        return ts;
    }



}