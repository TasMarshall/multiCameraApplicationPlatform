package platform.agents;

import java.io.Serializable;
import java.util.List;

public interface Controller {

    //////////////////////////////////////
    /* Controller User Interface functions */
    //////////////////////////////////////

    /**This function starts the controller gui*/
    public void initGUI();

    /**This function stops the controller gui*/
    public void closeGUI();

    //////////////////////////////////////
    /* External User Interface functions */
    //////////////////////////////////////

    /**This function starts the controller web interface*/
    public void startWebInterface();

    /**This function subscribes a user to the view*/
    public void subscribeUserToView();

    /**This function stops the controller web interface*/
    public void stopWebInterface();

    /////////////////////////////////////
    /* Distribution platform functions */
    /////////////////////////////////////

    ////    VIEW     ////

    /**This function starts the view component*/
    public void startView();

    /**This function starts the listener of the controller to the view component*/
    public void addViewReceiver();

    /**This function stops the view component*/
    public void stopView();

    ////    MODEL     ////

    /**This function starts the model component*/
    public void startModel();

    /**This function stops the model component*/
    public void stopModel();

    ////    CONTROLLER     ////

    //startApplication

    /**This function starts the controller interface and must be called at creation of the controller agent.*/
    public void initInterfaces();

    /**This function performs actions based on the users command*/
    public void processUserCommand(int command, Object o);

    /**This function sends a command from the controller to other agents*/
    public void sendCommandToModelComponents(List<String> componentNames, Serializable object);

    /**This function stops all model created components*/
    public void sendKillMessageToModelComponents();

    /**This function ends the entire application*/
    public void stopController();

}

