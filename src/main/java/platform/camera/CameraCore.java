package platform.camera;


import com.sun.javafx.geom.Vec3d;
import platform.camera.components.*;
import platform.goals.MultiCameraGoal;
import platform.utilities.CustomID;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CameraCore implements Serializable{

    //////////////////////////
    //     Model Generated  //
    //////////////////////////

    /**Unique identifier defined at run time*/
    private CustomID id;

    /**URL required as it is used to define the IP and identify a specifc camera (the id is used as name but the url identifies a unique camera*/
    private URL url;

    /**Defined by the URL and is used to communicate via ONVIF library to camera*/
    private String IP;

    /**Credentials to access secure functions of camera as per camera configuration set using cameras proprietary software*/
    private String username, password;

    /**View range in pan tilt and zoom*/
    private ViewCapabilities viewCapabilities;

    /**View vector in a global frame of reference using bearing, roll and tilt*/
    private CameraOrientation cameraOrientation; //

    /**Camera location in a global or local frame of reference using lats longs, x distances, y distances and height*/
    private CameraLocation location;

    /**Camera state including initialized, calibrated, operating, failed etc*/
    private CameraState cameraState = new CameraState();

    /**Camera stream URI used to contain the RTSP stream value of a camera*/
    private String streamURI;

    //////////////////////////
    //     CONSTRUCTOR      //
    //////////////////////////

    public CameraCore(String id, URL url, String username, String password, ViewCapabilities viewCapabilities, Vector3D globalVector, CameraLocation location) {

        this.setId(new CustomID(id));

        this.url = url;
        if (url.getPort()!= -1) {
            this.IP = url.getHost() + ":" + url.getPort();
        }
        else {
            this.IP = url.getHost();
        }

        this.username = username;
        this.password = password;

        this.viewCapabilities = viewCapabilities; //todo get info to populate viewCapabilities PTZControlDomain()
        this.cameraOrientation = new CameraOrientation(globalVector);
        this.location = location;


    }

    ///////////////////////////////
    // SPECIALIZEDD GET AND SET  //
    ///////////////////////////////

    public String getIdAsString(){
        return id.getSerialNumber();
    }


    ///////////////////////////////
    // GENERATED GET AND SET     //
    ///////////////////////////////

    public CustomID getId() {
        return id;
    }

    public void setId(CustomID id) {
        this.id = id;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ViewCapabilities getViewCapabilities() {
        return viewCapabilities;
    }

    public void setViewCapabilities(ViewCapabilities viewCapabilities) {
        this.viewCapabilities = viewCapabilities;
    }

    public CameraOrientation getCameraOrientation() {
        return cameraOrientation;
    }

    public void setCameraOrientation(CameraOrientation cameraOrientation) {
        this.cameraOrientation = cameraOrientation;
    }

    public CameraLocation getLocation() {
        return location;
    }

    public void setLocation(CameraLocation location) {
        this.location = location;
    }

    public CameraState getCameraState() {
        return cameraState;
    }

    public void setCameraState(CameraState cameraState) {
        this.cameraState = cameraState;
    }

    public String getStreamURI() {
        return streamURI;
    }

    public void setStreamURI(String streamURI) {
        this.streamURI = streamURI;
    }

}
