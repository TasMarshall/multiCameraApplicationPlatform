package platform.behaviors.impl;

import platform.behaviors.MotionController;
import platform.behaviors.components.PTZCommand;
import platform.camera.components.PTZVector;
import platform.camera.components.Vector1D;
import platform.camera.Camera;
import platform.camera.components.Vector2D;
import platform.goals.MultiCameraGoal;
import platform.imageAnalysis.impl.outputObjects.ObjLocBounds;
import platform.imageAnalysis.impl.outputObjects.ObjectLocations;
import platform.utilities.LoopTimer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ObjectToLowLimitMotionBehaviour extends MotionController {

    Map<Camera, LoopTimer> loopTimerMap = new HashMap<>();

    public PTZCommand calculatePTZVector(MultiCameraGoal multiCameraGoal, Camera camera) {

        PTZVector ptzVector = new PTZVector();
        Vector1D zoom = new Vector1D();
        Vector2D pt = new Vector2D();
        pt.setX(0);
        pt.setY(0);
        zoom.setX(0);

        Map<String,Serializable> result = multiCameraGoal.getNewAnalysisResultsMap().get(camera.getIdAsString());
        if( result != null) {

            ObjectLocations objectLocations = (ObjectLocations) multiCameraGoal.getNewAnalysisResultsMap().get(camera.getIdAsString()).get("objectLocations");
            ObjLocBounds objectLocation = null;

            if (objectLocations != null){
                objectLocation = (ObjLocBounds) objectLocations.getExtremesLocation();
            }

            //make a loop speed controller
            LoopTimer timer = loopTimerMap.get(camera);
            if (timer == null){
                timer = new LoopTimer();
                timer.start(2,1);
                loopTimerMap.put(camera, timer);
            }

            if (objectLocation != null) {

                if (timer.checkPulse()) {

                    if (objectLocation.getYMin() < 0.80) {
                        //move up
                        System.out.println(camera.getIdAsString() +" up. data is old by: " + (System.currentTimeMillis() - objectLocation.getCreationTime()));
                        pt.setY(camera.getViewCapabilities().getMaxTiltViewAngle());

                    } else if (objectLocation.getYMin() > 0.95) {
                        System.out.println(camera.getIdAsString() +" down.  data is old by: " + (System.currentTimeMillis() - objectLocation.getCreationTime()));
                        //move down
                        pt.setY(camera.getViewCapabilities().getMinTiltViewAngle());
                    }

                    float moveX = 0.5F - objectLocation.getX_centroid();

                    if (Math.abs(moveX) > 0.1) {
                        //add x movement to command
                        if (moveX > 0) {
                            System.out.println("move to the left. data is old by: " + (System.currentTimeMillis() - objectLocation.getCreationTime()));
                            pt.setX((float) camera.getViewCapabilities().getViewAngle() / 2 * moveX * -1);
                        } else {
                            System.out.println("move to the right. data is old by: " + (System.currentTimeMillis() - objectLocation.getCreationTime()));
                            pt.setX((float) camera.getViewCapabilities().getViewAngle() / 2 * moveX * -1);
                        }

                    }

                }

            }

        }

        ptzVector.setPanTilt(pt);
        ptzVector.setZoom(zoom);

        PTZCommand ptzCommand = new PTZCommand(ptzVector,5);

        return ptzCommand;

    }

    @Override
    public void init() {
        loopTimerMap = new HashMap<>();
    }
}



