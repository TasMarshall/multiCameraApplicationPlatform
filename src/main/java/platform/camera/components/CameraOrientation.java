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

    Vector3D globalVector;

    /**
     *
     * @param globalVector the global vector is the vector of the physical set up of the camera view on earth where x = bearing(yaw), y = roll, z = pitch
     */
     public CameraOrientation(Vector3D globalVector){

         LOGGER.setLevel(Level.CONFIG);

         this.globalVector = globalVector;

         validateCameraOrientation(null, LOGGER);

     }

     public boolean validateCameraOrientation (Camera camera, Logger logger){
         boolean valid = true;
         String cameraID = "";
         if (camera != null){
             cameraID = camera.getIdAsString();
         }

         if (globalVector.x >= 0 && globalVector.x <=360) {
         }
         else {
             logger.severe("Camera " + cameraID + " had an invalid camera orientation bearing value which has been fixed.");
             valid = false;
             if (globalVector.x > 360){
                 globalVector.x = 360;
             }
             if (globalVector.x < 0){
                 globalVector.x = 0;
             }
         }

         if (globalVector.y >= 0 && globalVector.y <=360) {
         }
         else {
             logger.severe("Camera " + cameraID + " had an invalid camera orientation roll value which has been fixed.");
             valid = false;
             if (globalVector.y > 360){
                 globalVector.y = 360;
             }
             if (globalVector.y < 0){
                 globalVector.y = 0;
             }
         }

         if (globalVector.z >= -90 && globalVector.z <= 90) {
         }
         else {
             logger.severe("Camera " + cameraID + " had an invalid camera orientation pitch value which has been fixed.");
             valid = false;
             if (globalVector.y > 90){
                 globalVector.y = 90;
             }
             if (globalVector.y < -90){
                 globalVector.y = -90;
             }
         }

         return valid;
     }

    public Vector3D getGlobalVector() {
        return globalVector;
    }

    public void setGlobalVector(Vector3D globalVector) {
        this.globalVector = globalVector;
    }

}
