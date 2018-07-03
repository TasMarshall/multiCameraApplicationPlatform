package platform;

import jade.core.Agent;

import java.io.FileNotFoundException;

public class ViewAgent extends Agent {

    String mcaFileName;
    MCP_Application_Configuration mcp_application_configuration;
    MapAndStreamGUI mapAndStreamGUI;

    protected void setup(){

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



    }

    private void addCoreComponents() {

        mapAndStreamGUI = new MapAndStreamGUI(mcp_application_configuration);

    }

    private void addCoreBehaviours() {
    }

}
