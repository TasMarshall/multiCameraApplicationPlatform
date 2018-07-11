package platform.core.imageAnalysis.impl;


import org.bytedeco.javacpp.opencv_core;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import platform.core.imageAnalysis.AnalysisResult;
import platform.core.imageAnalysis.ImageProcessor;
import platform.core.imageAnalysis.impl.outputObjects.TestObj;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

public class ToGrayScale extends ImageProcessor {

    public AnalysisResult performProcessing(opencv_core.Mat input, Map<String, Integer> additionalIntAttr){

        opencv_core.Mat output = input.clone();

        cvtColor(input, output, CV_BGR2GRAY);

        Map<String,Serializable> outInfo = new HashMap<>();
        AnalysisResult analysisResult = new AnalysisResult(output,outInfo);

        return analysisResult;

    }

    @Override
    public void defineKeys() {

    }
}
