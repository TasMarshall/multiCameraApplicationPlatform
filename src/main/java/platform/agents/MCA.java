package platform.agents;

import platform.MultiCameraCore;
import platform.camera.Camera;
import platform.jade.utilities.CameraAnalysisMessage;

public interface MCA {


    /**This function adds the multi-camera application behavioral loop as a repeating behavior to the agent platform, for example JADE.*/
     void addMCAExecutionLoop();

    /**This function adds listeners to the camera monitor heartbeat messages*/
     void addCameraMonitorListeners();

    /**This function adds listeners to the analysis agents for analysis result messages from the data fusion agent*/
     void addAnalysisResultListeners();

    /**This function is an alternate implementation of a manual snap shot behavior, it requires implmenting a listener to a component which sends a snapshot confirmation message*/
     void addSnapshotListener();

    /**This function adds a camera monitor.*/
     void addCameraMonitor(MultiCameraCore mcp_application, Camera camera);

    /**This function adds a camera stream analyzer.*/
     void addCameraStreamAnalyzer(MultiCameraCore mcp_application, Camera camera);

     /**This function adds a behavior to update a cameras analysis agent of the cameras new goal and hence image analysis algorithms*/
     void addCameraStreamCyclicUpdate(Camera camera);

    /**This function sends a message to the camera stream analyzer updating its algorithms*/
    void sendCameraAnalysisUpdate(Camera camera, CameraAnalysisMessage cameraAnalysisMessage);

    /**This function adds the data fusion agent */
    boolean addDataFusionAgent();
}

