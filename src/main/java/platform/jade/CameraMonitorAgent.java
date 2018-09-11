package platform.jade;

import jade.core.AID;
import jade.core.ServiceException;
import jade.core.behaviours.TickerBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import platform.agents.CameraMonitor;
import platform.camera.Camera;
import platform.camera.CameraConfigurationFile;
import platform.jade.utilities.CameraHeartbeatMessage;
import platform.jade.utilities.CombinedAnalysisResultsMessage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CameraMonitorAgent extends ControlledAgentImpl implements CameraMonitor {

    private final static Logger LOGGER = Logger.getLogger(CameraMonitorAgent.class.getName());

    Camera camera;
    String model_name;

    protected void setup(){

        LOGGER.setLevel(Level.CONFIG);
        LOGGER.config("CameraMonitor created, beginning setup.");

        Object[] args = getArguments();
        if (args != null && args.length > 2) {

            model_name = (String)args[2];
            // Printout a welcome message
            LOGGER.config("CameraMonitor agent "+ getAID().getName()+" initializing.");

            init(LOGGER);
            addCameraMonitorBehavior(args);

        }
        else{

            LOGGER.config("Camera Monitor failed to init due camera file, or heartbeat time not specified.");
            doSuspend();

        }
    }

    public void addCameraMonitorBehavior(Object[] args) {

        CameraConfigurationFile cameraConfigurationFile = new CameraConfigurationFile();

        try {

            initCamera(cameraConfigurationFile,args);

            //camera.init();

            LOGGER.config("Camera " + camera.getIdAsString() + " CameraMonitor adding topic for publishing of camera state and behaviour to regularly publish state.");

            createTopicAndCommunicationBehavior(args);


            LOGGER.config("Camera " + camera.getIdAsString() + " CameraMonitor adding CotrollerAgent listener.");

        } catch ( IOException e) {
            e.printStackTrace();
            LOGGER.severe("Camera file read failed to create camera.");
            doDelete();
        } catch (ServiceException e) {
            LOGGER.severe("Camera Monitor failed to publish to topic.");
        }

    }

    private void createTopicAndCommunicationBehavior(Object[] args) throws ServiceException {

        TopicManagementHelper topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
        final AID topic = topicHelper.createTopic("CameraMonitor" + camera.getIdAsString());

        addBehaviour(new TickerBehaviour(this, (Integer)args[1]/2) {
            protected void onTick() {

                testCamera();
                logTestResult();
                ACLMessage msg = new ACLMessage(ACLMessage.PROPAGATE);

                CameraHeartbeatMessage cameraHeartbeatMessage = new CameraHeartbeatMessage(camera.getIdAsString(),camera.isWorking(),System.nanoTime());

                try {
                    msg.setContentObject(cameraHeartbeatMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                msg.addReceiver( topic );
                send(msg);

            }
        } );

    }

    private void logTestResult() {
        if(!camera.isWorking()) LOGGER.info("Camera" + camera.getIdAsString() + " on " + getAID().getName() + " failed heartbeat test.");
    }

    public void initCamera(CameraConfigurationFile cameraConfigurationFile, Object[] args) throws FileNotFoundException, MalformedURLException {
        camera = cameraConfigurationFile.readFromCameraConfigurationFile((String) args[0]);
        camera.simpleInit();
    }

    public void testCamera() {

        boolean cameraWorking = camera.simpleUnsecuredFunctionTest();
        camera.setWorking(cameraWorking);

    }

}