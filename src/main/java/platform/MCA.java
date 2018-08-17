package platform;

import platform.core.camera.core.Camera;
import platform.jade.utilities.CameraAnalysisMessage;

public interface MCA {


     void addMCAExecutionLoop();

     void addCameraMonitorListeners();

     void addUpdateCameraAnalysers(MultiCameraCore mcp_application);

     void addAnalysisResultListeners();

     void addSnapshotListener();

    /**This function adds a camera monitor.*/
     void addCameraMonitor(MultiCameraCore mcp_application, Camera camera);

    /**This function adds a camera stream analyzer.*/
     void addCameraStreamAnalyzer(MultiCameraCore mcp_application, Camera camera);

     void addCameraStreamCyclicUpdate(Camera camera);

    /**This function sends a message to the camera stream analyzer updating its algorithms*/
    void sendCameraAnalysisUpdate(Camera camera, CameraAnalysisMessage cameraAnalysisMessage);

    boolean addDataFusionAgent();
}

