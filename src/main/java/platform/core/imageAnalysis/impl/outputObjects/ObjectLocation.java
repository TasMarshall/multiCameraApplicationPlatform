package platform.core.imageAnalysis.impl.outputObjects;

import platform.core.utilities.adaptation.core.components.ImageLocation;

import java.io.Serializable;

public class ObjectLocation implements ImageLocation, Serializable {

    float x_centroid;
    float y_centroid;

    float x_max;
    float x_min;

    float y_max;
    float y_min;

    double creationTime;

    public ObjectLocation(double x_centroid, double y_centroid, float x_max, float x_min, float y_max, float y_min, float imgWIDTH, float imgHEIGHT, double creationTime) {
        this.x_centroid = (float)x_centroid/imgWIDTH;
        this.y_centroid = (float)y_centroid/imgHEIGHT;
        this.x_max = x_max/imgWIDTH;
        this.x_min = x_min/imgWIDTH;
        this.y_max = y_max/imgHEIGHT;
        this.y_min = y_min/imgHEIGHT;
        this.creationTime = creationTime;
    }

    @Override
    public float getTargetRelX() {
        return x_centroid;
    }

    @Override
    public float getTargetRelY() {
        return y_centroid;
    }

    public float getX_centroid() {
        return x_centroid;
    }

    public void setX_centroid(float x_centroid) {
        this.x_centroid = x_centroid;
    }

    public float getY_centroid() {
        return y_centroid;
    }

    public void setY_centroid(float y_centroid) {
        this.y_centroid = y_centroid;
    }

    public float getX_max() {
        return x_max;
    }

    public void setX_max(float x_max) {
        this.x_max = x_max;
    }

    public float getX_min() {
        return x_min;
    }

    public void setX_min(float x_min) {
        this.x_min = x_min;
    }

    public float getY_max() {
        return y_max;
    }

    public void setY_max(float y_max) {
        this.y_max = y_max;
    }

    public float getY_min() {
        return y_min;
    }

    public void setY_min(float y_min) {
        this.y_min = y_min;
    }

    public double getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(double creationTime) {
        this.creationTime = creationTime;
    }
}
