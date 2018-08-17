package platform;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.enums.EnumConverter;
import com.thoughtworks.xstream.converters.extended.NamedMapConverter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import platform.core.camera.core.Camera;
import platform.core.camera.core.components.CameraConfigurationFile;
import platform.core.camera.core.components.ViewCapabilities;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.goals.core.components.VisualObservationOfInterest;
import platform.core.imageAnalysis.AnalysisTypeManager;
import platform.core.imageAnalysis.ImageAnalysis;
import platform.core.map.GlobalMap;
import platform.core.map.IndoorMap;
import platform.core.map.LocalMap;
import platform.core.utilities.LoopTimer;
import platform.core.utilities.adaptation.AdaptationTypeManager;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;

public class MultiCameraCore_Configuration {

    private String id = UUID.randomUUID().toString();

    private List<String> cameraConfigurationFiles = new ArrayList<>();
    private List<MultiCameraGoal> multiCameraGoals = new ArrayList<>();
    private AnalysisTypeManager analysisTypeManager;
    private AdaptationTypeManager adaptationTypeManager;

    private Map<String,Object> additionalFields = new HashMap<>();

    public static XStream xstream = new XStream(new DomDriver());

    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n";

    static {

        xstream.alias("multiCameraCore_configuration_file", MultiCameraCore_Configuration.class);
        xstream.useAttributeFor(MultiCameraCore_Configuration.class,"id");

        xstream.alias("multiCameraGoal",MultiCameraGoal.class);
        xstream.useAttributeFor(MultiCameraGoal.class, "priority");
        xstream.useAttributeFor(MultiCameraGoal.class,"id");
        xstream.omitField(MultiCameraGoal.class,"lastAnalysisResultTime");

        xstream.alias("visualObservationOfInterest",VisualObservationOfInterest.class);
        xstream.addImplicitCollection(VisualObservationOfInterest.class,"analysisAlgorithmsSet");

        xstream.alias("indoorMap",LocalMap.class);

        xstream.alias("loopConfig",LoopTimer.class);
        xstream.omitField(LoopTimer.class,"lastTime");
        xstream.omitField(LoopTimer.class,"loopActive");
        xstream.useAttributeFor(LoopTimer.class,"pulsesPerLoop");
        xstream.omitField(LoopTimer.class,"pulseCounter");

        xstream.omitField(MultiCameraCore.class,"globalMap");

        xstream.omitField(platform.core.map.Map.class,"polygon");
        xstream.omitField(platform.core.map.Map.class,"longDiff");
        xstream.omitField(platform.core.map.Map.class,"latDiff");
        xstream.omitField(platform.core.map.Map.class,"longMax");
        xstream.omitField(platform.core.map.Map.class,"latMax");
        xstream.omitField(platform.core.map.Map.class,"longMin");
        xstream.omitField(platform.core.map.Map.class,"latMin");

        xstream.alias("globalMap",GlobalMap.class);
        xstream.alias("localMap",LocalMap.class);

        xstream.aliasField("x", LocalMap.class,"x1");
        xstream.aliasField("y", LocalMap.class,"y1");

        xstream.omitField(platform.core.map.Map.class,"x");
        xstream.omitField(platform.core.map.Map.class,"y");

        xstream.omitField(LocalMap.class,"swLong");
        xstream.omitField(LocalMap.class,"swLat");
        xstream.omitField(LocalMap.class,"neLong");
        xstream.omitField(LocalMap.class,"neLat");

        xstream.alias("analysisAlgorithm",ImageAnalysis.class);
        xstream.useAttributeFor(ImageAnalysis.class,"precedence");
        xstream.useAttributeFor(ImageAnalysis.class,"imageAnalysisType");
        xstream.omitField(ImageAnalysis.class,"imageProcessor");
        xstream.omitField(ImageAnalysis.class,"analysisTypeManager");


        NamedMapConverter namedMapConverter = new NamedMapConverter(xstream.getMapper(),"attr","description",String.class,"value",String.class);
        xstream.registerConverter(namedMapConverter);

        xstream.omitField(MultiCameraGoal.class,"mcp_application");
        xstream.omitField(MultiCameraGoal.class,"cameras");
        xstream.omitField(MultiCameraGoal.class,"activeCamerasPerRegion");
        xstream.omitField(MultiCameraGoal.class,"adaptationMap");

        xstream.alias("map",Map.class);
        xstream.useAttributeFor(platform.core.map.Map.class,"mapType");
        xstream.useAttributeFor(platform.core.map.Map.class,"coordinateSys");

        EnumConverter enumConverter = new EnumConverter();
        enumConverter.canConvert(ViewCapabilities.PTZControl.class);
        enumConverter.canConvert(ViewCapabilities.PTZ.class);

    }

