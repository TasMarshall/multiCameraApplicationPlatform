package platform.core.camera.test;

import org.junit.Test;
import platform.core.camera.core.Camera;
import platform.core.camera.core.components.CameraConfigurationFile;
import platform.core.cameraMonitor.core.exceptions.CameraFunctionTestFailException;
import platform.core.utilities.CustomID;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

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
       /* LocalONVIFCameraMonitor localONVIFCameraMonitor = new LocalONVIFCameraMonitor();
        localONVIFCameraMonitor.findLocalCameraURLs();

        LocalONVIFCamera localONVIFCamera = null;

        try {
            localONVIFCamera = new HSIP2Time2Camera(new URL("http://192.168.1.75:10080/onvif/device_service"), "admin", "", new CameraLocation(10, 10, 10));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        assertEquals("192.168.1.75:10080", localONVIFCamera.getIP());*/


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
    public void vTest() throws MalformedURLException, CameraFunctionTestFailException {
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
    public void configurationLoadTest() {

        CameraConfigurationFile cameraConfigurationFile = new CameraConfigurationFile();

        try {
            Camera camera = cameraConfigurationFile.readFromCameraConfigurationFile("testFile.xml");

            assertEquals("3682328865", camera.getId().getSerialNumber());

            camera.setId(new CustomID("testOutputFile"));

            cameraConfigurationFile.writeConfigurationToXML(camera,"ONVIF");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
