package platform.agents;

import platform.camera.components.CameraConfigurationFile;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;

public interface CameraMonitor {

    /**This function creates a simple camera object which the minimum test functions can be performed on */
    void initCamera(CameraConfigurationFile cameraConfigurationFile, Object[] args) throws FileNotFoundException, MalformedURLException;

    /**This function performs the test functions desired to determine the state of a camera*/
    void testCamera();

    /**This function adds a topic and behavior for testing and sending the test result to other agents*/
    void addCameraMonitorBehavior(Object[] args);

}