    public MultiCameraCore_Configuration() {


    }


    public void writeConfigurationToXML(MultiCameraCore mcp_application) throws FileNotFoundException, MalformedURLException {

        System.out.println("WARNING: Produced XML is not bound to xsd, must add binding manually.");

        for (Camera camera: mcp_application.getAllCameras()){
            cameraConfigurationFiles.add(camera.getFilename());
        }

        multiCameraGoals.addAll(mcp_application.getMultiCameraGoals());
        additionalFields.putAll(mcp_application.getAdditionalFields());
        analysisTypeManager = mcp_application.getAnalysisTypeManager();
        adaptationTypeManager = mcp_application.getAdaptationTypeManager();

        //////////////////////////////
        //////////////////////////////
        //////////////////////////////

        FileOutputStream fop = null;
        File file;

        String content = "This is the text content";

        try {

            java.nio.file.Path path = java.nio.file.Paths.get("src", "main", "resources","\\multiCameraCore__configuration__" + id + ".xml");
            file = new File(path.toString());
            fop = new FileOutputStream(file);

            if (!file.exists()){
                file.createNewFile();
            }

            // get the content in bytes
            byte[] contentInBytes = XML_HEADER.getBytes();

            fop.write(contentInBytes);

            xstream.toXML(this,fop);

            fop.flush();
            fop.close();

        }
        catch (IOException e){
            e.printStackTrace();
        }
        finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    public MultiCameraCore_Configuration readMCPConfig(String fileName) throws FileNotFoundException {

        java.nio.file.Path path = java.nio.file.Paths.get("src", "main", "resources",fileName.toString());
        String file = path.toString();

        InputStream in = new FileInputStream(file);

        MultiCameraCore_Configuration mcp_application_configuration = (MultiCameraCore_Configuration) xstream.fromXML(in);

        if (in!=null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return mcp_application_configuration;
    }

    public MultiCameraCore createMCAppFromMCPConfigurationFile(String fileName) throws FileNotFoundException {

        MultiCameraCore_Configuration mcp_application_configuration = readMCPConfig(fileName);

        this.cameraConfigurationFiles = mcp_application_configuration.cameraConfigurationFiles;

        List<MultiCameraGoal> multiCameraGoals = new ArrayList<>();
        for (MultiCameraGoal multiCameraGoal: mcp_application_configuration.multiCameraGoals){

            if(multiCameraGoal.getMap().getMapType() == platform.core.map.Map.MapType.GLOBAL){
                multiCameraGoal.setMap(new GlobalMap());
            }
            else if (multiCameraGoal.getMap().getCoordinateSys() == platform.core.map.Map.CoordinateSys.INDOOR){
                multiCameraGoal.setMap(new IndoorMap(((LocalMap)multiCameraGoal.getMap()).getX1(),((LocalMap)multiCameraGoal.getMap()).getY1()));
            }
            else if (multiCameraGoal.getMap().getMapType() == platform.core.map.Map.MapType.LOCAL){
                multiCameraGoal.setMap(new LocalMap(multiCameraGoal.getMap().getCoordinateSys(),((LocalMap)multiCameraGoal.getMap()).getX1(),((LocalMap)multiCameraGoal.getMap()).getY1()));

            }

            multiCameraGoals.add(new MultiCameraGoal(multiCameraGoal.getId(),multiCameraGoal.isActivated(),multiCameraGoal.getPriority(), multiCameraGoal.getGoalIndependence(),multiCameraGoal.getCameraRequirements(),multiCameraGoal.getObjectsOfInterest(),multiCameraGoal.getMap(),multiCameraGoal.getMotionControllerType(),multiCameraGoal.getActionTypes(),multiCameraGoal.getCalibrationGoalIds(),multiCameraGoal.getAdditionalFieldMap()));

        }

        mcp_application_configuration.multiCameraGoals = multiCameraGoals;

        List<Camera> cameras = new ArrayList<>();

        for (String cameraConfigName : mcp_application_configuration.cameraConfigurationFiles){

            CameraConfigurationFile cameraConfigurationFile = new CameraConfigurationFile();
            try {
                Camera camera = cameraConfigurationFile.readFromCameraConfigurationFile(cameraConfigName);
                cameras.add(camera);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        MultiCameraCore mcp_application = new MultiCameraCore(mcp_application_configuration.multiCameraGoals,cameras,mcp_application_configuration.analysisTypeManager,mcp_application_configuration.adaptationTypeManager,mcp_application_configuration.additionalFields);

        return mcp_application;

    }

    public List<String> getCameraConfigurationFiles() {
        return cameraConfigurationFiles;
    }

    public void setCameraConfigurationFiles(List<String> cameraConfigurationFiles) {
        this.cameraConfigurationFiles = cameraConfigurationFiles;
    }
}
