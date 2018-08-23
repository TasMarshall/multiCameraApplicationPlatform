package platform.map;

public class SimpleMapConfig {

    private double swLong, swLat, neLong, neLat;
    private Map.CoordinateSys coordinateSys;

    public SimpleMapConfig(double swLong, double swLat, double neLong, double neLat, Map.CoordinateSys coordinateSys) {
        this.swLong = swLong;
        this.swLat = swLat;
        this.neLong = neLong;
        this.neLat = neLat;
        this.coordinateSys = coordinateSys;
    }

    public double getSwLong() {
        return swLong;
    }

    public void setSwLong(double swLong) {
        this.swLong = swLong;
    }

    public double getSwLat() {
        return swLat;
    }

    public void setSwLat(double swLat) {
        this.swLat = swLat;
    }

    public double getNeLong() {
        return neLong;
    }

    public void setNeLong(double neLong) {
        this.neLong = neLong;
    }

    public double getNeLat() {
        return neLat;
    }

    public void setNeLat(double neLat) {
        this.neLat = neLat;
    }

    public Map.CoordinateSys getCoordinateSys() {
        return coordinateSys;
    }

    public void setCoordinateSys(Map.CoordinateSys coordinateSys) {
        this.coordinateSys = coordinateSys;
    }
}
