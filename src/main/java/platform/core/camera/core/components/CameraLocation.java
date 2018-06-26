package platform.core.camera.core.components;

//Derived from Location @ com.google.api.gbase.client.

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class CameraLocation {

    @Id
    String id = UUID.randomUUID().toString();

    private boolean hasCoordinates;

    private float height2Ground;
    private double latitude;
    private double longitude;

    public CameraLocation(double lat, double lon, float height2Ground) {
        this.setLatitude(lat);
        this.setLongitude(lon);
        this.height2Ground = height2Ground;
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

}
