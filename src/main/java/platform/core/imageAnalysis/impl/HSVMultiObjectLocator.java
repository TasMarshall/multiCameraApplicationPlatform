package platform.core.imageAnalysis.impl;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.opencv_core;
import org.opencv.core.Core;
import platform.core.imageAnalysis.AnalysisResult;
import platform.core.imageAnalysis.ImageProcessor;
import platform.core.imageAnalysis.impl.outputObjects.ObjLocBounds;
import platform.core.imageAnalysis.impl.outputObjects.ObjectLocation;
import platform.core.imageAnalysis.impl.outputObjects.ObjectLocations;
import platform.core.utilities.LoopTimer;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_core.CV_32SC4;
import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import static org.bytedeco.javacpp.opencv_core.minMaxLoc;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacv.Java2DFrameUtils.toMat;

public class HSVMultiObjectLocator extends ImageProcessor {

    LoopTimer loopTimer;

    @Override
    public void init() {
        loopTimer = new LoopTimer();
        loopTimer.start(0.5,1);
    }

    //used
    @Override
    public void defineKeys() {
        keys.add("objectLocations");
    }

    public AnalysisResult performProcessing(String cameraId, BufferedImage bufferedImage, Map<String, Object> additionalIntAttr){

        opencv_core.Mat input =  toMat(bufferedImage);
        double start = System.currentTimeMillis();

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

        int numberObjects;

        if (additionalIntAttr.get("NUMBER_OBJECTS") != null){
            numberObjects = (Integer)additionalIntAttr.get("NUMBER_OBJECTS");
        }
        else {
            numberObjects = 3;
        }

        opencv_core.inRange(hsv, new opencv_core.Mat(1, 1, CV_32SC4, new opencv_core.Scalar(H_MIN, S_MIN, V_MIN, 0)), new opencv_core.Mat(1, 1, CV_32SC4, new opencv_core.Scalar(H_MAX, S_MAX, V_MAX, 0)), mask);

        opencv_core.Mat out = new opencv_core.Mat(mask.rows(),mask.cols(),CV_8UC1);;

        //GaussianBlur(mask, out, new opencv_core.Size(5, 5),0);


        opencv_core.MatVector matVector = new opencv_core.MatVector();

        findContours(mask,matVector,RETR_EXTERNAL,CV_CHAIN_APPROX_SIMPLE);

        List<opencv_core.Mat> mats = new ArrayList<>();


        for( int i = 0; i< matVector.size(); i++ )
        {
            opencv_core.Mat mat = matVector.get(i);
            double length = arcLength(matVector.get(i),true);

            if(length>1000){

                if (mats.size() < numberObjects) {
                    mats.add(mat);
                }
                else {

                    for ( int j = 0; j< mats.size(); j++ ){
                        if (length > arcLength(mats.get(j),true)){
                            mats.add(j,mats.get(j));
                            break;
                        }
                    }
                    if (mats.size()>numberObjects){
                        mats = mats.subList(0,numberObjects-1);
                    }

                }

            }

            //double a = 0.1*arcLength(matVector.get(i),true);
            //approxPolyDP(out,matVector.get(i),a,true);

        }

        opencv_core.MatVector matVector1 = new opencv_core.MatVector();

        List<ObjectLocation> objectLocations = new ArrayList<>();

        for (opencv_core.Mat mat : mats){

            opencv_core.Moments m = moments(mat);
            double x = (m.m10()/m.m00());
            double y = (m.m01()/m.m00());

            opencv_core.Rect rect = boundingRect(mat);
            opencv_core.Point br = rect.br();
            opencv_core.Point tl = rect.tl();

            ObjLocBounds objLocBounds = new ObjLocBounds(x,y,br.x(),tl.x(),tl.y(),br.y(),input.cols(),input.rows(),System.currentTimeMillis());
            objectLocations.add(objLocBounds);

            matVector1.push_back(mat);

        }


        //drawContours(out,matVector1,-1, new opencv_core.Scalar(124,255,13, 0));

        Map<String,Serializable> outInfo = new HashMap<>();

        if (objectLocations.size() > 0 ) {
            ObjectLocations objectLocationsOut = new ObjectLocations(objectLocations);
            outInfo.put("objectLocations", objectLocationsOut);
        }

        AnalysisResult analysisResult = new AnalysisResult(mask,outInfo);

        double end = System.currentTimeMillis();

        //System.out.println(this.getClass().getSimpleName() + " execution for camera " + cameraId + " took " + (end - start)+ " miliseconds. Number of results: " + objectLocations.size());

        return analysisResult;

    }
}
