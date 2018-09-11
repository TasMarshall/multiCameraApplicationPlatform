package platform.behaviors.impl;

import platform.behaviors.CameraMAPEBehavior;
import platform.camera.Camera;
import platform.goals.MultiCameraGoal;
import platform.imageAnalysis.impl.outputObjects.ObjectLocations;
import platform.jade.ModelAgent;
import platform.jade.utilities.CommunicationAction;
import platform.map.LocalMap;
import platform.map.SimpleMapConfig;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActivateAlarm extends CameraMAPEBehavior {

    private final static Logger LOGGER = Logger.getLogger(ActivateAlarm.class.getName());

    boolean alarmActivated = false;

    @Override
    public CommunicationAction plan(Camera camera, MultiCameraGoal multiCameraGoal) {
        return null;
    }

    @Override
    public CommunicationAction analyse(Camera camera, MultiCameraGoal multiCameraGoal) {
        return null;
    }

    @Override
    public CommunicationAction monitor(Camera camera, MultiCameraGoal multiCameraGoal) {

        if (getCameraBehaviorInfoMap().get(camera) == null) {
            getCameraBehaviorInfoMap().put(camera, new HashMap<String, Object>());
        }

        HashMap<String, java.lang.Object> cameraMap = ((HashMap<String, java.lang.Object>) getCameraBehaviorInfoMap().get(camera));


        //if there is a result...
        Map<String, Serializable> result = multiCameraGoal.getNewAnalysisResultsMap().get(camera.getIdAsString());
        if (result != null) {

            ObjectLocations objectLocations = (ObjectLocations) result.get("redObjectLocations");
            if (objectLocations != null) {

                cameraMap.put("lastDetectionTime", System.currentTimeMillis());

                MultiCameraGoal monitorEntry = multiCameraGoal.getMcp_application().getGoalById("monitorEntry");
                if (monitorEntry != null) {

                    if (!monitorEntry.isActivated()) {
                        activateAlarm(camera, multiCameraGoal, monitorEntry);
                        result.remove("redObjectLocations");
                        cameraMap.remove("lastDetectionTime");
                    }

                }
                else {
                    // if not paired do nothing
                    LOGGER.fine("MAPE behaviour, " + this.getClass().toString() + " could not be completed for camera " + camera.getIdAsString() + " due no map feature information.");
                }
            }
        }
        else if (alarmActivated){

            java.lang.Object lasttime = cameraMap.get("lastDetectionTime");
            if (lasttime == null) {
                //if there is no record of a last time assign one so we dont switch too fast and so that an assessment can be made next time.
                cameraMap.put("lastDetectionTime", System.currentTimeMillis());

            } else if ((System.currentTimeMillis() - (long) lasttime) > 60000) {
                //object is lost, default object lost to true if no other camera has seen the object

                Map<String, Serializable> ob = multiCameraGoal.getProcessedInfoMap().get(camera);
                if (ob == null){
                    multiCameraGoal.getProcessedInfoMap().put(camera,new HashMap<>());
                    ob = multiCameraGoal.getProcessedInfoMap().get(camera);
                }

                ob.put("cameraLostRedObject", new Boolean(true));
                alarmActivated = false;

                LOGGER.info("Red Object Lost from camera " + camera.getIdAsString() + "'s view.");
            }
        }

        return null;
    }

    private void activateAlarm(Camera camera, MultiCameraGoal multiCameraGoal, MultiCameraGoal monitorEntry) {
        LOGGER.info("Alarm has been activated by camera " + camera.getIdAsString());
        monitorEntry.setActivated(true);

        if (camera.getAdditionalAttributes().containsKey("mapFeature_Road")){
            Object o = camera.getAdditionalAttributes().get("mapFeature_Road");
            if(o != null){
                SimpleMapConfig s = (SimpleMapConfig)o;
                LocalMap localMap = new LocalMap(s.getCoordinateSys(),s.getSwLong(),s.getSwLat(),s.getNeLong(),s.getNeLat());

                for (Camera camera1: multiCameraGoal.getMcp_application().getAllCameras()){
                    if (camera1.inRange(localMap)) {
                        LOGGER.info("Monitor entries goal has added to the following in range camera " + camera1.getIdAsString());
                        alarmActivated = true;
                        if(!monitorEntry.getCameras().contains(camera1)) {
                            monitorEntry.getCameras().add(camera1);
                        }
                        if (!camera1.getMultiCameraGoalList().contains(monitorEntry)) {
                            camera1.getMultiCameraGoalList().add(monitorEntry);
                        }
                    }
                }

            }
        }
    }

    @Override
    public CommunicationAction execute(Camera camera, MultiCameraGoal multiCameraGoal) {
        return null;
    }

    @Override
    public void init() {

        LOGGER.setLevel(Level.FINE);

    }
}
