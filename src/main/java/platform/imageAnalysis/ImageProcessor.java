package platform.imageAnalysis;

import org.bytedeco.javacpp.opencv_core;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ImageProcessor implements Serializable {

    /**This function is used to initialize any internal data types*/
    public abstract void init();

    /**This function must be implemented to perform the image processing from the source BuffueredImage*/
    public abstract AnalysisResult performProcessing(String cameraId, BufferedImage inputImage, Map<String, Object> additionalIntAttr);

}
