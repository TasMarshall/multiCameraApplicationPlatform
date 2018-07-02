package platform.core.goals.core.components;

import platform.core.imageAnalysis.ImageAnalysis;

import java.util.HashSet;
import java.util.Set;

public class ObjectOfInterest {

    Set<ImageAnalysis> analysisAlgorithmsSet = new HashSet<>();

    public Set<ImageAnalysis> getAnalysisAlgorithmsSet() {
        return analysisAlgorithmsSet;
    }

    public void setAnalysisAlgorithmsSet(Set<ImageAnalysis> analysisAlgorithmsSet) {
        this.analysisAlgorithmsSet = analysisAlgorithmsSet;
    }
}
