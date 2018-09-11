package platform.behaviors.impl;

import platform.camera.components.PTZVector;
import platform.camera.components.Vector1D;
import platform.camera.Camera;
import platform.camera.components.Vector2D;
import platform.goals.MultiCameraGoal;
import platform.imageAnalysis.impl.outputObjects.ImageComparison;
import platform.imageAnalysis.impl.outputObjects.ObjectLocations;
import platform.behaviors.AdaptivePolicy;
import platform.behaviors.MotionController;
import platform.behaviors.components.PTZCommand;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuickScanForObject extends MotionController implements AdaptivePolicy{

    int imageNumber;

    Map<Camera,BackgroundData> cameraBackgrounds;

    Boolean stateCounterHasReset = false;
    Boolean found = false;


    @Override
    public void init() {
        imageNumber = 0;
        cameraBackgrounds = new HashMap<>();
        stateCounterHasReset = false;
        found = false;
    }

    public PTZCommand calculatePTZVector(MultiCameraGoal multiCameraGoal, Camera camera) {

        int counter = 0;
        float similarity = -1;

        PTZVector ptzVector = new PTZVector();
        Vector1D zoom = new Vector1D();
        Vector2D pt = new Vector2D();
        pt.setX(0);
        pt.setY(0);
        zoom.setX(0);

        //Get image similarity from newest image processing results
        Map<String,Serializable> result = multiCameraGoal.getNewAnalysisResultsMap().get(camera.getIdAsString());
        if( result != null) {

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

                    found = true;

                }

            }

            if (!found){

                ImageComparison imageComparison = (ImageComparison) multiCameraGoal.getNewAnalysisResultsMap().get(camera.getIdAsString()).get("imageComparison");
                if (imageComparison != null) {
                    counter = imageComparison.getCounter();
                    similarity = imageComparison.getSimilarity();
                }

                //ensure there is background data
                BackgroundData backgroundData = cameraBackgrounds.get(camera);
                if (backgroundData == null) {
                    backgroundData = new BackgroundData();
                    cameraBackgrounds.put(camera, backgroundData);
                }
                backgroundData.setSnapShotRequired(true);//getting past a bug in this object.

                if (backgroundData.getScanningState() != BackgroundData.ScanningState.WAITING_CAMERA_STREAM_INIT) {

                    if (backgroundData.getScanningState() == BackgroundData.ScanningState.LocatingSWCorner
                            || backgroundData.getScanningState() == BackgroundData.ScanningState.LocatingNECorner
                            || backgroundData.getScanningState() == BackgroundData.ScanningState.PanningLeft
                            || backgroundData.getScanningState() == BackgroundData.ScanningState.PanningRight) {

                        if (backgroundData.getScanningState() == BackgroundData.ScanningState.LocatingNECorner) {
                            backgroundData.nextState();
                            stateCounterHasReset = false;
                        } else if (backgroundData.getScanningState() == BackgroundData.ScanningState.LocatingSWCorner) {
                            if (counter < 1) {
                                stateCounterHasReset = true;
                            }

                            pt.setX(camera.getViewCapabilities().getMinPanViewAngle());
                            pt.setY(camera.getViewCapabilities().getMinTiltViewAngle());
                            zoom.setX(camera.getViewCapabilities().getMinZoom());

                            if (stateCounterHasReset) {
                                if (counter >= 6) {
                                    backgroundData.nextState();
                                    stateCounterHasReset = false;
                                }
                            }

                        } else {
                            if (counter < 1) {
                                stateCounterHasReset = true;
                            }

                            if (backgroundData.getScanningState() == BackgroundData.ScanningState.PanningRight) {
                                pt.setX(camera.getViewCapabilities().getMaxPanViewAngle());
                            } else if (backgroundData.getScanningState() == BackgroundData.ScanningState.PanningLeft) {
                                pt.setX(camera.getViewCapabilities().getMinPanViewAngle());

                            }

                            if (counter == 6) {
                                if (stateCounterHasReset) {
                                    backgroundData.nextState();
                                    stateCounterHasReset = false;
                                }
                            }
                        }

                    } else if (backgroundData.getScanningState() == BackgroundData.ScanningState.Tilting) {

                        if (!backgroundData.getLoopTimer().lookPulse()) {
                            if (backgroundData.getScanningState() == BackgroundData.ScanningState.Tilting) {
                                pt.setY(camera.getViewCapabilities().getMaxTiltViewAngle());

                                }
                        } else {
                                backgroundData.nextState();
                                stateCounterHasReset = false;
                            }
                        }


                } else if (similarity > 0 && similarity < 100) {
                    backgroundData.nextState();
                }

                System.out.println(backgroundData.getScanningState() + " " + counter + " " + similarity);

            }

        }

        ptzVector.setPanTilt(pt);
        ptzVector.setZoom(zoom);

        PTZCommand ptzCommand = new PTZCommand(ptzVector,100);

        return ptzCommand;

    }

}
