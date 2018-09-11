package platform.behaviors.impl;

import platform.behaviors.CameraMAPEBehavior;
import platform.camera.Camera;
import platform.goals.MultiCameraGoal;
import platform.jade.utilities.CommunicationAction;

import java.util.HashMap;
import java.util.Map;

public class BackgroundScannerSnapShot extends CameraMAPEBehavior {

    Map<Camera, Integer> cameraIntegerMap;

    @Override
    public CommunicationAction plan(Camera camera, MultiCameraGoal multiCameraGoal)
    {
        CommunicationAction communicationAction = null;
        //null
        return communicationAction;
    }

    @Override
    public CommunicationAction analyse(Camera camera, MultiCameraGoal multiCameraGoal) {
        CommunicationAction communicationAction = null;
        //null
        return communicationAction;
    }

    @Override
    public CommunicationAction monitor(Camera camera, MultiCameraGoal multiCameraGoal) {
        CommunicationAction communicationAction = null;

        if (!getCameraBehaviorInfoMap().containsKey(camera) || getCameraBehaviorInfoMap().get(camera) == null){
            getCameraBehaviorInfoMap().put(camera,new HashMap<>());
        }

        //check for the trigger?
        if (!multiCameraGoal.getProcessedInfoMap().containsKey(camera) || multiCameraGoal.getProcessedInfoMap().get(camera) == null){
            multiCameraGoal.getProcessedInfoMap().put(camera,new HashMap<>());
        }

        if (!cameraIntegerMap.containsKey(camera) || cameraIntegerMap.get(camera) == null){
            cameraIntegerMap.put(camera,new Integer(0));
        }

        if(multiCameraGoal.getProcessedInfoMap().get(camera).containsKey("isReadyForSnapShot") && multiCameraGoal.getProcessedInfoMap().get(camera).containsKey("snapName")){
            if ((Boolean)multiCameraGoal.getProcessedInfoMap().get(camera).get("isReadyForSnapShot")==true) {

                String snapShot = (String) multiCameraGoal.getProcessedInfoMap().get(camera).get("snapName");
                int counter = cameraIntegerMap.get(camera);

                String snapShotName = "";

                if (snapShot.equals("LocatingNECorner")){
                    snapShotName = camera.getIdAsString() + "_background_" + snapShot;
                    //trigger snapshot
                }
                else if (snapShot.equals("LocatingSWCorner")){
                    snapShotName = camera.getIdAsString() + "_background_" + snapShot;
                    //trigger snapshot
                }
                else if (snapShot.equals("PanningRight") || snapShot.equals("PanningLeft")){
                    snapShotName = camera.getIdAsString()  + "_background_" +  counter;
                    //trigger snapshot
                    cameraIntegerMap.put(camera,counter +1);
                }
                else if (snapShot.equals("Tilting")){
                    snapShotName = camera.getIdAsString() + "_background_" + counter;
                    //trigger snapshot
                    cameraIntegerMap.put(camera,counter +1);
                }
                else {
                    return null;
                }

                Map<String,Object> objectMap = new HashMap<>();
                objectMap.put("requestCameraSnapshot", camera.getIdAsString());
                objectMap.put("snapID", snapShotName);
                communicationAction = new CommunicationAction("CameraAnalyser" + camera.getIdAsString(),objectMap);

                //remove message trigger
                multiCameraGoal.getProcessedInfoMap().get(camera).remove("isReadyForSnapShot");
                multiCameraGoal.getProcessedInfoMap().get(camera).remove("snapName");

            }
        }

        return communicationAction;
    }

    @Override
    public CommunicationAction execute(Camera camera, MultiCameraGoal multiCameraGoal) {
        CommunicationAction communicationAction = null;
        return communicationAction;
    }

    @Override
    public void init() {
        cameraIntegerMap = new HashMap<>();
    }

}
