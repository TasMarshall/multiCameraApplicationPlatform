package platform.core.imageAnalysis;

import org.opencv.core.Mat;
import platform.core.camera.core.Camera;
import platform.core.camera.impl.SimulatedCamera;
import platform.core.cameraMonitor.core.DirectStreamView;
import platform.core.imageAnalysis.impl.CannyEdgeDetector;
import platform.core.imageAnalysis.impl.FaceDetectAndTrack;
import platform.core.imageAnalysis.impl.ToGrayScale;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ImageAnalyzer {

    Camera camera;
    DirectStreamView directStreamView;

    Mat inputImage;

    AnalysisResult analysisResult;

    Set<ImageAnalysis> sortedAlgorithmSet;

    public ImageAnalyzer (DirectStreamView directStreamView, Camera camera, List<ImageAnalysis> imageAnalysisList){

        this.camera = camera;
        this.directStreamView = directStreamView;

        sortedAlgorithmSet = new TreeSet<>((o1, o2) -> {
            int comparePrecedence = ((ImageAnalysis) o2).getPrecedence();
            return ((ImageAnalysis) o1).getPrecedence() - comparePrecedence;
        });

        sortedAlgorithmSet.addAll(imageAnalysisList);

    }

    public enum ImageAnalysisAlgorithmTypes {
        TO_GRAY_SCALE,
        FACE_DETECT,
        CANNY_EDGE_DETECT
    }

    public void performAnalysis() {

        if (directStreamView == null){
            directStreamView = camera.getCameraStreamManager().getDirectStreamView();
        }

        if (camera.isWorking()) {
            if (!(camera instanceof SimulatedCamera)) {
                try {
                    if (directStreamView.isStreamIsPlaying() == true) {
                        inputImage = directStreamView.getImageMat();

                        analysisResult.setOutput(inputImage.clone());
                        analysisResult.refresh();

                        processImage(inputImage);
                    }

                } catch (IOException e) {
                    System.out.println("Analysis failed to execute");
                }
            }
        }

    }

    private void processImage(Mat inputImage) {

        for (ImageAnalysis imageAnalysis: sortedAlgorithmSet){

            if (imageAnalysis.getImageAnalsysAlgorithmType() == ImageAnalysisAlgorithmTypes.TO_GRAY_SCALE){

                AnalysisResult analysisResult = ToGrayScale.performProcessing(inputImage, imageAnalysis.getAdditionalIntAttr());
                this.analysisResult.getAdditionalInformation().putAll(analysisResult.getAdditionalInformation());
                this.analysisResult.setOutput(analysisResult.getOutput());

            }

            else if (imageAnalysis.getImageAnalsysAlgorithmType() == ImageAnalysisAlgorithmTypes.CANNY_EDGE_DETECT){

                if (imageAnalysis.getAdditionalIntAttr().get("threshold") == null){
                    System.out.println("Must set other attribute 'threshold' to perform canny edge detection");
                }
                else {
                    AnalysisResult analysisResult = CannyEdgeDetector.performProcessing(inputImage, imageAnalysis.getAdditionalIntAttr());
                    this.analysisResult.getAdditionalInformation().putAll(analysisResult.getAdditionalInformation());
                    this.analysisResult.setOutput(analysisResult.getOutput());
                }

            }

            else if (imageAnalysis.getImageAnalsysAlgorithmType() == ImageAnalysisAlgorithmTypes.FACE_DETECT){

                AnalysisResult analysisResult = FaceDetectAndTrack.performProcessing(inputImage, imageAnalysis.getAdditionalIntAttr());
                this.analysisResult.getAdditionalInformation().putAll(analysisResult.getAdditionalInformation());
                this.analysisResult.setOutput(analysisResult.getOutput());

            }



        }

    }

}
