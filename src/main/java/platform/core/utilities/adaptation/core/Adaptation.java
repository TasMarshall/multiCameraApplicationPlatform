package platform.core.utilities.adaptation.core;

import java.util.HashMap;
import java.util.Map;

public abstract class Adaptation {

    protected Object adaptiveData;
    protected Map<String,Object> additionalAttribues = new HashMap<>();

    public Adaptation(Object data){
        this.adaptiveData = data;
    }

    public void directAdaptation(){
        adapt();
    }

    public void checkTrigger(){
        if (triggerAdaptation() == true){
            adapt();
        }
    }

    protected abstract boolean triggerAdaptation();
    protected abstract void adapt();

    protected abstract Object getData();
    protected abstract void setData(Object object);

    public Object getAdaptiveData() {
        return adaptiveData;
    }

    public void setAdaptiveData(Object adaptiveData) {
        this.adaptiveData = adaptiveData;
    }
}
