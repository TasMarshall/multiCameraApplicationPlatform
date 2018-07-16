package platform.core.camera.core.components;

import org.onvif.ver10.schema.PTZVector;
import org.onvif.ver10.schema.Vector1D;
import org.onvif.ver10.schema.Vector2D;

import java.util.List;
import java.util.UUID;

public class ViewCapabilities {

    String id = UUID.randomUUID().toString();

    public enum PTZ {P, T, Z, Nil}  //Options within scope
    public enum PTZControl {CONT,ABS,REL} //As defined by ONVIF standard

    //////////////////////////
    //     Model Generated  //
    //////////////////////////

    private int viewAngle;

    private List<PTZ> ptzType;
    private boolean isPTZ;

    private PTZControl ptzControl; //ONVIF devices can give you the command ranges, this is stored in this variable such that they can be accessed without need to know the values themselves according to the implemented command type, ABS, REL or CONT

    private float maxPanViewAngle = 0;  //Specified at initialization by user
    private float minPanViewAngle = 0;  //Specified at initialization by user

    private float maxTiltViewAngle = 0; //Specified at initialization by user
    private float minTiltViewAngle = 0; //Specified at initialization by user

    private float maxZoom = 0; //Specified at initialization by user
    private float minZoom = 0; //Specified at initialization by user

    //////////////////////////
    //     Private ONLY     //
    //////////////////////////

    private PTZControlDomain ptzControlDomain;

    /////////////////////////////
    //   CONSTRUCTOR           //
    /////////////////////////////

    /**
     * Creates an object with the physical view range values, the camera command range values,
     * the command type (ABS, REL, CONT) and the camera PTZ type. The user is able to access this information,
     * and in addition request a command vector to move the camera to a given angle using the function
     * {@link ViewCapabilities#getPTZCommandFmDomain(PTZVector)} according to the command type.
     *
     * @param viewAngle the physical view angle of the camera aperture,
     * @param ptzType the pan, tilt, zoom capability of a camera,
     * @param maxPanViewAngle the maximum pan view angle - i.e. camera angle from centre axis to max axis, not to true maximum angle
     * @param minPanViewAngle the minimum pan view angle - i.e. camera angle from centre axis to max axis, not to true maximum angle
     * @param maxTiltViewAngle the maximum tilt view angle - i.e. camera angle from centre axis to max axis, not to true maximum angle
     * @param minTiltViewAngle the minimum tilt view angle - i.e. camera angle from centre axis to max axis, not to true maximum angle
     * @param maxZoom the maximum zoom value
     * @param minZoom the minimum zoom value
     */

    public ViewCapabilities(int viewAngle, List<PTZ> ptzType, PTZControl ptzControl, float maxPanViewAngle, float minPanViewAngle, float maxTiltViewAngle, float minTiltViewAngle, float maxZoom, float minZoom) {

        this.viewAngle = viewAngle;

        this.ptzType = ptzType;

        this.ptzControl = ptzControl;

        if (ptzType.contains(PTZ.Nil)) {
            isPTZ = false;
        } else {
            isPTZ = true;

            if (ptzType.contains(PTZ.P)) {
                this.maxPanViewAngle = maxPanViewAngle;
                this.minPanViewAngle = minPanViewAngle;
            }

            if (ptzType.contains(PTZ.T)) {
                this.maxTiltViewAngle = maxTiltViewAngle;
                this.minTiltViewAngle = minTiltViewAngle;
            }

            if (ptzType.contains(PTZ.Z)) {
                this.maxZoom = maxZoom;
                this.minZoom = minZoom;
            }

        }

    }

