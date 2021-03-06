package platform.jade;

import platform.jade.utilities.AnalysisResultsMessage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataFusionAgent extends DataFusionAgentImpl {

    private final static Logger LOGGER = Logger.getLogger(DataFusionAgent.class.getName());

    String mcaName;
    String viewName;

    Map<String,AnalysisResultsMessage> analysisResultMap;

    protected void setup(){

        LOGGER.setLevel(Level.CONFIG);

        LOGGER.config("DataFusionAgent created, beginning setup.");

        Object[] args = getArguments();
        if (args != null && args.length > 1) {

            mcaName = (String) args[0];
            viewName = (String) args[1];

            analysisResultMap = new HashMap();

            init(LOGGER);

            LOGGER.config("DataFusionAgent adding analysis result listeners.");
            addAnalysisResultListeners();


            LOGGER.config("DataFusionAgent adding result combiner and sender.");
            addSendCombinedResultToModelAgent();

        }
        else {
            LOGGER.severe("DataFusionAgent could not be initialized due invalid arguments.");
            doDelete();
        }


    }

    @Override
    public void addAnalysisResultToData(Object content){

        if (content instanceof AnalysisResultsMessage) {

            AnalysisResultsMessage analysisResultsMessage = (AnalysisResultsMessage) content;
            analysisResultMap.put(analysisResultsMessage.getCameraID(),analysisResultsMessage);

        } else if (false) {

        }

    }

    @Override
    public void combineResults() {
        Map<String, Map<String, Map<String, Serializable>>> combinedResultMap = new HashMap<>();

        //for each cameras results
        for (String s : analysisResultMap.keySet()) {
            AnalysisResultsMessage a = analysisResultMap.get(s);

            //for each goal result of a cameras result
            for (String s2 : a.getResults().keySet()) {

                //if no info on current goal
                if (!combinedResultMap.containsKey(s2)) {

                    HashMap<String,Map<String,Serializable>> cameraToResultMap =  new HashMap();
                    cameraToResultMap.put(a.getCameraID(),a.getResults().get(s2));

                    combinedResultMap.put(s2, cameraToResultMap);

                } else {

                    HashMap<String,Map<String,Serializable>> cameraToResultMap =  new HashMap();
                    cameraToResultMap.put(a.getCameraID(),a.getResults().get(s2));

                    combinedResultMap.get(s2).putAll(cameraToResultMap);
                }

            }

        }

        if (!combinedResultMap.isEmpty()) {

            sendCombineResultMessage(mcaName,viewName,combinedResultMap);

            analysisResultMap.clear();
        }

    }

}
