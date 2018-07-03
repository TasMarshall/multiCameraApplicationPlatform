package platform.core.imageAnalysis.impl;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import platform.core.imageAnalysis.AnalysisResult;

import java.util.HashMap;
import java.util.Map;

public class FaceDetectAndTrack {

    // face cascade classifier
    private static CascadeClassifier faceCascade;
    static int absoluteFaceSize = 0;
    MatOfRect faces;

    static {
        faceCascade = new CascadeClassifier();
        // load the classifier(s)           /*C:\Users\tjtma\Downloads\thesis\multi-cam-platform\*/
        boolean success = faceCascade.load("src\\project\\platform\\core\\imageAnalysis\\resources\\haarcascade_frontalface_alt.xml");
        //this.faceCascade.load("resources/lbpcascades/lbpcascade_frontalface.xml");

    }

/*    public FaceDetectAndTrack(int precedence) {
        super(precedence);

    }

    @Override
    protected void processImage(Mat inputImage) {

        faces = new MatOfRect();

        ToGrayScale toGrayScale = new ToGrayScale(1);
        toGrayScale.performImageProcessing(inputImage);
        Mat grayMat = toGrayScale.getProcessedImage();

        Imgproc.equalizeHist(grayMat, grayMat);

        if (absoluteFaceSize == 0)
        {
            int height = grayMat.rows();
            if (Math.round(height * 0.2f) > 0)
            {
                absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        faceCascade.detectMultiScale(grayMat, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
            Imgproc.rectangle(getProcessedImage(), facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);



    }*/

    public MatOfRect getFaces() {
        return faces;
    }

    public void setFaces(MatOfRect faces) {
        this.faces = faces;
    }

/*
    public static AnalysisResult performProcessing(Mat inputImage, Map<String, Integer> additionalIntAttr) {

        Mat output = inputImage.clone();

        MatOfRect faces;
        faces = new MatOfRect();

        Mat grayMat = ToGrayScale.performProcessing(inputImage, null).getOutput();
        Imgproc.equalizeHist(grayMat, grayMat);

        if (absoluteFaceSize == 0)
        {
            int height = grayMat.rows();
            if (Math.round(height * 0.2f) > 0)
            {
                absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        faceCascade.detectMultiScale(grayMat, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(absoluteFaceSize, absoluteFaceSize), new Size());

      */
/*  Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
            Imgproc.rectangle(output, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
        *//*

        Map<String,Object> outInfo = new HashMap<>();
        outInfo.put("facesArray", faces);

        AnalysisResult analysisResult = new AnalysisResult(output,outInfo);
        return analysisResult;

    }
*/
}
