package platform.core.imageAnalysis;

import java.util.HashMap;
import java.util.Map;

public class ImageAnalysis {

    private ImageAnalyzer.ImageAnalysisAlgorithmTypes imageAnalsysAlgorithmType;
    private int precedence;

    private Map<String,Integer> additionalIntAttr = new HashMap<>();

    public ImageAnalysis(ImageAnalyzer.ImageAnalysisAlgorithmTypes imageAnalsysAlgorithmType, int precedence, Map<String,Integer> additionalIntAttr) {
        this.imageAnalsysAlgorithmType = imageAnalsysAlgorithmType;
        this.precedence = precedence;
        this.additionalIntAttr = additionalIntAttr;
    }

    public ImageAnalysis(ImageAnalyzer.ImageAnalysisAlgorithmTypes imageAnalsysAlgorithmType, int precedence) {
        this.imageAnalsysAlgorithmType = imageAnalsysAlgorithmType;
        this.precedence = precedence;
    }

    public ImageAnalyzer.ImageAnalysisAlgorithmTypes getImageAnalsysAlgorithmType() {
        return imageAnalsysAlgorithmType;
    }

    public void setImageAnalsysAlgorithmType(ImageAnalyzer.ImageAnalysisAlgorithmTypes imageAnalsysAlgorithmType) {
        this.imageAnalsysAlgorithmType = imageAnalsysAlgorithmType;
    }

    public int getPrecedence() {
        return precedence;
    }

    public void setPrecedence(int precedence) {
        this.precedence = precedence;
    }

    public Map<String, Integer> getAdditionalIntAttr() {
        return additionalIntAttr;
    }

    public void setAdditionalIntAttr(Map<String, Integer> additionalIntAttr) {
        this.additionalIntAttr = additionalIntAttr;
    }
}
