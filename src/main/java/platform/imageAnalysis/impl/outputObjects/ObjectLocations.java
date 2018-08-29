package platform.imageAnalysis.impl.outputObjects;

import java.io.Serializable;
import java.util.List;

public class ObjectLocations implements Serializable {

    List<ObjectLocation> objectLocationList;

    public ObjectLocations(List<ObjectLocation> objectLocationList) {
        this.objectLocationList = objectLocationList;
    }

    public List<ObjectLocation> getObjectLocationList() {
        return objectLocationList;
    }

    public void setObjectLocationList(List<ObjectLocation> objectLocationList) {
        this.objectLocationList = objectLocationList;
    }

    public void addObjectLocation(ObjectLocation o){
        objectLocationList.add(o);
    }

    public ObjectLocation getExtremesLocation(){

        float x_avg = 0;
        float y_avg = 0;

        float y_max = Float.POSITIVE_INFINITY;
        float y_min = Float.NEGATIVE_INFINITY;

        float x_max = Float.POSITIVE_INFINITY;
        float x_min = Float.NEGATIVE_INFINITY;

        for (ObjectLocation objectLocation : objectLocationList){

            x_avg += objectLocation.x_centroid;
            y_avg += objectLocation.y_centroid;

            if (objectLocation.y_max < y_max ){
                y_max = objectLocation.y_max;
            }
            if (objectLocation.y_min > y_min ){
                y_min = objectLocation.y_min;
            }
            if (objectLocation.x_max < x_max ){
                x_max = objectLocation.x_max;
            }
            if (objectLocation.x_min > x_min ){
                x_min = objectLocation.x_min;
            }

        }

        x_avg /= objectLocationList.size();
        y_avg /= objectLocationList.size();

        if (y_max == Float.POSITIVE_INFINITY){
            y_max = 0.15F;
        }
        if (y_min == Float.NEGATIVE_INFINITY){
            y_min = 0.15F;
        }
        if (x_max == Float.POSITIVE_INFINITY){
            x_max = 0.5F;
        }
        if (x_min == Float.NEGATIVE_INFINITY){
            x_min = 0.5F;
        }

        ObjectLocation objectLocation = new ObjLocBounds( x_avg,y_avg,x_max,x_min,y_max,y_min,1,1,objectLocationList.get(0).creationTime);

        return  objectLocation;
    }
}
