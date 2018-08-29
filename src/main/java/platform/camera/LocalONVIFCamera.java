package platform.camera;


import de.onvif.soap.OnvifDevice;
import de.onvif.soap.devices.PtzDevices;
import org.onvif.ver10.device.wsdl.GetDeviceInformationResponse;
import org.onvif.ver10.device.wsdl.Service;
import org.onvif.ver10.schema.PTZStatus;
import org.onvif.ver10.schema.Profile;
import platform.camera.components.*;
import platform.jade.ModelAgent;

import javax.xml.soap.SOAPException;
import java.io.IOException;
import java.lang.Object;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.me.javawsdiscovery.DeviceDiscovery.discoverWsDevicesAsUrls;

public class LocalONVIFCamera extends Camera {

    private final static Logger LOGGER = Logger.getLogger(ModelAgent.class.getName());

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

    public LocalONVIFCamera(String id, URL url, String username, String password, ViewCapabilities viewCapabilities, Vector3D globalVector, CameraLocation location, Map<String, Object> additionalAttributes) {

        super(id, url, username, password, viewCapabilities, globalVector, location, additionalAttributes);

        LOGGER.setLevel(Level.CONFIG);

        LOGGER.config("Onvif Camera created.");

    }

    @Override
    public void canConnectAndSimpleInit(){

        try {
            onvifDevice = new OnvifDevice(getIP().toString(),getUsername(),getPassword());
            if (onvifDevice == null) {
                canInstantiate = false;

                LOGGER.severe("Could not instantiate device as OnvifDevice.");
            }
            else {
                canInstantiate = true;

                LOGGER.config("Device instantiated as OnvifDevice.");
            }
        }
        catch (ConnectException | SOAPException e1) {
            canInstantiate = false;

            LOGGER.severe("Could not instantiate device as OnvifDevice.");
        }

    }

    public boolean reconnectToCamera(){

        if (canInstantiate && canRequestProfiles) {
                profiles = onvifDevice.getDevices().getProfiles();

                if (profiles == null) {
                    canRequestProfiles = false;

                    LOGGER.severe("Can not get profile information from camera through ONVIF protocol.");
                }
                else{
                    canRequestProfiles = true;
                    profileToken = profiles.get(0).getToken();
                    ptzDevices = onvifDevice.getPtz();
                }

                commandPTZStop();

        }
        else {
            connectToCamera();
        }

        if (canRequestProfiles && canInstantiate){
            return true;
        }
        else {
            return false;
        }

    }

