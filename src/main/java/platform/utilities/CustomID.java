package platform.utilities;

import java.io.Serializable;

public class CustomID implements Serializable{

    private String serialNumber;

    public CustomID(String serialNumber){
        this.serialNumber = serialNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

}
