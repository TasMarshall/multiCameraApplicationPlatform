package platform.core.imageAnalysis.impl;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import platform.core.imageAnalysis.AnalysisAlgorithm;

public class CannyEdgeDetector extends AnalysisAlgorithm {

    int threshold;

    public CannyEdgeDetector(int precedence, int threshold) {
        super(precedence);
        this.threshold = threshold;
    }

    @Override
    protected void processImage(Mat inputImage) {

        // init
        Mat grayMat = new Mat();
        Mat detectedEdges = new Mat();

        ToGrayScale toGrayScale = new ToGrayScale(1);
        toGrayScale.performImageProcessing(inputImage);
        grayMat = toGrayScale.getProcessedImage().clone();

        Imgproc.blur(grayMat, detectedEdges, new Size(3, 3));

        Imgproc.Canny(detectedEdges, detectedEdges, this.threshold, this.threshold * 3, 3, false);

        Mat dest = new Mat();
        grayMat.copyTo(dest, detectedEdges);

        setProcessedImage(detectedEdges);

    }

}
