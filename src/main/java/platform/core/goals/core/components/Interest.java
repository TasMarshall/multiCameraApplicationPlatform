package platform.core.goals.core.components;

import platform.core.imageAnalysis.ImageAnalysis;

import java.io.Serializable;
import java.util.*;

public abstract class Interest {

    String id = UUID.randomUUID().toString();

    Set<ImageAnalysis> analysisAlgorithmsSet = new HashSet<>();
    List<String> keys;

    Map<String,Object> results;

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

    public void recordResult(Map<String, Serializable> results) {

        for(String s: keys){
            if (results.containsKey(s)){
                if(this.results.containsKey(s)){
                    this.results.replace(s,results.get(s));
                }
                else {
                    this.results.put(s,results.get(s));
                }
            }
        }

    }

    public Map<String, Object> getResults() {
        return results;
    }

    public void setResults(Map<String, Object> results) {
        this.results = results;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
