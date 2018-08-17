package platform.interfaces;

import jade.gui.GuiEvent;
import platform.jade.ControllerAgent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static platform.jade.ControllerAgent.*;

public class SimpleStartStopControllerInterface extends JFrame implements ActionListener {

    private JButton Start, Stop, Exit;

    private JTextField msg;

    private ControllerAgent myAgent;

    public SimpleStartStopControllerInterface(ControllerAgent agent) {
        this.myAgent = agent;

        setTitle("Controller Agent - " + myAgent.getLocalName());

        JPanel base = new JPanel();
        base.setBorder(new EmptyBorder(15, 15, 15, 15));
        base.setLayout(new BorderLayout(10, 10));
        getContentPane().add(base);

        JPanel panel = new JPanel();
        base.add(panel, BorderLayout.WEST);

        panel.setLayout(new BorderLayout(0, 16));

        JPanel pane = new JPanel();

        panel.add(pane, BorderLayout.WEST);
        pane.setBorder(new EmptyBorder(0, 0, 0, 0));
        pane.setLayout(new GridLayout(3, 1, 5, 5));

        pane.add(Start = new JButton("Start"));
        Start.setToolTipText("Submit operation");
        Start.addActionListener(this);

        pane.add(Stop = new JButton("Stop"));
        Stop.setToolTipText("Submit operation");
        Stop.addActionListener(this);

        pane.add(Exit = new JButton("Quit"));
        Exit.setToolTipText("Stop agent and exit");
        Exit.addActionListener(this);

        setSize(190, 150);
        setResizable(false);
        Rectangle r = getGraphicsConfiguration().getBounds();
        setLocation(r.x + (r.width - getWidth()) / 2,
                r.y + (r.height - getHeight()) / 2);

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == Exit) {

            GuiEvent ge = new GuiEvent(this, QUIT);
            myAgent.postGuiEvent(ge);

            shutDown();

        } else if (e.getSource() == Start) {

            GuiEvent ge = new GuiEvent(this, START);
            myAgent.postGuiEvent(ge);

        } else if (e.getSource() == Stop) {

            GuiEvent ge = new GuiEvent(this, STOP);
            myAgent.postGuiEvent(ge);

        }


    }

    void shutDown() {
// -----------------  Control the closing of this gui

        int rep = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?",
                myAgent.getLocalName(),
                JOptionPane.YES_NO_OPTION);

        if (rep == JOptionPane.YES_OPTION) {
            GuiEvent ge = new GuiEvent(this, QUIT);
            myAgent.postGuiEvent(ge);
        }

    }

}