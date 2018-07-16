package platform.core.imageAnalysis.impl.outputObjects;

import platform.core.imageAnalysis.ImageProcessor;

import java.io.Serializable;

public class ImageComparison implements Serializable{

    float similarity;
    int counter;

    public ImageComparison(float similarity, Integer integer){
        this.similarity = similarity;
        this.counter = integer;
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
