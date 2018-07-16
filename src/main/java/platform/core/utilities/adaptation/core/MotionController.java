package platform.core.utilities.adaptation.core;

import org.onvif.ver10.schema.PTZVector;
import platform.core.camera.core.Camera;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.utilities.adaptation.core.components.PTZCommand;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class MotionController implements AdaptivePolicy {

    private Map<Camera, PTZCommand> commandMap;

    public void motInit(){
        commandMap = new HashMap<Camera,PTZCommand>();
    }

    public void planMotion(MultiCameraGoal multiCameraGoal, Camera camera){
        PTZCommand ptzCommand = calculatePTZVector(multiCameraGoal,camera);
        commandMap.put(camera,ptzCommand);
        removeInputData(multiCameraGoal,camera);
    }

    public abstract PTZCommand calculatePTZVector(MultiCameraGoal multiCameraGoal, Camera camera);

    public abstract void removeInputData(MultiCameraGoal multiCameraGoal, Camera camera);

    public void  executeMotion(Map<String, Long> motionActionStartTimes, Camera camera){

        boolean moveCommanded = false;
        String message = "";
        PTZCommand ptzCommand = commandMap.get(camera);
        if (ptzCommand != null){
            PTZVector ptzVector = ptzCommand.getPtzVector();
            message += "Object results detected, ptz vector calculated. ";
            if (ptzVector.getPanTilt().getX() != 0 || ptzVector.getPanTilt().getY() != 0 ||ptzVector.getZoom().getX() != 0 ){
                message += "PTZ movement required. ";
                moveCommanded = true;
            }
        }
        else {
            message += "No object results. ";
        }

        long currentTime = System.currentTimeMillis();

        if (currentTime - motionActionStartTimes.get(camera.getIdAsString()) > ptzCommand.getTimeMiliiSec()) {
            if (moveCommanded){
                motionActionStartTimes.replace(camera.getIdAsString(),currentTime);
                camera.commandPTZMovement(ptzCommand.getPtzVector());
            }
            else {
                camera.commandPTZStop();
            }
        }

    }

}
