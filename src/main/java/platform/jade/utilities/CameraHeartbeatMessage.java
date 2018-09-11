package platform.jade.utilities;

import platform.camera.Camera;

import java.io.Serializable;

public class CameraHeartbeatMessage implements Serializable{

    String id;
    boolean working;
    long timeCreated;

    public CameraHeartbeatMessage(String s, Boolean s1) {
        this.id = s;
        this.working = s1;
    }
    public CameraHeartbeatMessage(String s, Boolean s1, long timeCreated) {
        this.id = s;
        this.working = s1;
        this.timeCreated = timeCreated;
    }



    public String buildMessage( ) {

        return "Camera_Connection_Heartbeat " + id + " " + isWorking();

    }

    public static CameraHeartbeatMessage getMessage(String[] message) {
        return new CameraHeartbeatMessage(message[2], Boolean.valueOf(message[3]));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isWorking() {
        return working;
    }

    public void setWorking(boolean working) {
        this.working = working;
    }

    public long getTimeCreated() {
        return timeCreated;
    }
}
