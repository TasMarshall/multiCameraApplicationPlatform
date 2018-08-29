package platform;

import org.junit.Test;
import platform.behaviors.AdaptationTypeManager;
import platform.behaviors.AdaptivePolicy;
import platform.behaviors.impl.CalibratedScanForObject;
import platform.camera.Camera;
import platform.camera.components.CameraConfigurationFile;
import platform.goals.MultiCameraGoal;
import platform.goals.VisualObservationOfInterest;
import platform.imageAnalysis.AnalysisTypeManager;
import platform.imageAnalysis.ImageAnalysis;
import platform.imageAnalysis.ImageProcessor;
import platform.imageAnalysis.impl.HSVMultiObjectLocator;
import platform.map.GlobalMap;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.*;

import static org.junit.Assert.assertTrue;

public class MultiCameraCoreTests {

    @Test
    public void buildMultiCameraCoreFmJava() {

        /////////////////////////
        // BUILD CAMERAS       //
        /////////////////////////

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

        /////////////////////////////
        // BUILD ANALYSIS LIBRARY  //
        /////////////////////////////

        AnalysisTypeManager analysisTypeManager = new AnalysisTypeManager();

        HashMap<String, ImageProcessor> analysisMap = new HashMap<>();
        analysisMap.put("HSV", new HSVMultiObjectLocator());

        analysisTypeManager.setStringToAnalysisMap(analysisMap);

        /////////////////////////////
        // BUILD BEHAVIOR LIBRARY  //
        /////////////////////////////

        AdaptationTypeManager adaptationTypeManager = new AdaptationTypeManager();

        HashMap<String, AdaptivePolicy> behavior = new HashMap<>();
        behavior.put("CALIB", new CalibratedScanForObject());

        adaptationTypeManager.setStringToAdaptivePolicyMap(behavior);

        /////////////////////////////
        // BUILD GOALS             //
        /////////////////////////////

        VisualObservationOfInterest objectOfInterest = new VisualObservationOfInterest();

        Map<String, Object> attrs = new HashMap<>();
        attrs.put("threshold", new Integer(7));
        ImageAnalysis hsv = new ImageAnalysis("HSV",2, attrs);

        objectOfInterest.addAnalysisAlgorithm(hsv);

        List<String> calibrationGoalIds = new ArrayList<>();
        calibrationGoalIds.add("g1");

        List<String> actionTypes = new ArrayList<>();
        actionTypes.add("CALIB");

        MultiCameraGoal multiCameraGoal = new MultiCameraGoal("g1", true,1, MultiCameraGoal.GoalIndependence.NONEXCLUSIVE, MultiCameraGoal.CameraRequirements.CALIBRATION, Arrays.asList(objectOfInterest),new GlobalMap(),"CALIB",actionTypes,calibrationGoalIds, new HashMap<>());

        List<String> calibrationGoalIds2 = new ArrayList<>();

        List<String> actionTypes2 = new ArrayList<>();
        actionTypes2.add("CALIB");

        MultiCameraGoal multiCameraGoal2 = new MultiCameraGoal("g2",true, 2, MultiCameraGoal.GoalIndependence.NONEXCLUSIVE, MultiCameraGoal.CameraRequirements.VIEW_CONTROL_OPTIONAL,Arrays.asList(objectOfInterest),new GlobalMap(),"SIMPLE_IN_VIEW_MOT",actionTypes2,calibrationGoalIds2,new HashMap<>());

        /////////////////////////////
        // BUILD MCA APP           //
        /////////////////////////////

        HashMap<String,Object> obs = new HashMap<>();
        obs.put("heartbeat","100000");
        obs.put("testMode","100000");

        MultiCameraCore mcp_application = new MultiCameraCore(Arrays.asList(multiCameraGoal,multiCameraGoal2), onvifCameras,analysisTypeManager,adaptationTypeManager,obs);

        assertTrue(mcp_application!=null);
        assertTrue(mcp_application.getAllCameras().size() == 1);
        assertTrue(mcp_application.getMultiCameraGoals().size() == 2);
        assertTrue(mcp_application.getAdaptationTypeManager().getStringToAdaptivePolicyMap().size() == 1);
        assertTrue(mcp_application.getAnalysisTypeManager().getStringToAnalysisMap().size() == 1);

    }



}
