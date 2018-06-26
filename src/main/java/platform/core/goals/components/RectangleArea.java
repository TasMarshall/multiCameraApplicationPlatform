package platform.core.goals.components;

import platform.MCP_Application;

import java.awt.*;

public class RectangleArea extends Area {

    public RectangleArea(double swLong, double swLat, double neLong, double neLat){
        super (new double[]{swLong, swLong, neLong, neLong}, new double[]{swLat,neLat, neLat, swLat });
    }

    @Override
    public void render(Graphics g, double delta, MCP_Application application) {
        int x,y,width,height;

/*        double xInitRatio = getVerticiesX()[0] / application.getGlobalArea().getArea().xDiff;
        double yInitRatio = getVerticiesY()[0] / application.getGlobalArea().getArea().yDiff;

        x = (int)(xInitRatio*application.actualWidth);
        y = (int)(yInitRatio*application.actualHeight);

        double widthRatio = longDiff / application.getGlobalArea().getArea().xDiff;
        double heightRatio = latDiff / application.getGlobalArea().getArea().yDiff;

        width = (int)(widthRatio*application.actualWidth);
        height = (int)(heightRatio*application.actualHeight);

        g.setColor(Color.yellow);
        g.drawRect(application.txfm.plotX((int)(x)),application.txfm.plotY((int)(y + height)),(int)(width),(int)(height));*/
    }
}
