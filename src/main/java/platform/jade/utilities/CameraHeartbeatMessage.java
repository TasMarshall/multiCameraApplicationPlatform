package platform.jade.utilities;

import platform.camera.Camera;

import java.io.Serializable;

public class CameraHeartbeatMessage implements Serializable{

    String id;
    boolean working;

    public CameraHeartbeatMessage(String s, Boolean s1) {
        this.id = s;
        this.working = s1;
    }


    public static String buildMessage(Camera camera) {

        return "Camera_Connection_Heartbeat " + camera.getIdAsString() + " " + camera.isWorking();

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
}
