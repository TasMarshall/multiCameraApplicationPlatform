package platform.core.utilities.adaptation.impl;

import org.onvif.ver10.schema.PTZVector;
import org.onvif.ver10.schema.Vector1D;
import org.onvif.ver10.schema.Vector2D;
import platform.core.camera.core.Camera;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.imageAnalysis.impl.outputObjects.ObjLocBounds;
import platform.core.imageAnalysis.impl.outputObjects.ObjectLocations;
import platform.core.utilities.adaptation.core.AdaptivePolicy;
import platform.core.utilities.adaptation.core.ContMotionController;
import platform.core.utilities.adaptation.core.components.PTZCommand;
import platform.core.utilities.adaptation.core.components.SearchForCrashStateMachine;
import platform.jade.ModelAgent;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class BlueObjectLocator extends ContMotionController implements AdaptivePolicy {


    private final static Logger LOGGER = Logger.getLogger(BlueObjectLocator.class.getName());

    Map<Camera,SearchForCrashStateMachine> stateMachineMap;

    Camera recordingCamera = null;
    ObjLocBounds recordingCameraData;

    public BlueObjectLocator(){
        
        LOGGER.setLevel(Level.FINE);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.FINE);

        LOGGER.addHandler(handler);

    }

    public PTZCommand calculatePTZVector(MultiCameraGoal multiCameraGoal, Camera camera) {

        long start = System.currentTimeMillis();

        String debug = "" + camera.getIdAsString();

        PTZVector ptzVector = new PTZVector();
        Vector1D zoom = new Vector1D();
        Vector2D pt = new Vector2D();
        pt.setX(0);
        pt.setY(0);
        zoom.setX(0);

        Map<String, Serializable> result = multiCameraGoal.getNewAnalysisResultMap().get(camera.getIdAsString());

            //guard
        SearchForCrashStateMachine stateMachine = stateMachineMap.get(camera);
        if (stateMachine == null) {
            stateMachine = new SearchForCrashStateMachine();
            stateMachineMap.put(camera, stateMachine);
        }

        Map<String, java.lang.Object> map = multiCameraGoal.getProcessedInfoMap().get(camera);
        if (map == null) {
            map = new HashMap<>();
            multiCameraGoal.getProcessedInfoMap().put(camera,map);
        }

        if (stateMachine.getScanningState() == SearchForCrashStateMachine.ScanningState.InitCheckForObject) {

            debug += " InitCheckForObject,";

            if (result != null) {
                ObjLocBounds objectLocation = getObjectLocationFmObjectLocationsInResults("blueObjectLocations", result);
                if (objectLocation != null) {
                    stateMachine.nextState(true, false);
                }
            }
            else {
                if (stateMachine.getPausingLoopTimer().lookPulse()) {
                    stateMachine.nextState(false, false);
                }

            }

        }
        else {

            Object a= map.get("blueObjectLost");
            Object b= map.get("cameraLostBlueObject");
            if (a != null && ((Boolean)a) == true){
                debug += " blueObjectLost,";
                stateMachine.setScanningState(SearchForCrashStateMachine.ScanningState.Exit);
                a= map.remove("blueObjectLost");
            }
            else if (b != null && ((Boolean)b) == true) {
                debug += " cameraLostBlueObject,";
                if (stateMachine.getScanningState() == SearchForCrashStateMachine.ScanningState.RecordCrash){
                    stateMachine.setScanningState(SearchForCrashStateMachine.ScanningState.SearchScanToBottomOfFeature);
                }
                a= map.remove("cameraLostBlueObject");
            }

            if (stateMachine.getScanningState() == SearchForCrashStateMachine.ScanningState.Exit) {
                debug += " exiting goal,";
                camera.getMultiCameraGoalList().remove(multiCameraGoal);
                multiCameraGoal.getCameras().remove(camera);
                multiCameraGoal.getProcessedInfoMap().remove(camera);
                stateMachineMap.remove(camera);

                if (multiCameraGoal.getCameras().size() == 0){
                    debug += " goal deactivating,";
                    multiCameraGoal.setActivated(false);
                }

                if (recordingCamera!= null) {
                    if (recordingCamera.getId().equals(camera.getId())) {
                        recordingCamera = null;
                        recordingCameraData = null;
                    }
                }

                debug += " x: " + pt.getX() + " y: " + pt.getY() + "," ;
                System.out.println(debug);

            } else if (result != null) {
                debug += " result not null,";
                if (stateMachine.getScanningState() == SearchForCrashStateMachine.ScanningState.FocusOnCrash) {
                    debug += " FocusOnCrash,";
                    ObjLocBounds objectLocation = getObjectLocationFmObjectLocationsInResults("blueObjectLocations", result);
                    if (objectLocation != null) {
                        debug += " objectLocation found,";
                        calculatePTZVectorToMoveToCenter(multiCameraGoal, camera, pt, objectLocation, stateMachine, "blueObjectLocated");

                        java.lang.Object o = multiCameraGoal.getProcessedInfoMap().get(camera).get("blueObjectLocated");
                        if (o != null) {
                            if (((Boolean) o).booleanValue() == true) {
                                debug += " objectLocation is centre view,";
                                //determine if should replace camera who is recording then go next state
                                if (recordingCamera == null) {
                                    debug += " camera is selected for recording crash,";
                                    stateMachine.nextState(true, true);
                                    recordingCamera = camera;
                                    recordingCameraData = objectLocation;
                                } else if (recordingCameraData.calculateSquaresize() < objectLocation.calculateSquaresize()) {
                                    debug += " camera is selected for recording crash,";
                                    stateMachine.nextState(true, true);

                                    stateMachineMap.get(recordingCamera).nextState(false, false);

                                    recordingCamera = camera;
                                    recordingCameraData = objectLocation;
                                } else {
                                    debug += " camera is not selected and will exit,";
                                    stateMachine.nextState(true, false);
                                }

                            }
                        }
                    }

                }
                else if (stateMachine.getScanningState() == SearchForCrashStateMachine.ScanningState.RecordCrash) {
                    debug += " RecordCrash,";
                    ObjLocBounds objectLocation = getObjectLocationFmObjectLocationsInResults("blueObjectLocations", result);
                    if (objectLocation != null) {
                        calculatePTZVectorToMoveToCenter(multiCameraGoal, camera, pt, objectLocation, stateMachine, "blueObjectLocated");
                        recordingCameraData = objectLocation;
                    }

                }
                else if (stateMachine.getScanningState() == SearchForCrashStateMachine.ScanningState.SearchScanToBottomOfFeature) {
                    debug += " SearchScanToBottomOfFeature,";
                    ObjLocBounds objectLocation = getObjectLocationFmObjectLocationsInResults("blueObjectLocations", result);
                    if (objectLocation != null) {
                        debug += " found blue object,";
                        stateMachine.nextState(true, false);
                    } else {

                        //use road / green object feature and scan to bottom then go next state
                        ObjLocBounds roadObjectLocation = getObjectLocationFmObjectLocationsInResults("objectLocations", result);
                        if (roadObjectLocation != null) {
                            debug += " found road object,";
                            calculatePTZVectorToMoveToBottom(multiCameraGoal, camera, pt, roadObjectLocation, stateMachine, "bottomFound");
                        } else {
                            debug += " road object not found,";
                        }

                        java.lang.Object o = multiCameraGoal.getProcessedInfoMap().get(camera).get("bottomFound");
                        if (o != null) {
                            if (((Boolean) o).booleanValue() == true) {
                                debug += " found bottom of road object,";
                                multiCameraGoal.getProcessedInfoMap().get(camera).remove("bottomFound");
                                //determine if should replace camera who is recording then go next state
                                stateMachine.nextState(false, false);
                            }
                        }

                    }

                } else if (stateMachine.getScanningState() == SearchForCrashStateMachine.ScanningState.SearchScanToTopOfFeature) {
                    debug += " SearchScanToTopOfFeature,";
                    ObjLocBounds objectLocation = getObjectLocationFmObjectLocationsInResults("blueObjectLocations", result);
                    if (objectLocation != null) {
                        debug += " found blue object,";
                        stateMachine.nextState(true, false);
                    } else {

                        //use road / green object feature and scan to bottom then go next state
                        ObjLocBounds roadObjectLocation = getObjectLocationFmObjectLocationsInResults("objectLocations", result);
                        if (roadObjectLocation != null) {
                            debug += " found road object,";
                            calculatePTZVectorToMoveToTop(multiCameraGoal, camera, pt, roadObjectLocation, stateMachine, "topFound");
                        } else {
                            debug += " road object not found,";
                        }

                        java.lang.Object o = multiCameraGoal.getProcessedInfoMap().get(camera).get("topFound");
                        if (o != null) {
                            if (((Boolean) o).booleanValue() == true) {
                                debug += " found top of road object,";
                                multiCameraGoal.getProcessedInfoMap().get(camera).remove("topFound");
                                //determine if should replace camera who is recording then go next state
                                stateMachine.nextState(false, false);
                            }
                        }
                    }
                }

            }

            //System.out.println(camera.getIdAsString() + " " + stateMachine.getScanningState() + " x: " + pt.getX() + "y: " + pt.getY());
            debug += " x: " + pt.getX() + " y: " + pt.getY() + "," ;

        }

        float time = (System.currentTimeMillis() - start);

        LOGGER.fine("CrashLocator, " + debug + " Execution time: " + time);

        ptzVector.setPanTilt(pt);
        ptzVector.setZoom(zoom);

        PTZCommand ptzCommand = new PTZCommand(ptzVector,5);

        return ptzCommand;

    }


    private ObjLocBounds getObjectLocationFmObjectLocationsInResults(String s, Map<String, Serializable> result) {

        ObjectLocations objectLocations = (ObjectLocations) result.get(s);
        ObjLocBounds objectLocation = null;

        if (objectLocations != null){
            objectLocation = (ObjLocBounds) objectLocations.getExtremesLocation();
        }

        return objectLocation;

    }


    @Override
    public void init() {
        stateMachineMap = new HashMap<>();
    }
}



