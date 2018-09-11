package platform.agents;

public interface View {

    /////////////////////////////////////
    /* Distribution platform functions */
    /////////////////////////////////////

    ////    VIEW     ////

    /**This function sends a cyclic communication of multi camera application state from to the the controller and to subscribed users*/
    public void addSendViewToControllerAndSubscribedUsers();

    ////    MODEL     ////

    /**This function receives a cyclic communication of multi camera application state from the model agent*/
    public void addModelCyclicCommunicationReceiver();

    /** This function adds a listener to the DataFusion agent*/
    public void addDataFusionAgentListener();

}