    public PTZVector getPTZCommandFmDomain(PTZVector vec3d){


        PTZVector output = new PTZVector();
        float outX = 0;
        float outY = 0;
        float outZ = 0;

        if (ptzControl == PTZControl.ABS){

            System.out.println("absolute control PTZ Capability function getPTZCommandFmDomain has not been implemented, function called returned 0,0,0.");
            outX = 0;
            outY = 0;
            outZ = 0;

        }
        else if (ptzControl == PTZControl.CONT){

            if ( isPTZ == true) {

                if (ptzType.contains(PTZ.P)) {
                    if (vec3d.getPanTilt().getX() < 0){
                        outX = ptzControlDomain.getFloatPanRange().getMin();
                    }
                    else if (vec3d.getPanTilt().getX() > 0){
                        outX = ptzControlDomain.getFloatPanRange().getMax();
                    }
                    else{
                        outX = 0;
                    }
                }
                else {
                    outX = 0;
                }

                if (ptzType.contains(PTZ.T)) {
                    if (vec3d.getPanTilt().getY() < 0){
                        outY = ptzControlDomain.getFloatTiltRange().getMin();
                    }
                    else if (vec3d.getPanTilt().getY() > 0){
                        outY = ptzControlDomain.getFloatTiltRange().getMax();
                    }
                    else{
                        outY = 0;
                    }
                }
                else {
                    outY = 0;
                }

                if (ptzType.contains(PTZ.Z)) {
                    if (vec3d.getZoom().getX() < 0){
                        outZ = ptzControlDomain.getFloatZoomRange().getMin();
                    }
                    else if (vec3d.getZoom().getX() > 0){
                        outZ = ptzControlDomain.getFloatZoomRange().getMax();
                    }
                    else{
                        outZ = 0;
                    }
                }
                else {
                    outZ = 0;
                }
            }
        }
        else if (ptzControl == PTZControl.REL){

            System.out.println("relative control PTZ Capability function getPTZCommandFmDomain has not been implemented, function called returned 0,0,0.");
            outX = 0;
            outY = 0;
            outZ = 0;

        }

        Vector2D vector2D = new Vector2D();
        vector2D.setX(outX);
        vector2D.setY(outY);

        Vector1D vector1D = new Vector1D();
        vector1D.setX(outZ);

        output.setPanTilt(vector2D);
        output.setZoom(vector1D);

        return output;

    }

    /////////////////////////////
    //   GETTERS AND SETTERS   //
    /////////////////////////////

    public List<PTZ> getPtzType() {
        return ptzType;
    }

    public void setPtzType(List<PTZ> ptzType) {
        this.ptzType = ptzType;
    }

    public boolean isPTZ() {
        return isPTZ;
    }

    public void setPTZ(boolean PTZ) {
        isPTZ = PTZ;
    }

    public float getMaxPanViewAngle() {
        return maxPanViewAngle;
    }

    public void setMaxPanViewAngle(float maxPanViewAngle) {
        this.maxPanViewAngle = maxPanViewAngle;
    }

    public float getMinPanViewAngle() {
        return minPanViewAngle;
    }

    public void setMinPanViewAngle(float minPanViewAngle) {
        this.minPanViewAngle = minPanViewAngle;
    }

    public float getMaxTiltViewAngle() {
        return maxTiltViewAngle;
    }

    public void setMaxTiltViewAngle(float maxTiltViewAngle) {
        this.maxTiltViewAngle = maxTiltViewAngle;
    }

    public float getMinTiltViewAngle() {
        return minTiltViewAngle;
    }

    public void setMinTiltViewAngle(float minTiltViewAngle) {
        this.minTiltViewAngle = minTiltViewAngle;
    }

    public float getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(float maxZoom) {
        this.maxZoom = maxZoom;
    }

    public float getMinZoom() {
        return minZoom;
    }

    public void setMinZoom(float minZoom) {
        this.minZoom = minZoom;
    }

    public int getViewAngle() {
        return viewAngle;
    }

    public void setViewAngle(int viewAngle) {
        this.viewAngle = viewAngle;
    }

    public PTZControl getPtzControl() {
        return ptzControl;
    }

    public void setPtzControl(PTZControl ptzControl) {
        this.ptzControl = ptzControl;
    }

    public PTZControlDomain getPtzControlDomain() {
        return ptzControlDomain;
    }

    public void setPtzControlDomain(PTZControlDomain ptzControlDomain) {
        this.ptzControlDomain = ptzControlDomain;
    }
}