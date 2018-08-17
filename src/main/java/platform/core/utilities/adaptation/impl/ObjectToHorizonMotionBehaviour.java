package platform.core.utilities.adaptation.impl;

import org.onvif.ver10.schema.PTZVector;
import org.onvif.ver10.schema.Vector1D;
import org.onvif.ver10.schema.Vector2D;
import platform.core.camera.core.Camera;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.imageAnalysis.impl.outputObjects.ObjLocBounds;
import platform.core.imageAnalysis.impl.outputObjects.ObjectLocations;
import platform.core.utilities.LoopTimer;
import platform.core.utilities.adaptation.core.AdaptivePolicy;
import platform.core.utilities.adaptation.core.MotionController;
import platform.core.utilities.adaptation.core.components.PTZCommand;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ObjectToHorizonMotionBehaviour extends MotionController implements AdaptivePolicy {

    Map<Camera, LoopTimer> loopTimerMap = new HashMap<>();

    public PTZCommand calculatePTZVector(MultiCameraGoal multiCameraGoal, Camera camera) {

        PTZVector ptzVector = new PTZVector();
        Vector1D zoom = new Vector1D();
        Vector2D pt = new Vector2D();
        pt.setX(0);
        pt.setY(0);
        zoom.setX(0);

        Map<String,Serializable> result = multiCameraGoal.getNewAnalysisResultMap().get(camera.getIdAsString());
        if( result != null) {

            ObjectLocations objectLocations = (ObjectLocations) multiCameraGoal.getNewAnalysisResultMap().get(camera.getIdAsString()).get("objectLocations");
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

                    if (objectLocation.getYMax() < 0.05) {

                        //move up
                        System.out.println(camera.getIdAsString() +" up. data is old by: " + (System.currentTimeMillis() - objectLocation.getCreationTime()));
                        pt.setY(camera.getViewCapabilities().getMaxTiltViewAngle());

                    } else if (objectLocation.getYMax() > 0.15) {
                        System.out.println(camera.getIdAsString() +" down  data is old by: " + (System.currentTimeMillis() - objectLocation.getCreationTime()));
                        //move down
                        pt.setY(camera.getViewCapabilities().getMinTiltViewAngle());

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



