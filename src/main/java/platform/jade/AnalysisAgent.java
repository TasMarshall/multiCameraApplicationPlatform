package platform.jade;

import com.sun.org.apache.xml.internal.utils.SerializableLocatorImpl;
import jade.Boot;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SerialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import platform.GUI_Controller;
import platform.core.camera.core.Camera;
import platform.core.camera.core.components.CameraConfigurationFile;
import platform.core.cameraManager.core.CameraStreamManager;
import platform.core.imageAnalysis.AnalysisResult;
import platform.core.imageAnalysis.ImageAnalysis;
import platform.core.imageAnalysis.ImageAnalyzer;
import platform.jade.utilities.AnalysisResultsMessage;
import platform.jade.utilities.CameraAnalysisMessage;
import platform.jade.utilities.CameraHeartbeatMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.bytedeco.javacpp.opencv_highgui.namedWindow;

public class AnalysisAgent extends Agent {

    String mca_name;

    String cameraID;

    String streamURI;
    String username;
    String password;

    boolean cameraWorking;
    String cameraType;

    private CameraStreamManager cameraStreamManager = new CameraStreamManager();    //Camera stream video
    private HashMap<String,ImageAnalyzer> currentGoalImageAnalyzers = new HashMap<>();                                       //Populates image processing algorithms based on the cameras current goals*/

    private CameraAnalysisMessage cameraAnalysisMessage;

    final OpenCVFrameConverter converter = new OpenCVFrameConverter.ToMat();
    private JFrame frame;

    public void setup() {

        Object[] args = getArguments();
        if (args != null && args.length > 6) {

            streamURI = (String)args[0];
            username = (String)args[1];
            password = (String) args[2];
            cameraWorking = Boolean.valueOf((String)args[3]);
            cameraType = (String)args[4];
            cameraID = (String) args[5];

            mca_name = (String) args[6];
            // Printout a welcome message
            System.out.println("CameraAnalyzer agent " + getAID().getName() + " initializing.");

            addCoreComponents();
            addCoreBehaviours();


            /////////////////
            //  TEST VIEW  //
            /////////////////
            if (cameraType.equals("SIM")){
            }
            else{
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

    private void addCoreComponents() {

        addCameraStream();

    }

    private void updateImageAnalyzers() {

        if (cameraAnalysisMessage != null){
            if (cameraAnalysisMessage.getCurrentGoalsAnalysisAlgorithms().size() > 0) {
                //Add new analyzers if there is a new goal id
                for (String id: cameraAnalysisMessage.getCurrentGoalsAnalysisIds()) {
                    if (!currentGoalImageAnalyzers.containsKey(id)){
                        currentGoalImageAnalyzers.put(id,new ImageAnalyzer(cameraStreamManager.getDirectStreamView(), cameraType,cameraID, new ArrayList<>(cameraAnalysisMessage.getCurrentGoalsAnalysisAlgorithms().get(id))));
                    }
                }
                //Remove old analyzers if old goal is not in new goals
                for (String id: currentGoalImageAnalyzers.keySet()){
                    if (!cameraAnalysisMessage.getCurrentGoalsAnalysisIds().contains(id)){
                        currentGoalImageAnalyzers.get(id).close();
                        currentGoalImageAnalyzers.remove(id);
                    }
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
        addCameraMonitorListener();
        addCameraGoalUpdates();
        addAnalyzerExecution();

    }

    private void addAnalyzerExecution() {

        addBehaviour(new TickerBehaviour(this, 500) {

            HashMap<String, Map<String, Serializable>> results = new HashMap<>();
            protected void onTick() {

                if(!cameraType.equals("SIM")){
                    //canvas.showImage(converter.convert(cameraStreamManager.getDirectStreamView().getJavaCVImageMat()));
                    for (String key: currentGoalImageAnalyzers.keySet()){
                        currentGoalImageAnalyzers.get(key).performAnalysis(cameraWorking,cameraStreamManager.getDirectStreamView());

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
                        msg.addReceiver(new AID(mca_name, AID.ISGUID));
                        send(msg);
                        results = new HashMap<>();
                    }

                }
            }
        } );

    }

    private void addCameraGoalUpdates() {
        addBehaviour(new TickerBehaviour(this, 1000) {
            protected void onTick() {
                updateImageAnalyzers();
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
                            cameraAnalysisMessage = (CameraAnalysisMessage) content;
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
}
