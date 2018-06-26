package platform.core.imageAnalysis;

import platform.MCP_Application;
import platform.core.camera.core.Camera;
import platform.core.camera.impl.SimulatedCamera;
import platform.core.cameraMonitor.core.DirectStreamView;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.utilities.mapeLoop;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class AnalysisManager implements mapeLoop {

    DirectStreamView directStreamView;
    Camera camera;

    Set<MultiCameraGoal> multiCameraGoalSet = new HashSet<>();
    Set<AnalysisAlgorithm> analysisAlgorithmsSet = new HashSet<>();
    Set<AnalysisAlgorithm> sortedAlgorithmSet = new TreeSet<>();


    public AnalysisManager(MCP_Application mcp_application, Camera camera){
        this.directStreamView = camera.getCameraStreamManager().getDirectStreamView();
        this.camera = camera;

    }

    @Override
    public void monitor() {

        multiCameraGoalSet = new HashSet<>();
        analysisAlgorithmsSet = new HashSet<>();

        for (MultiCameraGoal multiCameraGoal : camera.getMultiCameraGoalList()){
            if (multiCameraGoal != null) {
                multiCameraGoalSet.add(multiCameraGoal);
            }
        }

        sortedAlgorithmSet = new TreeSet<>((o1, o2) -> {
            int comparePrecedence = ((AnalysisAlgorithm) o2).getPrecedence();
            return ((AnalysisAlgorithm) o1).getPrecedence() - comparePrecedence;
        });

        sortedAlgorithmSet.addAll(analysisAlgorithmsSet);

    }

    @Override
    public void analyse() {

        if (directStreamView == null){
            directStreamView = camera.getCameraStreamManager().getDirectStreamView();
        }

        if (camera.isWorking()) {
            if (!(camera instanceof SimulatedCamera)) {
                for (AnalysisAlgorithm analysisAlgorithm : sortedAlgorithmSet) {
                    analysisAlgorithm.performImageProcessing(directStreamView, camera);
                }
            }
        }

    }

    @Override
    public void plan() {

    }

    @Override
    public void execute() {

    }

    public Set<AnalysisAlgorithm> getSortedAlgorithmSet() {
        return sortedAlgorithmSet;
    }

    public void setSortedAlgorithmSet(Set<AnalysisAlgorithm> sortedAlgorithmSet) {
        this.sortedAlgorithmSet = sortedAlgorithmSet;
    }

    public Set<AnalysisAlgorithm> getAnalysisAlgorithmsSet() {
        return analysisAlgorithmsSet;
    }
}
