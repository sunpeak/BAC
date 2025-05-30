package extension;

import assistant.LogUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class BAC {
    private JTextField auth;
    private JButton start;
    public JPanel BAC_JPanel;
    private JTextField domain;
    private JTextArea log;

    public BAC() {
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MyExtension.Domain = domain.getText();
                Arrays.asList("=", ":").forEach(split -> {
                    int splitIndex = auth.getText().indexOf(split);
                    if (splitIndex > -1) {
                        MyExtension.AuthKey = auth.getText().substring(0, splitIndex).trim();
                        MyExtension.AuthValue = auth.getText().substring(splitIndex + 1).trim();
                    }
                });
                if (start.getText().contains("开启")) {
                    MyExtension.Start = true;
                    LogUtils.setLog(log);
                    start.setText("关闭检测");
                } else {
                    MyExtension.Start = false;
                    start.setText("开启检测");
                }
            }
        });
    }


}
