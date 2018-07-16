package platform.core.camera.core.components;

import com.sun.javafx.geom.Vec3d;

import java.util.UUID;

public class CameraOrientation {

    String id = UUID.randomUUID().toString();

    Vec3d globalVector;

     public enum ViewDomain {

         IMAGE_COMPARISON_BIND_CONTROL,
         ABS_CONTROL
     }

     private ViewDomain viewDomain;

    /**
     *
     * @param globalVector the global vector is the vector of the physical set up of the camera view on earth where x = bearing(yaw), y = roll, z = pitch
     */
     public CameraOrientation(Vec3d globalVector, ViewCapabilities viewCapabilities){

         this.globalVector = globalVector;

         init(viewCapabilities);

     }

     public void init(ViewCapabilities viewCapabilities){

             if (viewCapabilities.getPtzControl() == ViewCapabilities.PTZControl.ABS) {
                 this.viewDomain = ViewDomain.ABS_CONTROL;
             } else if (viewCapabilities.getPtzControl() == ViewCapabilities.PTZControl.CONT) {
                 this.viewDomain = ViewDomain.IMAGE_COMPARISON_BIND_CONTROL;
             } else if (viewCapabilities.getPtzControl() == ViewCapabilities.PTZControl.REL) {
                 this.viewDomain = ViewDomain.IMAGE_COMPARISON_BIND_CONTROL;
             }

     }

     public void calibrate(){

         if (this.viewDomain == ViewDomain.IMAGE_COMPARISON_BIND_CONTROL){

             //// TODO: 21/06/2018

             //bind current image to current view

             //get current image and save it

         }
         else if (this.viewDomain == ViewDomain.ABS_CONTROL){

             //// TODO: 21/06/2018

         }

     }

    public Vec3d getGlobalVector() {
        return globalVector;
    }

    public void setGlobalVector(Vec3d globalVector) {
        this.globalVector = globalVector;
    }

    public ViewDomain getViewDomain() {
        return viewDomain;
    }

    public void setViewDomain(ViewDomain viewDomain) {
        this.viewDomain = viewDomain;
    }
}
