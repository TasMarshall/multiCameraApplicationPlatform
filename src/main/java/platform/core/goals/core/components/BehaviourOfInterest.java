package platform.core.goals.core.components;

import platform.core.imageAnalysis.AnalysisAlgorithm;
import platform.core.utilities.mapeLoop;

import java.util.HashSet;
import java.util.Set;

public abstract class BehaviourOfInterest implements mapeLoop {

    Set<AnalysisAlgorithm> analysisAlgorithmsSet = new HashSet<>();

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
