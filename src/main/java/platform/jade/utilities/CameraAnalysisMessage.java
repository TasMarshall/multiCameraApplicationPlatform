package platform.jade.utilities;

import platform.core.goals.core.MultiCameraGoal;
import platform.core.imageAnalysis.ImageAnalysis;

import java.io.Serializable;
import java.util.*;

public class CameraAnalysisMessage implements Serializable {

    //Current goal list info -> Image Analysis Algorithms info by goal
    List<String> currentGoalsAnalysisIds = new ArrayList<>();
    HashMap<String,Set<ImageAnalysis>> currentGoalsAnalysisAlgorithms = new HashMap<>();



    public CameraAnalysisMessage(List<MultiCameraGoal> currentGoals) {

        for (MultiCameraGoal multiCameraGoal :currentGoals ) {
            currentGoalsAnalysisIds.add(multiCameraGoal.getId());
            currentGoalsAnalysisAlgorithms.put(multiCameraGoal.getId(),multiCameraGoal.getImageAnalysisAlgorithms());
        }

    }

    public List<String> getCurrentGoalsAnalysisIds() {
        return currentGoalsAnalysisIds;
    }

    public HashMap<String, Set<ImageAnalysis>> getCurrentGoalsAnalysisAlgorithms() {
        return currentGoalsAnalysisAlgorithms;
    }

}
