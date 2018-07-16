package platform.core.camera.core.components;

//Derived from Location @ com.google.api.gbase.client.

import platform.core.map.IndoorMap;
import platform.core.map.Map;

import java.util.UUID;

import static platform.MapView.distanceInLatLong;

public class CameraLocation {

    String id = UUID.randomUUID().toString();

    private boolean hasCoordinates;

    private float height2Ground;
    private double latitude;
    private double longitude;

    private Map.CoordinateSys coordinateSys;

    public CameraLocation(double lat, double lon, float height2Ground, Map.CoordinateSys coordinateSys) {
        this.setLatitude(lat);
        this.setLongitude(lon);
        this.height2Ground = height2Ground;
        this.coordinateSys = coordinateSys;
    }

    public CameraLocation(double xInMetresFromSWCorner, double yInMetresFromSWCorner, float height2Ground, IndoorMap indoorMap){

        double[] latLong = distanceInLatLong(Math.sqrt(xInMetresFromSWCorner*xInMetresFromSWCorner+yInMetresFromSWCorner*yInMetresFromSWCorner),indoorMap.getLatMin(),indoorMap.getLongMin(),Math.atan2(yInMetresFromSWCorner,xInMetresFromSWCorner));
        this.setLatitude(latLong[0]);
        this.setLongitude(latLong[1]);
        this.height2Ground = height2Ground;
        this.coordinateSys = Map.CoordinateSys.INDOOR;

    }

    public boolean hasCoordinates() {
        return this.hasCoordinates;
    }

    public double getLatitude() {
        this.assertHasCoordinates();
        return this.latitude;
    }

    public double getLongitude() {
        this.assertHasCoordinates();
        return this.longitude;
    }

    private void assertHasCoordinates() {
        if (!this.hasCoordinates) {
            throw new IllegalStateException("No coordinates have been defined. (Check with hasCoordinates() first)");
        }
    }

    public void setLongitude(double var1) {
        this.hasCoordinates = true;
        this.longitude = var1;
    }

    public void setLatitude(double var1) {
        this.hasCoordinates = true;
        this.latitude = var1;
    }

    public boolean equals(Object var1) {
        if (this == var1) {
            return true;
        } else if (!(var1 instanceof CameraLocation)) {
            return false;
        } else {
            CameraLocation var2 = (CameraLocation)var1;
            if (this.hasCoordinates != var2.hasCoordinates) {
                return false;
            } else {
                if (Double.compare(var2.latitude, this.latitude) != 0) {
                    return false;
                }
                if (Double.compare(var2.longitude, this.longitude) != 0) {
                    return false;
                }
                return true;
            }
        }
    }

    public float getHeight2Ground() {
        return height2Ground;
    }

    public void setHeight2Ground(float height2Ground) {
        this.height2Ground = height2Ground;
    }

    public Map.CoordinateSys getCoordinateSys() {
        return coordinateSys;
    }

    public void setCoordinateSys(Map.CoordinateSys coordinateSys) {
        this.coordinateSys = coordinateSys;
    }
}
