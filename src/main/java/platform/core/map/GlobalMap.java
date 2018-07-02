package platform.core.map;

public class GlobalMap extends Map {

    private double swLong, swLat, neLong, neLat;

    public GlobalMap(double swLong, double swLat, double neLong, double neLat){
        super(CoordinateSys.OUTDOOR,rectangleMap(swLong, swLat, neLong, neLat)[0],rectangleMap(swLong, swLat, neLong, neLat)[1],MapType.GLOBAL);
        this.swLong = swLong;
        this.swLat = swLat;
        this.neLat = neLat;
        this.neLong = neLong;
    }

    public GlobalMap(){
        super(CoordinateSys.OUTDOOR,new double[]{},new double[]{},MapType.GLOBAL);
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
}
