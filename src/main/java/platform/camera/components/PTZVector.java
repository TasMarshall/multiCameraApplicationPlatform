package platform.camera.components;

import java.io.Serializable;

public class PTZVector implements Serializable{

    protected Vector2D panTilt;

    protected Vector1D zoom;

    public PTZVector (){

    }

    public PTZVector (float pan, float tilt, float zoom){
        panTilt = new Vector2D(pan,tilt);
        this.zoom = new Vector1D(zoom);
    }


    public float getPan(){
        return panTilt.getX();
    }

    public void setPan(float pan){
        panTilt.setX(pan);
    }

    public float getTilt(){
        return panTilt.getY();
    }

    public void setTilt(float tilt){
        panTilt.setY(tilt);
    }

    public float getZoom(){
        return zoom.getX();
    }

    public void setZoom(float zoom){
        this.zoom.setX(zoom);
    }

    public Vector2D getPanTilt() {
        return panTilt;
    }

    public void setPanTilt(Vector2D value) {
        this.panTilt = value;
    }

    public Vector1D getZoomVec() {
        return zoom;
    }

    public void setZoom(Vector1D value) {
        this.zoom = value;
    }

}




