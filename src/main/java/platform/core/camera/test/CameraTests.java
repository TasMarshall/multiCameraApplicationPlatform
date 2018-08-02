package platform.core.camera.test;

import com.sun.javafx.geom.Vec3d;
import org.junit.Test;
import platform.core.camera.core.Camera;
import platform.core.camera.core.LocalONVIFCamera;
import platform.core.camera.core.components.CameraConfigurationFile;
import platform.core.camera.core.components.CameraLocation;
import platform.core.camera.impl.SimulatedCamera;
import platform.core.cameraManager.core.CameraManager;
import platform.core.map.Map;
import platform.core.utilities.CustomID;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class CameraTests {


/*    private static Client hermesClient;
    private static String server;

    public static void config(Map<String,String> config) {
        // Will be called first, and pass in any user configuration.
        server = config.get("server");
    }

    @BeforeClass
    public static void suiteSetup() {
        // Set up something for the test cycle
        hermesClient = Hermes.newClient(server);
    }

    @AfterClass
    public static void suiteTeardown() {
        // Tear down something for the test cycle
        hermesClient.close();
        hermesClient = null;
    }

    @Before
    public void setup() {
        // Set up something for the test.
    }

    @After
    public void teardown() {
        // Tear down something for the test.
    }

    @Test
    public void test1() throws Exception {
        // This is where you call hermesClient or your client and test your service.
//    }*/

    @Test
    public void testCameraCreation() {

        Collection<URL> urls = LocalONVIFCamera.findONVIFCameraURLs();

//        LocalONVIFCamera localONVIFCamera = null;
//
//        try {
//            localONVIFCamera = new HSIP2Time2Camera(new URL("http://192.168.1.75:10080/onvif/device_service"), "admin", "", new CameraLocation(10, 10, 10));
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//
//        assertEquals("192.168.1.75:10080", localONVIFCamera.getIP());*/


    }

    @Test
    public void testInit() {

     /*   LocalONVIFCameraMonitor localONVIFCameraMonitor = new LocalONVIFCameraMonitor();
        localONVIFCameraMonitor.findLocalCameraURLs();

        LocalONVIFCamera localONVIFCamera = null;

        try {
            localONVIFCamera = new HSIP2Time2Camera(new URL("http://192.168.1.75:10080/onvif/device_service"), "admin", "", new CameraLocation(10, 10, 10));
            //localONVIFCamera = new HSIP2Time2Camera(new URL("http://192.168.1.76:10080/onvif/device_service"), "admin", "J9th3rd3", new CameraLocation(10, 10, 10));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {

            localONVIFCamera.init();
        } catch (CameraFunctionTestFailException e) {
            e.printStackTrace();
        }

        assertEquals("305419896", localONVIFCamera.getId().getSerialNumber());*/

    }

    @Test
    public void motionTest() {

/*
        LocalONVIFCameraMonitor localONVIFCameraMonitor = new LocalONVIFCameraMonitor();
        localONVIFCameraMonitor.findLocalCameraURLs();

        LocalONVIFCamera localONVIFCamera = null;

        try {
            localONVIFCamera = new HSIP2Time2Camera(new URL("http://192.168.1.75:10080/onvif/device_service"), "admin", "", new CameraLocation(10, 10, 10));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            localONVIFCamera.init();
            localONVIFCamera.pvtMotionFunctionTest();
        } catch (CameraFunctionTestFailException e) {
            e.printStackTrace();
        }
*/

    }

    @Test
    public void vTest() throws MalformedURLException {
        /*OnvifDevice nvt = null;
        try {
            nvt = new OnvifDevice("192.168.1.75:10080", "admin", "");
        } catch (ConnectException e) {
            e.printStackTrace();
        } catch (SOAPException e) {
            e.printStackTrace();
        }
        List<Profile> profiles = nvt.getDevices().getProfiles();
        String profileToken = profiles.get(0).getToken();

        profiles.get(0).getVideoSourceConfiguration();
        profiles.get(0).getVideoEncoderConfiguration();
        StreamSetup streamSetup = new StreamSetup();

        System.out.println(nvt.getMedia().getStreamUri(profileToken,streamSetup));*/
/*
        HSIP2Time2Camera hsip2Time2Camera = new HSIP2Time2Camera(new URL("http://192.168.1.75:10080/onvif/device_service"), "admin", "", new CameraLocation(10, 10, 10));
        hsip2Time2Camera.init();
        hsip2Time2Camera.getCameraStreamConnection();*/


    }


    @Test
    public void configurationLoadTest() throws MalformedURLException {

        CameraConfigurationFile cameraConfigurationFile = new CameraConfigurationFile();


        try {
            Camera camera = new SimulatedCamera("sim10",new Vec3d(0,0,0),new CameraLocation(0,0,0, Map.CoordinateSys.INDOOR));

            //Camera camera = cameraConfigurationFile.readFromCameraConfigurationFile("testFile");

            //assertEquals("3682328865", camera.getId().getSerialNumber());

            camera.setId(new CustomID("testOutputFile"));
            camera.getLocation().setCoordinateSys(Map.CoordinateSys.OUTDOOR);

            cameraConfigurationFile.writeConfigurationToXML(camera,"ONVIF");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
