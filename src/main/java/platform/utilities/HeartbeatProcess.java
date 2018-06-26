package platform.utilities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import platform.core.camera.core.Camera;

import java.util.HashMap;

@Service
public class HeartbeatProcess {

    @Autowired
    CameraRepository cameraRepository;

    //Executes each 30s
    @Scheduled(fixedDelay=30000)
    public void heartbeat() {

        HashMap<String,String> hashMap = new HashMap<>();

        for (Camera camera: CameraMonitorService.heartbeat(cameraRepository)){
            if (camera.isWorking()){
                hashMap.put(camera.getIdAsString(),"working");
            }
            else {
                hashMap.put(camera.getIdAsString(),"not working");
            }
        }

    }

}
