package platform;

import platform.camera.Camera;
import platform.camera.CameraView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MultiCameraCore_View implements Serializable {

    List<CameraView> cameraViewList = new ArrayList<>();

    public MultiCameraCore_View (MultiCameraCore multiCameraCore){

        for (Camera camera: multiCameraCore.getAllCameras()){
            cameraViewList.add(new CameraView(camera));
        }

    }

    public String viewToString(){

        String view;

        view = "Multi-camera Application View State, Cameras [ ";

        for (CameraView cameraView: cameraViewList){

            view += "ID: ";
            view += cameraView.getIdAsString() + ", ";

            view += "IP: ";
            view += cameraView.getIP() + ", ";

            view += "WORKING: ";
            view += cameraView.isWorking() + ", ";

            view += "CURRENT GOALS:";

            for (String s: cameraView.getCurrentGoalIds()) {
                view += " " + s;
                view += ",";
            }

            view += ";";

        }

        view += "]";

        return view;
    }

}
