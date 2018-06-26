package platform.core.utilities;

import platform.MCP_Application;

import java.awt.*;

public interface SimulatedComponent {

    public abstract void render(Graphics g, double delta, final MCP_Application application);

}
