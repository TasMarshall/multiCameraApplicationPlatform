package platform.core.camera.impl;

import com.sun.javafx.geom.Vec3d;
import platform.core.camera.core.LocalONVIFCamera;
import platform.core.camera.core.components.CameraLocation;
import platform.core.camera.core.components.ViewCapabilities;
import platform.core.goals.core.MultiCameraGoal;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    public HSIP2Time2Camera(String modelID, URL url, String username, String password, Vec3d globalVector, CameraLocation location, List<String> calibrationGoalIds) {

        super(modelID, url, username, password, new ViewCapabilities(62, Arrays.asList(ViewCapabilities.PTZ.P, ViewCapabilities.PTZ.T), ViewCapabilities.PTZControl.CONT,327.5F,32.5F,45,-45,0,0), globalVector, location, calibrationGoalIds,additionalAttributes);
    }

/*    public static HSIP2Time2Camera createCameraFromConfigFile(String s) throws MalformedURLException, FileNotFoundException {

        CameraConfigurationFile cameraConfigurationFile = new CameraConfigurationFile();
        HSIP2Time2Camera hsip2Time2Camera;

        cameraConfigurationFile = cameraConfigurationFile.readFromCameraConfigurationFile(s);
        hsip2Time2Camera = new HSIP2Time2Camera("c1",
                new URL(cameraConfigurationFile.getUrl()),
                cameraConfigurationFile.getUsername(),
                cameraConfigurationFile.getPassword(),
                cameraConfigurationFile.getCameraOrientation().getGlobalVector(),
                cameraConfigurationFile.getLocation(),
                Collections.emptyList());hsip2Time2Camera.setTargetView(new TargetView());hsip2Time2Camera.getTargetView().setTargetLatLon(53.947529, -1.042098); //set target52return hsip2Time2Camera;

        return hsip2Time2Camera;
    }*/
}
