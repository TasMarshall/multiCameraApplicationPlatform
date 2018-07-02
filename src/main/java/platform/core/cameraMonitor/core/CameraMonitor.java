package platform.core.cameraMonitor.core;

import platform.core.camera.core.Camera;
import platform.core.camera.core.components.ViewCapabilities;
import platform.core.utilities.CustomID;
import platform.core.utilities.NanoTimeValue;
import platform.core.utilities.mapeLoop;

import java.util.*;

public abstract class CameraMonitor implements mapeLoop {

    private String id = UUID.randomUUID().toString();

    private List<Camera> cameras = new ArrayList<Camera>();
    private Map<String, Camera> cameraIdMap = new HashMap<String,Camera>();

    //camera heatbeat function
    private NanoTimeValue lastHeartBeatTime;
    private double heartBeatTimer;
    private boolean heartBeatIsActive = false;

    //Camera recovery function
    private NanoTimeValue lastCameraRecoveryTime;
    private double cameraRecoveryTimer;
    private boolean cameraRecoveryIsActive = false;
    private List<Camera> camerasToReInit;

    public CameraMonitor (List<Camera> cameras){

        addAndInitCameras(cameras);

        startHeartbeat(30);
        startCameraRecoverer(45);

    }

    public void initCameras(){

        for (Camera camera: cameras){
            initCamera(camera);
        }

    }

    public void reinitNotWorkingCameras(){

        for (Camera camera: camerasToReInit){
            if (!camera.isWorking()){

                initCamera(camera);

/*                if(camera.isWorking()){
                    camera.getCameraStreamManager().updateStreams();
                }*/
            }
        }
    }

    public void initCamera(Camera camera) {

        boolean success = camera.init();
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

    public List<? extends Camera> getWorkingCameras() {

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

    public abstract List<? extends Camera> getCameras();

    public List<Camera> getCameras2(){
        return cameras;
    }

    public void addAndInitCameras(List<Camera> cameras) {

        this.cameras = cameras;

        initCameras();

        for (Camera camera: this.cameras){
            cameraIdMap.put(camera.getIdAsString(),camera);
        }


    }

    @Override
    public void monitor(){

/*        for (Camera camera: cameras){
            camera.monitor();
        }*/

        //camera heartbeat
        if(isHeartBeatIsActive()){
            pulseHeartbeat();
        }

    }

    @Override
    public void analyse(){

/*        for (Camera camera: cameras){
            camera.analyse();
        }*/

        if (isCameraRecoveryIsActive()) {
            findCamerasToRecover();
        }

    }

    @Override
    public void plan(){

 /*       for (Camera camera: cameras){
            camera.plan();
        }*/

    }

    @Override
    public void execute(){

/*        for (Camera camera: cameras){
            camera.execute();
        }*/

        if(isCameraRecoveryIsActive()) {
            recoverCameras();
        }

    }

    public void heartBeat() {

        System.out.println("Heartbeat.");

        for (Camera camera: cameras) {
            if (camera.isWorking()) {
                camera.setWorking(camera.simpleUnsecuredFunctionTest());
                if(!camera.isWorking()) System.out.println(camera.getIdAsString() + " failed heartbeat test.");
            }
        }

    }

    public void startHeartbeat(double hbPeriodSeconds) {

        setHeartBeatTimer(hbPeriodSeconds);
        setHeartBeatIsActive(true);
        setLastHeartBeatTime(new NanoTimeValue(System.nanoTime()));

    }

    private void pulseHeartbeat() {

        NanoTimeValue currentTime = new NanoTimeValue(System.nanoTime());
        double hbT = (currentTime.value - getLastHeartBeatTime().value) / 1000000000.0;
        if (hbT > getHeartBeatTimer()) {
            heartBeat();
            setLastHeartBeatTime(new NanoTimeValue(currentTime.value));
        }
    }

    public void stopHeartbeat(){

        setHeartBeatIsActive(false);

    }

    public void startCameraRecoverer(double recoveryPeriodSeconds) {

        cameraRecoveryTimer = recoveryPeriodSeconds;
        cameraRecoveryIsActive = true;
        lastCameraRecoveryTime = new NanoTimeValue(System.nanoTime());

    }

    private void findCamerasToRecover() {

        NanoTimeValue currentTime = new NanoTimeValue(System.nanoTime());
        double cRT = (currentTime.value - lastCameraRecoveryTime.value) / 1000000000.0;
        if (cRT > cameraRecoveryTimer) {
            camerasToReInit = new ArrayList<Camera>();
            camerasToReInit.addAll(getNotWorkingCameras());
        }

    }

    private void recoverCameras() {
        NanoTimeValue currentTime = new NanoTimeValue(System.nanoTime());
        double cRT = (currentTime.value - lastCameraRecoveryTime.value) / 1000000000.0;
        if (cRT > cameraRecoveryTimer) {
            reinitNotWorkingCameras();
            lastCameraRecoveryTime.value = currentTime.value;
        }
    }

    public void stopCameraRecovery(){
        cameraRecoveryIsActive = false;
    }

    ///////////////////////////////////////////////////////////////////////////
    /////                       GETTERS AND SETTERS                       /////
    ///////////////////////////////////////////////////////////////////////////

    public Camera getCameraByID(String cameraID){
        return cameraIdMap.get(cameraID);
    }

    public NanoTimeValue getLastHeartBeatTime() {
        return lastHeartBeatTime;
    }

    public void setLastHeartBeatTime(NanoTimeValue lastHeartBeatTime) {
        this.lastHeartBeatTime = lastHeartBeatTime;
    }

    public double getHeartBeatTimer() {
        return heartBeatTimer;
    }

    public void setHeartBeatTimer(double heartBeatTimer) {
        this.heartBeatTimer = heartBeatTimer;
    }

    public boolean isHeartBeatIsActive() {
        return heartBeatIsActive;
    }

    public void setHeartBeatIsActive(boolean heartBeatIsActive) {
        this.heartBeatIsActive = heartBeatIsActive;
    }

    public NanoTimeValue getLastCameraRecoveryTime() {
        return lastCameraRecoveryTime;
    }

    public void setLastCameraRecoveryTime(NanoTimeValue lastCameraRecoveryTime) {
        this.lastCameraRecoveryTime = lastCameraRecoveryTime;
    }

    public double getCameraRecoveryTimer() {
        return cameraRecoveryTimer;
    }

    public void setCameraRecoveryTimer(double cameraRecoveryTimer) {
        this.cameraRecoveryTimer = cameraRecoveryTimer;
    }

    public boolean isCameraRecoveryIsActive() {
        return cameraRecoveryIsActive;
    }

    public void setCameraRecoveryIsActive(boolean cameraRecoveryIsActive) {
        this.cameraRecoveryIsActive = cameraRecoveryIsActive;
    }

}
