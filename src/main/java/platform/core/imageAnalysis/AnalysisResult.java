package platform.core.imageAnalysis;


import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.opencv.core.Mat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AnalysisResult {

    private opencv_core.Mat output;
    private Mat outputOPENCV;
    private Map<String,Serializable> additionalInformation = new HashMap<>();

    public AnalysisResult(Mat output, Map<String, Serializable> additionalInformation) {
        this.outputOPENCV = output;
        this.output = new org.bytedeco.javacpp.opencv_core.Mat((Pointer)null) { { address = output.getNativeObjAddr(); } };
        this.additionalInformation = additionalInformation;
    }

    public AnalysisResult(opencv_core.Mat output, Map<String, Serializable> additionalInformation) {
        this.output = output;
        this.additionalInformation = additionalInformation;
    }

    public void refresh(){
        additionalInformation = new HashMap<>();
    }

    public opencv_core.Mat getOutput() {
        return output;
    }

    public void setOutput(org.bytedeco.javacpp.opencv_core.Mat output) {
        this.output = output;
    }

    public Map<String, Serializable> getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(Map<String, Serializable> additionalInformation) {
        this.additionalInformation = additionalInformation;
    }
}
