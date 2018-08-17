package platform.jade;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import platform.jade.utilities.MCAStopMessage;

public abstract class ControlledAgentImpl extends Agent {

    public void addControllerReceiver(){

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
