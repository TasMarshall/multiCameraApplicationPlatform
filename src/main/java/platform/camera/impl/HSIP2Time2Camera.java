package platform.camera.impl;

import com.sun.javafx.geom.Vec3d;
import platform.camera.LocalONVIFCamera;
import platform.camera.components.CameraLocation;
import platform.camera.components.CameraOrientation;
import platform.camera.components.Vector3D;
import platform.camera.components.ViewCapabilities;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HSIP2Time2Camera extends LocalONVIFCamera {

    private static final Map<String,Object> additionalAttributes = new HashMap<>();
    static
    {
        additionalAttributes.put("streamProt",new String("rtsp"));
        additionalAttributes.put("streamExt",new String("10554/tcp/av0_0"));
        additionalAttributes.put("resolution",new String("720"));
        additionalAttributes.put("range",new Double("50"));
    }

    public HSIP2Time2Camera(String modelID, URL url, String username, String password, CameraOrientation cameraOrientation, CameraLocation location ) {

        super(modelID, url, username, password, new ViewCapabilities(62, Arrays.asList(ViewCapabilities.PTZ.P, ViewCapabilities.PTZ.T), ViewCapabilities.PTZControl.CONT,327.5F,32.5F,45,-45,0,0), cameraOrientation, location, additionalAttributes);
    }


}
