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
import platform.core.cameraManager.core.CameraStreamManager;
import platform.core.imageAnalysis.ImageAnalyzer;
import platform.core.utilities.adaptation.core.components.InMemoryBackground;
import platform.jade.utilities.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class AnalysisAgent extends ControlledAgentImpl {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    String mca_name;
    String dataFuserName;

    String cameraID;

    String streamURI;
    String username;
    String password;

    boolean cameraWorking;
    String cameraType;

    String mode;
    boolean testMode = false;

    boolean snapShotTaken = false;

    private CameraStreamManager cameraStreamManager = new CameraStreamManager();    //Camera stream video
    private HashMap<String,ImageAnalyzer> currentGoalImageAnalyzers = new HashMap<>();                                       //Populates image processing algorithms based on the cameras current goals*/

    private CameraAnalysisMessage cameraAnalysisMessage;

    private Map<String, Object> storedAnalysisInformation = new HashMap<>();

    private JFrame frame;

    public void setup() {

        Object[] args = getArguments();
        if (args != null && args.length > 8) {

            streamURI = (String)args[0];
            username = (String)args[1];
            password = (String) args[2];
            cameraWorking = Boolean.valueOf((String)args[3]);
            cameraType = (String)args[4];
            cameraID = (String) args[5];

            mca_name = (String) args[6];
            mode = (String) args[7];
            dataFuserName = (String) args[8];

            if (mode.equals("testMode")){
                testMode = true;
            }

            // Printout a welcome message
            System.out.println("CameraAnalyzer agent " + getAID().getName() + " initializing.");

            addCoreComponents();
            addCoreBehaviours();


            /////////////////
            //  TEST VIEW  //
            /////////////////
            if (!cameraType.equals("SIM") && testMode){
                frame = new JFrame("Direct Media Player");
                frame.setBounds(100, 100, 1280, 720);
                frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                frame.setContentPane(cameraStreamManager.getDirectStreamView().getVideoSurface());
                frame.setVisible(true);
            }

        } else {
            System.out.println("Camera stream agent arguments insufficient to instantiate.");
            doSuspend();
        }
    }

    @Override
    public void takeDown(){

        if(testMode) {
            frame.dispose();
        }

        for (String key: currentGoalImageAnalyzers.keySet()) {
            if (currentGoalImageAnalyzers.get(key).canvas != null){
                currentGoalImageAnalyzers.get(key).canvas.dispose();
            }
        }

    }

    private void addCoreComponents() {

        addCameraStream();

    }

    private void updateImageAnalyzers() {

        if (cameraAnalysisMessage != null){
            if (cameraAnalysisMessage.getCurrentGoalsAnalysisAlgorithms().size() > 0) {
                //Add new analyzers if there is a new goal id
                for (String id: cameraAnalysisMessage.getCurrentGoalsAnalysisIds()) {
                    if (!currentGoalImageAnalyzers.containsKey(id)){
                        currentGoalImageAnalyzers.put(id,new ImageAnalyzer(cameraStreamManager.getDirectStreamView(), cameraType,cameraID, new ArrayList<>(cameraAnalysisMessage.getCurrentGoalsAnalysisAlgorithms().get(id)),testMode));
                    }
                }
                //Remove old analyzers if old goal is not in new goals
                List<String> removals = new ArrayList<>();
                for (String id: currentGoalImageAnalyzers.keySet()){
                    if (!cameraAnalysisMessage.getCurrentGoalsAnalysisIds().contains(id)){
                        removals.add(id);
                    }
                }

                for (String s : removals){
                    currentGoalImageAnalyzers.get(s).close();
                    currentGoalImageAnalyzers.remove(s);
                }

            } else {
                currentGoalImageAnalyzers.clear();
            }
        }
    }

    private void addCameraStream() {

        cameraStreamManager.init(streamURI,username,password,cameraWorking,cameraType);

    }

    private void addCoreBehaviours() {

        addCameraGoalListener();
        addSnapshotListener();
        addCameraMonitorListener();
        addAnalyzerExecution();
        addControllerListener();

    }

    private void addAnalyzerExecution() {

        addBehaviour(new TickerBehaviour(this, 500) {

            HashMap<String, Map<String, Serializable>> results = new HashMap<>();
            protected void onTick() {

                if(!cameraType.equals("SIM")){
                    //canvas.showImage(converter.convert(cameraStreamManager.getDirectStreamView().getJavaCVImageMat()));
                    for (String key: currentGoalImageAnalyzers.keySet()){
                        currentGoalImageAnalyzers.get(key).performAnalysis(cameraWorking,cameraStreamManager.getDirectStreamView(),storedAnalysisInformation);

                        //only add to results message if there is something to send
                        if (currentGoalImageAnalyzers.get(key).getAnalysisResult().getAdditionalInformation().size() != 0) {
                            results.put(key, currentGoalImageAnalyzers.get(key).getAnalysisResult().getAdditionalInformation());
                            currentGoalImageAnalyzers.get(key).getAnalysisResult().setAdditionalInformation(new HashMap<String,Serializable>());
                        }

                    }

                    if(!results.isEmpty()) {
                        AnalysisResultsMessage analysisResultsMessage = new AnalysisResultsMessage(cameraID,results);
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        try {
                            msg.setContentObject(analysisResultsMessage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        msg.addReceiver(new AID(dataFuserName, AID.ISGUID));
                        send(msg);
                        results = new HashMap<>();
                    }

                }
            }
        } );

    }

    private void addCameraGoalListener() {
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    Object content = null;
                    try {
                        content = msg.getContentObject();
                        if (content instanceof CameraAnalysisMessage) {

                            CameraAnalysisMessage cameraAnalysisMessage2 = (CameraAnalysisMessage) content;
                            if (cameraAnalysisMessage == null) {
                                cameraAnalysisMessage = cameraAnalysisMessage2;
                                updateImageAnalyzers();
                            }
                            else {
                                if (!cameraAnalysisMessage2.equals(cameraAnalysisMessage)) {
                                    cameraAnalysisMessage = cameraAnalysisMessage2;
                                    updateImageAnalyzers();
                                }
                            }
                        }
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                }
                block();
            }
        });
    }

    private void addSnapshotListener() {
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(201);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    Object content = null;
                    try {
                        content = msg.getContentObject();
                        if (content instanceof CommunicationAction) {
                            CommunicationAction c = ((CommunicationAction) content);
                            if (c.getObjectMap().get("requestCameraSnapshot") != null) {
                                String snapID = (String) c.getObjectMap().get("snapID");
                                BufferedImage image = cameraStreamManager.getDirectStreamView().getBufferedImage();
                                File outputfile = new File("" + snapID + ".png");
                                try {

                                    ImageIO.write(image, "png", outputfile);

                                    if (!storedAnalysisInformation.containsKey("inMemBackground")){
                                        storedAnalysisInformation.put("inMemBackground", new InMemoryBackground());
                                    }

                                    InMemoryBackground inMemoryBackground = (InMemoryBackground) storedAnalysisInformation.get("inMemBackground");
                                    inMemoryBackground.add(image, snapID);

                                    ACLMessage msg2 = new ACLMessage(201);

                                    SnapshotConfirmationMessage snapshotConfirmationMessage = new SnapshotConfirmationMessage(snapID,true,cameraID);
                                    msg2.setContentObject(snapshotConfirmationMessage);
                                    msg2.addReceiver(new AID(mca_name, AID.ISGUID));
                                    send(msg2);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                }
                block();
            }
        });
    }

    private void addCameraMonitorListener() {

        TopicManagementHelper topicHelper = null;
        try {
            topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
            final AID topic = topicHelper.createTopic("CameraMonitor" + cameraID);
            topicHelper.register(topic);

            addBehaviour(new CyclicBehaviour(this) {

                CameraHeartbeatMessage cameraHeartbeatMessage;

                public void action() {
                    ACLMessage msg = myAgent.receive(MessageTemplate.MatchTopic(topic));
                    if (msg != null) {
                        Object content = null;
                        try {
                            content = msg.getContentObject();
                            if (content instanceof CameraHeartbeatMessage) {
                                cameraHeartbeatMessage = (CameraHeartbeatMessage) content;
                                cameraWorking = cameraHeartbeatMessage.isWorking();
                                System.out.println(" - " +
                                        myAgent.getLocalName() + " <- " +
                                        cameraHeartbeatMessage.getId() + " " + cameraHeartbeatMessage.isWorking());
                            }
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                    }
                    block();
                }
            });
        } catch (ServiceException e) {
            e.printStackTrace();
        }

    }

    public void addControllerListener(){

        TopicManagementHelper topicHelper = null;
        try {
            topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);

            final AID topic = topicHelper.createTopic("MCA_Controller");
            topicHelper.register(topic);

            addBehaviour(new CyclicBehaviour(this) {

                public void action() {
                    ACLMessage msg = myAgent.receive(MessageTemplate.MatchTopic(topic));
                    if (msg != null) {
                        try {
                            Object content = msg.getContentObject();
                            if (content instanceof MCAStopMessage) {
                                System.out.println(
                                        myAgent.getLocalName() + " is shutting down.");
                                doDelete();
                            }
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                    }
                    block();
                }
            });

        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }
}
