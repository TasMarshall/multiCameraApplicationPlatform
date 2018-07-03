package platform;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import org.opencv.core.Core;
import platform.core.camera.core.Camera;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.map.GlobalMap;
import platform.core.utilities.NanoTimeValue;
import platform.utilities.CameraHeartbeatMessage;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static platform.MapView.distanceInLatLong;


public class MCA_Agent extends Agent {

    MCP_Application mcp_application;

    private boolean cameraMonitorsAdded;

/*    private GlobalMap globalMap;

    private List<Camera> cameras;
    private List<MultiCameraGoal> multiCameraGoals;

    private NanoTimeValue lastTime;
    private NanoTimeValue currentTime;

    *//*    private ComponentState state = new ComponentState();*//*

    private Map<String, Object> additionalFields = new HashMap<>();*/

    ///////////////////////////////////////////////////////////////////////////
    /////                       CONSTRUCTOR                               /////
    ///////////////////////////////////////////////////////////////////////////

/*    public MCA_Agent(List<MultiCameraGoal> multiCameraGoals, List<Camera> cameras) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

*//*        this.multiCameraGoals = multiCameraGoals;
        this.cameras = cameras;

        this.lastTime = new NanoTimeValue(System.nanoTime());*//*

        init();
    }

    public void init() {

*//*        for (Camera camera: cameras){



        }*//*

        *//*createGlobalMap(multiCameraGoals,getAllCameras());*//*

        *//*for (MultiCameraGoal multiCameraGoal: multiCameraGoals){
            multiCameraGoal.init(this,0.1);
        }*//*

    }*/

    ///////////////////////////////////////////////////////////////////////////
    /////                       AGENCY                                    /////
    ///////////////////////////////////////////////////////////////////////////

    protected void setup() {

        // Printout a welcome message
        System.out.println("MCA_Agent "+ getAID().getName()+" initializing.");

        Object[] args = getArguments();
        if (args != null && args.length > 0) {

            MCP_Application_Configuration mcp_application_configuration = new MCP_Application_Configuration();


            try {
                mcp_application = mcp_application_configuration.createMCAppFromMCPConfigurationFile((String) args[0] +".xml");

                addCoreComponents();
                addCoreBehaviours();

            } catch (FileNotFoundException e) {
                System.out.println("MCA_Agent " + getAID().getName()+ " configuration file failed to read ");
            }

            System.out.println("MCA_Agent "+ getAID().getName()+" initialized.");
        }
        else{
            System.out.println("Multi Camera Application File Not Specified.");
            doDelete();
        }
    }

    private void addCoreBehaviours() {

        addMCAExecutionLoop();

        addCameraMonitorListeners();

    }

    private void addCameraMonitorListeners() {

        if (cameraMonitorsAdded == true) {

            TopicManagementHelper topicHelper = null;
            try {
                topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
                for (Camera camera : mcp_application.getAllCameras()) {
                    final AID topic = topicHelper.createTopic("CameraMonitor" + camera.getIdAsString());
                    topicHelper.register(topic);

                    addBehaviour(new CyclicBehaviour(this) {

                        String[] message;
                        CameraHeartbeatMessage cameraHeartbeatMessage;

                        public void action() {
                            ACLMessage msg = myAgent.receive(MessageTemplate.MatchTopic(topic));
                            if (msg != null) {
                                message = msg.getContent().split(" ");
                                if (message[1].equals("Camera_Connection_Heartbeat")) {
                                    cameraHeartbeatMessage = CameraHeartbeatMessage.getMessage(message);
                                    mcp_application.getCameraManager().getCameraByID(cameraHeartbeatMessage.getId()).setWorking(cameraHeartbeatMessage.isWorking());
                                    System.out.println(" - " +
                                            myAgent.getLocalName() + " <- " +
                                            cameraHeartbeatMessage.getId() + " " + cameraHeartbeatMessage.isWorking());
                                }
                            }
                            block();
                        }
                    });
                }
            } catch (ServiceException e) {
                e.printStackTrace();
            }
        }

    }

    private void addMCAExecutionLoop() {

        addBehaviour(new TickerBehaviour(this, 100) {
            protected void onTick() {
                mcp_application.executeMAPELoop();
            }
        } );

    }

    private void addCoreComponents() {

        addCameraMonitors();

    }

    private void addCameraMonitors() {

        if (mcp_application.getAdditionalFields().containsKey("heartbeat")){

            cameraMonitorsAdded = true;

            if(mcp_application.getAdditionalFields().get("heartbeat") instanceof String
                    && Integer.valueOf((String)mcp_application.getAdditionalFields().get("heartbeat")) > 0
                    && Integer.valueOf((String)mcp_application.getAdditionalFields().get("heartbeat")) < Integer.MAX_VALUE
                    ){

                for (Camera camera: mcp_application.getAllCameras()){

                    String name = "CameraMonitor" + camera.getIdAsString();

                    Object[] args = new Object[3];
                    args[0] = camera.getFilename();
                    args[1] = Integer.valueOf((String)mcp_application.getAdditionalFields().get("heartbeat"));
                    args[2] = getAID().getName();

                    AgentContainer c = getContainerController();

                    try {
                        AgentController a = c.createNewAgent( name, "platform.core.camera.core.CameraMonitorAgent", args );
                        a.start();
                    }
                    catch (Exception e){}

                }

            }

        }


    }


