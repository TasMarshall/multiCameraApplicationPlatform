package platform.map;

import java.awt.*;
import java.io.Serializable;

public class Map implements Serializable{

    public enum MapType {GLOBAL,LOCAL,NA}

    public enum CoordinateSys {INDOOR, OUTDOOR}

    /**A simple array of the x verticies of a map*/
    double[] lons;
    /**A simple array of the y verticies of a map*/
    double[] lats;

    /**The difference between the largest and smallest longitudinal value in map */
    double longDiff;
    /**The difference between the largest and smallest latitude value in map */
    double latDiff;

    /**The largest latitude value in map */
    double latMax = Double.NEGATIVE_INFINITY;
    /**The smallest latitude value in map */
    double latMin = Double.POSITIVE_INFINITY;

    /**The largest longitude value in map */
    double longMax = Double.NEGATIVE_INFINITY;
    /**The smallest longitude value in map */
    double longMin = Double.POSITIVE_INFINITY;

    /**A polygon version of the map points for use detecting overlap or other polygon related functionality*/
    Polygon polygon;


    /**The map can either be indoor or outdoor allowing for components such as goals to be indoor or outdoor goals such that cameras which are indoor / outdoor can only select those applicable*/
    CoordinateSys coordinateSys;

    /**Map type global can be used to allow map bounds to be dynamically bound at run time to the largest bounds, local means the map is positioned at a coordinate in the global map*/
    MapType mapType;

    public Map(CoordinateSys coordinateSys, double[] lons, double[] lats, MapType mapType){

        this.coordinateSys = coordinateSys;
        this.lons = lons;
        this.lats = lats;
        this.mapType = mapType;

        init();

    }

    public Map(CoordinateSys coordinateSys, double[][] xy, MapType mapType){

        this.coordinateSys = coordinateSys;
        this.lons = xy[0];
        this.lats = xy[1];
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

        for (int i = 0; i < lons.length; i++) {
            if (lons[i] > longMax){ longMax = lons[i];}
            if (lons[i] < longMin){ longMin = lons[i];}
        }

        longDiff = longMax - longMin;
    }

    public void calculateLongDiff(){

        for (int i = 0; i < lats.length; i++) {
            if (lats[i] > latMax){ latMax = lats[i];}
            if (lats[i] < latMin){ latMin = lats[i];}
        }

        latDiff = latMax - latMin;
    }

    protected void init(){

        int[] lats = new int[this.lats.length];
        int[] lons = new int[this.lons.length];

        for (int i = 0; i < lats.length; i++){
            lons[i] = (int)Math.round(lons[i]*10000000);
            lats[i] = (int)Math.round(lats[i]*10000000);
        }

        polygon = new Polygon(lons,lats,lons.length);

        calculateLongDiff();
        calculateLatDiff();

    }

    public MapType getMapType() {
        return mapType;
    }

    public void setMapType(MapType mapType) {
        this.mapType = mapType;
    }

    public double[] getLons() {
        return lons;
    }

    public void setLons(double[] lons) {
        this.lons = lons;
    }

    public double[] getLat() {
        return lats;
    }

    public void setY(double[] lats) {
        this.lats = lats;
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
