package platform.core.utilities.adaptation.impl;

import org.onvif.ver10.schema.Object;
import platform.core.camera.core.Camera;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.imageAnalysis.impl.outputObjects.ObjectLocation;
import platform.core.imageAnalysis.impl.outputObjects.ObjectLocations;
import platform.core.map.LocalMap;
import platform.core.map.SimpleMapConfig;
import platform.core.utilities.adaptation.core.CameraMAPEBehavior;

import platform.jade.utilities.CommunicationAction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static platform.MapView.distanceInLatLong;

public class ActivateMonitorCrashGoal extends CameraMAPEBehavior{

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
        Map<String, Serializable> result = multiCameraGoal.getNewAnalysisResultMap().get(camera.getIdAsString());
        if (result != null) {

            ObjectLocations objectLocations = (ObjectLocations) result.get("blueObjectLocations");
            if (objectLocations != null) {

                MultiCameraGoal monitorCrashGoal = multiCameraGoal.getMcp_application().getGoalById("monitorCrash");
                if (monitorCrashGoal != null) {

                    if (!monitorCrashGoal.isActivated()) {
                        System.out.println("Monitor crash goal has been activated by camera " + camera.getIdAsString());
                        monitorCrashGoal.setActivated(true);

                        monitorCrashGoal.getCameras().add(camera);
                        camera.getMultiCameraGoalList().add(monitorCrashGoal);

                        if (camera.getAdditionalAttributes().containsKey("mapFeature_Road")){
                            java.lang.Object o = camera.getAdditionalAttributes().get("mapFeature_Road");
                            if(o != null){
                                SimpleMapConfig s = (SimpleMapConfig)o;
                                LocalMap localMap = new LocalMap(s.getCoordinateSys(),s.getSwLong(),s.getSwLat(),s.getNeLong(),s.getNeLat());

                                for (Camera camera1: multiCameraGoal.getMcp_application().getAllCameras()){
                                    if (camera1.inRange(localMap)) {
                                        System.out.println("Monitor crash goal has added to the following in range camera " + camera1.getIdAsString());
                                        if(!monitorCrashGoal.getCameras().contains(camera1)) {
                                            monitorCrashGoal.getCameras().add(camera1);
                                        }
                                        if (!camera1.getMultiCameraGoalList().contains(monitorCrashGoal)) {
                                            camera1.getMultiCameraGoalList().add(monitorCrashGoal);
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
