package platform.behaviors.impl;

import platform.camera.components.PTZVector;
import platform.camera.components.Vector1D;
import platform.camera.Camera;
import platform.camera.components.Vector2D;
import platform.goals.MultiCameraGoal;
import platform.goals.VisualObservationOfInterest;
import platform.imageAnalysis.impl.outputObjects.ImageComparison;
import platform.behaviors.AdaptivePolicy;
import platform.behaviors.MotionController;
import platform.imageAnalysis.ImageLocation;
import platform.behaviors.components.PTZCommand;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BackgroundScanner extends MotionController implements AdaptivePolicy{

    int imageNumber;

    Map<Camera,BackgroundData> cameraBackgrounds;

    Boolean stateCounterHasReset = false;

    @Override
    public void init() {
        imageNumber = 0;
        cameraBackgrounds = new HashMap<>();
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
        int moveTime = 0;

        //Get image similarity from newest image processing results
        Map<String,Serializable> result = multiCameraGoal.getNewAnalysisResultsMap().get(camera.getIdAsString());
        if( result != null) {

            ImageComparison imageComparison = (ImageComparison) multiCameraGoal.getNewAnalysisResultsMap().get(camera.getIdAsString()).get("imageComparison");
            if (imageComparison != null){
                counter = imageComparison.getCounter();
                similarity = imageComparison.getSimilarity();
            }

            //ensure there is background data
            BackgroundData backgroundData = cameraBackgrounds.get(camera);
            if (backgroundData == null){
                backgroundData = new BackgroundData();
                cameraBackgrounds.put(camera, backgroundData);
            }


            if (backgroundData.getScanningState() != BackgroundData.ScanningState.WAITING_CAMERA_STREAM_INIT) {

                if (!backgroundData.getSnapShotRequired()) {
                    if (backgroundData.getScanningState() == BackgroundData.ScanningState.LocatingSWCorner
                            || backgroundData.getScanningState() == BackgroundData.ScanningState.LocatingNECorner
                            || backgroundData.getScanningState() == BackgroundData.ScanningState.PanningLeft
                            || backgroundData.getScanningState() == BackgroundData.ScanningState.PanningRight) {

                        moveTime = 1000;

                        if (backgroundData.getScanningState() == BackgroundData.ScanningState.LocatingNECorner) {
                            if (counter < 1) {
                                stateCounterHasReset = true;
                            }

                            pt.setX(camera.getViewCapabilities().getMaxPanViewAngle());
                            pt.setY(camera.getViewCapabilities().getMaxTiltViewAngle());
                            zoom.setX(camera.getViewCapabilities().getMinZoom());
                            moveTime = 1000;
                            if (counter >= 10) {
                                requestSnapShot(camera, multiCameraGoal, backgroundData.getScanningState().toString());
                            }
                        }
                        else if (backgroundData.getScanningState() == BackgroundData.ScanningState.LocatingSWCorner) {
                            if (counter < 1) {
                                stateCounterHasReset = true;
                            }

                            pt.setX(camera.getViewCapabilities().getMinPanViewAngle());
                            pt.setY(camera.getViewCapabilities().getMinTiltViewAngle());
                            zoom.setX(camera.getViewCapabilities().getMinZoom());
                            moveTime = 1000;

                            if (stateCounterHasReset) {
                                if (counter >= 10) {
                                    requestSnapShot(camera, multiCameraGoal, backgroundData.getScanningState().toString());
                                }
                            }
                        }
                        else {
                            if (counter < 1) {
                                stateCounterHasReset = true;
                            }

                            if (!backgroundData.getLoopTimer().lookPulse()) {

                                if (backgroundData.getScanningState() == BackgroundData.ScanningState.PanningRight) {
                                    pt.setX(camera.getViewCapabilities().getMaxPanViewAngle());
                                } else if (backgroundData.getScanningState() == BackgroundData.ScanningState.PanningLeft) {
                                    pt.setX(camera.getViewCapabilities().getMinPanViewAngle());

                                }

                            } else if (counter == 10) {
                                if (stateCounterHasReset) {
                                    requestSnapShot(camera, multiCameraGoal, backgroundData.getScanningState().toString());
                                }
                            }
                            else if (counter == 2) {

                                if (!backgroundData.isPausedInPlace()) {
                                    pauseForSnapShot(camera, multiCameraGoal, backgroundData.getScanningState().toString());
                                }

                            }
                        }

                    } else if (backgroundData.getScanningState() == BackgroundData.ScanningState.Tilting) {

                        moveTime = 500;

                        if (!backgroundData.getLoopTimer().lookPulse()) {
                            if (backgroundData.getScanningState() == BackgroundData.ScanningState.Tilting) {
                                pt.setY(camera.getViewCapabilities().getMaxTiltViewAngle());
                            }
                        } else {
                            requestSnapShot(camera, multiCameraGoal, backgroundData.getScanningState().toString());
                        }

                    }
                }

                if (backgroundData.getSnapShotRequired() || backgroundData.isPausedInPlace()){
                    if (camera.getAdditionalAttributes().containsKey("snapTaken")){

                        if(backgroundData.getSnapShotRequired()){
                            stateCounterHasReset = false;
                        }

                        String s = (String)camera.getAdditionalAttributes().get("snapTaken");
                        System.out.println((String)camera.getAdditionalAttributes().get("snapTaken"));
                        backgroundData.nextState();

                        camera.getAdditionalAttributes().remove("snapTaken");

                    }
                }

            }
            else if (similarity > 0 && similarity < 100){
                backgroundData.nextState();
            }

        }

        ptzVector.setPanTilt(pt);
        ptzVector.setZoom(zoom);

        PTZCommand ptzCommand = new PTZCommand(ptzVector,500);

        return ptzCommand;

    }

    private void pauseForSnapShot(Camera camera, MultiCameraGoal multiCameraGoal, String s) {

        cameraBackgrounds.get(camera).setPausedInPlace(true);

        Map<String,Serializable> map = multiCameraGoal.getProcessedInfoMap().get(camera);
        map.put("isReadyForSnapShot", new Boolean(true));
        map.put("snapName", s);

    }

    private void requestSnapShot(Camera camera, MultiCameraGoal multiCameraGoal, String s) {

        cameraBackgrounds.get(camera).setSnapShotRequired(true);

        Map<String,Serializable> map = multiCameraGoal.getProcessedInfoMap().get(camera);
        map.put("isReadyForSnapShot", new Boolean(true));
        map.put("snapName", s);

        stateCounterHasReset = false;

    }

    public ImageLocation getTargetObject(VisualObservationOfInterest interest, String resultName) {
        return null;
    }
}
