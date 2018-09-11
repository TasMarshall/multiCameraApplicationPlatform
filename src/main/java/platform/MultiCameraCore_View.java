package platform;

import platform.camera.Camera;
import platform.camera.CameraView;
import platform.goals.MultiCameraGoal;
import platform.goals.MultiCameraGoal_View;
import platform.jade.utilities.CombinedAnalysisResultsMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiCameraCore_View implements Serializable {

    List<CameraView> cameraViewList = new ArrayList<>();
    List<MultiCameraGoal_View> multiCameraGoal_views = new ArrayList<>();
    private CombinedAnalysisResultsMessage mostRecentResults;


    public MultiCameraCore_View (MultiCameraCore multiCameraCore){

        for (Camera camera: multiCameraCore.getAllCameras()){
            cameraViewList.add(new CameraView(camera));
        }

        for (MultiCameraGoal multiCameraGoal : multiCameraCore.getMultiCameraGoals()){
            multiCameraGoal_views.add(new MultiCameraGoal_View(multiCameraGoal));
        }

    }

    public String viewToString(){

        String view;

        view = "Multi-camera Application View State: \nCameras [ ";

        for (CameraView cameraView: cameraViewList){

            view += " ID: ";
            view += cameraView.getIdAsString() + ", ";

            view += "IP: ";
            view += cameraView.getIP() + ", ";

            view += "WORKING: ";
            view += cameraView.isWorking() + ", ";

            view += "CURRENT GOALS:";

            for (String s: cameraView.getCurrentGoalIds()) {
                view += " ID: " + s;
                view += ",";

                if (mostRecentResults!=null) {
                    if (mostRecentResults.getCombinedResultMap() != null && mostRecentResults.getCombinedResultMap().get(s) != null && mostRecentResults.getCombinedResultMap().get(s).get(cameraView.getIdAsString())!=null) {

                        view += "Most Recent Results: ";
                        Map<String, Serializable> goalResultMap = mostRecentResults.getCombinedResultMap().get(s).get(cameraView.getIdAsString());
                        for (String resultName : goalResultMap.keySet()) {
                            view += resultName + ", ";
                        }

                        view += ";";
                    }
                }

            }

            view += ";";

        }

        view += "] \nGoals [ ";

        for (MultiCameraGoal_View multiCameraGoal_view : multiCameraGoal_views){
            view += " ID: " + multiCameraGoal_view.getId() + ", Activated: " + multiCameraGoal_view.isActivated() + ";";
        }

        view += "] ";



        return view;
    }

    public void addResults( CombinedAnalysisResultsMessage mostRecentResults) {
        this.mostRecentResults = mostRecentResults;
    }
}
