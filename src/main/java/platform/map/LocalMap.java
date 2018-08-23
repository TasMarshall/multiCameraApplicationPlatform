package platform.map;

public class LocalMap extends Map{

    private double swLong, swLat, neLong, neLat;
    protected double[] x1,y1;

    public LocalMap(CoordinateSys coordinateSys, double[] x, double[] y){
        super(coordinateSys,x,y,MapType.LOCAL);
        this.x1=x;
        this.y1=y;

    }

    public LocalMap(CoordinateSys coordinateSys, double swLong, double swLat, double neLong, double neLat){
        super(coordinateSys,rectangleMap(swLat,  swLat,neLong,  neLat),MapType.LOCAL);
        this.swLong = swLong;
        this.swLat = swLat;
        this.neLat = neLat;
        this.neLong = neLong;
        this.x1=x;
        this.y1=y;
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
}
