package platform.core.camera.core;

import com.sun.javafx.geom.Vec3d;
import de.onvif.soap.OnvifDevice;
import de.onvif.soap.devices.PtzDevices;
import org.onvif.ver10.device.wsdl.GetDeviceInformationResponse;
import org.onvif.ver10.device.wsdl.Service;
import org.onvif.ver10.schema.*;
import platform.core.camera.core.components.CameraLocation;
import platform.core.camera.core.components.CurrentView;
import platform.core.camera.core.components.PTZControlDomain;
import platform.core.camera.core.components.ViewCapabilities;
import platform.core.goals.core.MultiCameraGoal;

import javax.xml.soap.SOAPException;
import java.lang.Object;
import java.net.ConnectException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.me.javawsdiscovery.DeviceDiscovery.discoverWsDevicesAsUrls;

public abstract class LocalONVIFCamera extends Camera {

    protected OnvifDevice onvifDevice;
    protected List<Service> services;
    protected List<Profile> profiles;
    protected String profileToken;
    protected PtzDevices ptzDevices;

    protected boolean canInstantiate = true;
    protected boolean canRequestRTSPStream = true;
    protected boolean canRequestProfiles = true;
    protected boolean canRequestServices = true;
    protected boolean canRequestInfo = true;
    protected boolean canRequestPTZStatus = true;

    public LocalONVIFCamera(String id, URL url, String username, String password, ViewCapabilities viewCapabilities, Vec3d globalVector, CameraLocation location, List<MultiCameraGoal> multiCameraGoalList, Map<String, Object> additionalAttributes) {
        super(id, url, username, password, viewCapabilities, globalVector, location, multiCameraGoalList, additionalAttributes);
    }

    public void simpleInit(){

        try {
            onvifDevice = new OnvifDevice(getIP().toString(),getUsername(),getPassword());
            if (onvifDevice == null) {
                canInstantiate = false;
                System.err.println("Could not instantiate device as OnvifDevice.");
            }
            else {
                canInstantiate = true;
            }
        }
        catch (ConnectException | SOAPException e1) {
            canInstantiate = false;
            System.err.println("Could not instantiate device as OnvifDevice.");
        }

    }

    @Override
    public boolean connectToCamera() {

        System.out.println("Connect to camera, please wait ...");

        simpleInit();

        if (canInstantiate) {

            if (services == null) {

                services = onvifDevice.getDevices().getServices(false);

                if (services == null || services.size() == 0) {
                    System.out.println("Can not get service information from camera through ONVIF protocol.");
                    canRequestServices = false;
                }
                else {
                    canRequestServices = true;
                }
            }

            if (profiles == null) {
                profiles = onvifDevice.getDevices().getProfiles();

                if (profiles == null) {
                    canRequestProfiles = false;
                    System.out.println("Can not get profile information from camera through ONVIF protocol.");
                }
                else{
                    canRequestProfiles = true;
                    profileToken = profiles.get(0).getToken();
                    ptzDevices = onvifDevice.getPtz();
                }
            }

            if (getStreamURI() == null || getStreamURI().equals("")){
                String streamURI = onvifDevice.getMedia().getRTSPStreamUri(1);
                if (streamURI == null) {
                    System.out.println("Can not get stream uri from camera through ONVIF protocol.");
                    canRequestRTSPStream = false;

                    try {

                        String prot = (String)getAdditionalAttributes().get("streamProt");
                        String ext = (String)getAdditionalAttributes().get("streamExt");

                        streamURI = prot + "://" + getUrl().getHost() + ":" + ext;

                        setStreamURI(streamURI);
                    }
                    catch (Exception e){
                        System.out.println("Stream URI not manually set, camera will not work, exeception thrown.");
                        return false;
                    }
                }
                else {
                    canRequestRTSPStream = true;
                    setStreamURI(streamURI);
                }
            }

            if (!(getViewCapabilities().getPtzType().contains(ViewCapabilities.PTZ.Nil))) {

                PTZStatus ptzStatus = onvifDevice.getPtz().getStatus(profileToken);

                if (ptzStatus == null){
                    System.out.println("Can not request ptz status from camera through ONVIF protocol");
                    canRequestPTZStatus = false;
                }
                else{
                    canRequestPTZStatus = true;

                    PTZVector ptzVector = ptzStatus.getPosition();

                    setCurrentView(new CurrentView(this,ptzVector));
                }
            }
        }

        return canInstantiate;

    }

    @Override
    public String getCameraUniqueIdentifier() {

        GetDeviceInformationResponse deviceInformation = onvifDevice.getDevices().getDeviceInformation();
        return deviceInformation.getSerialNumber();

    }


    public PTZControlDomain acquireCameraPTZCapabilities() {
        System.out.println("Acquiring PTZ Capabilities.");

        PTZControlDomain ptzControlDomain;

        List<Profile> profileList = onvifDevice.getDevices().getProfiles();
        String token = profileList.get(0).getToken();

        if (onvifDevice.getPtz().isPtzOperationsSupported(token)){
            //get pan, tilt and zoom ranges
            ptzControlDomain = new PTZControlDomain(onvifDevice.getPtz().getPanSpaces(token),onvifDevice.getPtz().getTiltSpaces(token),onvifDevice.getPtz().getZoomSpaces(token));
            System.out.println("PTZ Capabilities acquired.");
        }
        else {
            //ptzCapabilities.setPTZ(false);
            ptzControlDomain = null;
            System.out.println("PTZ Capabilities could not be acquired.");
        }

        return ptzControlDomain;

    }

