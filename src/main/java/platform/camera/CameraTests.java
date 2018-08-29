package platform.camera;

import com.sun.javafx.geom.Vec3d;
import org.bytedeco.javacpp.opencv_core;
import org.junit.Before;
import org.junit.Test;
import platform.camera.Camera;
import platform.camera.LocalONVIFCamera;
import platform.camera.components.*;
import platform.camera.impl.SimulatedCamera;
import platform.cameraManager.CameraStreamManager;
import platform.imageAnalysis.impl.components.ImageCompare;
import platform.map.Map;
import platform.utilities.CustomID;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacv.Java2DFrameUtils.toBufferedImage;
import static org.bytedeco.javacv.Java2DFrameUtils.toMat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CameraTests {

    CameraConfigurationFile cameraConfigurationFile;
    Camera camera;
    boolean connected =false;

    @Before
    public void setUp(){


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

        connected= camera.init();

    }

    @Test
    public void cameraClassCreatedFmXMLFile(){
        assertTrue(camera!=null);
        assertTrue(connected);
    }

    @Test
    public void cameraCreatedFromJava() throws MalformedURLException {

        LocalONVIFCamera localONVIFCamera = new LocalONVIFCamera("test",new URL("http://192.168.1.77:10080/onvif_device"),"admin", "", new ViewCapabilities(60, Arrays.asList(ViewCapabilities.PTZ.P), ViewCapabilities.PTZControl.CONT,100,-100,30,-300,0,0), new Vector3D(), new CameraLocation(0,0,0, Map.CoordinateSys.OUTDOOR), new HashMap<>());
        localONVIFCamera.init();

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
    public void testGetUniqueIdentifier(){

        //test only works for camera with serial number specified
        String serialNumberOfTestCamera = "305419896";

        String identifier = null;

        identifier = camera.getCameraUniqueIdentifier();

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

    /*test requires a static environment, i.e. objects in environment  not moving and lighting is not changing drastically*/
    @Test
    public void testControlPan(){

        boolean commandSuccess = false;

        CameraStreamManager cameraStreamManager = new CameraStreamManager();
        cameraStreamManager.init(camera.getStreamURI(),camera.getUsername(),camera.getPassword(),camera.isWorking(),"");

        PTZVector ptzVector1 = new PTZVector(-45,0,0);

        commandSuccess = camera.commandPTZMovement(ptzVector1);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        commandSuccess = camera.commandPTZStop();

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
                break;
            }

        }

        BufferedImage bufferedImage1 = cameraStreamManager.getDirectStreamView().getBufferedImage();

        opencv_core.Mat input =  toMat(bufferedImage1);
        opencv_core.Mat output = input.clone();

        //convert image to bgr format easy comparison
        cvtColor(input, output, CV_BGR2GRAY);

        //turn back to buffered image to use image-compare algorithm
        bufferedImage1 = toBufferedImage(output);


        PTZVector ptzVector2 = new PTZVector(45,0,0);

        camera.commandPTZMovement(ptzVector2);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        camera.commandPTZStop();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        BufferedImage bufferedImage2 = cameraStreamManager.getDirectStreamView().getBufferedImage();

        input =  toMat(bufferedImage2);
        output = input.clone();

        //convert image to bgr format easy comparison
        cvtColor(input, output, CV_BGR2GRAY);

        //turn back to buffered image to use image-compare algorithm
        bufferedImage2 = toBufferedImage(output);

        ImageCompare imageCompare = new ImageCompare(bufferedImage1,bufferedImage2);
        imageCompare.setParameters(20,20,10,10);
        float compareVal = imageCompare.compare();

        System.out.println(compareVal + " " + commandSuccess);

        assertTrue(commandSuccess);
        assertTrue(compareVal< 0.98);

    }

    /*test requires a static environment, i.e. objects in environment  not moving and lighting is not changing drastically*/
    @Test
    public void testControlTilt(){

        boolean commandSuccess = false;

        CameraStreamManager cameraStreamManager = new CameraStreamManager();
        cameraStreamManager.init(camera.getStreamURI(),camera.getUsername(),camera.getPassword(),camera.isWorking(),"");

        PTZVector ptzVector1 = new PTZVector(0,-45,0);

        commandSuccess = camera.commandPTZMovement(ptzVector1);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        commandSuccess = camera.commandPTZStop();

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
                break;
            }

        }

        BufferedImage bufferedImage1 = cameraStreamManager.getDirectStreamView().getBufferedImage();

        opencv_core.Mat input =  toMat(bufferedImage1);
        opencv_core.Mat output = input.clone();

        //convert image to bgr format easy comparison
        cvtColor(input, output, CV_BGR2GRAY);

        //turn back to buffered image to use image-compare algorithm
        bufferedImage1 = toBufferedImage(output);
        PTZVector ptzVector2 = new PTZVector(0,45,0);

        camera.commandPTZMovement(ptzVector2);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        camera.commandPTZStop();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        BufferedImage bufferedImage2 = cameraStreamManager.getDirectStreamView().getBufferedImage();

        input =  toMat(bufferedImage2);
        output = input.clone();

        //convert image to bgr format easy comparison
        cvtColor(input, output, CV_BGR2GRAY);

        //turn back to buffered image to use image-compare algorithm
        bufferedImage2 = toBufferedImage(output);

        ImageCompare imageCompare = new ImageCompare(bufferedImage1,bufferedImage2);
        imageCompare.setParameters(20,20,10,10);

        float compareVal = imageCompare.compare();
        System.out.println(compareVal + " " + commandSuccess);

        assertTrue(commandSuccess);
        assertTrue(compareVal< 0.98);

    }

    /*test requires a static environment, i.e. objects in environment  not moving and lighting is not changing drastically*/
    @Test
    public void testControlZoom(){

        boolean commandSuccess = false;

        CameraStreamManager cameraStreamManager = new CameraStreamManager();
        cameraStreamManager.init(camera.getStreamURI(),camera.getUsername(),camera.getPassword(),camera.isWorking(),"");

        PTZVector ptzVector1 = new PTZVector(0,0,-45);

        commandSuccess = camera.commandPTZMovement(ptzVector1);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        commandSuccess = camera.commandPTZStop();

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
                break;
            }

        }

        BufferedImage bufferedImage1 = cameraStreamManager.getDirectStreamView().getBufferedImage();

        opencv_core.Mat input =  toMat(bufferedImage1);
        opencv_core.Mat output = input.clone();

        //convert image to bgr format easy comparison
        cvtColor(input, output, CV_BGR2GRAY);

        //turn back to buffered image to use image-compare algorithm
        bufferedImage1 = toBufferedImage(output);
        PTZVector ptzVector2 = new PTZVector(0,0,45);

        camera.commandPTZMovement(ptzVector2);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        camera.commandPTZStop();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        BufferedImage bufferedImage2 = cameraStreamManager.getDirectStreamView().getBufferedImage();

        input =  toMat(bufferedImage2);
        output = input.clone();

        //convert image to bgr format easy comparison
        cvtColor(input, output, CV_BGR2GRAY);

        //turn back to buffered image to use image-compare algorithm
        bufferedImage2 = toBufferedImage(output);

        ImageCompare imageCompare = new ImageCompare(bufferedImage1,bufferedImage2);
        imageCompare.setParameters(20,20,10,10);

        float compareVal = imageCompare.compare();

        assertTrue(commandSuccess);
        assertTrue(compareVal< 0.98);

    }

    @Test
    public void testConnectionTest(){
        boolean test = camera.simpleUnsecuredFunctionTest();
        assertTrue(test);
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
    public void writeToCameraConfigFile() {

        CameraConfigurationFile cameraConfigurationFile = new CameraConfigurationFile();

        cameraConfigurationFile.writeConfigurationToXML(camera,"ONVIF");

    }

}
