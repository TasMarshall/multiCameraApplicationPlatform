package platform.map;

import java.awt.*;

public class Map {

    public enum MapType {GLOBAL,LOCAL,NA}

    public enum CoordinateSys {INDOOR, OUTDOOR}

    public enum MapUnit {
        METRES,
    }

    double[] x;
    double[] y;

    double longDiff;
    double latDiff;

    double latMax = Double.NEGATIVE_INFINITY;
    double latMin = Double.POSITIVE_INFINITY;

    double longMax = Double.NEGATIVE_INFINITY;
    double longMin = Double.POSITIVE_INFINITY;

    Polygon polygon;

    CoordinateSys coordinateSys;
    MapType mapType;

    public Map(CoordinateSys coordinateSys, double[] x, double[] y, MapType mapType){

        this.coordinateSys = coordinateSys;
        this.x = x;
        this.y = y;
        this.mapType = mapType;

        init();

    }

    public Map(CoordinateSys coordinateSys, double[][] xy, MapType mapType){

        this.coordinateSys = coordinateSys;
        this.x = xy[1];
        this.y = xy[0];
        this.mapType = mapType;

        init();

    }

    public Map(){

    }

    public static double[][] rectangleMap(double swLong, double swLat, double neLong, double neLat){

        double[] lons = new double[]{swLong, swLong, neLong, neLong};
        double[] lats = new double[]{ swLat,neLat, neLat, swLat};

        return new double[][]{lons,lats};
    }

    public void calculateLatDiff(){

        for (int i = 0; i < x.length; i++) {
            if (x[i] > latMax){ latMax = x[i];}
            if (x[i] < latMin){ latMin = x[i];}
        }

        latDiff = latMax - latMin;
    }
    public void calculateLongDiff(){

        for (int i = 0; i < y.length; i++) {
            if (y[i] > longMax){ longMax = y[i];}
            if (y[i] < longMin){ longMin = y[i];}
        }

        longDiff = longMax - longMin;
    }

    protected void init(){

        int[] lats = new int[x.length];
        int[] lons = new int[y.length];

        for (int i = 0; i < y.length; i++){
            lons[i] = (int)Math.round(y[i]*10000000);
            lats[i] = (int)Math.round(x[i]*10000000);
        }

        polygon = new Polygon(lons,lats,lons.length);

        calculateLongDiff();
        calculateLatDiff();

    }



    private double[][] convertMetresToGlobal(LocalMap localMap) {
/*
        double[] lats = new double[localMap.getX().length];
        double[] lons = new double[localMap.getX().length];

        for (int i = 0; i < localMap.getX().length; i ++){
            lons[i] = distanceInLatLong(localMap.getX()[i],latAnchor,lonAnchor,90)[1] + lonAnchor;
            lats[i] = distanceInLatLong(localMap.getY()[i],latAnchor,lonAnchor,0)[0] + latAnchor;
        }

        return new double[][]{lons,lats};*/
        return null;
    }


    public MapType getMapType() {
        return mapType;
    }

    public void setMapType(MapType mapType) {
        this.mapType = mapType;
    }

    public double[] getX() {
        return x;
    }

    public void setX(double[] x) {
        this.x = x;
    }

    public double[] getY() {
        return y;
    }

    public void setY(double[] y) {
        this.y = y;
    }

    public double getLongDiff() {
        return longDiff;
    }

    public void setLongDiff(double longDiff) {
        this.longDiff = longDiff;
    }

    public double getLatDiff() {
        return latDiff;
    }

    public void setLatDiff(double latDiff) {
        this.latDiff = latDiff;
    }

    public double getLatMax() {
        return latMax;
    }

    public void setLatMax(double latMax) {
        this.latMax = latMax;
    }

    public double getLatMin() {
        return latMin;
    }

    public void setLatMin(double latMin) {
        this.latMin = latMin;
    }

    public double getLongMax() {
        return longMax;
    }

    public void setLongMax(double longMax) {
        this.longMax = longMax;
    }

    public double getLongMin() {
        return longMin;
    }

    public void setLongMin(double longMin) {
        this.longMin = longMin;
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }

    public CoordinateSys getCoordinateSys() {
        return coordinateSys;
    }

    public void setCoordinateSys(CoordinateSys coordinateSys) {
        this.coordinateSys = coordinateSys;
    }
}
