package platform.core.goals.core.components;

import platform.core.camera.core.Camera;
import platform.core.goals.components.Area;
import platform.core.imageAnalysis.AnalysisAlgorithm;
import platform.core.utilities.mapeLoop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class RegionOfInterest implements mapeLoop {

    //this region
    BehaviourOfInterest[] behavioursOfInterest;
    ObjectOfInterest[] objectsOfInterest;

    Area.CoordinateSys coordinateSys;

    Area area;

    List<Camera> camerasInRegion = new ArrayList<>();

    Set<AnalysisAlgorithm> analysisAlgorithmsSet = new HashSet<>();

    //a protective boolean so for global area and drawing functions so that they dont try to access an undefined area
    public boolean definedArea;

    public RegionOfInterest(Area area, Area.CoordinateSys inOrOut){
        this.area = area;
        this.coordinateSys = inOrOut;

        definedArea = true;

    }

    public RegionOfInterest(Area.CoordinateSys inOrOut){
        definedArea = false;

        this.coordinateSys = inOrOut;

    }

    public BehaviourOfInterest[] getBehavioursOfInterest() {
        return behavioursOfInterest;
    }

    public void setBehavioursOfInterest(BehaviourOfInterest[] behavioursOfInterest) {
        this.behavioursOfInterest = behavioursOfInterest;
    }

    public ObjectOfInterest[] getObjectsOfInterest() {
        return objectsOfInterest;
    }

    public void setObjectsOfInterest(ObjectOfInterest[] objectsOfInterest) {
        this.objectsOfInterest = objectsOfInterest;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Area.CoordinateSys getCoordinateSys() {
        return coordinateSys;
    }

    public void setCoordinateSys(Area.CoordinateSys coordinateSys) {
        this.coordinateSys = coordinateSys;
    }

    public List<Camera> getCamerasInRegion() {
        return camerasInRegion;
    }

    public void setCamerasInRegion(List<Camera> camerasInRegion) {
        this.camerasInRegion = camerasInRegion;
    }

    public boolean isDefinedArea() {
        return definedArea;
    }

    public void setDefinedArea(boolean definedArea) {
        this.definedArea = definedArea;
    }

    @Override
    public void monitor() {

    }

    @Override
    public void analyse() {

    }

    public Set<AnalysisAlgorithm> getAnalysisAlgorithmsSet() {
        return analysisAlgorithmsSet;
    }

    public void setAnalysisAlgorithmsSet(Set<AnalysisAlgorithm> analysisAlgorithmsSet) {
        this.analysisAlgorithmsSet = analysisAlgorithmsSet;
    }
}
