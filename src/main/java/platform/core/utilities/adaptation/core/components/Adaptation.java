package platform.core.utilities.adaptation.core.components;

public interface Adaptation {

    public boolean isRequired(Adaptor currentAdaptor, Adaptor oldAdaptor);

    public void adapt(Adaptor adaptor, AdaptiveData adaptiveData);
}
