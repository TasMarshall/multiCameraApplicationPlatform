package platform.core.imageAnalysis;

import platform.core.imageAnalysis.impl.CannyEdgeDetector;
import platform.core.imageAnalysis.impl.FaceDetectAndTrack;
import platform.core.imageAnalysis.impl.Stitching;
import platform.core.imageAnalysis.impl.ToGrayScale;

import java.io.Serializable;
import java.util.HashMap;

public class AnalysisTypeManager implements Serializable {

    private HashMap<String, ImageProcessor> stringToAnalysisMap = new HashMap<>();

    public AnalysisTypeManager(){

        stringToAnalysisMap.put("TO_GRAY_SCALE", new ToGrayScale());/*
        stringToAnalysisMap.put("FACE_DETECT", new FaceDetectAndTrack());
        stringToAnalysisMap.put("STITCHER", new Stitching());*/
        stringToAnalysisMap.put("CANNY_EDGE_DETECT", new CannyEdgeDetector());

    }

    public AnalysisTypeManager(HashMap<String, ImageProcessor> stringToAnalysisMap){

        this.stringToAnalysisMap = stringToAnalysisMap;
    }

    public ImageProcessor getImageProcessObject(String s){
        if(stringToAnalysisMap.containsKey(s)) {
            return stringToAnalysisMap.get(s);
        }
        else {
            return null;
        }
    }

    public HashMap<String, ImageProcessor> getStringToAnalysisMap() {
        return stringToAnalysisMap;
    }

    public void setStringToAnalysisMap(HashMap<String, ImageProcessor> stringToAnalysisMap) {
        this.stringToAnalysisMap = stringToAnalysisMap;
    }
}
