package platform.jade;

import jade.core.Agent;
import platform.MapAndStreamGUI;
import platform.View;

import java.io.FileNotFoundException;

public class ViewAgent extends ControlledAgentImpl implements View {

   // String mcaFileName;
    //MCP_Application_Configuration mcp_application_configuration;

    protected void setup(){

/*
        Object[] args = getArguments();
        if (args != null && args.length > 0) {

            mcaFileName = (String)args[0];

            mcp_application_configuration = new MCP_Application_Configuration();

            try {
                mcp_application_configuration = mcp_application_configuration.readMCPConfig((String) args[0] +".xml");

                addCoreBehaviours();
                addCoreComponents();

            } catch (FileNotFoundException e) {
                System.out.println("MCA_Agent " + getAID().getName()+ " configuration file failed to read ");
            }

        }
        else{
            System.out.println("Multi Camera Application File Not Specified.");
            doSuspend();
        }
*/



    }

/*    private void addCoreComponents() {

    }

    private void addCoreBehaviours() {
    }*/

    @Override
    public void addSendViewToControllerAndSubscribedUsers() {

    }

    @Override
    public void addModelCyclicCommunicationReceiver() {

    }

}
