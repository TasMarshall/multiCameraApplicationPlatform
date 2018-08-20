package platform.core.imageAnalysis.impl;

import org.bytedeco.javacpp.opencv_core;
import platform.core.imageAnalysis.AnalysisResult;
import platform.core.imageAnalysis.ImageProcessor;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacv.Java2DFrameUtils.toMat;

public class Blur extends ImageProcessor {
    @Override
    public void init() {

    }

    @Override
    public void defineKeys() {

    }

    @Override
    public AnalysisResult performProcessing(String cameraId, BufferedImage inputImage, Map<String, Object> additionalIntAttr) {


        opencv_core.Mat input =  toMat(inputImage);

        opencv_core.Mat blur = new opencv_core.Mat();

        input.copyTo(blur);

        blur(input, blur, new opencv_core.Size(3, 3));

        Map<String,Serializable> outInfo = new HashMap<>();
        AnalysisResult analysisResult = new AnalysisResult(blur,outInfo);

        return analysisResult;

    }
}