    @Override
    public boolean connectToCamera() {

        LOGGER.info("Connect to camera...");

        simpleInit();

        if (canInstantiate) {

            if (services == null) {

                services = onvifDevice.getDevices().getServices(false);

                if (services == null || services.size() == 0) {

                    LOGGER.config("Can not get service information from camera through ONVIF protocol.");
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

                    LOGGER.severe("Can not get profile information from camera through ONVIF protocol.");
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

                    LOGGER.severe("Can not get stream uri from camera through ONVIF protocol.");
                    canRequestRTSPStream = false;

                    try {

                        String prot = (String)getAdditionalAttributes().get("streamProt");
                        String ext = (String)getAdditionalAttributes().get("streamExt");

                        streamURI = prot + "://" + getUrl().getHost() + ":" + ext;

                        setStreamURI(streamURI);
                    }
                    catch (Exception e){

                        LOGGER.severe("Stream URI required but not manually set, camera will not work, exception thrown.");
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
                    LOGGER.config("Can not request ptz status from camera through ONVIF protocol.");
                    canRequestPTZStatus = false;
                }
                else{
                    canRequestPTZStatus = true;

                    org.onvif.ver10.schema.PTZVector ptzVector = ptzStatus.getPosition();
                    platform.camera.components.PTZVector ptzVector1 = new platform.camera.components.PTZVector();

                    platform.camera.components.Vector1D vector1D = new platform.camera.components.Vector1D();
                    vector1D.setX(ptzVector.getZoom().getX());

                    ptzVector1.setZoom(vector1D);

                    platform.camera.components.Vector2D vector2D = new platform.camera.components.Vector2D();
                    vector2D.setX(ptzVector.getPanTilt().getX());
                    vector2D.setY(ptzVector.getPanTilt().getY());

                    ptzVector1.setPanTilt(vector2D);

                    setCurrentView(new CurrentView(this,ptzVector1));
                }
            }
        }

        return canInstantiate;

    }

    @Override
    public String getCameraUniqueIdentifier() {


        LOGGER.info("Acquiring Unique Identifier.");

        GetDeviceInformationResponse deviceInformation = onvifDevice.getDevices().getDeviceInformation();

        LOGGER.info("Unique Identifier Acquired " + deviceInformation.getSerialNumber());

        return deviceInformation.getSerialNumber();



    }


    public PTZControlDomain acquireCameraPTZCapabilities() {

        LOGGER.info("Acquiring PTZ Capabilities.");

        PTZControlDomain ptzControlDomain;

        List<Profile> profileList = onvifDevice.getDevices().getProfiles();
        String token = profileList.get(0).getToken();

        if (onvifDevice.getPtz().isPtzOperationsSupported(token)){
            //get pan, tilt and zoom ranges
            org.onvif.ver10.schema.FloatRange pan1 = onvifDevice.getPtz().getPanSpaces(token);
            org.onvif.ver10.schema.FloatRange tilt1 = onvifDevice.getPtz().getTiltSpaces(token);
            org.onvif.ver10.schema.FloatRange zoom1 = onvifDevice.getPtz().getZoomSpaces(token);

            platform.camera.components.FloatRange pan = new platform.camera.components.FloatRange();
            pan.setMax(pan1.getMax());
            pan.setMin(pan1.getMin());

            platform.camera.components.FloatRange tilt = new platform.camera.components.FloatRange();
            tilt.setMax(tilt1.getMax());
            tilt.setMin(tilt1.getMin());

            platform.camera.components.FloatRange zoom = new platform.camera.components.FloatRange();
            zoom.setMax(zoom1.getMax());
            zoom.setMin(zoom1.getMin());

            ptzControlDomain = new PTZControlDomain(pan,tilt,zoom);

            LOGGER.info("PTZ Capabilities acquired.");
        }
        else {
            //ptzCapabilities.setPTZ(false);
            platform.camera.components.FloatRange floatRange = new platform.camera.components.FloatRange();
            floatRange.setMin(-1);
            floatRange.setMax(1);

            ptzControlDomain = new PTZControlDomain(floatRange,floatRange,floatRange);

            LOGGER.info("PTZ Capabilities could not be acquired.");
        }

        return ptzControlDomain;

    }

    @Override
    public CurrentView getCameraCurrentView() {

        CurrentView currentView;

        LOGGER.info("Requesting current camera view.");
        if (canRequestPTZStatus){

            PTZStatus ptzStatus = onvifDevice.getPtz().getStatus(profileToken);
            org.onvif.ver10.schema.PTZVector ptzVector = ptzStatus.getPosition();

            platform.camera.components.PTZVector ptzVector1 = new platform.camera.components.PTZVector();

            platform.camera.components.Vector1D vector1D = new platform.camera.components.Vector1D();
            vector1D.setX(ptzVector.getZoom().getX());

            ptzVector1.setZoom(vector1D);

            platform.camera.components.Vector2D vector2D = new platform.camera.components.Vector2D();
            vector2D.setX(ptzVector.getPanTilt().getX());
            vector2D.setY(ptzVector.getPanTilt().getY());

            ptzVector1.setPanTilt(vector2D);

            currentView =  new CurrentView(this,ptzVector1);


            LOGGER.fine("Camera view request successful.");
            System.out.println("");
        }
        else {

            LOGGER.fine("Camera view request failed as ONVIF library / protocol does not work with camera.");
            currentView = null;

        }

        return currentView;
    }

    @Override
    public boolean simpleUnsecuredFunctionTest() {


        LOGGER.info("Camera " + getIdAsString() + " Simple function Test.");

        Date nvtDate = null;
        try {
            if (onvifDevice != null && isOnline()) {
                nvtDate = onvifDevice.getDevices().getDate();
                if (nvtDate == null) {
                    return false;
                }

                LOGGER.fine("Date Function Test Result:" + new SimpleDateFormat().format(nvtDate));
                return true;
            }
            else {
                LOGGER.fine("Date function test failed.");
                return false;
            }
        }
        catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean simpleSecuredFunctionTest() {

        LOGGER.fine("Simple secured function Test.. not implemented.");

       /* List<Profile> profiles = onvifDevice.getDevices().getProfiles();
        for (Profile p : profiles) {
            System.out.println("URL von Profil \'" + p.getName() + "\': " + onvifDevice.getMedia().getSnapshotUri(p.getToken()));
        }*/

       return true;

    }

    @Override
    public boolean videoSimpleFunctionTest() {

        LOGGER.fine("Simple video function Test.");

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

        LOGGER.info("Video function Test.. not implemented.");
        //todo
        return true;
    }

    @Override
    public boolean pvtSimpleMotionFunctionTest() {


        LOGGER.fine("Simple motion test started.");

        PtzDevices ptzDevices = onvifDevice.getPtz();

        boolean success = commandPTZStop();


        LOGGER.fine("Simple motion test complete.");

        return success;
    }


    @Override
    public boolean pvtMotionFunctionTest() {


        LOGGER.info("Complete motion test started.");

        commandPTZByIMGTest();

        LOGGER.info("Complete motion test ended.");
        return true;
    }

    @Override
    public boolean commandPTZByIMGTest(){


        LOGGER.info("Motion function Test.. image comparison not implemented.");

        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();

        platform.camera.components.Vector2D vector2D = new platform.camera.components.Vector2D();
        vector2D.setX((float)-45);
        vector2D.setY((float)-45);

        platform.camera.components.Vector1D vector1D = new platform.camera.components.Vector1D();
        vector1D.setX((float)0);

        platform.camera.components.PTZVector ptzVectorCommand = new platform.camera.components.PTZVector();
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

        LOGGER.fine("Searching for Devices..");
        Collection<URL> urls = discoverWsDevicesAsUrls();

        for(URL url: urls){

            LOGGER.info("Found " + url.toString());
        }

        LOGGER.fine("Search complete.");

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
    public boolean commandPTZMovement(platform.camera.components.PTZVector ptzVector) {


        LOGGER.finest("Camera move commanded: Pan-" + ptzVector.getPanTilt().getX()+ "Tilt- " + ptzVector.getPanTilt().getY()+ "Zoom- " + ptzVector.getZoom().getX());

        boolean success = false;

        if (getViewCapabilities().getPtzControl() == ViewCapabilities.PTZControl.ABS){

            success = false;

        }
        else if (getViewCapabilities().getPtzControl() == ViewCapabilities.PTZControl.CONT){

            platform.camera.components.PTZVector ptzVec = getViewCapabilities().getPTZCommandFmDomain(ptzVector);

            success = ptzDevices.continuousMove(profileToken, (float)ptzVec.getPanTilt().getX(), (float)ptzVec.getPanTilt().getY(), (float)ptzVec.getZoom().getX());

        }
        else if (getViewCapabilities().getPtzControl() == ViewCapabilities.PTZControl.REL){

            success = false;

        }

        return success;
    }

    @Override
    public boolean commandPTZStop(){

        boolean success = false;

        LOGGER.finest("Camera stop commanded");


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

    boolean isOnline() {
        // without port
        if (!getIP().contains(":")) {
            try {
                InetAddress.getByName(getIP()).isReachable(7500);
            }
            catch (IOException e) {
                return false;
            }
        }
        // with port
        else {
            String ip = getIP().substring(0, getIP().indexOf(':'));
            String port = getIP().substring(getIP().indexOf(':') + 1);
            Socket socket = null;
            try {
                SocketAddress sockaddr = new InetSocketAddress(ip, new Integer(port));
                socket = new Socket();

                socket.connect(sockaddr, 5000);
            }
            catch (NumberFormatException | IOException e) {
                return false;
            }
            finally {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                }
                catch (IOException ex) {
                }
            }
        }
        return true;
    }

}
