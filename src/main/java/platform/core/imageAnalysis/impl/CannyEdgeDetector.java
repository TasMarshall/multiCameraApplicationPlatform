package platform.core.imageAnalysis.impl;

import org.bytedeco.javacpp.opencv_core;
import platform.core.imageAnalysis.AnalysisResult;
import platform.core.imageAnalysis.ImageProcessor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_imgproc.Canny;

public class CannyEdgeDetector extends ImageProcessor {

    public AnalysisResult performProcessing(String cameraId, opencv_core.Mat inputImage, Map<String, Object> additionalIntAttr) {

        opencv_core.Mat detectedEdges = new opencv_core.Mat();

        /*org.bytedeco.javacpp.opencv_core.Mat grayMat = ToGrayScale.performProcessing(inputImage, null).getOutput();
*/
        opencv_core.Mat output = new opencv_core.Mat();

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

        Canny(inputImage, output, threshold1, threshold2, apertureSize, true /*L2 gradient*/);

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
