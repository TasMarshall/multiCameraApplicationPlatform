package platform.map;

public class LocalMap extends Map{

    private double swLong, swLat, neLong, neLat;
    protected double[] x1,y1;

    public LocalMap(CoordinateSys coordinateSys, double[] lons, double[] lats){
        super(coordinateSys,lons,lats,MapType.LOCAL);
        this.x1=lons;
        this.y1=lats;

    }

    public LocalMap(CoordinateSys coordinateSys, double swLong, double swLat, double neLong, double neLat){
        super(coordinateSys,rectangleMap(swLong,  swLat,neLong,  neLat),MapType.LOCAL);
        this.swLong = swLong;
        this.swLat = swLat;
        this.neLat = neLat;
        this.neLong = neLong;
        this.x1=lons;
        this.y1=lats;
    }

    public LocalMap() {
    }

    public double[] getX1() {
        return x1;
    }

    public void setX1(double[] x1) {
        this.x1 = x1;
    }

    public double[] getY1() {
        return y1;
    }

    public void setY1(double[] y1) {
        this.y1 = y1;
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
