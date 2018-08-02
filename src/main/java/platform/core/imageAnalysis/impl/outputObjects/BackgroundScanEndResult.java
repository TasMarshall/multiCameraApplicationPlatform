package platform.core.imageAnalysis.impl.outputObjects;

import java.io.Serializable;

public class BackgroundScanEndResult implements Serializable{

    boolean end;

    public BackgroundScanEndResult(boolean end) {
        this.end = end;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }
}
