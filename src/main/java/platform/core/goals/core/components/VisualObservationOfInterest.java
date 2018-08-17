package platform.core.goals.core.components;

import platform.core.imageAnalysis.ImageAnalysis;

import java.io.Serializable;
import java.util.*;

public class VisualObservationOfInterest {

    String id = UUID.randomUUID().toString();

    Set<ImageAnalysis> analysisAlgorithmsSet = new HashSet<>();
    List<String> keys;

    Map<String,Map<String,Object>> results;

    public Set<ImageAnalysis> getAnalysisAlgorithmsSet() {
        return analysisAlgorithmsSet;
    }

    public void setAnalysisAlgorithmsSet(Set<ImageAnalysis> analysisAlgorithmsSet) {
        this.analysisAlgorithmsSet = analysisAlgorithmsSet;
    }

    public void init(){
        results = new HashMap<>();
        keys = new ArrayList<>();

        if(getAnalysisAlgorithmsSet() != null) {
            for (ImageAnalysis imageAnalysis : analysisAlgorithmsSet) {
                keys.addAll(imageAnalysis.getAnalysisTypeManager().getImageProcessObject(imageAnalysis.getImageAnalysisType().toString()).getOutputInfoKeys());
            }
        }
    }

    public void recordResult(Map<String, Serializable> results, String key) {

        for(String s: keys){
            if (results.containsKey(s)){

                if (this.results.get(key) == null){
                    this.results.put(key,new HashMap<String,Object>());
                }

                if(this.results.get(key).containsKey(s)){
                    this.results.get(key).replace(s,results.get(s));
                }
                else {
                    this.results.get(key).put(s,results.get(s));
                }
            }
        }

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
