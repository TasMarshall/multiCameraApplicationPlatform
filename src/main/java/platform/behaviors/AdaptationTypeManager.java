package platform.behaviors;

import platform.behaviors.impl.SimpleInScreenPointViewAdaptation;

import java.util.HashMap;

public class AdaptationTypeManager {

    private HashMap<String, AdaptivePolicy> stringToAdaptationMap = new HashMap<>();

    public AdaptationTypeManager(){

        stringToAdaptationMap.put("SIMPLE_IN_VIEW_MOT", new SimpleInScreenPointViewAdaptation());

    }

    public AdaptationTypeManager(HashMap<String, AdaptivePolicy> stringToAdaptationMap){

        this.stringToAdaptationMap = stringToAdaptationMap;
    }

    public AdaptivePolicy getAdaptivePolicy(String s){
        if(stringToAdaptationMap.containsKey(s)) {
            return stringToAdaptationMap.get(s);
        }
        else {
            return null;
        }
    }

    public HashMap<String, AdaptivePolicy> getStringToAdaptivePolicyMap() {
        return stringToAdaptationMap;
    }

    public void setStringToAdaptivePolicyMap(HashMap<String, AdaptivePolicy> stringToAdaptivePolicyMap) {
        this.stringToAdaptationMap = stringToAdaptationMap;
    }
}
