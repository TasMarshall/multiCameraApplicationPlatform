package platform.jade.utilities;

import java.io.Serializable;

public class SnapshotMessage implements Serializable {

    String snapShotName;

    public SnapshotMessage(String snapShotName) {
        this.snapShotName = snapShotName;

    }
}
