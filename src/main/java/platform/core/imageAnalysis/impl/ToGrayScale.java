package platform.core.imageAnalysis.impl;


import org.bytedeco.javacpp.opencv_core;

import platform.core.imageAnalysis.AnalysisResult;
import platform.core.imageAnalysis.ImageProcessor;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacv.Java2DFrameUtils.toMat;

public class ToGrayScale extends ImageProcessor {

    public AnalysisResult performProcessing(String cameraId, BufferedImage bufferedImage, Map<String, Object> additionalIntAttr){

        opencv_core.Mat input =  toMat(bufferedImage);
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
