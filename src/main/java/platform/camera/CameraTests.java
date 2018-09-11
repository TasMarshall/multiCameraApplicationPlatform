package platform.camera;

import org.junit.BeforeClass;
import org.junit.Test;
import platform.camera.components.*;
import platform.cameraManager.CameraStreamManager;
import platform.imageAnalysis.impl.components.ImageCompare;
import platform.map.Map;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacv.Java2DFrameUtils.toBufferedImage;
import static org.bytedeco.javacv.Java2DFrameUtils.toMat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CameraTests {

    static CameraConfigurationFile cameraConfigurationFile;
    static Camera camera;
    static Camera camera2;
    static boolean connected =false;
    static boolean connected2 =false;

    @BeforeClass
    public static void setUp(){

        //vlcj native library
        new NativeDiscovery().discover();

        cameraConfigurationFile = new CameraConfigurationFile();
        try {
            camera = cameraConfigurationFile.readFromCameraConfigurationFile("test_camera_configuration_onvif1");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        connected= camera.inititializeCamera();

        cameraConfigurationFile = new CameraConfigurationFile();
        try {
            camera2 = cameraConfigurationFile.readFromCameraConfigurationFile("test_camera_configuration_onvif2");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        connected2= camera2.inititializeCamera();

    }

    @Test
    public void cameraClassCreatedFmXMLFile(){
        assertTrue(camera!=null);
        assertTrue(connected);
    }

    @Test
    public void cameraClassCreatedFmXMLFile2(){
        assertTrue(camera2!=null);
        assertTrue(connected2);
    }

    @Test
    public void cameraCreatedFromJava() throws MalformedURLException {

        LocalONVIFCamera localONVIFCamera = new LocalONVIFCamera("test",new URL("http://192.168.1.77:10080/onvif_device"),"admin", "", new ViewCapabilities(60, Arrays.asList(ViewCapabilities.PTZ.P), ViewCapabilities.PTZControl.CONT,100,-100,30,-300,0,0), new CameraOrientation(0,0,0), new CameraLocation(0,0,0, Map.CoordinateSys.OUTDOOR), new HashMap<>());
        localONVIFCamera.inititializeCamera();

        assertTrue(localONVIFCamera.isOnline());
        assertTrue(localONVIFCamera.isWorking());

    }

    @Test
    public void cameraCreatedFromJava2() throws MalformedURLException {

        LocalONVIFCamera localONVIFCamera = new LocalONVIFCamera("test",new URL("http://192.168.1.109/onvif_device"),"admin", "softwareplatform2018", new ViewCapabilities(60, Arrays.asList(ViewCapabilities.PTZ.P), ViewCapabilities.PTZControl.CONT,100,-100,30,-300,0,0),new CameraOrientation(0,0,0), new CameraLocation(0,0,0, Map.CoordinateSys.OUTDOOR), new HashMap<>());
        localONVIFCamera.inititializeCamera();

        assertTrue(localONVIFCamera.isOnline());
        assertTrue(localONVIFCamera.isWorking());

    }

    @Test
    public void testCameraFinder() {

        //requires cameras to be connected to network by cable
        Collection<URL> urls = LocalONVIFCamera.findONVIFCameraURLs();

    }

    @Test
    public void testConnectToCamera(){

        assertTrue(connected);

    }

    @Test
    public void testConnectToCamera2(){

        assertTrue(connected2);

    }

    @Test
    public void testGetUniqueIdentifier(){

        //test only works for camera with serial number specified
        String serialNumberOfTestCamera = "305419896";

        String identifier = null;

        identifier = camera.getCameraUniqueIdentifier();

        assertTrue(identifier.equals(serialNumberOfTestCamera));

    }

    @Test
    public void testGetUniqueIdentifier2(){

        //test only works for camera with serial number specified
        String serialNumberOfTestCamera = "4E01A28PAJ1E65E";

        String identifier = null;

        identifier = camera2.getCameraUniqueIdentifier();

        assertTrue(identifier.equals(serialNumberOfTestCamera));

    }

    @Test
    public void testAcquireCameraInfoAndCap(){

        //id must be that specified in the configuration file
        assertTrue(camera.getIdAsString().equals("3682328865"));

        //if tested camera is working it should be working by test
        assertTrue(camera.isWorking());

        //view capabilities should have been acquired
        assertTrue(camera.getViewCapabilities() != null);

        //camera view is not neccesarily supported by cameras depending on motion control type and therefore is not tested.

    }

    @Test
    public void testAcquireCameraInfoAndCap2(){

        //id must be that specified in the configuration file
        assertTrue(camera2.getIdAsString().equals("dahua_ptz"));

        //if tested camera is working it should be working by test
        assertTrue(camera2.isWorking());

        //view capabilities should have been acquired
        assertTrue(camera2.getViewCapabilities() != null);

        //camera view is not neccesarily supported by cameras depending on motion control type and therefore is not tested.

    }

    @Test
    public void testStreamVideoFm(){

        CameraStreamManager cameraStreamManager = new CameraStreamManager();
        cameraStreamManager.init(camera.getStreamURI(),camera.getUsername(),camera.getPassword(),camera.isWorking(),"");

        boolean streamInitiatilzed = false;

        while (!streamInitiatilzed){

            BufferedImage bufferedImage1 = cameraStreamManager.getDirectStreamView().getBufferedImage();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            BufferedImage bufferedImage2 = cameraStreamManager.getDirectStreamView().getBufferedImage();

            ImageCompare imageCompare = new ImageCompare(bufferedImage1,bufferedImage2);

            float compareVal = imageCompare.compare();

            if (compareVal < 1){
                streamInitiatilzed = true;
                break;
            }

        }

        assertTrue(streamInitiatilzed);
        assertTrue(cameraStreamManager.getDirectStreamView().isStreamIsPlaying());

    }
    @Test
    public void testStreamVideoFm2(){

        CameraStreamManager cameraStreamManager = new CameraStreamManager();
        cameraStreamManager.init(camera2.getStreamURI(),camera2.getUsername(),camera2.getPassword(),camera2.isWorking(),"");

        boolean streamInitiatilzed = false;

        while (!streamInitiatilzed){

            BufferedImage bufferedImage1 = cameraStreamManager.getDirectStreamView().getBufferedImage();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            BufferedImage bufferedImage2 = cameraStreamManager.getDirectStreamView().getBufferedImage();

            ImageCompare imageCompare = new ImageCompare(bufferedImage1,bufferedImage2);

            float compareVal = imageCompare.compare();

            if (compareVal < 1){
                streamInitiatilzed = true;
                break;
            }

        }

        assertTrue(streamInitiatilzed);
        assertTrue(cameraStreamManager.getDirectStreamView().isStreamIsPlaying());

    }

    /*test requires a static environment, i.e. objects in environment  not moving and lighting is not changing drastically*/

    @Test
    public void testConnectionTest(){
        boolean test = camera.simpleUnsecuredFunctionTest();
        assertTrue(test);
    }

    @Test
    public void testConnectionTest2(){
        boolean test = camera2.simpleUnsecuredFunctionTest();
        assertTrue(test);
    }

    @Test
    public void writeToCameraConfigFile() {

        CameraConfigurationFile cameraConfigurationFile = new CameraConfigurationFile();

        cameraConfigurationFile.writeConfigurationToXML(camera,"ONVIF");

    }

}
