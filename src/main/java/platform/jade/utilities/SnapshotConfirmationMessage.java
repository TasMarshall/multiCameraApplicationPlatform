package platform.jade.utilities;

import java.io.Serializable;

public class SnapshotConfirmationMessage implements Serializable {

    String snapShotName;
    boolean snapTaken;
    String cameraID;

    public SnapshotConfirmationMessage(String snapShotName, boolean snapTaken, String cameraID) {
        this.snapShotName = snapShotName;
        this.snapTaken = snapTaken;
        this.cameraID = cameraID;
    }

    public String getSnapShotName() {
        return snapShotName;
    }

    public void setSnapShotName(String snapShotName) {
        this.snapShotName = snapShotName;
    }

    public boolean isSnapTaken() {
        return snapTaken;
    }

    public void setSnapTaken(boolean snapTaken) {
        this.snapTaken = snapTaken;
    }

    public String getCameraID() {
        return cameraID;
    }

    public void setCameraID(String cameraID) {
        this.cameraID = cameraID;
    }
}
