package platform.core.imageAnalysis;

import org.opencv.core.Mat;

import java.util.HashMap;
import java.util.Map;

public class AnalysisResult {

    private Mat output;
    private Map<String,Object> additionalInformation = new HashMap<>();

    public AnalysisResult(Mat output, Map<String, Object> additionalInformation) {
        this.output = output;
        this.additionalInformation = additionalInformation;
    }

    public void refresh(){
        additionalInformation = new HashMap<>();
    }

    public Mat getOutput() {
        return output;
    }

    public void setOutput(Mat output) {
        this.output = output;
    }

    public Map<String, Object> getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(Map<String, Object> additionalInformation) {
        this.additionalInformation = additionalInformation;
    }
}
