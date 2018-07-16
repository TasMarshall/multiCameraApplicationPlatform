package platform.core.imageAnalysis.impl.outputObjects;

import org.bytedeco.javacpp.opencv_core;
import platform.core.utilities.adaptation.core.components.ImageLocation;

import java.io.Serializable;

public class CircleLocationInImage implements Serializable, ImageLocation {

    int x= 0;
    int y= 0;
    int r= 0;

    int width= 0;
    int height= 0;

    float relX = 0;
    float relY= 0;

    public CircleLocationInImage(opencv_core.CvPoint curCenter, int radius, int width, int height) {

        this.x = curCenter.x();
        this.y = curCenter.y();
        this.r = radius;

        this.width=width;
        this.height=height;

        this.relX = (float)x/width;
        this.relY = (float)y/height;

    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float getRelX() {
        return relX;
    }

    public void setRelX(float relX) {
        this.relX = relX;
    }

    public float getRelY() {
        return relY;
    }

    public void setRelY(float relY) {
        this.relY = relY;
    }

    @Override
    public float getTargetRelX() {
        return relX;
    }

    @Override
    public float getTargetRelY() {
        return relY;
    }
}
