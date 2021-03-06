package platform.camera.components;


import java.io.Serializable;
import java.util.UUID;


public class PTZControlDomain implements Serializable {

    String id = UUID.randomUUID().toString();

    private FloatRange floatPanRange;
    private FloatRange floatTiltRange;
    private FloatRange floatZoomRange;

    public PTZControlDomain(FloatRange floatPanRange, FloatRange floatTiltRange, FloatRange floatZoomRange) {
        this.floatPanRange = floatPanRange;
        this.floatTiltRange = floatTiltRange;
        this.floatZoomRange = floatZoomRange;
    }

    public FloatRange getFloatPanRange() {
        return floatPanRange;
    }

    public void setFloatPanRange(FloatRange floatPanRange) {
        this.floatPanRange = floatPanRange;
    }

    public FloatRange getFloatTiltRange() {
        return floatTiltRange;
    }

    public void setFloatTiltRange(FloatRange floatTiltRange) {
        this.floatTiltRange = floatTiltRange;
    }

    public FloatRange getFloatZoomRange() {
        return floatZoomRange;
    }

    public void setFloatZoomRange(FloatRange floatZoomRange) {
        this.floatZoomRange = floatZoomRange;
    }
}
