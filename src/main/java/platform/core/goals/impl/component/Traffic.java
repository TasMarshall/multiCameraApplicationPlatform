package platform.core.goals.impl.component;

import platform.core.goals.core.components.ObjectOfInterest;
import platform.core.imageAnalysis.impl.CannyEdgeDetector;
import platform.core.imageAnalysis.impl.ToGrayScale;

public class Traffic extends ObjectOfInterest {

    public Traffic(){
        //getAnalysisAlgorithmsSet().add(new Stitching(1));
        getAnalysisAlgorithmsSet().add(new CannyEdgeDetector(1, 7));
        getAnalysisAlgorithmsSet().add(new ToGrayScale(2));

    }

    @Override
    public void plan() {

    }

    @Override
    public void execute() {

    }
}
