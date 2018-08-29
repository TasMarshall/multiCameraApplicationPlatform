package platform.behaviors.components;

import platform.camera.components.PTZVector;

public class PTZCommand {

    /**Direction of commanded PTZ movement, where positive is one direction and negative the other in continuous control*/
    PTZVector ptzVector;

    /**Value which can be used to control the duration of  a commanded movement*/
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
