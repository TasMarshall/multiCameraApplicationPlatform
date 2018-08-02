package platform.core.imageAnalysis.impl.outputObjects;

public class ObjLocBounds extends ObjectLocation {

    public ObjLocBounds(double x_centroid, double y_centroid, float x_max, float x_min, float y_max, float y_min, float imgWIDTH, float imgHEIGHT, double creationTime) {
        super(x_centroid, y_centroid, x_max, x_min, y_max, y_min, imgWIDTH, imgHEIGHT, creationTime);
    }

    public float getYMax(){
        return y_max;
    }

    public float getYMin(){
        return y_min;
    }

    public float getXMax(){
        return x_max;
    }

    public float getXMin(){
        return x_min;
    }

}
