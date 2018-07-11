package platform.jade;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.TickerBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import platform.core.camera.core.Camera;
import platform.core.camera.core.components.CameraConfigurationFile;
import platform.jade.utilities.CameraHeartbeatMessage;

import java.io.IOException;

public class CameraMonitorAgent extends Agent {

    String filename;
    Camera camera;
    String mca_name;

    protected void setup(){

        Object[] args = getArguments();
        if (args != null && args.length > 2) {

            mca_name = (String)args[2];
            // Printout a welcome message
            System.out.println("CameraMonitor agent "+ getAID().getName()+" initializing.");

            CameraConfigurationFile cameraConfigurationFile = new CameraConfigurationFile();

            try {

                camera = cameraConfigurationFile.readFromCameraConfigurationFile((String) args[0]);
                TopicManagementHelper topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
                final AID topic = topicHelper.createTopic("CameraMonitor" + camera.getIdAsString());


                addBehaviour(new TickerBehaviour(this, (Integer)args[1]/2) {
                    protected void onTick() {

                        camera.simpleInit();
                        camera.setWorking(camera.simpleUnsecuredFunctionTest());

                        if(!camera.isWorking()) System.out.println(topic.getName() +"Camera" + camera.getIdAsString() + " on " + getAID().getName() + " failed heartbeat test.");

                        ACLMessage msg = new ACLMessage(ACLMessage.PROPAGATE);
                        try {
                            msg.setContentObject(new CameraHeartbeatMessage(camera.getIdAsString(),camera.isWorking()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //msg.setContent("Message# " + CameraHeartbeatMessage.buildMessage(camera));
                        msg.addReceiver( topic );
                        send(msg);

                    }
                } );

            } catch ( IOException e) {
                e.printStackTrace();
                System.out.println("Camera file read failed to create camera.");
                doDelete();
            } catch (ServiceException e) {
                System.out.println("Camera Monitor failed to publish to topic.");
            }


        }
        else{
            System.out.println("Camera File, or heartbeat time not specified.");
            doSuspend();
        }
    }
}
