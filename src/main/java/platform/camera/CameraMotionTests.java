package platform.camera;

import org.bytedeco.javacpp.opencv_core;
import org.junit.BeforeClass;
import org.junit.Test;
import platform.camera.components.PTZVector;
import platform.cameraManager.CameraStreamManager;
import platform.imageAnalysis.impl.components.ImageCompare;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacv.Java2DFrameUtils.toBufferedImage;
import static org.bytedeco.javacv.Java2DFrameUtils.toMat;
import static org.junit.Assert.assertTrue;

public class CameraMotionTests {

    static CameraConfigurationFile cameraConfigurationFile;
    static Camera camera;
    static Camera camera2;
    static boolean connected =false;
    static boolean connected2 =false;
    static boolean streamInitialized =false;
    static boolean streamInitialized2 =false;
    static CameraStreamManager cameraStreamManager;
    static CameraStreamManager cameraStreamManager2;

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

        cameraStreamManager = new CameraStreamManager();
        cameraStreamManager.init(camera.getStreamURI(),camera.getUsername(),camera.getPassword(),camera.isWorking(),"");

        streamInitialized = false;

        while (!streamInitialized){

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
                streamInitialized = true;
                break;
            }

        }

        cameraStreamManager2 = new CameraStreamManager();
        cameraStreamManager2.init(camera2.getStreamURI(),camera2.getUsername(),camera2.getPassword(),camera2.isWorking(),"");

        streamInitialized2 = false;

        while (!streamInitialized2){

            BufferedImage bufferedImage1 = cameraStreamManager2.getDirectStreamView().getBufferedImage();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            BufferedImage bufferedImage2 = cameraStreamManager2.getDirectStreamView().getBufferedImage();

            ImageCompare imageCompare = new ImageCompare(bufferedImage1,bufferedImage2);

            float compareVal = imageCompare.compare();

            if (compareVal < 1){
                streamInitialized2 = true;
                break;
            }

        }

    }

    @Test
    public void testControlPan(){

        boolean commandSuccess = false;

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

    @Test
    public void testControlPan2(){

        boolean commandSuccess = false;

        PTZVector ptzVector1 = new PTZVector(-1,0,0);

        commandSuccess = camera2.commandPTZMovement(ptzVector1);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        commandSuccess = camera2.commandPTZStop();

        boolean streamInitiatilzed = false;

        while (!streamInitiatilzed){

            BufferedImage bufferedImage1 = cameraStreamManager2.getDirectStreamView().getBufferedImage();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            BufferedImage bufferedImage2 = cameraStreamManager2.getDirectStreamView().getBufferedImage();

            ImageCompare imageCompare = new ImageCompare(bufferedImage1,bufferedImage2);

            float compareVal = imageCompare.compare();

            if (compareVal < 1){
                break;
            }

        }

        BufferedImage bufferedImage1 = cameraStreamManager2.getDirectStreamView().getBufferedImage();

        opencv_core.Mat input =  toMat(bufferedImage1);
        opencv_core.Mat output = input.clone();

        //convert image to bgr format easy comparison
        cvtColor(input, output, CV_BGR2GRAY);

        //turn back to buffered image to use image-compare algorithm
        bufferedImage1 = toBufferedImage(output);


        PTZVector ptzVector2 = new PTZVector(1,0,0);

        camera2.commandPTZMovement(ptzVector2);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        camera2.commandPTZStop();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        BufferedImage bufferedImage2 = cameraStreamManager2.getDirectStreamView().getBufferedImage();

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

    @Test
    public void testControlTilt2(){

        boolean commandSuccess = false;

        PTZVector ptzVector1 = new PTZVector(0,-1,0);

        commandSuccess = camera2.commandPTZMovement(ptzVector1);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        commandSuccess = camera2.commandPTZStop();

        boolean streamInitiatilzed = false;

        while (!streamInitiatilzed){

            BufferedImage bufferedImage1 = cameraStreamManager2.getDirectStreamView().getBufferedImage();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            BufferedImage bufferedImage2 = cameraStreamManager2.getDirectStreamView().getBufferedImage();

            ImageCompare imageCompare = new ImageCompare(bufferedImage1,bufferedImage2);

            float compareVal = imageCompare.compare();

            if (compareVal < 1){
                break;
            }

        }

        BufferedImage bufferedImage1 = cameraStreamManager2.getDirectStreamView().getBufferedImage();

        opencv_core.Mat input =  toMat(bufferedImage1);
        opencv_core.Mat output = input.clone();

        //convert image to bgr format easy comparison
        cvtColor(input, output, CV_BGR2GRAY);

        //turn back to buffered image to use image-compare algorithm
        bufferedImage1 = toBufferedImage(output);
        PTZVector ptzVector2 = new PTZVector(0,1,0);

        camera2.commandPTZMovement(ptzVector2);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        camera2.commandPTZStop();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        BufferedImage bufferedImage2 = cameraStreamManager2.getDirectStreamView().getBufferedImage();

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
    public void testControlZoomOnNonZoomCamera(){

        boolean commandSuccess = false;

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

        assertTrue(compareVal> 0.98);


    }


    @Test
    public void testControlZoom2(){

        boolean commandSuccess = false;

        PTZVector ptzVector1 = new PTZVector(0,0,-1);

        commandSuccess = camera2.commandPTZMovement(ptzVector1);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        commandSuccess = camera2.commandPTZStop();

        boolean streamInitiatilzed = false;

        while (!streamInitiatilzed){

            BufferedImage bufferedImage1 = cameraStreamManager2.getDirectStreamView().getBufferedImage();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            BufferedImage bufferedImage2 = cameraStreamManager2.getDirectStreamView().getBufferedImage();

            ImageCompare imageCompare = new ImageCompare(bufferedImage1,bufferedImage2);

            float compareVal = imageCompare.compare();

            if (compareVal < 1){
                break;
            }

        }

        BufferedImage bufferedImage1 = cameraStreamManager2.getDirectStreamView().getBufferedImage();

        opencv_core.Mat input =  toMat(bufferedImage1);
        opencv_core.Mat output = input.clone();

        //convert image to bgr format easy comparison
        cvtColor(input, output, CV_BGR2GRAY);

        //turn back to buffered image to use image-compare algorithm
        bufferedImage1 = toBufferedImage(output);
        PTZVector ptzVector2 = new PTZVector(0,0,1);

        camera2.commandPTZMovement(ptzVector2);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        camera2.commandPTZStop();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        BufferedImage bufferedImage2 = cameraStreamManager2.getDirectStreamView().getBufferedImage();

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

}
