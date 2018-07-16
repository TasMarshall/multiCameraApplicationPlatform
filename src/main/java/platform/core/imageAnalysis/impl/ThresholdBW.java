package platform.core.imageAnalysis.impl;


import org.bytedeco.javacpp.opencv_core;
import platform.core.imageAnalysis.AnalysisResult;
import platform.core.imageAnalysis.ImageProcessor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class ThresholdBW extends ImageProcessor {

    public AnalysisResult performProcessing(String cameraId, opencv_core.Mat input, Map<String, Object> additionalIntAttr){

        double minIntensity;
        if (additionalIntAttr.get("minIntensity") != null){
            minIntensity = (Integer)additionalIntAttr.get("threshold");
        }
        else {
            minIntensity = 125;
        }

        double maxIntensity;
        if (additionalIntAttr.get("maxIntensity") != null){
            maxIntensity = (Integer)additionalIntAttr.get("threshold");
        }
        else {
            maxIntensity = 255;
        }

        opencv_core.Mat output = input.clone();
        opencv_core.IplImage in = new opencv_core.IplImage(input);
        opencv_core.IplImage out = new opencv_core.IplImage(output);

        cvThreshold(in, out, minIntensity, maxIntensity, CV_THRESH_BINARY);

        Map<String,Serializable> outInfo = new HashMap<>();
        AnalysisResult analysisResult = new AnalysisResult(opencv_core.cvarrToMat(out),outInfo);

        return analysisResult;

    }

    @Override
    public void init() {

    }

    @Override
    public void defineKeys() {

    }

}
