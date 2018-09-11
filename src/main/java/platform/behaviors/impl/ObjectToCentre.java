package platform.behaviors.impl;

import platform.behaviors.MotionController;
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

public class ObjectToCentre extends MotionController {

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


                    float moveY = 0.5F - objectLocation.getY_centroid();


                    if (Math.abs(moveY) > 0.1) {

                        if (moveY > 0) {
                            if (camera.getCameraOrientation().getRoll() == 180){
                                pt.setY(-0.5F);
                            }
                            else {
                                pt.setY(0.5F); // this moves correctly with upright cameras
                            }

                        } else {
                            //System.out.println(camera.getIdAsString() +" down.  data is old by: " + (System.currentTimeMillis() - objectLocation.getCreationTime()));
                            //move down
                            pt.setY(-1);
                        }
                    }

                    float moveX = 0.5F - objectLocation.getX_centroid();

                    if (Math.abs(moveX) > 0.1) {
                        //add x movement to command
                        if (moveX > 0) {
                            //System.out.println("move to the left. data is old by: " + (System.currentTimeMillis() - objectLocation.getCreationTime()));
                            pt.setX((float) -0.5F);
                        } else {
                            //System.out.println("move to the right. data is old by: " + (System.currentTimeMillis() - objectLocation.getCreationTime()));
                            pt.setX((float)0.5F);
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

        LOGGER = Logger.getLogger(ObjectToCentre.class.getName());
        LOGGER.setLevel(Level.FINE);
    }
}



