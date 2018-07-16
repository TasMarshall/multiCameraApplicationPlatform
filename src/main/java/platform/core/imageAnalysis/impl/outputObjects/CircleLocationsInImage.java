package platform.core.imageAnalysis.impl.outputObjects;

import org.bytedeco.javacpp.opencv_core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CircleLocationsInImage implements Serializable {

    List<CircleLocationInImage> circleLocationInImageList = new ArrayList<>();

    public CircleLocationInImage getBiggestCircle() {
        int biggestR = 0;
        CircleLocationInImage biggestCircle = null;
        for (CircleLocationInImage c : circleLocationInImageList){
            if (c.getR() > biggestR){
                biggestR =  c.getR();
                biggestCircle = c;
            }
        }
        return biggestCircle;
    }

    public void addCircle(CircleLocationInImage circleLocationInImage){
        circleLocationInImageList.add(circleLocationInImage);
    }

    public List<CircleLocationInImage> getCircleLocationInImageList() {
        return circleLocationInImageList;
    }

    public void setCircleLocationInImageList(List<CircleLocationInImage> circleLocationInImageList) {
        this.circleLocationInImageList = circleLocationInImageList;
    }
}
