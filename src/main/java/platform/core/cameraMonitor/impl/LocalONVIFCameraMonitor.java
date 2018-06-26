package platform.core.cameraMonitor.impl;

import platform.core.camera.core.Camera;
import platform.core.camera.core.LocalONVIFCamera;
import platform.core.cameraMonitor.core.CameraMonitor;

import java.util.ArrayList;
import java.util.List;

public class LocalONVIFCameraMonitor extends CameraMonitor {

    public static List<LocalONVIFCamera> heartbeat(List<LocalONVIFCamera> cameras){
        List<LocalONVIFCamera> workingCameras = new ArrayList<>();

        for (LocalONVIFCamera camera: cameras){
            camera.simpleInit();
            camera.setWorking(camera.simpleUnsecuredFunctionTest());
            if(!camera.isWorking()) System.out.println(camera.getIdAsString() + " failed heartbeat test.");
        }

        return workingCameras;
    }

    public LocalONVIFCameraMonitor(List<Camera> cameras) {
        super(cameras);
    }

    @Override
    public List<? extends Camera> getCameras() {
        List<LocalONVIFCamera> localONVIFCameras = new ArrayList<LocalONVIFCamera>();
        for (int i = 0; i < getCameras2().size(); i++) {
            localONVIFCameras.add((LocalONVIFCamera)getCameras2().get(i));
        }
        return localONVIFCameras;
    }

}
