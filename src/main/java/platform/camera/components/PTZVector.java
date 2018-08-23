package platform.camera.components;

public class PTZVector {

    protected Vector2D panTilt;

    protected Vector1D zoom;

    public Vector2D getPanTilt() {
        return panTilt;
    }

    public void setPanTilt(Vector2D value) {
        this.panTilt = value;
    }

    public Vector1D getZoom() {
        return zoom;
    }

    public void setZoom(Vector1D value) {
        this.zoom = value;
    }

}




