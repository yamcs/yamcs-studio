package org.yamcs.studio.ui.eventlog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.yamcs.utils.TimeEncoding;

public class YarchPastEventsDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    private PastEventParams params;
    JTextField startTextField;
    JTextField stopTextField;
    //JCheckBox sslCheckBox;

    static YarchPastEventsDialog dialog;

    YarchPastEventsDialog(JFrame parent) {
        super(parent, "Retrieve past events", true);

        params = new PastEventParams(TimeEncoding.currentInstant() - 1000L * 3600 * 24 * 30, TimeEncoding.currentInstant());

        JPanel inputPanel, buttonPanel;
        JLabel lab;
        JButton button;

        // input panel

        inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        getContentPane().add(inputPanel, BorderLayout.CENTER);

        lab = new JLabel("Start: ");
        lab.setHorizontalAlignment(SwingConstants.RIGHT);
        c.gridy = 1;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        inputPanel.add(lab, c);
        startTextField = new JTextField(TimeEncoding.toString(params.start));
        c.gridy = 1;
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        inputPanel.add(startTextField, c);

        lab = new JLabel("Stop: ");
        lab.setHorizontalAlignment(SwingConstants.RIGHT);
        c.gridy = 2;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        inputPanel.add(lab, c);
        stopTextField = new JTextField(TimeEncoding.toString(params.stop));
        c.gridy = 2;
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        inputPanel.add(stopTextField, c);

        // button panel

        buttonPanel = new JPanel();
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        button = new JButton("OK");
        button.setActionCommand("ok");
        button.addActionListener(this);
        getRootPane().setDefaultButton(button);
        buttonPanel.add(button);

        button = new JButton("Cancel");
        button.setActionCommand("cancel");
        button.addActionListener(this);
        buttonPanel.add(button);

        setMinimumSize(new Dimension(150, 100));
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("ok")) {
            try {
                params.start = TimeEncoding.parse(startTextField.getText());
                params.stop = TimeEncoding.parse(stopTextField.getText());
                params.ok = true;
                setVisible(false);
            } catch (NumberFormatException x) {
                // do not close the dialogue
            }
        } else if (e.getActionCommand().equals("cancel")) {
            params.ok = false;
            setVisible(false);
        }
    }

    public final static PastEventParams showDialog(JFrame parent) {
        if (dialog == null)
            dialog = new YarchPastEventsDialog(parent);
        dialog.setVisible(true);
        return dialog.params;
    }
}
