package platform.jade;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import platform.MCP_Application;
import platform.MCP_Application_Configuration;
import platform.core.camera.core.Camera;
import platform.core.camera.impl.SimulatedCamera;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.imageAnalysis.AnalysisResult;
import platform.core.imageAnalysis.ImageAnalysis;
import platform.jade.utilities.*;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static platform.MapView.distanceInLatLong;


public class MCA_Agent extends Agent {

    MCP_Application mcp_application;

    private boolean cameraMonitorsAdded;
    private HashMap<String, Boolean> cameraHeartbeatReceived = new HashMap<>();
    private Heartbeat heartbeat;
    int cameraMonitorTimer;

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

        //init vlcj
        new NativeDiscovery().discover();

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
        addUpdateCameraAnalysers();
        addGeneralInformListeners();

    }

    private void addGeneralInformListeners() {

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    try {
                        Object content = msg.getContentObject();

                        if (content instanceof AnalysisResultsMessage) {
                            AnalysisResultsMessage analysisResultsMessage = (AnalysisResultsMessage) content;
                            for (String key : analysisResultsMessage.getResults().keySet()) {

                                Map<String,Map<String,Serializable>> newAnalysisResultMap = mcp_application.getGoalById(key).getNewAnalysisResultMap();
                                newAnalysisResultMap.put(analysisResultsMessage.getCameraID(),analysisResultsMessage.getResults().get(key));

                                mcp_application.getGoalById(key).getLatestAnalysisResults().put(analysisResultsMessage.getCameraID(),analysisResultsMessage);

                            }
                        } else if (false) {

                        }

                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                }
                block();
            }
        });

    }

    private void addUpdateCameraAnalysers() {

        //SEND ACTIVE GOALS TO CAMERA_ANALYSER
        for (Camera camera: mcp_application.getAllCameras()) {

            addBehaviour(new TickerBehaviour(this,1000) {
                protected void onTick() {

                    CameraAnalysisMessage cameraAnalysisMessage = new CameraAnalysisMessage(camera.getCurrentGoals());
                    if(camera.isWorking()) {
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        try {
                            msg.setContentObject(cameraAnalysisMessage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        msg.addReceiver(new AID("CameraAnalyser" + camera.getIdAsString(),AID.ISLOCALNAME));
                        send(msg);
                    }
                }
            } );
        }
    }

    private void addCameraMonitorListeners() {

        if (cameraMonitorsAdded == true) {

            heartbeat.init(mcp_application.getAllCameras());

            TopicManagementHelper topicHelper = null;
            try {
                topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
                for (Camera camera : mcp_application.getAllCameras()) {

                    final AID topic = topicHelper.createTopic("CameraMonitor" + camera.getIdAsString());
                    topicHelper.register(topic);

                    addBehaviour(new CyclicBehaviour(this) {

                        CameraHeartbeatMessage cameraHeartbeatMessage;

                        public void action() {
                            ACLMessage msg = myAgent.receive(MessageTemplate.MatchTopic(topic));
                            if (msg != null) {
                                try {
                                    Object content = msg.getContentObject();
                                    if (content instanceof CameraHeartbeatMessage) {
                                        cameraHeartbeatMessage = (CameraHeartbeatMessage) content;
                                        if (cameraHeartbeatMessage.isWorking() == false)  mcp_application.getCameraManager().getCameraByID(cameraHeartbeatMessage.getId()).setWorking(false);
                                        heartbeat.recordHeartbeat(cameraHeartbeatMessage.getId());
                                        System.out.println(" - " +
                                                myAgent.getLocalName() + " <- " +
                                                cameraHeartbeatMessage.getId() + " " + cameraHeartbeatMessage.isWorking());
                                    }
                                } catch (UnreadableException e) {
                                    e.printStackTrace();
                                }
                            }
                            block();
                        }
                    });

                }

                addBehaviour(new TickerBehaviour(this,cameraMonitorTimer) {

                    @Override
                    protected void onTick() {

                        List<String> failedHeartbeatIDs = heartbeat.checkHeartBeats();

                        for (String s: failedHeartbeatIDs){
                            mcp_application.getCameraManager().getCameraByID(s).setWorking(false);
                        }

                    }
                });

            } catch (ServiceException e) {
                e.printStackTrace();
            }
        }

    }

    private void addMCAExecutionLoop() {

        addBehaviour(new TickerBehaviour(this, 100) {
            protected void onTick() {

                List<Serializable> actionMessages = mcp_application.executeMAPELoop();

                /*for (Serializable s : actionMessages) {

                    if (s instanceof MotionActionMessage) {
                        MotionActionMessage m = (MotionActionMessage) s;
                        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                        try {
                            msg.setContentObject(m);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        msg.addReceiver(new AID("CameraMonitor" + m.getCameraID(), AID.ISLOCALNAME));
                        send(msg);
                    }
                }*/


            }
        } );

    }

    private void addCoreComponents() {

        addCameraStreamAnalysers();
        addHeartbeat();

    }

    private void addHeartbeat() {

        if (mcp_application.getAdditionalFields().containsKey("heartbeat")
                && mcp_application.getAdditionalFields().get("heartbeat") instanceof String
                && Integer.valueOf((String)mcp_application.getAdditionalFields().get("heartbeat")) > 0
                && Integer.valueOf((String)mcp_application.getAdditionalFields().get("heartbeat")) < Integer.MAX_VALUE )
        {
            cameraMonitorTimer = Integer.valueOf((String)mcp_application.getAdditionalFields().get("heartbeat"));
        }
        else {
            cameraMonitorTimer = 60000;
        }

        heartbeat = new Heartbeat(cameraMonitorTimer);
        addCameraMonitors();

    }

    private void addCameraStreamAnalysers() {

        for (Camera camera: mcp_application.getAllCameras()){

            String cameraType;
            if (camera instanceof SimulatedCamera){
                cameraType = "SIM";
            }
            else { cameraType = "NONSIM";}

            String name = "CameraAnalyser" + camera.getIdAsString();

            Object[] args = new Object[7];
            args[0] = camera.getStreamURI();
            args[1] = camera.getUsername();
            args[2] = camera.getPassword();
            args[3] = String.valueOf(camera.isWorking());
            args[4] = cameraType;
            args[5] = camera.getIdAsString();
            args[6] = getAID().getName();

            AgentContainer c = getContainerController();

            try {
                AgentController a = c.createNewAgent( name, "platform.jade.AnalysisAgent", args );
                a.start();
            }
            catch (Exception e){}

        }

    }

    private void addCameraMonitors() {

        cameraMonitorsAdded = true;

        for (Camera camera: mcp_application.getAllCameras()){

            String name = "CameraMonitor" + camera.getIdAsString();

            Object[] args = new Object[3];
            args[0] = camera.getFilename();
            args[1] = Integer.valueOf((String)mcp_application.getAdditionalFields().get("heartbeat"));
            args[2] = getAID().getName();

            AgentContainer c = getContainerController();

            try {
                AgentController a = c.createNewAgent( name, "platform.jade.CameraMonitorAgent", args );
                a.start();
                }
                catch (Exception e){}
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