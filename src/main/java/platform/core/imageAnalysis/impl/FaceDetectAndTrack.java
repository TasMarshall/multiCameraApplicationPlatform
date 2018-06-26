package platform.core.imageAnalysis.impl;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import platform.core.imageAnalysis.AnalysisAlgorithm;

public class FaceDetectAndTrack  extends AnalysisAlgorithm {

    // face cascade classifier
    private CascadeClassifier faceCascade;
    int absoluteFaceSize = 0;
    MatOfRect faces;

    public FaceDetectAndTrack(int precedence) {
        super(precedence);

    }

    @Override
    protected void processImage(Mat inputImage) {

        this.faceCascade = new CascadeClassifier();
        // load the classifier(s)
        boolean success = this.faceCascade.load("C:\\Users\\tjtma\\Downloads\\thesis\\multi-cam-platform\\src\\project\\platform\\core\\imageAnalysis\\resources\\haarcascade_frontalface_alt.xml");
        //this.faceCascade.load("resources/lbpcascades/lbpcascade_frontalface.xml");

        faces = new MatOfRect();

        ToGrayScale toGrayScale = new ToGrayScale(1);
        toGrayScale.performImageProcessing(inputImage);
        Mat grayMat = toGrayScale.getProcessedImage();

        Imgproc.equalizeHist(grayMat, grayMat);

        if (this.absoluteFaceSize == 0)
        {
            int height = grayMat.rows();
            if (Math.round(height * 0.2f) > 0)
            {
                this.absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        this.faceCascade.detectMultiScale(grayMat, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
            Imgproc.rectangle(getProcessedImage(), facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);



    }

    public MatOfRect getFaces() {
        return faces;
    }

    public void setFaces(MatOfRect faces) {
        this.faces = faces;
    }
}
