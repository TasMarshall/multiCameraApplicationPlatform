package platform.utilities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import platform.core.camera.core.Camera;
import platform.core.camera.core.LocalONVIFCamera;
import platform.core.camera.core.components.CameraConfigurationFile;
import platform.core.cameraMonitor.core.CameraMonitor;
import platform.core.cameraMonitor.impl.LocalONVIFCameraMonitor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

@Service
public class CameraMonitorService {



    public List<Camera> getCameraStates() {
        List<Camera> cameras = new ArrayList<>();
        /*cameraMonitorRepository.findAll().forEach(cameras::add);*/
        return cameras;
    }

/*    public void addCameraState(String filepath){
        cameraRepository.save(new SimpleCameraState(filepath, "", false));
    }*/

/*    public void removeCameraFilepath(String id) {
        for(SimpleCameraState simpleCameraState: cameraRepository.findAll()) {
            //if valid input
            if (!id.equals("")) {

                if (simpleCameraState.filepath.equals(id) || simpleCameraState.cameraID.equals(id)){
                    cameraRepository.delete(simpleCameraState);
                }
            }
        }
    }*/

    public static void updateCameraState(String filepath, String id, boolean state, CameraRepository cameraRepository){
        for(SimpleCameraState simpleCameraState: cameraRepository.findAll()) {
            //if valid input
            if (!filepath.equals("")) {
                if (simpleCameraState.filepath.equals(filepath)) {
                    simpleCameraState.setCameraID(id);
                    simpleCameraState.isWorking = state;
                }
            }
            else if (!id.equals("")) {
                if (simpleCameraState.cameraID.equals(id)){
                    simpleCameraState.isWorking = state;
                }
            }
        }

    }

    public static List<? extends Camera> heartbeat(CameraRepository cameraRepository) {

        List<LocalONVIFCamera> cameras = new ArrayList<>();

        for (SimpleCameraState s: cameraRepository.findAll()){

            CameraConfigurationFile cameraConfigurationFile = new CameraConfigurationFile();
            try {
                Camera camera = cameraConfigurationFile.readFromCameraConfigurationFile(s.filepath);

                if(camera instanceof LocalONVIFCamera){
                    cameras.add((LocalONVIFCamera)camera);
                    updateCameraState(s.filepath,camera.getIdAsString(),false,cameraRepository); /// change to update not save
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        LocalONVIFCameraMonitor.heartbeat(cameras);

        for (LocalONVIFCamera localONVIFCamera: cameras){

            updateCameraState("",localONVIFCamera.getIdAsString(),localONVIFCamera.isWorking(),cameraRepository);

        }

        return cameras;

    }
}
