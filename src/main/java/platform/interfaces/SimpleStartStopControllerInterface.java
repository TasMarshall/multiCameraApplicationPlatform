package platform.interfaces;

import jade.gui.GuiEvent;
import platform.MultiCameraCore_View;
import platform.jade.ControllerAgent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static platform.jade.ControllerAgent.*;

public class SimpleStartStopControllerInterface extends JFrame implements ActionListener {

    private boolean modelRunning = false;
    private boolean modelInitializing = false;

    private JButton Start, Stop, Exit;

    private JTextArea msg;

    private ControllerAgent myAgent;

    MultiCameraCore_View multiCameraCore_view;

    public SimpleStartStopControllerInterface(ControllerAgent agent) {
        this.myAgent = agent;

        setTitle("Controller Agent - " + myAgent.getLocalName());

        JPanel base = new JPanel();
        base.setBorder(new EmptyBorder(15, 15, 15, 15));
        base.setLayout(new GridLayout(1, 2, 5, 5));
        getContentPane().add(base);

        JPanel pane = new JPanel();

        base.add(pane);
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


        JPanel pane2 = new JPanel();

        pane2.setBorder(new EmptyBorder(0, 0, 0, 0));
        pane2.setLayout(new GridLayout(1, 1, 5, 5));


        JScrollPane jScrollPane = new JScrollPane(pane2);

        base.add(jScrollPane);

        pane2.add(msg = new JTextArea("Model State"));
        msg.setColumns(1);
        msg.setRows(20);
        msg.setLineWrap(true);
        msg.setWrapStyleWord(true);

        setSize(600, 400);
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
            if (!(modelRunning||modelInitializing)) {
                GuiEvent ge = new GuiEvent(this, START);
                myAgent.postGuiEvent(ge);
                modelInitializing = true;
            }
            else {

            }

        } else if (e.getSource() == Stop) {
            if (modelRunning && !modelInitializing) {
                GuiEvent ge = new GuiEvent(this, STOP);
                myAgent.postGuiEvent(ge);
            }
            else {

            }
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

    public void updateGUIDisplay(boolean modelRunning, MultiCameraCore_View multiCameraCore_view) {

        if(modelRunning) {
            this.multiCameraCore_view = multiCameraCore_view;
            msg.setText(multiCameraCore_view.viewToString());
            this.modelRunning = true;
            modelInitializing = false;
        }
        else {
            msg.setText("Model not running.");
            this.modelRunning = false;
            modelInitializing =false;
        }

    }



}