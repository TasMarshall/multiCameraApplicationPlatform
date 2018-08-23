package platform.behaviors.impl;

import platform.behaviors.AdaptivePolicy;
import platform.camera.components.PTZVector;
import platform.camera.components.Vector1D;
import platform.camera.components.Vector2D;
import platform.imageAnalysis.ImageLocation;
import platform.behaviors.MotionController;
import platform.behaviors.components.PTZCommand;
import platform.camera.Camera;
import platform.goals.MultiCameraGoal;
import platform.goals.VisualObservationOfInterest;
import platform.imageAnalysis.impl.outputObjects.CircleLocationInImage;
import platform.imageAnalysis.impl.outputObjects.CircleLocationsInImage;

import java.util.HashMap;
import java.util.Map;

public class SimpleInScreenPointViewAdaptation extends MotionController implements AdaptivePolicy {

    String interestObjectId;
    String targetField;

    public PTZCommand calculatePTZVector(MultiCameraGoal multiCameraGoal, Camera camera) {

        PTZVector ptzVectorCommand = null;

        VisualObservationOfInterest interest = multiCameraGoal.getInterestById(interestObjectId);

        Map<String, Object> inputs = new HashMap<>();

        if (interest != null) {

            if (interest.getResults(camera.getIdAsString()) != null && interest.getResults(camera.getIdAsString()).size() != 0 /*&& (System.nanoTime() - lastAnalysisResultTime)/1000000 < 10*/) {

                ImageLocation object = getTargetObject(interest, targetField,camera.getIdAsString());

                if (object != null) {

                    float moveX = 0.5F - object.getTargetRelX();
                    float moveY = 0.5F - object.getTargetRelY();

                    Vector2D vector2D = new Vector2D();
                    Vector1D vector1D = new Vector1D();
                    ptzVectorCommand = new PTZVector();

                    if (Math.abs(moveX) > 0.1) {
                        //add x movement to command
                        if (moveX > 0) {
                            vector2D.setX((float) camera.getViewCapabilities().getViewAngle() / 2 * moveX * -1);
                        } else {
                            vector2D.setX((float) camera.getViewCapabilities().getViewAngle() / 2 * moveX * -1);
                        }

                    } else {
                        vector2D.setX((float) 0);
                    }

                    if (Math.abs(moveY) > 0.1) {
                        //add y movement to command
                        if (moveY > 0) {
                            vector2D.setY((float) camera.getViewCapabilities().getViewAngle() / 2 * moveY * 1);
                        } else {
                            vector2D.setY((float) camera.getViewCapabilities().getViewAngle() / 2 * moveY * 1);
                        }
                    } else {
                        vector2D.setY((float) 0);
                    }

                    vector1D.setX((float) 0);

                    ptzVectorCommand.setPanTilt(vector2D);
                    ptzVectorCommand.setZoom(vector1D);

                }

            }

        }

        PTZCommand ptzCommand = new PTZCommand(ptzVectorCommand,5);

        return ptzCommand;


    }


    public ImageLocation getTargetObject(VisualObservationOfInterest interest, String resultName, String cameraID) {

        CircleLocationInImage circleLocationInImage = ((CircleLocationsInImage) interest.getResults(cameraID).get(resultName)).getBiggestCircle();

        return  circleLocationInImage;

    }

    @Override
    public void init() {
        interestObjectId = "houghCircles";
        targetField = "circles";
    }
}



