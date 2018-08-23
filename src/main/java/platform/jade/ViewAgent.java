package platform.jade;

import platform.agents.View;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ViewAgent extends ControlledAgentImpl implements View {


    private final static Logger LOGGER = Logger.getLogger(ViewAgent.class.getName());

   // String mcaFileName;
    //MCP_Application_Configuration mcp_application_configuration;

    protected void setup(){

        LOGGER.setLevel(Level.CONFIG);

        LOGGER.config("ViewAgent created, beginning setup.");

        LOGGER.config("View agent setup but not view specific behaviors yet implemented.");

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