    ///////////////////////////////////////////////////////////////////////////
    /////                       MAPE LOOP                                 /////
    ///////////////////////////////////////////////////////////////////////////

/*    public void executeMAPELoop() {

        currentTime = new NanoTimeValue(System.nanoTime());
*//*
        monitor();
        analyse();
        plan();
        execute();*//*

        lastTime = currentTime;

    }*/

/*    public void monitor() {

        for (MultiCameraGoal multiCameraGoal: multiCameraGoals){
            multiCameraGoal.monitor();
        }

        for (Camera camera: getAllCameras()) {
            camera.getAnalysisManager().monitor();
        }

        localONVIFCameraMonitor.monitor();
        simulatedCameraMonitor.monitor();

    }

    public void analyse() {

        for (MultiCameraGoal multiCameraGoal: multiCameraGoals){
            multiCameraGoal.analyse();
        }

        localONVIFCameraMonitor.analyse();
        simulatedCameraMonitor.analyse();

        //if goals to be analysed
        //gather affected cameras

    }

    public void plan() {

        for (MultiCameraGoal multiCameraGoal: multiCameraGoals){
            multiCameraGoal.plan();
        }

        localONVIFCameraMonitor.plan();
        simulatedCameraMonitor.plan();
        //plan goal distribution between independent groups of affected cameras

    }

        public void execute() {

            for (MultiCameraGoal multiCameraGoal: multiCameraGoals){
                multiCameraGoal.execute();
            }

            localONVIFCameraMonitor.execute();
            simulatedCameraMonitor.execute();

    }*/


    ///////////////////////////////////////////////////////////////////////////
    /////                       CLASS FUNCTIONS                           /////
    ///////////////////////////////////////////////////////////////////////////


    public void createGlobalMap(List<MultiCameraGoal> multiCameraGoals, List<? extends Camera> cameras) {

        double minLat = Double.POSITIVE_INFINITY;
        double minLong = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        double maxLong = Double.NEGATIVE_INFINITY;

        platform.core.map.Map.CoordinateSys coordinateSys = platform.core.map.Map.CoordinateSys.INDOOR;

        if (multiCameraGoals.size() > 0) {

            for (MultiCameraGoal multiCameraGoal : multiCameraGoals) {

                if (multiCameraGoal.getMap().getMapType() == platform.core.map.Map.MapType.LOCAL) {

                    if (multiCameraGoal.getMap().getLongMin() < minLong)
                        minLong = multiCameraGoal.getMap().getLongMin();
                    if (multiCameraGoal.getMap().getLongMax() > maxLong)
                        maxLong = multiCameraGoal.getMap().getLongMax();
                    if (multiCameraGoal.getMap().getLatMin() < minLat)
                        minLat = multiCameraGoal.getMap().getLatMin();
                    if (multiCameraGoal.getMap().getLatMax() > maxLat)
                        maxLat = multiCameraGoal.getMap().getLatMax();

                    if (multiCameraGoal.getMap().getCoordinateSys() == platform.core.map.Map.CoordinateSys.OUTDOOR) {
                        coordinateSys = platform.core.map.Map.CoordinateSys.OUTDOOR;
                    }

                }

            }

        }

        for (Camera camera : cameras) {

            double camLat = camera.getLocation().getLatitude();
            double camLong = camera.getLocation().getLongitude();

            double dLat = distanceInLatLong((double) camera.getAdditionalAttributes().get("range"), camLat, camLong, 0)[0];
            double dLong = distanceInLatLong((double) camera.getAdditionalAttributes().get("range"), camLat, camLong, 90)[1];

            if (camLong - dLong < minLong)
                minLong = camera.getLocation().getLongitude() - dLong;
            if (camera.getLocation().getLongitude() + dLong > maxLong)
                maxLong = camera.getLocation().getLongitude() + dLong;
            if (camera.getLocation().getLatitude() - dLat < minLat)
                minLat = camera.getLocation().getLatitude() - dLat;
            if (camera.getLocation().getLatitude() + dLat > maxLat)
                maxLat = camera.getLocation().getLatitude() + dLat;

            /*globalMap = new GlobalMap(minLong - 0.0001, minLat - 0.0001, maxLong + 0.0001, maxLat + 0.0001);*/

        }

    }


    ///////////////////////////////////////////////////////////////////////////
    /////                       GETTERS AND SETTERS                       /////
    ///////////////////////////////////////////////////////////////////////////

}