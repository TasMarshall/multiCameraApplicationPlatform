package platform.jade.utilities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AnalysisResultsMessage implements Serializable {

    String cameraID;
    HashMap<String, Map<String, Serializable>> results;


    long timeCreated = System.nanoTime();

    public AnalysisResultsMessage(String cameraID, HashMap<String, Map<String, Serializable>> results) {
        this.results = results;
        this.cameraID = cameraID;

    }

    public HashMap<String, Map<String, Serializable>> getResults() {
        return results;
    }

    public void setResults(HashMap<String, Map<String, Serializable>> results) {
        this.results = results;
    }

    public String getCameraID() {
        return cameraID;
    }

    public void setCameraID(String cameraID) {
        this.cameraID = cameraID;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }
}
