package platform.core.imageAnalysis.impl;

/*M///////////////////////////////////////////////////////////////////////////////////////
//
// IMPORTANT: READ BEFORE DOWNLOADING, COPYING, INSTALLING OR USING.
//
// By downloading, copying, installing or using the software you agree to this license.
// If you do not agree to this license, do not download, install,
// copy or use the software.
//
//
// License Agreement
// For Open Source Computer Vision Library
//
// Copyright (C) 2000-2008, Intel Corporation, all rights reserved.
// Copyright (C) 2009, Willow Garage Inc., all rights reserved.
// Third party copyrights are property of their respective owners.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
// * Redistribution's of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
//
// * Redistribution's in binary form must reproduce the above copyright notice,
// this list of conditions and the following disclaimer in the documentation
// and/or other materials provided with the distribution.
//
// * The name of the copyright holders may not be used to endorse or promote products
// derived from this software without specific prior written permission.
//
// This software is provided by the copyright holders and contributors "as is" and
// any express or implied warranties, including, but not limited to, the implied
// warranties of merchantability and fitness for a particular purpose are disclaimed.
// In no event shall the Intel Corporation or contributors be liable for any direct,
// indirect, incidental, special, exemplary, or consequential damages
// (including, but not limited to, procurement of substitute goods or services;
// loss of use, data, or profits; or business interruption) however caused
// and on any theory of liability, whether in contract, strict liability,
// or tort (including negligence or otherwise) arising in any way out of
// the use of this software, even if advised of the possibility of such damage.
//
//M*/

import platform.core.imageAnalysis.AnalysisResult;
import platform.core.imageAnalysis.ImageProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2HSV;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_stitching.Stitcher;

public class Stitch extends ImageProcessor{

    Map<String, Mat> panos = new HashMap<>();

    static boolean try_use_gpu = false;
    static MatVector imgs = new MatVector();

    static String result_name = "src//main//resources//testImages//result.jpg";

    /*public Mat stitch(List<Mat> mats){

        Mat pano = new Mat();

        for (int i = 0; i < mats.size() - 1; i++){
            if (i == 0){
                pano = stitch(mats.get(i),mats.get(i+1));
            }
            else {
                pano = stitch(pano, mats.get(i+1));
            }
        }
        return pano;
    }*/

    public Mat stitch(Mat mat1, Mat mat2){

       /* Mat mat12 = imread("src//main//resources//testImages//r1531575375128.PNG");
        Mat mat22 = imread("src//main//resources//testImages//r1531575377114.PNG");
        Mat mat23 = imread("src//main//resources//testImages//r1531575379040.PNG");


        List<Mat> mats = new ArrayList<>();
        mats.add(mat12);
        mats.add(mat22);
        mats.add(mat23);*/

        List<Mat> mats = new ArrayList<>();
        mats.add(mat1);
        mats.add(mat2);

        Stitching stitching = new Stitching();
        Mat pano = stitching.stitch(mats);

        imwrite("src//main//resources//testImages//result"+ ".PNG", pano);


        return pano;

    }

    public static void main(String[] args){

        Mat mat12 = imread("src//main//resources//testImages//result1531580083869.PNG");
        Mat mat22 = imread("src//main//resources//testImages//result1531580088105.PNG");

        Mat mat24 = imread("src//main//resources//testImages//result1531580089883.PNG");
        Mat mat25 = imread("src//main//resources//testImages//result1531580090476.PNG");

        Mat mat27 = imread("src//main//resources//testImages//result1531580092848.PNG");
        Mat mat28 = imread("src//main//resources//testImages//result1531580094043.PNG");
        Mat mat29 = imread("src//main//resources//testImages//result1531580095226.PNG");
        Mat mat30 = imread("src//main//resources//testImages//result1531580096408.PNG");

        List<Mat> mats = new ArrayList<>();
        mats.add(mat12);
        mats.add(mat22);

        mats.add(mat24);
        mats.add(mat25);

        mats.add(mat27);
        mats.add(mat28);
        mats.add(mat29);
        mats.add(mat30);

        Stitching stitching = new Stitching();
        Mat pano = stitching.stitch(mats);

        imwrite("src//main//resources//testImages//result.PNG", pano);

    }

    @Override
    public void init() {
        panos = new HashMap<>();
    }

    @Override
    public void defineKeys() {

    }

    @Override
    public AnalysisResult performProcessing(String cameraId, Mat inputImage, Map<String, Object> additionalIntAttr) {

        imwrite("src//main//resources//testImages//result"+ System.currentTimeMillis() + ".PNG", inputImage);

        Mat pano = panos.get(cameraId);

        if (pano != null){
            pano = stitch(pano,inputImage);
        }
        else {
            pano = new Mat();
            inputImage.copyTo(pano);
            panos.put(cameraId,pano);
        }

        AnalysisResult analysisResult = new AnalysisResult(pano,new HashMap<>());

        return  analysisResult;
    }
}
