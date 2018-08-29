package platform.utilities;

import platform.camera.Camera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Heartbeat {

    int timer;
    private HashMap<String, Boolean> cameraHeartbeatReceived = new HashMap<>();

    public Heartbeat(int cameraMonitorTimer) {
        timer = cameraMonitorTimer;
    }

    public void init(List<Camera> cameraList){
        for (Camera camera: cameraList){
            cameraHeartbeatReceived.put(camera.getIdAsString(), false);
        }
    }

    public void recordHeartbeat(String id){
        cameraHeartbeatReceived.put(id, true);
    }

    public List<String> checkHeartBeats(){

        List<String> failedCameras = new ArrayList<>();

        for(String s: cameraHeartbeatReceived.keySet()){
            if (cameraHeartbeatReceived.get(s) == false){
                System.out.println("Heartbeat for Camera " + s + " not received, camera set off.");
                failedCameras.add(s);
            }
            else {
                System.out.println("Heartbeat for Camera " + s + " received.");
            }

            cameraHeartbeatReceived.replace(s,false);
        }

        return failedCameras;

    }

    public int getTimer() {
        return timer;
    }
}
