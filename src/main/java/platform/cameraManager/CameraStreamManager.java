package platform.cameraManager;

/**
 *
 * This class manages a cameras stream. It creates a stream when the init function is called. The update streams function
 * should be called periodically as it stops unnecessary drawing of the camera stream when the camera has failed.
 *
 */
public class CameraStreamManager {

    DirectStreamView directStreamView;

    String streamURI;
    String username;
    String password;

    boolean cameraWorking;
    String cameraType;

    boolean simulated;

    boolean initialized = false;

    /**
     *
     * This function initializes a camera stream manager and the stream it manages by starting a new stream or by restarting
     * an old stream if it has already been initialized.
     *
     * @param
     */
    public void init(String streamURI, String username, String password, boolean cameraWorking, String cameraType) {

        this.streamURI = streamURI;
        this.username = username;
        this.password = password;
        this.cameraWorking = cameraWorking;
        this.cameraType = cameraType;

        if(!initialized) {

            if (cameraType.equals("SIM")){
                simulated = true;
            }

            startRealCameraStreams();
            initialized =true;

        }
        else {

            if (cameraWorking){
                updateStreams(true);
                directStreamView.playFromURIandUserPW();
            }

        }
    }

    /**
     *
     * This function stops unnecessary drawing of the camera stream when the camera has failed and should be called periodically.
     *
     */
    public void updateStreams(boolean cameraWorking) {

        directStreamView.updateStreamState(cameraWorking);

    }

    private void startRealCameraStreams() {
        if (!(simulated)) {
            this.directStreamView = new DirectStreamView(streamURI,username,password);
        }
    }

    public DirectStreamView getDirectStreamView() {
        return directStreamView;
    }

}
