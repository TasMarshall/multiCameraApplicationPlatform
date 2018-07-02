package platform;

import platform.core.camera.core.Camera;
import platform.core.camera.core.components.CameraConfigurationFile;
import platform.core.camera.core.components.TargetView;
import platform.core.camera.impl.SimulatedCamera;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.goals.core.components.ObjectOfInterest;
import platform.core.goals.core.components.RegionOfInterest;
import platform.core.imageAnalysis.ImageAnalysis;
import platform.core.imageAnalysis.ImageAnalyzer;
import platform.core.map.GlobalMap;
import platform.core.map.IndoorMap;

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
            Camera camera = cameraConfigurationFile.readFromCameraConfigurationFile("camera_configuration_onvif1.xml");
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
            simulatedCamera = (SimulatedCamera) cameraConfigurationFile.readFromCameraConfigurationFile("camera_configuration_sim1.xml");
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
            simulatedCamera2 = (SimulatedCamera) cameraConfigurationFile.readFromCameraConfigurationFile("camera_configuration_sim2.xml");
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

        ImageAnalysis toGray = new ImageAnalysis(ImageAnalyzer.ImageAnalysisAlgorithmTypes.TO_GRAY_SCALE,1);
        Map<String, Integer> cannyAttrs = new HashMap<>();
        cannyAttrs.put("threshold",7);
        ImageAnalysis canny = new ImageAnalysis(ImageAnalyzer.ImageAnalysisAlgorithmTypes.CANNY_EDGE_DETECT,2, cannyAttrs);

        regionOfInterest.getAnalysisAlgorithmsSet().add(canny);
        regionOfInterest.getAnalysisAlgorithmsSet().add(toGray);

        MultiCameraGoal multiCameraGoal = new MultiCameraGoal(1,Arrays.asList(regionOfInterest),Arrays.asList(objectOfInterest),new GlobalMap(),1);

        IndoorMap indoorMap = new IndoorMap(5,5,53.954058,-1.084363,40);
        MultiCameraGoal multiCameraGoal2 = new MultiCameraGoal(2,Arrays.asList(regionOfInterest),Arrays.asList(objectOfInterest),indoorMap,1);



        //EvenCameraCoverageSim evenCameraCoverageSim = new EvenCameraCoverageSim(1, Arrays.asList(area), Arrays.asList(traffic),Arrays.asList(trafficFlow));

        MCP_Application mcp_application = new MCP_Application(Arrays.asList(multiCameraGoal,multiCameraGoal2), onvifCameras, simulatedCameras);

        try {
            mcp_application_configuration.writeConfigurationToXML(mcp_application);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            mcp_application_configuration.readFromMCPConfigurationFile("mcp_configuration_e0900c2e-4922-414c-be79-29ba775e15cb.xml");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }




}
