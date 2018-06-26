package platform.core.cameraMonitor.impl;

import platform.core.camera.core.Camera;
import platform.core.camera.impl.SimulatedCamera;
import platform.core.cameraMonitor.core.CameraMonitor;

import java.util.ArrayList;
import java.util.List;

public class SimulatedCameraMonitor extends CameraMonitor {

    public SimulatedCameraMonitor(List<Camera> cameras) {
        super(cameras);
    }

    @Override
    public List<? extends Camera> getCameras() {
        List<SimulatedCamera> localONVIFCameras = new ArrayList<SimulatedCamera>();
        for (int i = 0; i < getCameras2().size(); i++) {
            localONVIFCameras.add((SimulatedCamera) getCameras2().get(i));
        }

        return localONVIFCameras;
    }

}
