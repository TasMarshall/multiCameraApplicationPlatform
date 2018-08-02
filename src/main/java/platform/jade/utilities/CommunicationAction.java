package platform.jade.utilities;

import java.io.Serializable;
import java.util.Map;

public class CommunicationAction implements Serializable {

    //id from a selection of ids
    String id;
    //message objects
    Map<String,Object> objectMap;

    public CommunicationAction(String id, Map<String, Object> objectMap) {
        this.id = id;
        this.objectMap = objectMap;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getObjectMap() {
        return objectMap;
    }

    public void setObjectMap(Map<String, Object> objectMap) {
        this.objectMap = objectMap;
    }
}
