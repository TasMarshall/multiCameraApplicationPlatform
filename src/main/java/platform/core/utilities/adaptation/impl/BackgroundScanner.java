package platform.core.utilities.adaptation.impl;

import org.onvif.ver10.schema.PTZVector;
import org.onvif.ver10.schema.Vector1D;
import org.onvif.ver10.schema.Vector2D;
import platform.core.camera.core.Camera;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.goals.core.components.Interest;
import platform.core.imageAnalysis.impl.outputObjects.ImageComparison;
import platform.core.utilities.adaptation.core.AdaptivePolicy;
import platform.core.utilities.adaptation.core.MotionController;
import platform.core.utilities.adaptation.core.components.BackgroundData;
import platform.core.utilities.adaptation.core.components.ImageLocation;
import platform.core.utilities.adaptation.core.components.PTZCommand;

import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BackgroundScanner extends MotionController implements AdaptivePolicy{

    int imageNumber;

    Map<Camera,BackgroundData> cameraBackgrounds;

    String interestObjectId = "imageCompare";
    String targetField = "imageComparison";

    @Override
    public void init() {
        imageNumber = 0;
        cameraBackgrounds = new HashMap<>();
        interestObjectId = "imageCompare";
        targetField = "imageComparison";
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
        Map<String,Serializable> result = multiCameraGoal.getNewAnalysisResultMap().get(camera.getIdAsString());
        if( result != null) {

            ImageComparison imageComparison = (ImageComparison) multiCameraGoal.getNewAnalysisResultMap().get(camera.getIdAsString()).get("imageComparison");
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

                if (backgroundData.getScanningState() == BackgroundData.ScanningState.LocatingSWCorner
                        || backgroundData.getScanningState() == BackgroundData.ScanningState.PanningLeft
                        || backgroundData.getScanningState() == BackgroundData.ScanningState.PanningRight ) {

                    moveTime = 1000;

                    if (backgroundData.getScanningState() == BackgroundData.ScanningState.LocatingSWCorner) {
                        pt.setX(camera.getViewCapabilities().getMinPanViewAngle());
                        pt.setY(camera.getViewCapabilities().getMinTiltViewAngle());
                        zoom.setX(camera.getViewCapabilities().getMinZoom());
                        moveTime = 1000;
                        if (counter >= 10) {
                            backgroundData.nextState();
                        }
                    }
                    else if ( !backgroundData.getLoopTimer().lookPulse()) {
                         if (backgroundData.getScanningState() == BackgroundData.ScanningState.PanningRight) {
                            pt.setX(camera.getViewCapabilities().getMaxPanViewAngle());
                        } else if (backgroundData.getScanningState() == BackgroundData.ScanningState.PanningLeft) {
                            pt.setX(camera.getViewCapabilities().getMinPanViewAngle());

                        }
                    }
                    else if (counter == 3) {
                        backgroundData.getLoopTimer().resetPulse();
                    }
                    else if (counter == 10) {
                        backgroundData.nextState();
                    }

                }
                else if (backgroundData.getScanningState() == BackgroundData.ScanningState.Tilting) {

                    moveTime = 500;

                    if ( !backgroundData.getLoopTimer().lookPulse()) {
                        if (backgroundData.getScanningState() == BackgroundData.ScanningState.Tilting) {
                            pt.setY(camera.getViewCapabilities().getMaxTiltViewAngle());
                            counter = 0;
                        }
                    }
                    else if (counter > 20){
                        System.out.println("end");
                    }
                    else if (counter > 3 && counter < 10){
                        backgroundData.nextState();
                    }

                }

            }
            else if (similarity > 0 && similarity < 100){
                backgroundData.nextState();
            }

            System.out.println(  backgroundData.getScanningState() + " " + counter + " " + similarity);

        }

        ptzVector.setPanTilt(pt);
        ptzVector.setZoom(zoom);

        PTZCommand ptzCommand = new PTZCommand(ptzVector,500);

        return ptzCommand;

    }

    @Override
    public void removeInputData(MultiCameraGoal multiCameraGoal, Camera camera) {
        multiCameraGoal.getNewAnalysisResultMap().remove(camera.getIdAsString());
        Interest interest = multiCameraGoal.getInterestById(interestObjectId);
        interest.getResults().remove(targetField);
    }

    public ImageLocation getTargetObject(Interest interest, String resultName) {
        return null;
    }
}
