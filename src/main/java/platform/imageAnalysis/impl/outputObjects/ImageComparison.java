package platform.imageAnalysis.impl.outputObjects;

import platform.imageAnalysis.ImageProcessor;

import java.io.Serializable;

public class ImageComparison implements Serializable{

    float similarity;
    int counter;
    boolean snapShotTaken;

    public ImageComparison(float similarity, Integer integer, boolean snapShotTaken){
        this.similarity = similarity;
        this.counter = integer;
        this.snapShotTaken = snapShotTaken;
    }

    public boolean isSnapShotTaken() {
        return snapShotTaken;
    }

    public void setSnapShotTaken(boolean snapShotTaken) {
        this.snapShotTaken = snapShotTaken;
    }

    public float getSimilarity() {
        return similarity;
    }

    public void setSimilarity(float similarity) {
        this.similarity = similarity;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }
}
