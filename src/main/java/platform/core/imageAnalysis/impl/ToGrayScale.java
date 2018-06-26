package platform.core.imageAnalysis.impl;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import platform.core.imageAnalysis.AnalysisAlgorithm;

public class ToGrayScale extends AnalysisAlgorithm {

    public ToGrayScale(int precedence) {
        super(precedence);
    }

    @Override
    protected void processImage(Mat inputImage) {
        Imgproc.cvtColor(inputImage, getProcessedImage(), Imgproc.COLOR_BGR2GRAY);
    }
}
