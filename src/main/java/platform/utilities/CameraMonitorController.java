package platform.utilities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import platform.core.camera.core.Camera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class CameraMonitorController {

    @Autowired
    HeartbeatProcess heartbeatProcess;

    private final CameraRepository cameraRepository;

    @Autowired
    CameraMonitorController(CameraRepository cameraRepository){
        this.cameraRepository = cameraRepository;
    }

    @RequestMapping("/cameraStates")
    public List<SimpleCameraState> sayHi(){
        List<SimpleCameraState> cameras = new ArrayList<>();
        cameraRepository.findAll().forEach(cameras::add);
        return cameras;
    }

    @RequestMapping("/hello")
    public long count(){

        return cameraRepository.count();
    }

    @RequestMapping(method= RequestMethod.POST,value ="/addCameras")
    public void addCameraFilepath(@RequestBody String cameraFilepath){

        String add = cameraFilepath.substring(1,cameraFilepath.length()-1);
        cameraRepository.save(new SimpleCameraState(add, "", false));
    }

    @RequestMapping(method= RequestMethod.POST,value ="/removeCameras")
    public void removeCameraFilepath(@RequestBody String cameraFilepath){
        for(SimpleCameraState simpleCameraState: cameraRepository.findAll()) {
            //if valid input
            if (!cameraFilepath.equals("")) {

                if (simpleCameraState.filepath.equals(cameraFilepath) || simpleCameraState.cameraID.equals(cameraFilepath)){
                    cameraRepository.delete(simpleCameraState);
                }
            }
        }
    }

}
