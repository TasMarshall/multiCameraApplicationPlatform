package platform.core.imageAnalysis;

import org.bytedeco.javacpp.opencv_core;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ImageProcessor implements Serializable {

    protected List<String> keys;

    public void defineInfoKeys(){
        keys = new ArrayList<String>();
        defineKeys();
    };

    public abstract void init();

    public abstract void defineKeys();

    public abstract AnalysisResult performProcessing(String cameraId, BufferedImage inputImage, Map<String, Object> additionalIntAttr);

    public List<String> getOutputInfoKeys(){
        return keys;
    };
}
