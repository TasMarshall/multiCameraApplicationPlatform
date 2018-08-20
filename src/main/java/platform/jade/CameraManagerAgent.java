package platform.jade;

import jade.core.Agent;
import platform.core.camera.core.Camera;

import java.util.logging.Logger;

public class CameraManagerAgent extends ControlledAgentImpl {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    String filename;
    Camera camera;
    String mca_name;

    protected void setup(){

    }


}
