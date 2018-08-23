package platform.jade;


import platform.agents.Controller;

public class ControllerEngine {

    /**This function creates the model and view agents then sets up listeners to each of them.*/
    public void startMCA(Controller c){

        c.startModel();
        c.startView();
        c.addViewReceiver();
        c.addModelReceiver();

    }

    public void stopMCA(Controller c){

        c.stopView();
        c.stopModel();

    }

    /**This function completes all required steps to close the entire application.*/
    public void exitApplication(Controller c){

        //Stop externalUI
        c.stopWebInterface();

        //Stop model and view
        stopMCA(c);

        //Stop controller UI
        c.closeGUI();

        //Stop controller
        c.stopController();


    }

}

