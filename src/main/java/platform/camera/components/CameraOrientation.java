package platform.camera.components;

import com.sun.javafx.geom.Vec3d;
import platform.camera.Camera;
import platform.jade.ModelAgent;

import java.io.Serializable;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CameraOrientation implements Serializable {


    private final static Logger LOGGER = Logger.getLogger(CameraOrientation.class.getName());

    double bearing;
    double roll;
    double pitch;

    /**
     *
     */
    public CameraOrientation(double bearing, double roll, double pitch) {
        LOGGER.setLevel(Level.CONFIG);
        this.bearing = bearing;
        this.roll = roll;
        this.pitch = pitch;

        validateCameraOrientation(null, LOGGER);
    }

     public boolean validateCameraOrientation (Camera camera, Logger logger){
         boolean valid = true;
         String cameraID = "";
         if (camera != null){
             cameraID = camera.getIdAsString();
         }

         if (bearing >= 0 && bearing <=360) {
             //values are good move on
         }
         else {
             logger.severe("Camera " + cameraID + " had an invalid camera orientation bearing value which has been fixed.");
             valid = false;
             if (bearing> 360){
                 bearing = 360;
             }
             if (bearing < 0){
                 bearing = 0;
             }
         }

         if (roll >= 0 && roll <=360) {
             //values are good move on
         }
         else {
             logger.severe("Camera " + cameraID + " had an invalid camera orientation roll value which has been fixed.");
             valid = false;
             if (roll > 360){
                 roll = 360;
             }
             if (roll < 0){
                 roll = 0;
             }
         }

         if (pitch >= -90 && pitch <= 90) {
             //values are good move on
         }
         else {
             logger.severe("Camera " + cameraID + " had an invalid camera orientation pitch value which has been fixed.");
             valid = false;
             if (roll > 90){
                 roll = 90;
             }
             if (roll < -90){
                 roll = -90;
             }
         }

         return valid;
     }

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public double getRoll() {
        return roll;
    }

    public void setRoll(double roll) {
        this.roll = roll;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }
}
