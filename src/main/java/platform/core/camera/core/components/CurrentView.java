package platform.core.camera.core.components;

import com.sun.javafx.geom.Vec2d;
import org.onvif.ver10.schema.PTZVector;
import platform.core.camera.core.Camera;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;


@Entity
public class CurrentView {

    @Id
    String id = UUID.randomUUID().toString();

    private Camera camera;
    private PTZVector localPTZVector;
    private Vec2d globalBearingAndPitch = new Vec2d();

    public CurrentView(Camera camera, PTZVector currentView) {

        this.camera = camera;
        this.localPTZVector = currentView;

        calculateGlobalBearingAndPitch();

    }

    public void updateViewByLocalPTZVector(PTZVector currentView, boolean recalibration){

        if (camera.getViewCapabilities().isPTZ() || recalibration == true){

            this.localPTZVector = currentView;

            calculateGlobalBearingAndPitch();

        }
        else{

            System.out.println("Error - Cannot update current view of a non PTZ camera, set recalibration parameter true for override.");

        }

    }

    private void calculateGlobalBearingAndPitch() {

        Vec2d temp = new Vec2d();

        //Get the global bearing and pitch using camera base orientation and current pan and tilt
        temp.x = camera.getCameraOrientation().getGlobalVector().x + localPTZVector.getPanTilt().getX();  //bearing
        temp.y = camera.getCameraOrientation().getGlobalVector().z + localPTZVector.getPanTilt().getY();  //pitch

        //convert according to the roll of the device
        double roll = Math.PI / 180 * camera.getCameraOrientation().getGlobalVector().y; //roll in radians

                                  //cosine maintains @ 0 min at 90*, sine zeros @ 0 and max at 90*
        globalBearingAndPitch.x = temp.x * Math.cos(roll) + temp.y * Math.sin(roll); //bearing
        globalBearingAndPitch.y = temp.y * Math.cos(roll) + temp.x * Math.sin(roll); //pitch

    }


}