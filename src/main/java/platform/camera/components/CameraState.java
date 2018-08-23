package platform.camera.components;

import java.util.UUID;

public class CameraState {

    String id = UUID.randomUUID().toString();

    public boolean connected = false;
    public boolean initialized = false;
    public boolean calibrated = false;
    public boolean reconnectable = false;

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean isCalibrated() {
        return calibrated;
    }

    public void setCalibrated(boolean calibrated) {
        this.calibrated = calibrated;
    }

    public boolean isReconnectable() {
        return reconnectable;
    }

    public void setReconnectable(boolean reconnectable) {
        this.reconnectable = reconnectable;
    }
}
