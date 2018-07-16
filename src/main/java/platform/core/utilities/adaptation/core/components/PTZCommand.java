package platform.core.utilities.adaptation.core.components;

import org.onvif.ver10.schema.PTZVector;

public class PTZCommand {

    PTZVector ptzVector;
    int timeMiliiSec;

    public PTZCommand(PTZVector ptzVector, int timeMiliiSec) {
        this.ptzVector = ptzVector;
        this.timeMiliiSec = timeMiliiSec;
    }

    public PTZVector getPtzVector() {
        return ptzVector;
    }

    public void setPtzVector(PTZVector ptzVector) {
        this.ptzVector = ptzVector;
    }

    public int getTimeMiliiSec() {
        return timeMiliiSec;
    }

    public void setTimeMiliiSec(int timeMiliiSec) {
        this.timeMiliiSec = timeMiliiSec;
    }
}
