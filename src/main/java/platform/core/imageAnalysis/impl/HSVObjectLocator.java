package platform.core.imageAnalysis.impl;

import org.bytedeco.javacpp.opencv_core;
import platform.core.imageAnalysis.AnalysisResult;
import platform.core.imageAnalysis.ImageProcessor;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_core.CV_32SC4;
import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacv.Java2DFrameUtils.toMat;

public class HSVObjectLocator extends ImageProcessor {

    @Override
    public void init() {

    }

    @Override
    public void defineKeys() {

    }

    public AnalysisResult performProcessing(String cameraId, BufferedImage bufferedImage, Map<String, Object> additionalIntAttr){

        opencv_core.Mat input =  toMat(bufferedImage);
        opencv_core.Mat blur = new opencv_core.Mat();
        opencv_core.Mat hsv = new opencv_core.Mat();
        opencv_core.Mat mask = new opencv_core.Mat(input.rows(),input.cols(),CV_8UC1);

        // remove some
        blur(input, blur, new opencv_core.Size(3, 3));

        cvtColor(blur, hsv, CV_BGR2HSV);

        //hue is represented as degrees from 0 to 360 (in OpenCV, to fit into the an 8-bit unsigned integer format, they degrees are divided by two to get a number from 0 to 179
        int H_MIN, H_MAX, S_MIN,S_MAX,V_MIN,V_MAX;

        if (additionalIntAttr.get("H_MIN") != null){
            H_MIN = (Integer)additionalIntAttr.get("H_MIN");
        }
        else {
            H_MIN = 0;
        }

        if (additionalIntAttr.get("H_MAX") != null){
            H_MAX = (Integer)additionalIntAttr.get("H_MAX");
        }
        else {
            H_MAX = 180;
        }

        if (additionalIntAttr.get("S_MIN") != null){
            S_MIN = (Integer)additionalIntAttr.get("S_MIN");
        }
        else {
            S_MIN = 0;
        }
        if (additionalIntAttr.get("S_MAX") != null){
            S_MAX = (Integer)additionalIntAttr.get("S_MAX");
        }
        else {
            S_MAX = 255;
        }
        if (additionalIntAttr.get("V_MIN") != null){
            V_MIN = (Integer)additionalIntAttr.get("V_MIN");
        }
        else {
            V_MIN = 0;
        }
        if (additionalIntAttr.get("V_MAX") != null){
            V_MAX = (Integer)additionalIntAttr.get("V_MAX");
        }
        else {
            V_MAX = 50;
        }

        opencv_core.inRange(hsv, new opencv_core.Mat(1, 1, CV_32SC4, new opencv_core.Scalar(H_MIN, S_MIN, V_MIN, 0)), new opencv_core.Mat(1, 1, CV_32SC4, new opencv_core.Scalar(H_MAX, S_MAX, V_MAX, 0)), mask);

        opencv_core.Mat out = new opencv_core.Mat(mask.rows(),mask.cols(),CV_8UC1);;

        GaussianBlur(mask, out, new opencv_core.Size(5, 5),0);


        opencv_core.MatVector matVector = new opencv_core.MatVector();

        findContours(out,matVector,RETR_EXTERNAL,CV_CHAIN_APPROX_SIMPLE);

        opencv_core.MatVector finalMatVector = new opencv_core.MatVector();


        for( int i = 0; i< matVector.size(); i++ )
        {

            //matVector.get(i).
            System.out.println(arcLength(matVector.get(i),false));
            if(arcLength(matVector.get(i),true)>500){
                finalMatVector.push_back(matVector.get(i));

                opencv_core.Moments m = moments(matVector.get(i));
                double x = (m.m10()/m.m00());
                double y = (m.m01()/m.m00());

            }

            //double a = 0.1*arcLength(matVector.get(i),true);
            //approxPolyDP(out,matVector.get(i),a,true);

        }
        System.out.println(finalMatVector.size());



        drawContours(out,finalMatVector,-1, new opencv_core.Scalar(124,255,13, 0));


        Map<String,Serializable> outInfo = new HashMap<>();
        AnalysisResult analysisResult = new AnalysisResult(out,outInfo);

        return analysisResult;

    }
}
