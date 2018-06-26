package platform.core.camera.core.components;

import com.sun.javafx.geom.Vec3d;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.enums.EnumConverter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import platform.core.camera.core.Camera;
import platform.core.camera.core.LocalONVIFCamera;
import platform.core.camera.impl.SimulatedCamera;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
public class CameraConfigurationFile implements Serializable {

    @Id
    private String id = UUID.randomUUID().toString();

    private String cameraDesignStandard;

    private String xsi = "";
    private String xmls = "";

    private String url;  //Defined by the URL at initialization
    private String streamURI;   //Private attribute used by the CameraStreamManager
    private String username, password;  //Credentials
    private ViewCapabilities viewCapabilities;

    private CameraOrientation cameraOrientation;
    private CameraLocation location;    //Location of Camera in a specified Map
    private Map<String, Object> additionalAttributes;

    public static XStream xstream = new XStream(new DomDriver());

    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n";

    static {

        //CameraConfigurationfile Classes
        xstream.alias("cameraConfigurationFile", CameraConfigurationFile.class);

        //Primitives
        xstream.useAttributeFor(CameraConfigurationFile.class,"id");
        xstream.useAttributeFor(CameraConfigurationFile.class,"url");
        xstream.useAttributeFor(CameraConfigurationFile.class,"username");
        xstream.useAttributeFor(CameraConfigurationFile.class,"password");
        xstream.useAttributeFor(CameraConfigurationFile.class,"streamURI");
        xstream.useAttributeFor(CameraConfigurationFile.class,"cameraDesignStandard");

        //View Capabilities SubClass Classes
        xstream.alias("viewCapabilities", ViewCapabilities.class);
        //Primitives
        xstream.useAttributeFor(ViewCapabilities.class,"viewAngle");
        xstream.useAttributeFor(ViewCapabilities.class,"isPTZ");
        xstream.useAttributeFor(ViewCapabilities.class,"maxPanViewAngle");
        xstream.useAttributeFor(ViewCapabilities.class,"minPanViewAngle");
        xstream.useAttributeFor(ViewCapabilities.class,"maxTiltViewAngle");
        xstream.useAttributeFor(ViewCapabilities.class,"minTiltViewAngle");
        xstream.useAttributeFor(ViewCapabilities.class,"maxZoom");
        xstream.useAttributeFor(ViewCapabilities.class,"minZoom");
        xstream.omitField(ViewCapabilities.class,"ptzControlDomain");

        EnumConverter enumConverter = new EnumConverter();
        enumConverter.canConvert(ViewCapabilities.PTZControl.class);
        enumConverter.canConvert(ViewCapabilities.PTZ.class);

        xstream.aliasField("ptzControl",ViewCapabilities.class,"ptzControl");
        xstream.addImplicitCollection(ViewCapabilities.class, "ptzType");

        xstream.alias( "ptzType",ViewCapabilities.PTZ.class);

        //Camera Location
        xstream.alias("cameraOrientation", CameraOrientation.class);

        xstream.omitField(CameraOrientation.class,"viewDomain");

        xstream.alias("globalVector", Vec3d.class);
        xstream.aliasField("bearing", Vec3d.class, "x");
        xstream.aliasField("roll", Vec3d.class, "y");
        xstream.aliasField("pitch", Vec3d.class, "z");

        //Camera Location
        xstream.alias("cameraLocation", CameraLocation.class);
        //Primitives
        xstream.useAttributeFor(CameraLocation.class,"hasCoordinates");
        xstream.useAttributeFor(CameraLocation.class,"height2Ground");
        xstream.useAttributeFor(CameraLocation.class,"latitude");
        xstream.useAttributeFor(CameraLocation.class,"longitude");

        xstream.alias("additionalAttributes", HashMap.class);

    }

    public CameraConfigurationFile() {


    }


    public void writeConfigurationToXML(Camera camera, String databaseURL, String cameraDesignStandard){

        System.out.println("WARNING: Produced XML is not bound to xsd, must add binding manually. Add 'xsi:noNamespaceSchemaLocation=\"camera_configuration_schema.xsd\"' and 'xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\' to the appropriate locations.");

        this.cameraDesignStandard = cameraDesignStandard;

        this.id = camera.getIdAsString();
        this.url = camera.getUrl().toString();
        this.streamURI = camera.getStreamURI();
        this.username = camera.getUsername();
        this.password =camera.getPassword();
        this.viewCapabilities = camera.getViewCapabilities();

        this.cameraOrientation = camera.getCameraOrientation();
        this.location = camera.getLocation();
        this.additionalAttributes = camera.getAdditionalAttributes();

        //////////////////////////////
        //////////////////////////////
        //////////////////////////////

        FileOutputStream fop = null;
        File file;

        String content = "This is the text content";

        try {
            file = new File(databaseURL + "\\camera_configuration_" + id + ".xml");
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

    public Camera readFromCameraConfigurationFile(String fileName) throws FileNotFoundException, MalformedURLException {

        java.nio.file.Path path = java.nio.file.Paths.get("src", "main", "resources",fileName.toString());
        String file = path.toString();

        InputStream in = new FileInputStream(file);

        CameraConfigurationFile cameraConfigurationFile = (CameraConfigurationFile) xstream.fromXML(in);

        if (cameraConfigurationFile.cameraDesignStandard.equals("ONVIF")){

            LocalONVIFCamera localONVIFCamera;
            localONVIFCamera = new LocalONVIFCamera(cameraConfigurationFile.id,
                    new URL(cameraConfigurationFile.getUrl()),
                    cameraConfigurationFile.getUsername(),
                    cameraConfigurationFile.getPassword(),
                    cameraConfigurationFile.getViewCapabilities(),
                    cameraConfigurationFile.getCameraOrientation().getGlobalVector(),
                    cameraConfigurationFile.getLocation(),
                    Collections.emptyList() ,
                    cameraConfigurationFile.getAdditionalAttributes()) {
            };

            return localONVIFCamera;

        }
        else if (cameraConfigurationFile.cameraDesignStandard.equals("SIM")){

            SimulatedCamera simulatedCamera;
            simulatedCamera = new SimulatedCamera(cameraConfigurationFile.id,
                    cameraConfigurationFile.getCameraOrientation().getGlobalVector(),
                    cameraConfigurationFile.getLocation(),
                    Collections.emptyList()) {
            };

            return simulatedCamera;

        }

        return null;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStreamURI() {
        return streamURI;
    }

    public void setStreamURI(String streamURI) {
        this.streamURI = streamURI;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ViewCapabilities getViewCapabilities() {
        return viewCapabilities;
    }

    public void setViewCapabilities(ViewCapabilities viewCapabilities) {
        this.viewCapabilities = viewCapabilities;
    }

    public CameraOrientation getCameraOrientation() {
        return cameraOrientation;
    }

    public void setCameraOrientation(CameraOrientation cameraOrientation) {
        this.cameraOrientation = cameraOrientation;
    }

    public CameraLocation getLocation() {
        return location;
    }

    public void setLocation(CameraLocation location) {
        this.location = location;
    }

    public Map<String, Object> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(Map<String, Object> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }
}
