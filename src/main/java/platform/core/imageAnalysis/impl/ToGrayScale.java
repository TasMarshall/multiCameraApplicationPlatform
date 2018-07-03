package platform.core.imageAnalysis.impl;


import org.bytedeco.javacpp.opencv_core;

import platform.core.imageAnalysis.AnalysisResult;

import java.util.HashMap;
import java.util.Map;

public class ToGrayScale {

    public static AnalysisResult performProcessing(opencv_core.Mat input, Map<String, Integer> additionalIntAttr){
        opencv_core.Mat output = input.clone();

        /*Imgproc.cvtColor(input, output, Imgproc.COLOR_BGR2GRAY);


        Map<String,Object> outInfo = new HashMap<>();

        AnalysisResult analysisResult = new AnalysisResult(output,outInfo);
        return analysisResult;*/
        return null;
    }
}
