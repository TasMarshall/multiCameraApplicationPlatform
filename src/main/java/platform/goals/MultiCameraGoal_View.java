package platform.goals;

import platform.imageAnalysis.ImageAnalysis;
import platform.map.Map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MultiCameraGoal_View extends MultiCameraGoalCore {

    Set<String> imageAnalysisTypes = new HashSet<>();

    public MultiCameraGoal_View(MultiCameraGoal multiCameraGoal) {
        this(multiCameraGoal.getId(),multiCameraGoal.isActivated(),multiCameraGoal.getPriority(),multiCameraGoal.goalType,multiCameraGoal.cameraRequirements,multiCameraGoal.map,multiCameraGoal.motionControllerType, multiCameraGoal.nonMotionBehaviors);

        for (VisualObservationOfInterest v : multiCameraGoal.visualObservationsOfInterest){
            for (ImageAnalysis a: v.getAnalysisAlgorithmsSet()){
                imageAnalysisTypes.add(a.getImageAnalysisType());
            }
        }
    }

    public MultiCameraGoal_View(String id, boolean activated, int priority, GoalType goalType, CameraRequirements cameraRequirements, Map map, String motionControllerType, List<String> actionTypes) {
        super(id, activated, priority, goalType, cameraRequirements, map, motionControllerType, actionTypes);

    }

}
