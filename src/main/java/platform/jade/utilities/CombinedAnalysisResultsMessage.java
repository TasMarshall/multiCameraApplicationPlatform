package platform.jade.utilities;

import java.io.Serializable;
import java.util.Map;

public class CombinedAnalysisResultsMessage implements Serializable{

    /**A camera id to a goal to results map, i.e. camera id -> all current goals results,
     *  for each goal by id -> all of goal image analysis results by specialized id to result object*/
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
