package platform;

import platform.core.camera.core.Camera;
import platform.jade.utilities.CameraAnalysisMessage;

import java.io.Serializable;
import java.util.List;

public interface Model {

    /////////////////////////////////////
    /* Distribution platform functions */
    /////////////////////////////////////

    ////    VIEW     ////

    /**This function cyclically sends a multi camera application state to a topic*/
    public void addViewCyclicCommunicationBehavior();

    ////    MODEL     ////

    /**This function adds a camera monitor.*/
    public Object[] getArgs();

    public void cancelInit();

    ////    CONTROLLER     ////

    /**This function starts the listener of the controller to the model component*/
    public void addControllerReceiver();

}

