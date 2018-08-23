package platform.imageAnalysis.impl;

import org.bytedeco.javacpp.opencv_core;
import platform.imageAnalysis.AnalysisResult;
import platform.imageAnalysis.ImageProcessor;
import platform.imageAnalysis.impl.outputObjects.CircleLocationInImage;
import platform.imageAnalysis.impl.outputObjects.CircleLocationsInImage;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacv.Java2DFrameUtils.toMat;

public class HoughCircles extends ImageProcessor {

    @Override
    public AnalysisResult performProcessing(String cameraId, BufferedImage inputImage, Map<String, Object> additionalIntAttr) {

        opencv_core.Mat input =  toMat(inputImage);

        int inverseRatio, minDist, cannyThres, detThres, minR, maxR;

        if (additionalIntAttr.get("inverseRatio") != null){
            inverseRatio = (Integer)additionalIntAttr.get("inverseRatio");
        }
        else {
            inverseRatio = 1;
        }

        if (additionalIntAttr.get("minR") != null){
            minR = (Integer)additionalIntAttr.get("minR");
        }
        else {
            minR = input.size().width()/8;
        }

        if (additionalIntAttr.get("maxR") != null){
            maxR =(Integer) additionalIntAttr.get("maxR");
        }
        else {
            maxR = minR * 2;
        }

        if (additionalIntAttr.get("minDist") != null){
            minDist = (Integer)additionalIntAttr.get("minDist");
        }
        else {
            //default minimum distance between circles to the radius of the circle + 1. i.e. no overlap
            minDist = minR/2;
        }

        if (additionalIntAttr.get("cannyThres") != null){
            cannyThres =(Integer) additionalIntAttr.get("cannyThres");
        }
        else {
            cannyThres = 60;
        }

        if (additionalIntAttr.get("detThres") != null){
            detThres = (Integer)additionalIntAttr.get("detThres");
        }
        else {
            detThres = 35;
        }

        opencv_core.Mat in = new opencv_core.Mat();

        cvtColor(input, in, CV_BGR2GRAY);

        opencv_core.IplImage gray = new opencv_core.IplImage(in);

        cvSmooth(gray, gray, CV_GAUSSIAN, 3, 0, 0, 0);

        CvMemStorage mem = CvMemStorage.create();

        CvSeq circles = cvHoughCircles(
                gray, //Input image
                mem, //Memory Storage
                CV_HOUGH_GRADIENT, //Detection method
                inverseRatio, //Inverse ratio
                minDist, //Minimum distance between the centers of the detected circles
                cannyThres, //Higher threshold for canny edge detector
                detThres, //Threshold at the center detection stage
                minR, //min radius
                maxR //max radius
        );

        IplImage temp = new IplImage(gray);

        Map<String,Serializable> outInfo = new HashMap<>();
        if (circles!=null && circles.total() > 0){

            CircleLocationsInImage circleLocationsInImage = new CircleLocationsInImage();

            for (int i = 0; i < circles.total(); i++) {

                CvPoint3D32f circle = new CvPoint3D32f(cvGetSeqElem(circles, i));
                CvPoint center = cvPointFrom32f(new CvPoint2D32f(circle.x(), circle.y()));
                int radius = Math.round(circle.z());
                CircleLocationInImage circleLocationInImage = new CircleLocationInImage(center,radius,gray.width(),gray.height());
                circleLocationsInImage.addCircle(circleLocationInImage);
                cvCircle(temp, center, radius, CvScalar.GREEN, 1, CV_AA, 0);

            }

            outInfo.put("circles",circleLocationsInImage);

        }

        opencv_core.Mat out = cvarrToMat(temp);
        AnalysisResult analysisResult = new AnalysisResult(out,outInfo);

        return analysisResult;

    }

    @Override
    public void init() {

    }

}
