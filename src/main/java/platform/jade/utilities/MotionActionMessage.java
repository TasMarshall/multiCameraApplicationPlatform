package platform.jade.utilities;

import platform.camera.components.PTZVector;

import java.io.Serializable;

public class MotionActionMessage implements Serializable {

    String cameraID;
    float pan, tilt, zoom;
    int time;

    public MotionActionMessage(String cameraID, float pan, float tilt, float zoom, int time) {
        this.cameraID = cameraID;
        this.pan = pan;
        this.tilt = tilt;
        this.zoom = zoom;
        this.time = time;
    }

    public MotionActionMessage(String cameraID, PTZVector ptzVector, int time){
        this.cameraID = cameraID;
        this.pan = ptzVector.getPanTilt().getX();
        this.tilt = ptzVector.getPanTilt().getY();;
        this.zoom = ptzVector.getZoom().getX();;
        this.time = time;
    }

    public String getCameraID() {
        return cameraID;
    }

    public void setCameraID(String cameraID) {
        this.cameraID = cameraID;
    }

    public float getPan() {
        return pan;
    }

    public void setPan(float pan) {
        this.pan = pan;
    }

    public float getTilt() {
        return tilt;
    }

    public void setTilt(float tilt) {
        this.tilt = tilt;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
