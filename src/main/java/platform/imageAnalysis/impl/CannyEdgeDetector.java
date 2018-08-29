package platform.imageAnalysis.impl;

import org.bytedeco.javacpp.opencv_core;
import platform.imageAnalysis.AnalysisResult;
import platform.imageAnalysis.ImageProcessor;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_imgproc.Canny;
import static org.bytedeco.javacv.Java2DFrameUtils.toMat;

public class CannyEdgeDetector extends ImageProcessor {

    public AnalysisResult performProcessing(String cameraId, BufferedImage inputImage, Map<String, Object> additionalIntAttr) {

        opencv_core.Mat input =  toMat(inputImage);
        opencv_core.Mat output = input.clone();

        opencv_core.Mat detectedEdges = new opencv_core.Mat();

        double threshold1;
        if (additionalIntAttr.get("threshold") != null){
            threshold1 = (Integer)additionalIntAttr.get("threshold");
        }
        else {
            threshold1 = 125;
        }

        double threshold2;
        if (additionalIntAttr.get("threshold2") != null){
            threshold2 =(Integer)additionalIntAttr.get("threshold2");
        }
        else {
            threshold2 = 350;
        }

        int apertureSize;
        if (additionalIntAttr.get("apertureSize") != null){
            apertureSize =(Integer) additionalIntAttr.get("apertureSize");
        }
        else {
            apertureSize = 3;
        }

        Canny(input, output, threshold1, threshold2, apertureSize, true /*L2 gradient*/);

        Map<String,Serializable> outInfo = new HashMap<>();
        AnalysisResult analysisResult = new AnalysisResult(output,outInfo);

        return analysisResult;

    }

    @Override
    public void init() {

    }

}
