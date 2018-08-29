package platform.jade.utilities;

import platform.goals.MultiCameraGoal;
import platform.imageAnalysis.ImageAnalysis;

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

    @Override
    public boolean equals(Object o){

        boolean same = true;

        if (((CameraAnalysisMessage)o).currentGoalsAnalysisIds.size() != currentGoalsAnalysisIds.size()){
            same = false;
        }
        else {
            for (String s: ((CameraAnalysisMessage)o).currentGoalsAnalysisIds){
                if (!currentGoalsAnalysisIds.contains(s)){
                    same = false;
                }
            }
        }
        return same;
    }

}
