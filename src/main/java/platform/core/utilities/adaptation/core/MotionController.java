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
    private Map<Camera, Boolean> lastStopCommand;

    public void motInit(){
        commandMap = new HashMap<Camera,PTZCommand>();
        lastStopCommand = new HashMap<>();
    }

    public void planMotion(MultiCameraGoal multiCameraGoal, Camera camera){
        PTZCommand ptzCommand = calculatePTZVector(multiCameraGoal,camera);
        commandMap.put(camera,ptzCommand);
    }

    public abstract PTZCommand calculatePTZVector(MultiCameraGoal multiCameraGoal, Camera camera);



    public void  executeMotion(Map<String, Long> motionActionEndTimes, Camera camera){

        boolean moveCommanded = false;
        String message = "";
        PTZCommand ptzCommand = commandMap.get(camera);

        if (ptzCommand != null){
            PTZVector ptzVector = ptzCommand.getPtzVector();
            message += "Object results detected, ptz vector calculated. ";
            if (ptzVector != null && (ptzVector.getPanTilt().getX() != 0 || ptzVector.getPanTilt().getY() != 0 ||ptzVector.getZoom().getX() != 0 )){
                message += "PTZ movement required. ";
                moveCommanded = true;
            }
        }
        else {
            message += "No object results. ";
        }

        long currentTime = System.currentTimeMillis();


            if (moveCommanded){
                motionActionEndTimes.replace(camera.getIdAsString(),currentTime+ptzCommand.getTimeMiliiSec());
                camera.commandPTZMovement(ptzCommand.getPtzVector());
                lastStopCommand.put(camera,false);
            }
            else {

                if (currentTime > motionActionEndTimes.get(camera.getIdAsString()) ) {
                    //try to not send unnecessary commands
                    if (lastStopCommand.get(camera) != null && lastStopCommand.get(camera) == false) {
                        camera.commandPTZStop();
                        lastStopCommand.put(camera, true);
                    }
                }
            }


    }

}
