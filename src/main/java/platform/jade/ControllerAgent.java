package platform.jade;


import jade.gui.GuiEvent;
import platform.interfaces.SimpleStartStopControllerInterface;


public class ControllerAgent extends ControllerAgentImpl {

    /*User action constants*/
    public final static int START = 0;
    public final static int STOP = 1;
    public final static int QUIT = 2;
    public final static int USER_COMMAND = 3;

    /**State*/
    private boolean started = false;

    transient protected SimpleStartStopControllerInterface myGui;

    ControllerEngine controllerEngine;

    ///////////////////////////////////////////////////////
    ///         Platform specific functions             ///
    ///////////////////////////////////////////////////////

    /** This function overrides the agent set up function and is run when jade.boot is launched on this class */
    protected void setup() {

        controllerEngine = new ControllerEngine();

        initInterfaces();
    }

    protected void onGuiEvent(GuiEvent guiEvent) {

        // Process the event according to it's type
        int command = guiEvent.getType();

        processUserCommand(command, null); //todo: add user command to second parameter

    }

    public void processUserCommand(int command, Object o){

        if (command == QUIT) {

            controllerEngine.exitApplication(this);

        } else if (command == START) {

            if (started == false){
                controllerEngine.startMCA(this);
                started = true;
            }
            else {

            }

        } else if (command == STOP) {

            if ( started == true) {

                controllerEngine.stopMCA(this);
                started = false;
            }

        }
        else if (command == USER_COMMAND) {

            sendCommandToModelComponents(null,null);

        }

    }

    ///////////////////////////////////////////////////////
    ///             MCAP Defined Functions              ///
    ///////////////////////////////////////////////////////

    @Override
    public void initGUI() {

        myGui = new SimpleStartStopControllerInterface(this);
        myGui.setVisible(true);

    }

    @Override
    public void closeGUI() {
        myGui.dispose();
    }



    @Override
    public void startWebInterface() {

    }

    @Override
    public void stopWebInterface() {

    }


}

