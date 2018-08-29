package platform.behaviors.components;

import org.bytedeco.javacpp.opencv_core;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacv.Java2DFrameUtils.toMat;

public class InMemoryBackground {

    Map<String, BufferedImage> bufferedImageMap = new HashMap<>();
    opencv_core.Mat neCornerMat;

    public void add(BufferedImage image, String snapID) {
        bufferedImageMap.put(snapID,image);
        if (snapID.contains("LocatingNECorner")){
            opencv_core.Mat mat = toMat(image);
            cvtColor(mat, mat, CV_BGR2GRAY);
            this.neCornerMat = mat;
        }
    }

    public opencv_core.Mat getNEImage(String s){
        return neCornerMat;
    }

}
