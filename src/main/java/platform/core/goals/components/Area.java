package platform.core.goals.components;

import platform.MCP_Application;
import platform.core.utilities.SimulatedComponent;

import java.awt.*;

public abstract class Area implements SimulatedComponent {

    public enum CoordinateSys {INDOOR, OUTDOOR}

    Polygon polygon;

    private double[] verticiesLong;
    private double[] verticiesLat;

    double longDiff;
    double latDiff;

    double latMax = Double.NEGATIVE_INFINITY;
    double latMin = Double.POSITIVE_INFINITY;

    double longMax = Double.NEGATIVE_INFINITY;
    double longMin = Double.POSITIVE_INFINITY;

    public Area(double[] inputLong, double[] inputLat) {

            this.verticiesLong = inputLong;
            this.verticiesLat = inputLat;

            int[] lons = new int[inputLong.length];
            int[] lats = new int[inputLong.length];

            for (int i = 0; i < inputLong.length; i++){
                lons[i] = (int)Math.round(inputLong[i]*10000000);
                lats[i] = (int)Math.round(inputLat[i]*10000000);
            }

            polygon = new Polygon(lons,lats,lons.length);

            calculateLongDiff();
            calculateLatDiff();

    }

    public void calculateLongDiff(){

        for (int i = 0; i < verticiesLong.length; i++) {
            if (verticiesLong[i] > longMax){ longMax = verticiesLong[i];}
            if (verticiesLong[i] < longMin){ longMin = verticiesLong[i];}
        }

        longDiff = longMax - longMin;
    }
    public void calculateLatDiff(){

        for (int i = 0; i < verticiesLat.length; i++) {
            if (verticiesLat[i] > latMax){ latMax = verticiesLat[i];}
            if (verticiesLat[i] < latMin){ latMin = verticiesLat[i];}
        }

        latDiff = latMax - latMin;
    }

    public void render(Graphics g, double delta, MCP_Application application) {

        //g.setColor(Color.white);

        //g.drawRect(application.txfm.plotX(application.minX),application.minY,application.actualWidth,application.actualHeight);

    }

    public double[] getVerticiesLong() {
        return verticiesLong;
    }

    public void setVerticiesLong(double[] verticiesLong) {
        this.verticiesLong = verticiesLong;
    }

    public double[] getVerticiesLat() {
        return verticiesLat;
    }

    public void setVerticiesLat(double[] verticiesLat) {
        this.verticiesLat = verticiesLat;
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
}
