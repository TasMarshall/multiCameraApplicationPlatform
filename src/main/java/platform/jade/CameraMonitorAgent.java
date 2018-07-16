package platform.jade;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import org.onvif.ver10.schema.PTZVector;
import org.onvif.ver10.schema.Vector1D;
import org.onvif.ver10.schema.Vector2D;
import platform.core.camera.core.Camera;
import platform.core.camera.core.components.CameraConfigurationFile;
import platform.jade.utilities.CameraAnalysisMessage;
import platform.jade.utilities.CameraHeartbeatMessage;
import platform.jade.utilities.MotionActionMessage;

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
                camera.simpleInit();
                //camera.init();

                TopicManagementHelper topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
                final AID topic = topicHelper.createTopic("CameraMonitor" + camera.getIdAsString());

                addBehaviour(new TickerBehaviour(this, (Integer)args[1]/2) {
                    protected void onTick() {


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

                /*addBehaviour(new CyclicBehaviour(this) {
                    public void action() {


                        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                        ACLMessage msg = myAgent.receive(mt);

                        if (camera.isWorking()) {

                            if (msg != null) {
                                MotionActionMessage m;
                                Object content = null;
                                try {
                                    content = msg.getContentObject();
                                    if (content instanceof MotionActionMessage) {
                                        m = (MotionActionMessage) content;

                                        Vector2D vector2D = new Vector2D();
                                        vector2D.setX(m.getPan());
                                        vector2D.setY(m.getTilt());

                                        Vector1D vector1D = new Vector1D();
                                        vector1D.setX(m.getZoom());

                                        PTZVector ptzVectorCommand = new PTZVector();
                                        ptzVectorCommand.setPanTilt(vector2D);
                                        ptzVectorCommand.setZoom(vector1D);

                                        camera.commandPTZMovement(ptzVectorCommand);

                                        long startTime = System.currentTimeMillis();
                                        long currentTime = System.currentTimeMillis();

                                        while (currentTime - startTime < m.getTime()) {
                                            currentTime = System.currentTimeMillis();
                                        }

                                        camera.commandPTZStop();

                                    }
                                } catch (UnreadableException e) {
                                    e.printStackTrace();
                                }
                            }
                            block();
                        }
                    }
                });*/

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