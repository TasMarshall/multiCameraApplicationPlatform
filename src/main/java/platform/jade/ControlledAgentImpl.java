package platform.jade;

import com.sun.jna.platform.win32.WinDef;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import platform.jade.utilities.MCAStopMessage;

import java.util.logging.Logger;

public abstract class ControlledAgentImpl extends Agent {

    public void init(Logger logger){
        addControllerReceiver(logger);
    }

    public void addControllerReceiver(Logger logger){

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
                               logger.config(
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
