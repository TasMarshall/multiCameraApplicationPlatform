package platform.behaviors.impl;

import platform.behaviors.ContMotionController;
import platform.behaviors.components.PTZCommand;
import platform.camera.Camera;
import platform.camera.components.PTZVector;
import platform.camera.components.Vector1D;
import platform.camera.components.Vector2D;
import platform.goals.MultiCameraGoal;
import platform.imageAnalysis.impl.outputObjects.ObjLocBounds;
import platform.imageAnalysis.impl.outputObjects.ObjectLocations;
import platform.utilities.LoopTimer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObserveEntry extends ContMotionController  {

    private final static Logger LOGGER = Logger.getLogger(ObserveEntry.class.getName());

    Map<Camera,ObserveEntryStateMachine> stateMachineMap;
    Map<Camera,BackgroundDataCalibScan> cameraBackgrounds;

    double panTime, tiltTime;

    public ObserveEntry(){

    }

    public PTZCommand calculatePTZVector(MultiCameraGoal multiCameraGoal, Camera camera) {



        long start = System.currentTimeMillis();

        String debug = "" + camera.getIdAsString();

        PTZVector ptzVector = new PTZVector(0,0,0);

        if (multiCameraGoal.isActivated()) {
            //guard
            ObserveEntryStateMachine stateMachine = stateMachineMap.get(camera);
            if (stateMachine == null) {
                stateMachine = new ObserveEntryStateMachine(LOGGER);
                stateMachineMap.put(camera, stateMachine);
            }

            Map<String, Serializable> map = multiCameraGoal.getProcessedInfoMap().get(camera);
            if (map == null) {
                map = new HashMap<>();
                multiCameraGoal.getProcessedInfoMap().put(camera, map);
            }

            //ensure there is background data
            BackgroundDataCalibScan backgroundData = cameraBackgrounds.get(camera);
            if (backgroundData == null) {
                backgroundData = new BackgroundDataCalibScan();

                //read camera configurable values
                //read pan value
                Double panVal = Double.valueOf((String) camera.getAdditionalAttributes().get("pan2Entry"));
                if (panVal != null) {
                    backgroundData.getCalibPanTime().start(panVal, 1);
                } else {
                    backgroundData.getCalibPanTime().start(panTime, 1);
                }

                //read tilt value
                Double tiltVal = Double.valueOf((String) camera.getAdditionalAttributes().get("tilt2Entry"));
                if (tiltVal != null) {
                    backgroundData.getCalibTiltTime().start(tiltVal, 1);
                } else {
                    backgroundData.getCalibTiltTime().start(tiltTime, 1);
                }
                cameraBackgrounds.put(camera, backgroundData);
                backgroundData.setSnapShotRequired(true);
                backgroundData.setFound(false);
            }

            if (stateMachine.getScanningState() == ObserveEntryStateMachine.ScanningState.OrientateOnEntrys) {

                debug += " OrientateOnEntry,";

                //time based calibrated movement

                if (!backgroundData.getCalibPanTime().lookPulse()) {
                    String s = (String) camera.getAdditionalAttributes().get("EntryDirection");
                    if (s != null) {
                        if (s.equals("right")) {
                            ptzVector.getPanTilt().setX(0.7F);
                        } else if (s.equals("left")) {
                            ptzVector.getPanTilt().setX(-0.7F);
                        }
                    }
                }

                if (!backgroundData.getCalibTiltTime().lookPulse()) {
                    ptzVector.getPanTilt().setY(0.5F);
                }

                if (backgroundData.getCalibPanTime().lookPulse() && backgroundData.getCalibTiltTime().lookPulse()) {
                    LOGGER.info("Camera: " + camera.getIdAsString() + " has moved the calibrated distance.");
                    stateMachine.nextState();
                }

            } else if (stateMachine.getScanningState() == ObserveEntryStateMachine.ScanningState.FocusOnEntry) {

                //centre on red object
                Map s = multiCameraGoal.getNewAnalysisResultsMap().get(camera.getIdAsString());
                if (s != null) {
                    ObjectLocations objectLocations = (ObjectLocations) s.get("redObjectLocations");
                    ObjLocBounds objectLocation = null;

                    if (objectLocations != null) {
                        objectLocation = (ObjLocBounds) objectLocations.getExtremesLocation();
                    }

                    if (objectLocation != null) {

                        float moveY = 0.5F - objectLocation.getY_centroid();

                        if (Math.abs(moveY) > 0.1) {

                            if (moveY > 0) {
                                if (camera.getCameraOrientation().getRoll() == 180) {
                                    ptzVector.getPanTilt().setY(-0.3F);
                                } else {
                                    ptzVector.getPanTilt().setY(0.3F); // this moves correctly with upright cameras
                                }

                            } else {
                                //System.out.println(camera.getIdAsString() +" down.  data is old by: " + (System.currentTimeMillis() - objectLocation.getCreationTime()));
                                //move down
                                if (camera.getCameraOrientation().getRoll() == 180) {
                                    ptzVector.getPanTilt().setY(0.3F);
                                } else {
                                    ptzVector.getPanTilt().setY(-0.3F); // this moves correctly with upright cameras
                                }
                            }
                        }

                        float moveX = 0.5F - objectLocation.getX_centroid();

                        if (Math.abs(moveX) > 0.1) {
                            //add x movement to command
                            if (moveX > 0) {
                                //System.out.println("move to the left. data is old by: " + (System.currentTimeMillis() - objectLocation.getCreationTime()));
                                ptzVector.getPanTilt().setX((float) -0.3F);
                            } else {
                                //System.out.println("move to the right. data is old by: " + (System.currentTimeMillis() - objectLocation.getCreationTime()));
                                ptzVector.getPanTilt().setX((float) 0.3F);
                            }

                        }

                    } else {
                        objectLocations = (ObjectLocations) s.get("whiteObjectLocations");
                        objectLocation = null;

                        if (objectLocations != null) {
                            objectLocation = (ObjLocBounds) objectLocations.getExtremesLocation();
                        }

                        if (objectLocation != null) {

                            float moveY = 0.5F - objectLocation.getY_centroid();

                            if (Math.abs(moveY) > 0.1) {

                                if (moveY > 0) {
                                    if (camera.getCameraOrientation().getRoll() == 180) {
                                        ptzVector.getPanTilt().setY(-0.3F);
                                    } else {
                                        ptzVector.getPanTilt().setY(0.3F); // this moves correctly with upright cameras
                                    }

                                } else {
                                    //System.out.println(camera.getIdAsString() +" down.  data is old by: " + (System.currentTimeMillis() - objectLocation.getCreationTime()));
                                    //move down
                                    if (camera.getCameraOrientation().getRoll() == 180) {
                                        ptzVector.getPanTilt().setY(0.3F);
                                    } else {
                                        ptzVector.getPanTilt().setY(-0.3F); // this moves correctly with upright cameras
                                    }
                                }
                            }

                            float moveX = 0.5F - objectLocation.getX_centroid();

                            if (Math.abs(moveX) > 0.1) {
                                //add x movement to command
                                if (moveX > 0) {
                                    //System.out.println("move to the left. data is old by: " + (System.currentTimeMillis() - objectLocation.getCreationTime()));
                                    ptzVector.getPanTilt().setX((float) -0.3F);
                                } else {
                                    //System.out.println("move to the right. data is old by: " + (System.currentTimeMillis() - objectLocation.getCreationTime()));
                                    ptzVector.getPanTilt().setX((float) 0.3F);
                                }
                            }
                        }
                    }
                }

                if (multiCameraGoal.getAdditionalFieldMap().containsKey("stopMonitorEntry")) {
                    stateMachine.nextState();
                    backgroundData.getCalibTiltTime().resetPulse();
                    backgroundData.getCalibPanTime().resetPulse();
                }


            } else if (stateMachine.getScanningState() == ObserveEntryStateMachine.ScanningState.ReturnToRoad) {

                //time based calibrated movement
                if (!backgroundData.getCalibPanTime().lookPulse()) {
                    String s = (String) camera.getAdditionalAttributes().get("EntryDirection");
                    if (s != null) {
                        if (s.equals("right")) {
                            ptzVector.getPanTilt().setX(-0.8F);
                        } else if (s.equals("left")) {
                            ptzVector.getPanTilt().setX(0.8F);
                        }
                    }
                }

                if (!backgroundData.getCalibTiltTime().lookPulse()) {
                    ptzVector.getPanTilt().setY(-0.5F);
                }

                if (backgroundData.getCalibPanTime().lookPulse() && backgroundData.getCalibTiltTime().lookPulse()) {
                    LOGGER.info("Camera: " + camera.getIdAsString() + " has moved the calibrated distance.");
                    stateMachine.nextState();
                }

            } else if (stateMachine.getScanningState() == ObserveEntryStateMachine.ScanningState.Exit) {

                multiCameraGoal.getProcessedInfoMap().get(camera).put("stopMonitorEntry", "");

                boolean allCamerasFinished = true;

                for (Camera c : multiCameraGoal.getActiveCameras()) {
                    if (!multiCameraGoal.getProcessedInfoMap().get(camera).containsKey("stopMonitorEntry")) {
                        allCamerasFinished = false;
                    }
                }

                if (allCamerasFinished) {
                    for (Camera c : multiCameraGoal.getProcessedInfoMap().keySet()) {
                        multiCameraGoal.getProcessedInfoMap().get(c).remove("stopMonitorEntry");
                    }
                    multiCameraGoal.getAdditionalFieldMap().remove("stopMonitorEntry");
                    cameraBackgrounds.clear();
                    multiCameraGoal.setActivated(false);
                    stateMachineMap.clear();
                }

            }


            float time = (System.currentTimeMillis() - start);

            LOGGER.fine("ObserveEntry, " + debug + " Execution time: " + time);

        }

        PTZCommand ptzCommand = new PTZCommand(ptzVector,5);

        return ptzCommand;

    }

    @Override
    public void init() {

        LOGGER.setLevel(Level.FINE);

        panTime = 0;
        tiltTime = 0;

        stateMachineMap = new HashMap<>();
        cameraBackgrounds = new HashMap<>();
    }
}



