package platform.jade;

import jade.core.AID;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import platform.MultiCameraCore;
import platform.MultiCameraCore_View;
import platform.agents.View;
import platform.jade.utilities.CameraAnalysisMessage;
import platform.jade.utilities.CameraHeartbeatMessage;
import platform.jade.utilities.CombinedAnalysisResultsMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViewAgent extends ControlledAgentImpl implements View {

    private final static Logger LOGGER = Logger.getLogger(ViewAgent.class.getName());

    String controllerName;

    MultiCameraCore_View multiCameraCore_view;

    private CombinedAnalysisResultsMessage mostRecentResults;

    boolean updated = false;

    protected void setup(){

        LOGGER.setLevel(Level.CONFIG);

        LOGGER.config("ViewAgent created, beginning setup.");

        LOGGER.config("View agent setup but not view specific behaviors yet implemented.");

        Object[] args = getArguments();
        if (args != null && args.length > 0) {

            controllerName = (String)args[0];

            addCoreBehaviours();
            addCoreComponents();

        }
        else{
            LOGGER.severe("View agent failed due to incorrect arguments.");
            doSuspend();
        }

        mostRecentResults = new CombinedAnalysisResultsMessage(new HashMap<>());

    }

    private void addCoreComponents() {

    }

    private void addCoreBehaviours() {

        init(LOGGER);
        addSendViewToControllerAndSubscribedUsers();
        addModelCyclicCommunicationReceiver();
        addDataFusionAgentListener();
    }

    @Override
    public void addDataFusionAgentListener(){
        LOGGER.config("View agent analysis results listeners behavior added.");

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(99);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    try {
                        Object content = msg.getContentObject();
                        if (content instanceof CombinedAnalysisResultsMessage) {
                            CombinedAnalysisResultsMessage analysisResultsMessage = (CombinedAnalysisResultsMessage) content;

                            mostRecentResults.getCombinedResultMap().putAll(analysisResultsMessage.getCombinedResultMap());

                        }

                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                }
                block();
            }
        });

    }

    @Override
    public void addSendViewToControllerAndSubscribedUsers() {

        TopicManagementHelper topicHelper = null;
        try {
            topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
            final AID topic = topicHelper.createTopic("MCA_View");
            addBehaviour(new TickerBehaviour(this, 1000) {
                protected void onTick() {

                    if (multiCameraCore_view !=null && updated == true) {
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        try {
                            multiCameraCore_view.addResults(mostRecentResults);
                            msg.setContentObject(multiCameraCore_view);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        msg.addReceiver(topic);
                        send(msg);
                        updated = false;
                    }

                }
            } );

            LOGGER.config("View agent view communication to controller and subscribed users added to topic named 'MCA_View'.");

        } catch (ServiceException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void addModelCyclicCommunicationReceiver() {

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    Object content = null;
                    try {
                        content = msg.getContentObject();
                        if (content instanceof MultiCameraCore_View) {

                            LOGGER.info("View agent model updated by model agent.");
                            multiCameraCore_view = (MultiCameraCore_View) content;
                            updated=true;

                            LOGGER.fine(multiCameraCore_view.viewToString());
                        }
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                }
                block();
            }
        });

        LOGGER.config("View agent model receiver added.");


    }

}
