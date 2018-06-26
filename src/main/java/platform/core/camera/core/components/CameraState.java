package platform.core.camera.core.components;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class CameraState {

    @Id
    String id = UUID.randomUUID().toString();

    public boolean connected = false;
    public boolean initialized = false;
    public boolean calibrated = false;

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
}
