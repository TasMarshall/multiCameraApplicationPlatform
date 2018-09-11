package platform.cameraManager;

import platform.MultiCameraCore;
import platform.camera.Camera;
import platform.camera.components.ViewCapabilities;
import platform.goals.MultiCameraGoal;
import platform.utilities.CustomID;

import java.util.*;

public class CameraManager {

    private String id = UUID.randomUUID().toString();

    private List<Camera> cameras = new ArrayList<Camera>();
    private Map<String, Camera> cameraIdMap = new HashMap<String,Camera>();

    public CameraManager(List<Camera> cameras){

        addAndInitCameras(cameras);


    }

    /** This function initializes all cameras in the camera manager*/
    public void initCameras(){

        for (Camera camera: cameras){
            initCamera(camera);
        }

    }

    /** This function performs the minimal required initialization and testing with a camera to determine if it is working */
    public static List<Camera> heartbeat(List<Camera> cameras){
        List<Camera> workingCameras = new ArrayList<>();

        for (Camera camera: cameras){
            camera.simpleInit();
            camera.setWorking(camera.simpleUnsecuredFunctionTest());
            if(!camera.isWorking()) System.out.println(camera.getIdAsString() + " failed heartbeat test.");
        }

        return workingCameras;
    }

    public void reinitNotWorkingCameras(MultiCameraCore mcc){

        for (Camera camera: getNotWorkingCameras()){
            if (camera.getCameraState().isReconnectable()){

                for (MultiCameraGoal m: mcc.getMultiCameraGoals()){
                    if (m.isActivated()){
                        if (camera.inRange(m.getMap())){

                            if (camera.reconnectToCamera()){
                                camera.setWorking(true);
                                camera.getCameraState().setReconnectable(false);

                                camera.addMultiCameraGoal(m);
                                m.getCameras().add(camera);
                            };

                        }
                    }
                }
            }
        }
    }

    public void initCamera(Camera camera) {

        boolean success = camera.inititializeCamera();
        camera.setWorking(success);

        if (camera.getId() == null){
            camera.setId(new CustomID(UUID.randomUUID().toString()));
            System.out.println("Camera without ID created, id set as: " + camera.getIdAsString());
        }

        if (success){

            boolean vidSuccess = camera.videoSimpleFunctionTest();
            camera.setWorking(vidSuccess);

            if (!camera.getViewCapabilities().getPtzType().contains(ViewCapabilities.PTZ.Nil)) {
                boolean motSuccess = camera.pvtSimpleMotionFunctionTest();
                camera.setPTZWorking(motSuccess);
            }

            camera.getCameraState().initialized = true;

            System.out.println("Camera " + camera.getIdAsString() + " successfully initialized.");

        }
        else {
            System.out.println("Camera " + camera.getIdAsString() + " failed to initialize.");
        }


        /*camera.setAnalysisManager(new AnalysisManager(camera));*/

    }

    public List<Camera> getWorkingCameras() {

        List<Camera> cameras = new ArrayList<Camera>();

        for (Camera camera: this.cameras){
            if(camera.isWorking()){
                cameras.add(camera);
            }
        }

        return cameras;

    }

    public List<? extends Camera> getNotWorkingCameras() {

        List<Camera> cameras = new ArrayList<Camera>();

        for (Camera camera: this.cameras){
            if(!camera.isWorking()){
                cameras.add(camera);
            }
        }

        return cameras;

    }

    public void testSimpleAllCameras () {
        for (Camera camera: cameras) {
            if (camera.isWorking()) {
                camera.setWorking(camera.simpleUnsecuredFunctionTest());
            }
        }
    }

    /*public void testVideoFunctionalityOfCameras () {

        for (Camera camera: cameras) {
            if (camera.isWorking()) {

                boolean simpleVideoWorking = camera.videoSimpleFunctionTest();
                camera.setWorking(simpleVideoWorking);

                boolean videoWorking = camera.videoFunctionTest();
                camera.setWorking(videoWorking);

            }
        }
    }

    public void testCompleteFunctionalityOfCameras () {

        for (Camera camera: cameras) {
            if (camera.isWorking()) {

                camera.simpleUnsecuredFunctionTest();
                camera.simpleSecuredFunctionTest();
                camera.videoFunctionTest();
                if (camera.getPtzCapability() != CameraCore.PTZ.Nil)
                {
                    camera.pvtMotionFunctionTest();
                }

            }
        }
    }*/

    public List<Camera> getCameras(){
        return cameras;
    }


    public void addAndInitCameras(List<Camera> cameras) {

        this.cameras = cameras;

        initCameras();

        for (Camera camera: this.cameras){
            cameraIdMap.put(camera.getIdAsString(),camera);
        }


    }

    ///////////////////////////////////////////////////////////////////////////
    /////                       GETTERS AND SETTERS                       /////
    ///////////////////////////////////////////////////////////////////////////

    public Camera getCameraByID(String cameraID){
        return cameraIdMap.get(cameraID);
    }

}
