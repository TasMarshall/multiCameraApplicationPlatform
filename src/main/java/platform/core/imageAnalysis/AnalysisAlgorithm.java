package platform.core.imageAnalysis;

import org.opencv.core.Mat;
import platform.core.camera.core.Camera;
import platform.core.cameraMonitor.core.DirectStreamView;

import java.io.IOException;

public abstract class AnalysisAlgorithm {

    int precedence;

    Mat inputImage;
    Mat processedImage;

    public AnalysisAlgorithm(int precedence){
        this.precedence = precedence;
    }

    public void performImageProcessing(DirectStreamView directStreamView, Camera camera){

        try {
            if (directStreamView.isStreamIsPlaying() == true) {
                inputImage = directStreamView.getImageMat();
                processedImage = inputImage.clone();

                processImage(inputImage);
            }

        } catch (IOException e) {
            System.out.println("Analysis Algorithm failed to execute");
        }

    }

    //pr
    public void performImageProcessing(Mat mat){

        inputImage = mat;
        processedImage = mat.clone();
        processImage(inputImage);

    }

    protected abstract void processImage(Mat inputImage);

    public int getPrecedence() {
        return precedence;
    }

    public void setPrecedence(int precedence) {
        this.precedence = precedence;
    }

    public Mat getProcessedImage() {
        return processedImage;
    }

    public void setProcessedImage(Mat processedImage) {
        this.processedImage = processedImage;
    }
}
