package platform.core.imageAnalysis.impl;

import org.bytedeco.javacpp.opencv_core;
import platform.core.imageAnalysis.AnalysisResult;
import platform.core.imageAnalysis.ImageProcessor;
import platform.core.imageAnalysis.impl.components.ImageCompare;
import platform.core.imageAnalysis.impl.outputObjects.ImageComparison;
import platform.core.utilities.adaptation.core.components.InMemoryBackground;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacv.Java2DFrameUtils.toBufferedImage;
import static org.bytedeco.javacv.Java2DFrameUtils.toMat;

public class ImageComparator extends ImageProcessor {

    private final static Logger LOGGER = Logger.getLogger(ImageComparator.class.getName());

    Map<String, BufferedImage> bufferedImageMap;
    Map<String, Integer> counterMap;
    Map<String, Integer> lowPassCounterMap;

    @Override
    public void init() {
        bufferedImageMap = new HashMap<>();
        counterMap = new HashMap<>();
        lowPassCounterMap = new HashMap<>();

        LOGGER.setLevel(Level.FINE);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.FINE);

        LOGGER.addHandler(handler);
    }

    @Override
    public void defineKeys() {

    }

    @Override
    public AnalysisResult performProcessing(String cameraId, BufferedImage inputImage, Map<String, Object> additionalIntAttr) {

        float similarity = 0;

        opencv_core.Mat input =  toMat(inputImage);
        opencv_core.Mat output = input.clone();

        //convert image to bgr format easy comparison
        cvtColor(input, output, CV_BGR2GRAY);

        //turn back to buffered image to use image-compare algorithm
        BufferedImage bufferedImage = toBufferedImage(output);


        if (bufferedImageMap.get(cameraId) == null){
            bufferedImageMap.put(cameraId,bufferedImage);
            counterMap.put(cameraId,0);
            lowPassCounterMap.put(cameraId,0);
            similarity = 0;
        }
        else {

            ImageCompare imageCompare = new ImageCompare(bufferedImage,bufferedImageMap.get(cameraId));
            imageCompare.setParameters(12, 7, 5, 10);

            // Display some indication of the differences in the image.
            //imageCompare.setDebugMode(0);
            // Compare.
            similarity = imageCompare.compare();
            similarity*=100.0;

            if (counterMap.get(cameraId) % 5 ==0) {
                LOGGER.fine("Camera: " + cameraId + ", Image Analyzer: Image Comparator, Similarity Result: " + similarity + ", Number of similar images: " + counterMap.get(cameraId));
            }

            //measure similarity consistency
            if (similarity > 97 && similarity <= 100) {
                int counter = counterMap.get(cameraId);
                counter++;

                counterMap.put(cameraId,counter);
            } else {
                counterMap.put(cameraId,0);
            }

            if (similarity > 90 && similarity <= 100){
                int counter = lowPassCounterMap.get(cameraId);
                counter++;
                lowPassCounterMap.put(cameraId,counter);
            } else {
                lowPassCounterMap.put(cameraId,0);
            }

            bufferedImageMap.put(cameraId,bufferedImage);

        }

        int value = counterMap.get(cameraId);
        int value2 = lowPassCounterMap.get(cameraId);
        int outVal;

        if (value <2 && value2 > 50){
            outVal = value2;
        }
        else {
            outVal = value;
        }

        Map<String,Serializable> out = new HashMap<>();
        ImageComparison imageComparison = new ImageComparison(similarity,outVal,false);
        out.put("imageComparison", imageComparison);

        AnalysisResult analysisResult = new AnalysisResult(output,out);

        return analysisResult;
    }

}
