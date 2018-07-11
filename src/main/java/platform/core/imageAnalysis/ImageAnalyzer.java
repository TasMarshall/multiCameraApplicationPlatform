package platform.core.imageAnalysis;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import platform.core.camera.core.Camera;
import platform.core.camera.impl.SimulatedCamera;
import platform.core.cameraManager.core.DirectStreamView;
import platform.core.imageAnalysis.impl.CannyEdgeDetector;
import platform.core.imageAnalysis.impl.FaceDetectAndTrack;
import platform.core.imageAnalysis.impl.ToGrayScale;

import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.bytedeco.javacpp.opencv_core.cvPoint;
import static org.bytedeco.javacpp.opencv_core.cvScalarAll;
import static org.bytedeco.javacpp.opencv_highgui.destroyWindow;
import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.bytedeco.javacpp.opencv_highgui.namedWindow;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.CV_FONT_HERSHEY_SIMPLEX;
import static org.bytedeco.javacpp.opencv_imgproc.cvPutText;

public class ImageAnalyzer {

    String cameraType;

    DirectStreamView directStreamView;

    opencv_core.Mat inputImage;

    AnalysisResult analysisResult;

    Set<ImageAnalysis> sortedAlgorithmSet;

    final CanvasFrame canvas = new CanvasFrame("Analyzer Analysis Demo", 1.0);;
    final OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();


    public ImageAnalyzer (DirectStreamView directStreamView, String cameraType, List<ImageAnalysis> imageAnalysisList){

        this.cameraType = cameraType;
        this.directStreamView = directStreamView;

        sortedAlgorithmSet = new TreeSet<>((o1, o2) -> {
            int comparePrecedence = ((ImageAnalysis) o2).getPrecedence();
            return ((ImageAnalysis) o1).getPrecedence() - comparePrecedence;
        });

        sortedAlgorithmSet.addAll(imageAnalysisList);

        // Request closing of the application when the image window is closed.
        canvas.setCanvasSize(200, 180);
        if (cameraType.equals("SIM")) canvas.dispose();

    }

    public void close() {
        canvas.dispose();
    }

    public enum ImageAnalysisAlgorithmTypes {
        TO_GRAY_SCALE,
        FACE_DETECT,
        CANNY_EDGE_DETECT
    }

    public void performAnalysis(boolean cameraWorking, DirectStreamView directStreamView) {

        if (this.directStreamView == null){
            this.directStreamView = directStreamView;
        }

        if (cameraWorking) {
            if (!(cameraType.equals("SIM"))) {
                if (this.directStreamView.isStreamIsPlaying() == true) {
                    inputImage = directStreamView.getJavaCVImageMat();

                    analysisResult = new AnalysisResult(inputImage.clone(),new HashMap<>());
                    processImage();

                    // Convert from OpenCV Mat to Java Buffered image for display

                    // Show image on window.
                    /*imwrite("in.jpg",directStreamView.getJavaCVImageMat());*/
                    //imwrite("out.jpg",analysisResult.getOutput());

                    canvas.showImage(converter.convert(analysisResult.getOutput()));
                    /*show(analysisResult.getOutput(),"result");*/

                }

            }
            else {

            }
        }



    }

    private void processImage() {

        for (ImageAnalysis imageAnalysis: sortedAlgorithmSet){

            //For each analysis build upon the output of the last analysis
            opencv_core.Mat inputImage = analysisResult.getOutput();

            ImageProcessor imageProcessor = imageAnalysis.getImageProcessor();
            AnalysisResult analysisResult = imageProcessor.performProcessing(inputImage, imageAnalysis.getAdditionalIntAttr());
            this.analysisResult.getAdditionalInformation().putAll(analysisResult.getAdditionalInformation());
            this.analysisResult.setOutput(analysisResult.getOutput());

        }

    }

    public AnalysisResult getAnalysisResult() {
        return analysisResult;
    }

    public void setAnalysisResult(AnalysisResult analysisResult) {
        this.analysisResult = analysisResult;
    }

}
