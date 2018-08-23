package platform.utilities;

import platform.MultiCameraCore;

import java.awt.*;

public interface SimulatedComponent {

    public abstract void render(Graphics g, double delta, final MultiCameraCore application);

}
