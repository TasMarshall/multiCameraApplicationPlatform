package platform.behaviors.impl;

import platform.camera.components.PTZVector;
import platform.camera.components.Vector1D;
import platform.camera.Camera;
import platform.camera.components.Vector2D;
import platform.goals.MultiCameraGoal;
import platform.imageAnalysis.impl.outputObjects.ImageComparison;
import platform.imageAnalysis.impl.outputObjects.ObjectLocations;
import platform.behaviors.MotionController;
import platform.behaviors.components.PTZCommand;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class CalibratedScanForObject extends MotionController {

    int imageNumber;

    //this stores data for specific cameras as other fields are common between cameras. There is only one of this object in the library
    Map<Camera,BackgroundDataCalibScan> cameraBackgrounds;

    Boolean stateCounterHasReset = false;

    double panTime, tiltTime;

    @Override
    public void init() {
        imageNumber = 0;
        cameraBackgrounds = new HashMap<>();
        stateCounterHasReset = false;

        panTime = 8;
        tiltTime = 1.5;

        LOGGER = Logger.getLogger(CalibratedScanForObject.class.getName());
        LOGGER.setLevel(Level.FINE);

    }

    public PTZCommand calculatePTZVector(MultiCameraGoal multiCameraGoal, Camera camera) {

        double time = 250;

        int counter = 0;
        float similarity = -1;

        //Create a default ptz vector
        PTZVector ptzVector = new PTZVector(0,0,0);

        //Get image similarity from newest image processing results

        //if there is a result
        Map<String,Serializable> result = multiCameraGoal.getNewAnalysisResultsMap().get(camera.getIdAsString());
        if( result != null) {

            //ensure there is background data
            BackgroundDataCalibScan backgroundData = cameraBackgrounds.get(camera);
            if (backgroundData == null) {
                backgroundData = new BackgroundDataCalibScan();

                //read camera configurable values

                //read pan value
                if (camera.getAdditionalAttributes().get("panCalibValue") != null){
                    backgroundData.getCalibPanTime().start(Double.valueOf((String)camera.getAdditionalAttributes().get("panCalibValue")),1);                }
                else {
                    backgroundData.getCalibPanTime().start(panTime,1);
                }

                //read tilt value
                if (camera.getAdditionalAttributes().get("tiltCalibValue") != null){
                    backgroundData.getCalibTiltTime().start(Double.valueOf((String)camera.getAdditionalAttributes().get("tiltCalibValue")),1);                }
                else {
                    backgroundData.getCalibTiltTime().start(tiltTime, 1);
                }
                cameraBackgrounds.put(camera, backgroundData);
            }
            backgroundData.setSnapShotRequired(true);//getting past a bug in this object.

            if (!backgroundData.getFound()){

                ImageComparison imageComparison = (ImageComparison) multiCameraGoal.getNewAnalysisResultsMap().get(camera.getIdAsString()).get("imageComparison");
                if (imageComparison != null) {
                    counter = imageComparison.getCounter();
                    similarity = imageComparison.getSimilarity();
                }

                if (backgroundData.getScanningState() != BackgroundDataCalibScan.ScanningState.WAITING_CAMERA_STREAM_INIT) {

                    if (backgroundData.getScanningState() == BackgroundDataCalibScan.ScanningState.LocatingSWCorner
                            || backgroundData.getScanningState() == BackgroundDataCalibScan.ScanningState.LocatingNECorner) {

                        if (backgroundData.getScanningState() == BackgroundDataCalibScan.ScanningState.LocatingNECorner) {
                            backgroundData.nextState();
                            stateCounterHasReset = false;
                        } else if (backgroundData.getScanningState() == BackgroundDataCalibScan.ScanningState.LocatingSWCorner) {
                            if (counter < 1) {
                                stateCounterHasReset = true;
                            }

                            ptzVector = new PTZVector(-1,-1,-1);

                            if (stateCounterHasReset) {
                                if (counter >= 10) {
                                    backgroundData.nextState();
                                    stateCounterHasReset = false;
                                    backgroundData.getCalibPanTime().resetPulse();
                                }
                            }
                            else if (counter >= 15) {
                                //THIS NUMBER IS DEPENDENT ON THE SPEED OF THE COMPARATOR FUNCTION AND THEREFORE IS PRONE TO ERROR
                                backgroundData.nextState();
                                stateCounterHasReset = false;
                                backgroundData.getCalibPanTime().resetPulse();
                            }

                        }

                    }

                    else {

                        ObjectLocations objectLocations = (ObjectLocations) multiCameraGoal.getNewAnalysisResultsMap().get(camera.getIdAsString()).get("objectLocations");

                        if (objectLocations != null ) {

                            System.out.println(objectLocations.getObjectLocationList().size());

                            if(objectLocations.getObjectLocationList().size() == 3) {

                                if (!camera.getAdditionalAttributes().containsKey("completedGoals") || camera.getAdditionalAttributes().get("completedGoals") == null) {
                                    camera.getAdditionalAttributes().put("completedGoals", new ArrayList<String>());
                                }

                                List<String> completedGoalList = (List<String>) camera.getAdditionalAttributes().get("completedGoals");
                                completedGoalList.add(multiCameraGoal.getId());

                                camera.getAdditionalAttributes().put("completedGoals", completedGoalList);

                                backgroundData.setFound(true);

                                LOGGER.fine("Camera " + camera.getId() + " found 3 roads and set the calibrated scan for object goal to completed.");

                            }

                        }

                        if (backgroundData.getScanningState() == BackgroundDataCalibScan.ScanningState.PanningRight)
                        {

                             ptzVector.getPanTilt().setX(1);


                            if (backgroundData.getCalibPanTime().lookPulse()) {
                                LOGGER.info("Camera: " + camera.getIdAsString() + " has moved the calibrated distance to the right and will now move up.");

                                backgroundData.nextState();
                                stateCounterHasReset = false;
                                backgroundData.getCalibTiltTime().resetPulse();
                            }

                        } else if (backgroundData.getScanningState() == BackgroundDataCalibScan.ScanningState.Tilting) {


                            ptzVector.getPanTilt().setY(1);

                            if (backgroundData.getCalibTiltTime().lookPulse()) {
                                LOGGER.info("Camera: " + camera.getIdAsString() + " has moved the calibrated distance to up and is now in place.");

                                backgroundData.nextState();
                                stateCounterHasReset = false;
                            }

                        }

                        else if (backgroundData.getScanningState() == BackgroundDataCalibScan.ScanningState.PanningLeft)
                        {
                            completeGoal(multiCameraGoal,camera,backgroundData);
                        }
                    }

                } else if (similarity > 0 && similarity < 100) {

                    LOGGER.info("Camera: " + camera.getIdAsString() + " image has been received and calibrated scanner is leaving initialization state.");

                    backgroundData.nextState();
                }

            }

            LOGGER.fine("Camera " + camera.getIdAsString() + " State: " + backgroundData.getScanningState() +" ");

        }


        PTZCommand ptzCommand = new PTZCommand(ptzVector,(int) time);

        return ptzCommand;

    }

    private void completeGoal(MultiCameraGoal multiCameraGoal, Camera camera, BackgroundDataCalibScan backgroundData) {

        if (!camera.getAdditionalAttributes().containsKey("completedGoals") || camera.getAdditionalAttributes().get("completedGoals") == null) {
            camera.getAdditionalAttributes().put("completedGoals", new ArrayList<String>());
        }

        List<String> completedGoalList = (List<String>) camera.getAdditionalAttributes().get("completedGoals");
        completedGoalList.add(multiCameraGoal.getId());

        camera.getAdditionalAttributes().put("completedGoals", completedGoalList);

        backgroundData.setFound(true);

    }

}
