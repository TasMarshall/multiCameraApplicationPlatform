package platform.behaviors.impl;

import platform.behaviors.CameraMAPEBehavior;
import platform.camera.Camera;
import platform.goals.MultiCameraGoal;
import platform.imageAnalysis.impl.outputObjects.ObjectLocation;
import platform.imageAnalysis.impl.outputObjects.ObjectLocations;
import platform.jade.utilities.CommunicationAction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PairedCameraLRBal extends CameraMAPEBehavior {

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

        String paired = (String)camera.getAdditionalAttributes().get("pairedCamera");
        String pairedPosition = (String)camera.getAdditionalAttributes().get("pairedCameraPosition");
        int pairedData = Integer.valueOf((String)camera.getAdditionalAttributes().get("pairedCameraData"));

        if ((paired != null || !paired.equals("")) && (pairedPosition != null || !pairedPosition.equals(""))&& (pairedData != 0)){
            Camera pairedCam = multiCameraGoal.getMcp_application().getCameraManager().getCameraByID(paired);

            if (pairedCam !=null && pairedCam.isWorking() && multiCameraGoal.getActiveCameras().contains(pairedCam)) {

                //if there is a result...
                Map<String, Serializable> result = multiCameraGoal.getNewAnalysisResultsMap().get(camera.getIdAsString());
                if (result != null) {
                    ObjectLocations objectLocations = (ObjectLocations) result.get("objectLocations");
                    if (objectLocations != null) {

                        //if paired and relative position specified act based on position
                        if (pairedPosition.equals("left")) {
                            //if on the left leave only the left most half of expected detected objects, i.e. lower x centroid values

                            objectLocations.getObjectLocationList().sort(Comparator.comparingDouble(ObjectLocation::getX_min));

                            int half = pairedData;

                            List<ObjectLocation> objectLocationList = new ArrayList<>();
                            for (int i = 0; i < objectLocations.getObjectLocationList().size(); i++) {
                                if (i <= half - 1) {
                                    objectLocationList.add(objectLocations.getObjectLocationList().get(i));
                                }
                            }

                            ObjectLocations objectLocationsOut = new ObjectLocations(objectLocationList);
                            multiCameraGoal.getNewAnalysisResultsMap().get(camera.getIdAsString()).put("objectLocations", objectLocationsOut);


                        } else if (pairedPosition.equals("right")) {

                            //if on the right leave only the right most half of expected detected objects
                            objectLocations.getObjectLocationList().sort(Comparator.comparingDouble(ObjectLocation::getX_min).reversed());

                            int half = pairedData;

                            List<ObjectLocation> objectLocationList = new ArrayList<>();
                            for (int i = 0; i < objectLocations.getObjectLocationList().size(); i++) {
                                if (i <= half - 1) {
                                    objectLocationList.add(objectLocations.getObjectLocationList().get(i));
                                }
                            }

                            ObjectLocations objectLocationsOut = new ObjectLocations(objectLocationList);
                            multiCameraGoal.getNewAnalysisResultsMap().get(camera.getIdAsString()).put("objectLocations", objectLocationsOut);

                        }

                    }

                }

            }


        }
        else {
            // if not paired do nothing
            System.out.println("MAPE behaviour, " + this.getClass().toString() + " could not be completed for camera " + camera.getIdAsString() + " due no pairing information.");
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
