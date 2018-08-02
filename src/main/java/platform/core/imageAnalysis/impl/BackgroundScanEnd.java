package platform.core.imageAnalysis.impl;

import org.bytedeco.javacpp.opencv_core;
import platform.core.imageAnalysis.AnalysisResult;
import platform.core.imageAnalysis.ImageProcessor;
import platform.core.imageAnalysis.impl.components.ImageCompare;
import platform.core.imageAnalysis.impl.outputObjects.BackgroundScanEndResult;
import platform.core.imageAnalysis.impl.outputObjects.ImageComparison;
import platform.core.utilities.adaptation.core.components.InMemoryBackground;
import platform.core.utilities.adaptation.impl.BackgroundScanner;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacv.Java2DFrameUtils.toBufferedImage;

public class BackgroundScanEnd extends ImageProcessor {

    Map<String, BufferedImage> bufferedImageMap;
    Map<String, Integer> counterMap;
    Map<String, Integer> lowPassCounterMap;

    @Override
    public void init() {
        bufferedImageMap = new HashMap<>();
        counterMap = new HashMap<>();
        lowPassCounterMap = new HashMap<>();
    }

    @Override
    public void defineKeys() {

    }

    @Override
    public AnalysisResult performProcessing(String cameraId, opencv_core.Mat inputImage, Map<String, Object> additionalIntAttr) {
        boolean backgroundScanFinished = false;

        /*opencv_core.Mat output = inputImage.clone();
        cvtColor(inputImage, output, CV_BGR2GRAY);*/

        BufferedImage bufferedImage2 = toBufferedImage(inputImage);

        if (additionalIntAttr.containsKey("inMemBackground")){
            InMemoryBackground inMemoryBackground = (InMemoryBackground) additionalIntAttr.get("inMemBackground");
            if(inMemoryBackground != null) {
                opencv_core.Mat neCornerImage = inMemoryBackground.getNEImage(cameraId); // need to convert to BW
                if (neCornerImage != null) {

                    BufferedImage tempBI = toBufferedImage(neCornerImage);
                    //compare current to NE corner
                    ImageCompare imageCompare2 = new ImageCompare(bufferedImage2,tempBI);
                    imageCompare2.setParameters(12, 7, 5, 10);
                    /*float similarity2 = imageCompare2.compare();

                    if(similarity2 > 0.98){
                        //spazz out
                        System.out.println("WAGHAHHA");
                        backgroundScanFinished = true;

                    }*/
                }
            }
        }

        Map<String,Serializable> out = new HashMap<>();
        BackgroundScanEndResult backgroundScanEndResult = new BackgroundScanEndResult(backgroundScanFinished);
        out.put("backgroundScanEndResult", backgroundScanEndResult);

        AnalysisResult analysisResult = new AnalysisResult(inputImage,out);

        return analysisResult;
    }

}
