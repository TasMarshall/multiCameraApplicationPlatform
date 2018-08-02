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

import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacv.Java2DFrameUtils.toBufferedImage;

public class ImageComparator extends ImageProcessor {

    Map<String, BufferedImage> bufferedImageMap;
    Map<String, Integer> counterMap;
    Map<String, Integer> lowPassCounterMap;

    @Override
    public void init() {
        bufferedImageMap = new HashMap<>();
        counterMap = new HashMap<>();
        lowPassCounterMap = new HashMap<>();
    }

    @Override
    public void defineKeys() {

    }

    @Override
    public AnalysisResult performProcessing(String cameraId, opencv_core.Mat inputImage, Map<String, Object> additionalIntAttr) {

        float similarity = 0;

        opencv_core.Mat output = inputImage.clone();
        cvtColor(inputImage, output, CV_BGR2GRAY);

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
            //measure similarity consistency
            if (similarity > 97 && similarity <= 100) {
                int counter = counterMap.get(cameraId);
                counter++;

                /*if (counter == 3){
                    imwrite(cameraId + "_" + System.currentTimeMillis()+".PNG",inputImage);
                }*/

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
