package platform.core.imageAnalysis.impl;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import platform.core.imageAnalysis.AnalysisResult;

import java.util.HashMap;
import java.util.Map;

public class ToGrayScale {

    public static AnalysisResult performProcessing(Mat input, Map<String, Integer> additionalIntAttr){
        Mat output = input.clone();

        Imgproc.cvtColor(input, output, Imgproc.COLOR_BGR2GRAY);


        Map<String,Object> outInfo = new HashMap<>();

        AnalysisResult analysisResult = new AnalysisResult(output,outInfo);
        return analysisResult;
    }
}
