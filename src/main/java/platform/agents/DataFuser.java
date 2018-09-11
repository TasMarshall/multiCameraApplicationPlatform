package platform.agents;

import java.io.Serializable;
import java.util.Map;

public interface DataFuser {

    /**This function creates a listener to the various camera stream analyzers*/
    void addAnalysisResultListeners();

    /**This function creates a cyclic behavior which sends the combine results to the model agent by using the combineResults
     * and sendCombineResultMessage*/
    void addSendCombinedResultToModelAgent();

    /**This function takes received info and adds it to the Datafuser data if applicable */
    void addAnalysisResultToData(Object content);

    /**This function combines received info since the last sending in preparation to send */
    void combineResults();

    /** This function sends the combine results to the model agent*/
    void sendCombineResultMessage(String modelAgentName,String viewAgentName, Map<String, Map<String, Map<String, Serializable>>> combinedResultMap);


}

