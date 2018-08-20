package platform.core.utilities.adaptation.impl;

import org.onvif.ver10.schema.PTZVector;
import org.onvif.ver10.schema.Vector1D;
import org.onvif.ver10.schema.Vector2D;
import platform.core.camera.core.Camera;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.imageAnalysis.impl.ImageComparator;
import platform.core.imageAnalysis.impl.outputObjects.ImageComparison;
import platform.core.imageAnalysis.impl.outputObjects.ObjectLocations;
import platform.core.utilities.LoopTimer;
import platform.core.utilities.adaptation.core.AdaptivePolicy;
import platform.core.utilities.adaptation.core.MotionController;
import platform.core.utilities.adaptation.core.components.BackgroundData;
import platform.core.utilities.adaptation.core.components.BackgroundDataCalibScan;
import platform.core.utilities.adaptation.core.components.PTZCommand;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class CalibratedScanForObject extends MotionController implements AdaptivePolicy{

    private final static Logger LOGGER = Logger.getLogger(CalibratedScanForObject.class.getName());

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
        tiltTime = 0.5;

        LOGGER.setLevel(Level.FINE);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.FINE);

        LOGGER.addHandler(handler);
    }

    public PTZCommand calculatePTZVector(MultiCameraGoal multiCameraGoal, Camera camera) {

        double time = 0;

        int counter = 0;
        float similarity = -1;

        PTZVector ptzVector = new PTZVector();
        Vector1D zoom = new Vector1D();
        Vector2D pt = new Vector2D();
        pt.setX(0);
        pt.setY(0);
        zoom.setX(0);

        //Get image similarity from newest image processing results
        Map<String,Serializable> result = multiCameraGoal.getNewAnalysisResultMap().get(camera.getIdAsString());
        if( result != null) {

            //ensure there is background data
            BackgroundDataCalibScan backgroundData = cameraBackgrounds.get(camera);
            if (backgroundData == null) {
                backgroundData = new BackgroundDataCalibScan();

                if (camera.getAdditionalAttributes().get("panCalibValue") != null){
                    backgroundData.getCalibPanTime().start(Double.valueOf((String)camera.getAdditionalAttributes().get("panCalibValue")),1);                }
                else {
                    backgroundData.getCalibPanTime().start(panTime,1);
                }

                backgroundData.getCalibTiltTime().start(tiltTime,1);
                cameraBackgrounds.put(camera, backgroundData);
            }
            backgroundData.setSnapShotRequired(true);//getting past a bug in this object.

            if (!backgroundData.getFound()){

                ImageComparison imageComparison = (ImageComparison) multiCameraGoal.getNewAnalysisResultMap().get(camera.getIdAsString()).get("imageComparison");
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

                            pt.setX(camera.getViewCapabilities().getMinPanViewAngle());
                            pt.setY(camera.getViewCapabilities().getMinTiltViewAngle());
                            zoom.setX(camera.getViewCapabilities().getMinZoom());
                            time = 500;

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

                        ObjectLocations objectLocations = (ObjectLocations) multiCameraGoal.getNewAnalysisResultMap().get(camera.getIdAsString()).get("objectLocations");

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

                            pt.setX(camera.getViewCapabilities().getMaxPanViewAngle());
                            time = 500;

                            if (backgroundData.getCalibPanTime().lookPulse()) {
                                LOGGER.info("Camera: " + camera.getIdAsString() + " has moved the calibrated distance to the right and will now move up.");

                                backgroundData.nextState();
                                stateCounterHasReset = false;
                                backgroundData.getCalibTiltTime().resetPulse();
                            }

                        } else if (backgroundData.getScanningState() == BackgroundDataCalibScan.ScanningState.Tilting) {

                            pt.setY(camera.getViewCapabilities().getMaxTiltViewAngle());
                            time = 500;

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

        }

        ptzVector.setPanTilt(pt);
        ptzVector.setZoom(zoom);

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
