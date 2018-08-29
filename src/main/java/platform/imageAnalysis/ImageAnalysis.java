package platform.imageAnalysis;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ImageAnalysis implements Serializable {

    private int precedence;

    private Map<String,Object> additionalIntAttr = new HashMap<>();

    private String imageAnalysisType;
    private AnalysisTypeManager analysisTypeManager;

    public ImageAnalysis(String imageAnalsysAlgorithmType, int precedence, Map<String,Object> additionalIntAttr) {
        this.imageAnalysisType = imageAnalsysAlgorithmType;
        this.precedence = precedence;
        this.additionalIntAttr = additionalIntAttr;
    }

    public ImageAnalysis(String imageAnalsysAlgorithmType, int precedence) {
        this.imageAnalysisType = imageAnalsysAlgorithmType;
        this.precedence = precedence;
    }

    public int getPrecedence() {
        return precedence;
    }

    public void setPrecedence(int precedence) {
        this.precedence = precedence;
    }

    public Map<String, Object> getAdditionalIntAttr() {
        return additionalIntAttr;
    }

    public void setAdditionalIntAttr(Map<String, Object> additionalIntAttr) {
        this.additionalIntAttr = additionalIntAttr;
    }

    public ImageProcessor getImageProcessor() {
        return analysisTypeManager.getImageProcessObject(imageAnalysisType.toString());
    }

    public String getImageAnalysisType() {
        return imageAnalysisType;
    }

    public void setImageAnalysisType(String imageAnalysisType) {
        this.imageAnalysisType = imageAnalysisType;
    }

    public AnalysisTypeManager getAnalysisTypeManager() {
        return analysisTypeManager;
    }

    public void setAnalysisTypeManager(AnalysisTypeManager analysisTypeManager) {
        this.analysisTypeManager = analysisTypeManager;
    }
}
