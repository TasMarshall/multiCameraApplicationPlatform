package platform.jade;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.gui.GuiAgent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import platform.MultiCameraCore_View;
import platform.agents.Controller;
import platform.camera.Camera;
import platform.goals.MultiCameraGoal;
import platform.jade.utilities.CameraHeartbeatMessage;
import platform.jade.utilities.CombinedAnalysisResultsMessage;
import platform.jade.utilities.MCAStopMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public abstract class ControllerAgentImpl extends GuiAgent implements Controller {


    protected final static Logger LOGGER = Logger.getLogger(ControllerAgent.class.getName());

    MultiCameraCore_View multiCameraCore_view;

    /**The agent name of the MCA agent*/
    protected String mcaAgentName;
    protected String viewAgentName;
    protected String mcaConfigName;

    protected boolean xmlconfig = true;

    //////////////////////////////////////
    /* External User Interface functions */
    //////////////////////////////////////

    public void subscribeUserToView() {
        //todo
    }

    /////////////////////////////////////
    /* Distribution platform functions */
    /////////////////////////////////////

    ////    VIEW     ////

    /**This function starts the view component*/
    public void startView() {

        String name = "ViewAgent";

        Object[] args = new Object[1];
        args[0] = mcaAgentName;

        AgentContainer c = getContainerController();

        try {
            AgentController a = c.createNewAgent( name, "platform.jade.ViewAgent", args );
            a.start();

            viewAgentName = a.getName();
        }
        catch (Exception e){}

    }

    /**This function starts the listener of the controller to the view component*/
    public void addViewReceiver() {

        TopicManagementHelper topicHelper = null;
        try {
            topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);

            final AID topic = topicHelper.createTopic("MCA_View");
            topicHelper.register(topic);

            addBehaviour(new CyclicBehaviour(this) {
                public void action() {
                    ACLMessage msg = myAgent.receive(MessageTemplate.MatchTopic(topic));
                    if (msg != null) {
                        try {
                            Object content = msg.getContentObject();
                            if (content instanceof MultiCameraCore_View) {
                                multiCameraCore_view = (MultiCameraCore_View) content;
                                LOGGER.info("Controller agent model updated by view agent.");
                                LOGGER.fine(multiCameraCore_view.viewToString());
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

    /**This function stops the view component*/
    public void stopView()
    {
        //view stop is inherent in model stop.
    }

    ////    MODEL     ////

    /**This function starts the model component*/
    public void startModel(){
        String name = "ModelAgent";

        Object[] args = new Object[2];
        if (xmlconfig) {
            args[0] = mcaConfigName;
        }
        else {
            args[0] = "jade";
        }

        args[1] = viewAgentName;

        AgentContainer c = getContainerController();

        try {
            AgentController a = c.createNewAgent( name, "platform.jade.ModelAgent", args );
            a.start();

            mcaAgentName = a.getName();
        }
        catch (Exception e){}
    }

    /**This function stops the model component*/
    public void stopModel() {
        sendKillMessageToModelComponents();
    }


    ////    CONTROLLER     ////

    //startApplication

    /**This function starts the controller interface and must be called at creation of the controller agent.*/
    public void initInterfaces(){

        initGUI();
        startWebInterface();

    }

    /**This function sends a command from the controller to other agents*/
    public void sendCommandToModelComponents(List<String> componentNames, Serializable object) {
        //todo implement command component
    }

    public void sendKillMessageToModelComponents() {

        TopicManagementHelper topicHelper = null;
        try {
            topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
            final AID topic = topicHelper.createTopic("MCA_Controller");

            System.out.println(topic.getName() + " Multi-Camera Application Stop Command Sent.");

            ACLMessage msg = new ACLMessage(ACLMessage.PROPAGATE);
            msg.setContentObject(new MCAStopMessage());
            msg.addReceiver(topic);
            send(msg);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

    }

    /**This function ends the entire application*/
    public void stopController(){
        Codec codec = new SLCodec();
        Ontology jmo = JADEManagementOntology.getInstance();
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(jmo);
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(getAMS());
        msg.setLanguage(codec.getName());
        msg.setOntology(jmo.getName());
        try {
            getContentManager().fillContent(msg, new Action(getAID(), new ShutdownPlatform()));
            send(msg);
        }
        catch (Exception e) {}
    }

}

