package platform.utilities;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class SimpleCameraState {

    @Id
    private String id = UUID.randomUUID().toString();

    String filepath;
    String cameraID;
    boolean isWorking;

    public  SimpleCameraState(){

    }

    public SimpleCameraState (String filepath, String cameraID, boolean isWorking){

        this.filepath = filepath;
        this.cameraID = cameraID;
        this.isWorking=isWorking;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getCameraID() {
        return cameraID;
    }

    public void setCameraID(String cameraID) {
        this.cameraID = cameraID;
    }

    public boolean isWorking() {
        return isWorking;
    }

    public void setWorking(boolean working) {
        isWorking = working;
    }

    @Override
    public String toString() {
        return String.format("SimpleCameraState{file=%s, id=%s, working=%s}", getFilepath(), getCameraID(),isWorking);
    }
}
