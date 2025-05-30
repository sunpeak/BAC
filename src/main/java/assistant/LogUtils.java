package assistant;

import javax.swing.*;

public class LogUtils {

    private static JTextArea log;

    public static void setLog(JTextArea log) {
        LogUtils.log = log;
    }

    public static void log(String message) {
        log.append(message + "\n"); // 将消息追加到 JTextArea
        log.setCaretPosition(log.getDocument().getLength()); // 自动滚动到底部
    }
}
