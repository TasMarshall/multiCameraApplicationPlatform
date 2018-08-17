package platform.jade.utilities;

import java.io.Serializable;
import java.util.Map;

public class CombinedAnalysisResultsMessage implements Serializable{

    Map<String, Map<String, Map<String, Serializable>>> combinedResultMap;

    public CombinedAnalysisResultsMessage(Map<String, Map<String, Map<String, Serializable>>> combinedResultMap) {
        this.combinedResultMap = combinedResultMap;
    }

    public Map<String, Map<String, Map<String, Serializable>>> getCombinedResultMap() {
        return combinedResultMap;
    }

    public void setCombinedResultMap(Map<String, Map<String, Map<String, Serializable>>> combinedResultMap) {
        this.combinedResultMap = combinedResultMap;
    }

}
