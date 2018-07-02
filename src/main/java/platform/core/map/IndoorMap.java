package platform.core.map;

import org.opencv.core.Mat;

import static platform.MapView.distanceInLatLong;

public class IndoorMap extends LocalMap{

    private double bearing;

    public IndoorMap(double width_in_metres, double height_in_metres, double nwLat, double nwLong, double bearing) {

        this.bearing = bearing;
        setCoordinateSys(CoordinateSys.INDOOR);
        setMapType(MapType.LOCAL);

        double[] widthInLatLong = distanceInLatLong(width_in_metres,nwLat,nwLong,bearing*Math.PI/180);
        double[] heightInLatLong = distanceInLatLong(height_in_metres,nwLat,nwLong,(bearing+90)*Math.PI/180);

        double[] x = new double[]{nwLong, nwLong+widthInLatLong[1],nwLong+widthInLatLong[1]+heightInLatLong[1],nwLong+heightInLatLong[1]};
        double[] y = new double[]{nwLat, nwLat+widthInLatLong[0],nwLat+widthInLatLong[0]+heightInLatLong[0],nwLat+heightInLatLong[0]};

        init();

    }

    public IndoorMap(double[] x1, double[] y1) {
        super(CoordinateSys.INDOOR,x1,y1);
    }
}
