package platform;

import org.onvif.ver10.schema.Object;
import platform.core.camera.core.Camera;
import platform.core.camera.core.components.CameraConfigurationFile;
import platform.core.camera.core.components.TargetView;
import platform.core.camera.impl.SimulatedCamera;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.goals.core.components.Interest;
import platform.core.goals.core.components.ObjectOfInterest;
import platform.core.goals.core.components.RegionOfInterest;
import platform.core.imageAnalysis.AnalysisTypeManager;
import platform.core.imageAnalysis.ImageAnalysis;
import platform.core.imageAnalysis.ImageAnalyzer;
import platform.core.map.GlobalMap;
import platform.core.map.IndoorMap;
import platform.core.utilities.adaptation.AdaptationTypeManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

public class MCP_App_Test {

    public static void main(String[] args){

        MCP_Application_Configuration mcp_application_configuration = new MCP_Application_Configuration();

        List<Camera> onvifCameras = new ArrayList<>();

        CameraConfigurationFile cameraConfigurationFile = new CameraConfigurationFile();

        try {
            Camera camera = cameraConfigurationFile.readFromCameraConfigurationFile("camera_configuration_onvif1");
            onvifCameras.add(camera);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

        ObjectOfInterest objectOfInterest = new ObjectOfInterest();
        RegionOfInterest regionOfInterest = new RegionOfInterest();//new RectangleArea(-1.044000,53.947100,-1.039891, 53.947718), Area.CoordinateSys.OUTDOOR);

        ImageAnalysis toGray = new ImageAnalysis(ImageAnalyzer.ImageAnalysisAlgorithmTypes.TO_GRAY_SCALE.name(),1);
        Map<String, java.lang.Object> cannyAttrs = new HashMap<>();
        cannyAttrs.put("threshold", new Integer(7));
        ImageAnalysis canny = new ImageAnalysis(ImageAnalyzer.ImageAnalysisAlgorithmTypes.CANNY_EDGE_DETECT.toString(),2, cannyAttrs);

        regionOfInterest.getAnalysisAlgorithmsSet().add(canny);
        regionOfInterest.getAnalysisAlgorithmsSet().add(toGray);

        List<String> s = new ArrayList<>();
        s.add("backgroundBuilder");

        MultiCameraGoal multiCameraGoal = new MultiCameraGoal(1, MultiCameraGoal.GoalIndependence.PASSIVE,Arrays.asList(regionOfInterest),Arrays.asList(objectOfInterest),new GlobalMap(),1,"SIMPLE_IN_VIEW_MOT",s);

        IndoorMap indoorMap = new IndoorMap(5,5,53.954058,-1.084363,40);
        MultiCameraGoal multiCameraGoal2 = new MultiCameraGoal(2, MultiCameraGoal.GoalIndependence.VIEW_CONTROL_OPTIONAL,Arrays.asList(regionOfInterest),Arrays.asList(objectOfInterest),indoorMap,1,"SIMPLE_IN_VIEW_MOT",s);

        onvifCameras.addAll(simulatedCameras);

        //EvenCameraCoverageSim evenCameraCoverageSim = new EvenCameraCoverageSim(1, Arrays.asList(area), Arrays.asList(traffic),Arrays.asList(trafficFlow));

        AnalysisTypeManager analysisTypeManager = new AnalysisTypeManager();
        AdaptationTypeManager adaptationTypeManager = new AdaptationTypeManager();

        MCP_Application mcp_application = new MCP_Application(Arrays.asList(multiCameraGoal,multiCameraGoal2), onvifCameras,analysisTypeManager,adaptationTypeManager,null);
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