    @Override
    public CurrentView getCameraCurrentView() {

        CurrentView currentView;

        System.out.println("Requesting current camera view.");
        if (canRequestPTZStatus){

            PTZStatus ptzStatus = onvifDevice.getPtz().getStatus(profileToken);
            PTZVector ptzVector = ptzStatus.getPosition();

            System.out.println("Camera view request successful.");

            currentView =  new CurrentView(this,ptzVector);
        }
        else {
            System.out.println("Camera view request failed as ONVIF library / protocol does not work with camera.");
            currentView = null;

        }

        return currentView;
    }

    @Override
    public boolean simpleUnsecuredFunctionTest() {
        System.out.println("Simple function Test.");

        Date nvtDate = null;
        try {
            if (onvifDevice != null && onvifDevice.isOnline()) {
                nvtDate = onvifDevice.getDevices().getDate();
                if (nvtDate == null) {
                    return false;
                }
                System.out.println("Date Function Test Result:" + new SimpleDateFormat().format(nvtDate));
                return true;
            }
            else {
                System.out.println("Date function test failed.");
                return false;
            }
        }
        catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean simpleSecuredFunctionTest() {
        System.out.println("Simple secured function Test.. not implemented.");

       /* List<Profile> profiles = onvifDevice.getDevices().getProfiles();
        for (Profile p : profiles) {
            System.out.println("URL von Profil \'" + p.getName() + "\': " + onvifDevice.getMedia().getSnapshotUri(p.getToken()));
        }*/

       return true;

    }

    @Override
    public boolean videoSimpleFunctionTest() {
        System.out.println("Simple video function Test.");

        String imgURI = onvifDevice.getMedia().getSnapshotUri(profileToken);

        if (imgURI == null){
            return false;
        }
        else{
            return true;
        }

    }

    @Override
    public boolean videoFunctionTest() {

        System.out.println("Video function Test.. not implemented.");
        //todo
        return true;
    }

    @Override
    public boolean pvtSimpleMotionFunctionTest() {

        System.out.println("Simple motion test started.");

        PtzDevices ptzDevices = onvifDevice.getPtz();

        boolean success = commandPTZStop();

        System.out.println("Simple motion test complete.");

        return success;
    }


    @Override
    public boolean pvtMotionFunctionTest() {
        System.out.println("Complete motion test started.");

        commandPTZByIMGTest();

        System.out.println("Simple motion test ended.");
        return true;
    }

    @Override
    public boolean commandPTZByIMGTest(){

        System.out.println("Motion function Test.. image comparison not implemented.");

        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();

        Vector2D vector2D = new Vector2D();
        vector2D.setX((float)-45);
        vector2D.setY((float)-45);

        Vector1D vector1D = new Vector1D();
        vector1D.setX((float)0);

        PTZVector ptzVectorCommand = new PTZVector();
        ptzVectorCommand.setPanTilt(vector2D);
        ptzVectorCommand.setZoom(vector1D);

        while( currentTime  - startTime < 1000) {

            commandPTZMovement(ptzVectorCommand);
            currentTime = System.currentTimeMillis();
        }

        commandPTZStop();

        startTime = System.currentTimeMillis();

        vector2D.setX((float)+45);
        vector2D.setY((float)+45);
        vector1D.setX((float)0);

        ptzVectorCommand.setPanTilt(vector2D);
        ptzVectorCommand.setZoom(vector1D);

        while( currentTime - startTime < 1000) {
            commandPTZMovement(ptzVectorCommand);
            currentTime = System.currentTimeMillis();
        }

        return commandPTZStop();
    }

    public static Collection<URL> findONVIFCameraURLs()
    {

        System.out.println("Searching for Devices..");
        Collection<URL> urls = discoverWsDevicesAsUrls();

        for(URL url: urls){
            System.out.println(url.toString());
        }
        System.out.println("Search complete.");

        return urls;

    }

    @Override
    /**
     *
     * Command the camera to move according to its {@link PTZControlDomain} type, i.e. abs, cont or rel. Only
     * CONT is implemented, and continuous control only needs a positive or negative command to move in the positive or negative
     * axis direction and hence when using for a continuous control device the in to out of a particular axis is,
     * negative -> go negative, positive -> go positive, zero -> stop.
     *
     */
    public boolean commandPTZMovement(PTZVector ptzVector) {

        System.out.println("Camera move commanded.");

        boolean success = false;

        if (getViewCapabilities().getPtzControl() == ViewCapabilities.PTZControl.ABS){

            success = false;

        }
        else if (getViewCapabilities().getPtzControl() == ViewCapabilities.PTZControl.CONT){

            PTZVector ptzVec = getViewCapabilities().getPTZCommandFmDomain(ptzVector);

            success = ptzDevices.continuousMove(profileToken, (float)ptzVec.getPanTilt().getX(), (float)ptzVec.getPanTilt().getY(), (float)ptzVec.getZoom().getX());

        }
        else if (getViewCapabilities().getPtzControl() == ViewCapabilities.PTZControl.REL){

            success = false;

        }

        System.out.println("Camera continuous movement complete.");


        return success;
    }

    @Override
    public boolean commandPTZStop(){

        boolean success = false;

        if (getViewCapabilities().getPtzControl() == ViewCapabilities.PTZControl.ABS){

            success = false;

        }
        else if (getViewCapabilities().getPtzControl() == ViewCapabilities.PTZControl.CONT) {

            success = ptzDevices.continuousMove(profileToken, (float) 0, (float) 0, (float) 0);

        }
        else if (getViewCapabilities().getPtzControl() == ViewCapabilities.PTZControl.REL){

            success = false;

        }

        return success;

    }

}
