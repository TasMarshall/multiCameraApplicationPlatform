package platform.goals;

import platform.imageAnalysis.ImageAnalysis;

import java.io.Serializable;
import java.util.*;

public class VisualObservationOfInterest {

    String id = UUID.randomUUID().toString();

    Set<ImageAnalysis> analysisAlgorithmsSet = new HashSet<>();

    Map<String,Map<String,Object>> results;

    public Set<ImageAnalysis> getAnalysisAlgorithmsSet() {
        return analysisAlgorithmsSet;
    }

    public void setAnalysisAlgorithmsSet(Set<ImageAnalysis> analysisAlgorithmsSet) {
        this.analysisAlgorithmsSet = analysisAlgorithmsSet;
    }

    public void init(){
        results = new HashMap<>();
    }

    public Map<String, Object> getResults(String cameraID) {
        return results.get(cameraID);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
