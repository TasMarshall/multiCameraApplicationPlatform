package platform.core.imageAnalysis.impl;


import org.bytedeco.javacpp.opencv_core;

import platform.core.imageAnalysis.AnalysisResult;
import platform.core.imageAnalysis.ImageProcessor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

public class ToGrayScale extends ImageProcessor {

    public AnalysisResult performProcessing(String cameraId, opencv_core.Mat input, Map<String, Object> additionalIntAttr){

        opencv_core.Mat output = input.clone();

        cvtColor(input, output, CV_BGR2GRAY);

        Map<String,Serializable> outInfo = new HashMap<>();
        AnalysisResult analysisResult = new AnalysisResult(output,outInfo);

        return analysisResult;

    }

    @Override
    public void init() {

    }

    @Override
    public void defineKeys() {

    }
}
