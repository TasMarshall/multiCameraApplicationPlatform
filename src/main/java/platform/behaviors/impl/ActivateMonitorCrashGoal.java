package platform.behaviors.impl;

import platform.behaviors.CameraMAPEBehavior;
import platform.camera.Camera;
import platform.goals.MultiCameraGoal;
import platform.imageAnalysis.impl.outputObjects.ObjectLocations;
import platform.map.LocalMap;
import platform.map.SimpleMapConfig;

import platform.jade.utilities.CommunicationAction;

import java.io.Serializable;
import java.util.Map;

public class ActivateMonitorCrashGoal extends CameraMAPEBehavior {

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

        //if there is a result...
        Map<String, Serializable> result = multiCameraGoal.getNewAnalysisResultsMap().get(camera.getIdAsString());
        if (result != null) {

            ObjectLocations objectLocations = (ObjectLocations) result.get("blueObjectLocations");
            if (objectLocations != null) {

                MultiCameraGoal monitorCrashGoal = multiCameraGoal.getMcp_application().getGoalById("monitorCrash");
                if (monitorCrashGoal != null) {

                    if (!monitorCrashGoal.isActivated()) {
                        System.out.println("Monitor crash goal has been activated by camera " + camera.getIdAsString());
                        monitorCrashGoal.setActivated(true);

                        if (monitorCrashGoal.getCameraRequirements().checkBaseRequirements(multiCameraGoal,camera)) {
                            monitorCrashGoal.getCameras().add(camera);
                            camera.getMultiCameraGoalList().add(monitorCrashGoal);
                        }

                        if (camera.getAdditionalAttributes().containsKey("mapFeature_Road")){
                            java.lang.Object o = camera.getAdditionalAttributes().get("mapFeature_Road");
                            if(o != null){
                                SimpleMapConfig s = (SimpleMapConfig)o;
                                LocalMap localMap = new LocalMap(s.getCoordinateSys(),s.getSwLong(),s.getSwLat(),s.getNeLong(),s.getNeLat());

                                for (Camera camera1: multiCameraGoal.getMcp_application().getAllCameras()){
                                    if (camera1.inRange(localMap)) {
                                        System.out.println("Monitor crash goal has added to the following in range camera " + camera1.getIdAsString());
                                        if (monitorCrashGoal.getCameraRequirements().checkBaseRequirements(monitorCrashGoal,camera1)) {
                                            if (!monitorCrashGoal.getCameras().contains(camera1)) {
                                                monitorCrashGoal.getCameras().add(camera1);
                                            }
                                            if (!camera1.getMultiCameraGoalList().contains(monitorCrashGoal)) {
                                                camera1.getMultiCameraGoalList().add(monitorCrashGoal);
                                            }
                                        }
                                    }
                                }

                            }
                        }

                        result.remove("blueObjectLocations");

                    }

                }
                else {
                    // if not paired do nothing
                    System.out.println("MAPE behaviour, " + this.getClass().toString() + " could not be completed for camera " + camera.getIdAsString() + " due no pairing information.");
                }

            }
        }

        return null;
    }

    @Override
    public CommunicationAction execute(Camera camera, MultiCameraGoal multiCameraGoal) {
        return null;
    }

    @Override
    public void init() {

    }
}
