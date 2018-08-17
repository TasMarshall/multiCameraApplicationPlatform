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
import platform.Controller;
import platform.DataFuser;
import platform.jade.utilities.AnalysisResultsMessage;
import platform.jade.utilities.CombinedAnalysisResultsMessage;
import platform.jade.utilities.MCAStopMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DataFusionAgentImpl extends ControlledAgentImpl implements DataFuser{

    public void addAnalysisResultListeners() {

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    try {
                        Object content = msg.getContentObject();

                        addAnalysisResultToData(content);

                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                }
                block();
            }
        });

    }

    public void addSendCombinedResultToModelAgent(){


        addBehaviour(new TickerBehaviour(this, 500) {

            protected void onTick() {

                combineResults();

            }

        });

    }

    public void sendCombineResultMessage(String m,Map<String, Map<String, Map<String, Serializable>>> combinedResultMap){

        CombinedAnalysisResultsMessage analysisResultsMessage = new CombinedAnalysisResultsMessage(combinedResultMap);
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        try {
            msg.setContentObject(analysisResultsMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        msg.addReceiver(new AID(m, AID.ISGUID));
        send(msg);

    }

}

