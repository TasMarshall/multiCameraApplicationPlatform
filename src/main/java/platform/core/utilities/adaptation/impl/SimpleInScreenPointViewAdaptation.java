package platform.core.utilities.adaptation.impl;

import org.onvif.ver10.schema.PTZVector;
import org.onvif.ver10.schema.Vector1D;
import org.onvif.ver10.schema.Vector2D;
import platform.core.camera.core.Camera;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.goals.core.components.Interest;
import platform.core.imageAnalysis.impl.outputObjects.CircleLocationInImage;
import platform.core.imageAnalysis.impl.outputObjects.CircleLocationsInImage;
import platform.core.utilities.adaptation.core.AdaptivePolicy;
import platform.core.utilities.adaptation.core.MotionController;
import platform.core.utilities.adaptation.core.components.ImageLocation;
import platform.core.utilities.adaptation.core.components.PTZCommand;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SimpleInScreenPointViewAdaptation extends MotionController implements AdaptivePolicy {

    String interestObjectId;
    String targetField;

    public PTZCommand calculatePTZVector(MultiCameraGoal multiCameraGoal, Camera camera) {

        PTZVector ptzVectorCommand = null;

        Interest interest = multiCameraGoal.getInterestById(interestObjectId);

        Map<String, Object> inputs = new HashMap<>();

        if (interest != null) {

            if (interest.getResults() != null && interest.getResults().size() != 0 /*&& (System.nanoTime() - lastAnalysisResultTime)/1000000 < 10*/) {

                ImageLocation object = getTargetObject(interest, targetField);

                if (object != null) {

                    float moveX = 0.5F - object.getTargetRelX();
                    float moveY = 0.5F - object.getTargetRelY();

                    Vector2D vector2D = new Vector2D();
                    Vector1D vector1D = new Vector1D();
                    ptzVectorCommand = new PTZVector();

                    if (Math.abs(moveX) > 0.1) {
                        //add x movement to command
                        if (moveX > 0) {
                            System.out.println("move to the left");
                            vector2D.setX((float) camera.getViewCapabilities().getViewAngle() / 2 * moveX * -1);
                        } else {
                            System.out.println("move to the right");
                            vector2D.setX((float) camera.getViewCapabilities().getViewAngle() / 2 * moveX * -1);
                        }

                    } else {
                        vector2D.setX((float) 0);
                    }

                    if (Math.abs(moveY) > 0.1) {
                        //add y movement to command
                        if (moveY > 0) {
                            System.out.println("move up");
                            vector2D.setY((float) camera.getViewCapabilities().getViewAngle() / 2 * moveY * 1);
                        } else {
                            System.out.println("move down");
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

    @Override
    public void removeInputData(MultiCameraGoal multiCameraGoal, Camera camera) {

        multiCameraGoal.getNewAnalysisResultMap().remove(camera.getIdAsString());
        Interest interest = multiCameraGoal.getInterestById(interestObjectId);
        interest.getResults().remove(targetField);

    }

    public ImageLocation getTargetObject(Interest interest, String resultName) {

        CircleLocationInImage circleLocationInImage = ((CircleLocationsInImage) interest.getResults().get(resultName)).getBiggestCircle();

        return  circleLocationInImage;

    }

    @Override
    public void init() {
        interestObjectId = "houghCircles";
        targetField = "circles";
    }
}



