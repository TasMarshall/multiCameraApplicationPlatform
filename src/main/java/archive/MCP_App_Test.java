package archive;

import platform.MultiCameraCore;
import platform.MultiCameraCore_Configuration;
import platform.camera.Camera;
import platform.camera.CameraConfigurationFile;
import platform.camera.components.TargetView;
import platform.camera.impl.SimulatedCamera;
import platform.goals.CameraRequirements;
import platform.goals.MultiCameraGoal;
import platform.goals.VisualObservationOfInterest;
import platform.imageAnalysis.AnalysisTypeManager;
import platform.imageAnalysis.ImageAnalysis;
import platform.map.GlobalMap;
import platform.map.IndoorMap;
import platform.behaviors.AdaptationTypeManager;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.*;

public class MCP_App_Test {

    public static void main(String[] args){

        MultiCameraCore_Configuration mcp_application_configuration = new MultiCameraCore_Configuration();

        List<Camera> onvifCameras = new ArrayList<>();

        CameraConfigurationFile cameraConfigurationFile = new CameraConfigurationFile();

        try {
            Camera camera = cameraConfigurationFile.readFromCameraConfigurationFile("camera_configuration_onvif1");
            onvifCameras.add(camera);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        List<Camera> simulatedCameras = new ArrayList<>();
        SimulatedCamera simulatedCamera = null;
        try {
            simulatedCamera = (SimulatedCamera) cameraConfigurationFile.readFromCameraConfigurationFile("camera_configuration_sim1");
            simulatedCameras.add(simulatedCamera);
            simulatedCamera.setTargetView(new TargetView());
            simulatedCamera.getTargetView().setTargetLatLon(53.947529, -1.042098); //set target52


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        SimulatedCamera simulatedCamera2 = null;

        try {
            simulatedCamera2 = (SimulatedCamera) cameraConfigurationFile.readFromCameraConfigurationFile("camera_configuration_sim2");
            simulatedCamera2.setTargetView(new TargetView());
            simulatedCamera2.getTargetView().setTargetLatLon(53.947529, -1.042098); //set target52
            simulatedCameras.add(simulatedCamera2);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        VisualObservationOfInterest objectOfInterest = new VisualObservationOfInterest();

        ImageAnalysis toGray = new ImageAnalysis("TO_GRAY_SCALE",1);
        Map<String, java.lang.Object> cannyAttrs = new HashMap<>();
        cannyAttrs.put("threshold", new Integer(7));
        ImageAnalysis canny = new ImageAnalysis("CANNY_EDGE_DETECT",2, cannyAttrs);

        List<String> a = new ArrayList<>();
        a.add("SNAPSHOTBACKGROUND");

        MultiCameraGoal multiCameraGoal = new MultiCameraGoal("g1", true,1, MultiCameraGoal.GoalType.NORMAL, new CameraRequirements(true,true,true,true,true,true,false,false,true,false,new ArrayList<>(),new ArrayList<>()),Arrays.asList(objectOfInterest),new GlobalMap(),"SIMPLE_IN_VIEW_MOT",a, new HashMap<>());

        IndoorMap indoorMap = new IndoorMap(5,5,53.954058,-1.084363,40);
        MultiCameraGoal multiCameraGoal2 = new MultiCameraGoal("g2",false, 2, MultiCameraGoal.GoalType.CALIBRATION, new CameraRequirements(true,true,true,true,true,true,false,false,true,false,new ArrayList<>(),new ArrayList<>()),Arrays.asList(objectOfInterest),indoorMap,"SIMPLE_IN_VIEW_MOT",a,new HashMap<>());

        onvifCameras.addAll(simulatedCameras);

        //EvenCameraCoverageSim evenCameraCoverageSim = new EvenCameraCoverageSim(1, Arrays.asList(area), Arrays.asList(traffic),Arrays.asList(trafficFlow));

        AnalysisTypeManager analysisTypeManager = new AnalysisTypeManager();
        AdaptationTypeManager adaptationTypeManager = new AdaptationTypeManager();

        MultiCameraCore mcp_application = new MultiCameraCore(Arrays.asList(multiCameraGoal,multiCameraGoal2), onvifCameras,analysisTypeManager,adaptationTypeManager,null);
        mcp_application.getAdditionalFields().put("heartbeat","10000");

        try {
            mcp_application_configuration.writeConfigurationToXML(mcp_application);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            mcp_application_configuration.createMCAppFromMCPConfigurationFile("testMCPConfigFile.xml");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}
