package platform.behaviors.impl;

import platform.behaviors.GoalMAPEBehavior;
import platform.camera.Camera;
import platform.goals.MultiCameraGoal;
import platform.imageAnalysis.impl.outputObjects.ObjectLocations;
import platform.jade.ModelAgent;
import platform.jade.utilities.CommunicationAction;
import platform.utilities.LoopTimer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DetectLostRedObject extends GoalMAPEBehavior {

    private final static Logger LOGGER = Logger.getLogger(DetectLostRedObject.class.getName());

    LoopTimer loopTimer;
    LoopTimer resetTimer;

    @Override
    public void init() {

        //a timer for .5 seconds during which this function will run then stop
        loopTimer = new LoopTimer();
        loopTimer.start(0.5,1);

        //a timer for 5s which will reset the .5s run timer so that it can run .5s every 5s
        resetTimer = new LoopTimer();
        resetTimer.start(5,1);

        LOGGER.setLevel(Level.FINE);

    }

    @Override
    public CommunicationAction plan(MultiCameraGoal multiCameraGoal) {

        if (!loopTimer.lookPulse()) {

            boolean objectLost = true;
            HashMap<String, Object> cameraMap;
            Object o = null;
            ObjectLocations objectLocations = null;

            for (Camera camera : multiCameraGoal.getActiveCameras()) {

                if (getGoalBehaviorInfoMap().get(camera.getIdAsString()) == null) {
                    getGoalBehaviorInfoMap().put(camera.getIdAsString(), new HashMap<String, Object>());
                }
                cameraMap = ((HashMap<String, Object>) getGoalBehaviorInfoMap().get(camera.getIdAsString()));

                o = multiCameraGoal.getNewAnalysisResultsMap().get(camera.getIdAsString());
                if (o != null) {
                    objectLocations = (ObjectLocations)((HashMap<String, Object>) o).get("redObjectLocations");
                    if (objectLocations != null) {
                        objectLost = false;
                        cameraMap.put("lastDetectionTime", System.currentTimeMillis());
                    }
                } else {

                    Object lasttime = cameraMap.get("lastDetectionTime");
                    if (lasttime == null) {
                        //if there is no record of a last time assign one so we dont switch too fast and so that an assessment can be made next time.
                        cameraMap.put("lastDetectionTime", System.currentTimeMillis());
                        objectLost = false;
                    } else if ((System.currentTimeMillis() - (long) lasttime) > 60000) {
                        //object is lost, default object lost to true if no other camera has seen the object
                        Map<String, Serializable> ob = multiCameraGoal.getProcessedInfoMap().get(camera);
                        if (ob == null){
                            multiCameraGoal.getProcessedInfoMap().put(camera,new HashMap<>());
                            ob = multiCameraGoal.getProcessedInfoMap().get(camera);
                        }

                        ob.put("cameraLostRedObject", new Boolean(true));

                    } else {
                        //if object hasnt been lost for long enough then it is not yet lost
                        objectLost = false;
                    }
                }

            }

            if (objectLost) {

                MultiCameraGoal multiCameraGoal1 = multiCameraGoal.getMcp_application().getGoalById("monitorInterior");
                if (multiCameraGoal1 != null) {
                    Set<Camera> set = multiCameraGoal1.getProcessedInfoMap().keySet();
                    if(set.size() > 0) {
                        for (Camera c : set) {
                            if (!multiCameraGoal1.getProcessedInfoMap().get(c).containsKey("cameraLostRedObject")) {
                                objectLost = false;
                                LOGGER.info("Red object was lost by external cameras but not internal cameras.");
                            }
                        }
                    }
                    else {
                        objectLost = false;
                        LOGGER.info("Red object was lost by external cameras but not internal cameras.");
                    }
                }
            }

            if (objectLost) {

                LOGGER.info("Red object was lost and monitor entry goal has been notified.");
                System.out.println("Red object was lost and monitor entry goal has been notified.");
                getGoalBehaviorInfoMap().clear();

                MultiCameraGoal multiCameraGoal1 = multiCameraGoal.getMcp_application().getGoalById("monitorInterior");
                if (multiCameraGoal1 != null) {
                    Set<Camera> set = multiCameraGoal1.getProcessedInfoMap().keySet();
                    for (Camera c : set) {
                        multiCameraGoal1.getProcessedInfoMap().get(c).remove("cameraLostRedObject");
                    }
                }

                multiCameraGoal.getAdditionalFieldMap().put("stopMonitorEntry","");

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

}
