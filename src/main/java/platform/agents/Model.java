package platform.agents;

public interface Model {

    /////////////////////////////////////
    /* Distribution platform functions */
    /////////////////////////////////////

    ////    VIEW     ////

    /**This function cyclically sends a multi camera application state to a topic*/
    public void addViewCyclicCommunicationBehavior();

    ////    MODEL     ////

    /**This function adds a camera monitor.*/
    public Object[] getArgs();

    public void cancelInit();

    ////    CONTROLLER     ////

}

