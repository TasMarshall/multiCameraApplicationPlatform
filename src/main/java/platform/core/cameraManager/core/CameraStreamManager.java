package platform.core.cameraManager.core;

import platform.core.camera.core.Camera;
import platform.core.camera.impl.SimulatedCamera;

/**
 *
 * This class manages a cameras stream. It creates a stream when the init function is called. The update streams function
 * should be called periodically as it stops unnecessary drawing of the camera stream when the camera has failed.
 *
 */
public class CameraStreamManager {

    DirectStreamView directStreamView;
    Camera camera;

    /**
     *
     * This function initializes a camera stream manager and the stream it manages by starting a new stream or by restarting
     * an old stream if it has already been initialized.
     *
     * @param camera
     */
    public void init(Camera camera) {

        if(this.camera == null) {
            this.camera = camera;
            startRealCameraStreams();
        }
        else {
            if (camera.isWorking()){
                directStreamView.playFromURIandUserPW(camera);
            }
        }
    }

    /**
     *
     * This function stops unnecessary drawing of the camera stream when the camera has failed and should be called periodically.
     *
     */
    public void updateStreams() {

        directStreamView.updateStreamState();

    }

    private void startRealCameraStreams() {
        if (!(camera instanceof SimulatedCamera)) {
            this.directStreamView = new DirectStreamView(camera);
        }
    }

    public DirectStreamView getDirectStreamView() {
        return directStreamView;
    }

}
