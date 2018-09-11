package platform.jade;


import platform.agents.Controller;

public class ControllerEngine {

    /**This function creates the model and view agents then sets up listeners to each of them.*/
    public void startMCA(Controller c){

        c.startView(); //must start view first to give model view agent name
        c.startModel();
        c.addViewReceiver();

    }

    public void stopMCA(Controller c){

        c.stopView();
        c.stopModel();

        boolean modelRunning = false;
        c.updateGUI(modelRunning);

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

