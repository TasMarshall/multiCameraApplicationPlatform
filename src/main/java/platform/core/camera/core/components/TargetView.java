package platform.core.camera.core.components;

import com.sun.javafx.geom.Vec2d;
import com.sun.javafx.geom.Vec3d;

import java.util.UUID;

public class TargetView {

    String id = UUID.randomUUID().toString();

    private double targetLat;
    private double targetLon;

    private Vec3d localVect3D;

    private Vec3d globalVec3d;
    private Vec2d globalVec2d;

    public TargetView() {
    }

    public void setLocalVect3D(Vec3d localVect3D) {
        this.localVect3D = localVect3D;

        //todo convert fm local to global
        globalVec3d = localVect3D;

        this.globalVec2d.x = globalVec3d.x;
        this.globalVec2d.y = globalVec2d.y;
    }

    public Vec3d getGlobalVec3d() {
        return globalVec3d;
    }

    public Vec2d getGlobalVec2d() {
        return globalVec2d;
    }

    public Vec3d getLocalVect3D() {
        return localVect3D;
    }

    public void setGlobalVec3d(Vec3d globalVec3d) {
        this.globalVec3d = globalVec3d;
    }

    public void setGlobalVec2d(Vec2d globalVec2d) {
        this.globalVec2d = globalVec2d;
    }

    public double[] getTargetLatLon() {
        return new double[]{targetLat,targetLon};
    }

    public void setTargetLatLon(double targetLat, double targetLon) {
        this.targetLat = targetLat;
        this.targetLon = targetLon;
    }

}