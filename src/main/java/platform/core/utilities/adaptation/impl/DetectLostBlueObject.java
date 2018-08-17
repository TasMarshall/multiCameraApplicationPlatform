package platform.core.utilities.adaptation.impl;

import platform.core.camera.core.Camera;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.imageAnalysis.impl.outputObjects.ObjectLocations;
import platform.core.utilities.LoopTimer;
import platform.core.utilities.adaptation.core.GoalMAPEBehavior;
import platform.jade.utilities.CommunicationAction;

import java.util.HashMap;
import java.util.Map;

public class DetectLostBlueObject extends GoalMAPEBehavior {

    LoopTimer loopTimer;
    LoopTimer resetTimer;

    @Override
    public CommunicationAction plan(MultiCameraGoal multiCameraGoal) {

        if (!loopTimer.lookPulse()) {

            boolean objectLost = true;
            HashMap<String, java.lang.Object> cameraMap;
            java.lang.Object o = null;
            ObjectLocations objectLocations = null;

            for (Camera camera : multiCameraGoal.getActiveCameras()) {

                if (getHaspMap().get(camera.getIdAsString()) == null) {
                    getHaspMap().put(camera.getIdAsString(), new HashMap<String, java.lang.Object>());
                }
                cameraMap = ((HashMap<String, java.lang.Object>) getHaspMap().get(camera.getIdAsString()));

                o = multiCameraGoal.getNewAnalysisResultMap().get(camera.getIdAsString());
                if (o != null) {
                    objectLocations = (ObjectLocations)((HashMap<String, Object>) o).get("blueObjectLocations");
                    if (objectLocations != null) {
                        objectLost = false;
                        cameraMap.put("lastDetectionTime", System.currentTimeMillis());
                        System.out.println("Blue object found, reset lost timer.");
                    }
                } else {

                    java.lang.Object lasttime = cameraMap.get("lastDetectionTime");
                    if (lasttime == null) {
                        //if there is no record of a last time assign one so we dont switch too fast and so that an assessment can be made next time.
                        cameraMap.put("lastDetectionTime", System.currentTimeMillis());
                        objectLost = false;
                    } else if ((System.currentTimeMillis() - (long) lasttime) > 30000) {
                        //object is lost, default object lost to true if no other camera has seen the object
                        Map<String, java.lang.Object> ob = multiCameraGoal.getProcessedInfoMap().get(camera);
                        if (ob == null){
                            multiCameraGoal.getProcessedInfoMap().put(camera,new HashMap<>());
                            ob = multiCameraGoal.getProcessedInfoMap().get(camera);
                        }

                        ob.put("cameraLostBlueObject", new Boolean(true));

                    } else {
                        //if object hasnt been lost for long enough then it is not yet lost
                        objectLost = false;
                    }
                }

            }

            if (objectLost) {

                System.out.println("Blue object was lost and crash monitor goal has been deactivated.");

                //multiCameraGoal.setActivated(false);

                for (Camera camera : multiCameraGoal.getActiveCameras()) {

                    Map<String, java.lang.Object> ob = multiCameraGoal.getProcessedInfoMap().get(camera);
                    if (ob == null){
                        multiCameraGoal.getProcessedInfoMap().put(camera,new HashMap<>());
                        ob = multiCameraGoal.getProcessedInfoMap().get(camera);
                    }

                    ob.put("blueObjectLost", new Boolean(true));

                }
                getHaspMap().clear();

            }

        }

        if (resetTimer.checkPulse()){
            loopTimer.resetPulse();
        }


        return null;
    }

    @Override
    public CommunicationAction analyse(MultiCameraGoal multiCameraGoal) {
        return null;
    }

    @Override
    public CommunicationAction monitor(MultiCameraGoal multiCameraGoal) {

        return null;

    }

    @Override
    public CommunicationAction execute(MultiCameraGoal multiCameraGoal) {
        return null;
    }

    @Override
    public void init() {

        //a timer for .5 seconds during which this function will run then stop
        loopTimer = new LoopTimer();
        loopTimer.start(0.5,1);

        //a timer for 5s which will reset the .5s run timer so that it can run .5s every 5s
        resetTimer = new LoopTimer();
        resetTimer.start(5,1);

    }


}