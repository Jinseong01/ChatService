package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ChatWindow extends JFrame {
    private String chatRoomId;
    private ClientHandler handler;
    private String loginID;

    private JTextPane chatArea = new JTextPane();
    private JScrollPane chatScrollPane = new JScrollPane(chatArea);
    private JTextField inputField = new JTextField(15);
    private JButton sendButton = new JButton("전송");
    private StringBuilder htmlContent = new StringBuilder("<html><body>");
    private JButton additionalOptionsButton = new JButton("+"); // 추가 옵션 버튼

    private boolean receivingHistory = false;
    private List<String> tempChatHistory = new ArrayList<>();

    public ChatWindow(String loginID, String chatRoomId, ClientHandler handler) {
        this.loginID = loginID;
        this.chatRoomId = chatRoomId;
        this.handler = handler;

        setTitle("채팅방 - " + chatRoomId);
        setSize(400, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        chatArea.setEditable(false);
        chatArea.setContentType("text/html");
        chatArea.setText("<html><body></body></html>");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());

        JPanel inputPanel = new JPanel(new BorderLayout(5,5));
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.add(additionalOptionsButton, BorderLayout.WEST); // + 버튼 추가

        // + 버튼 클릭 시 동작
        additionalOptionsButton.addActionListener(e -> {
            AdditionalOptionsWindow optionsWindow = new AdditionalOptionsWindow(handler); // handler 전달
            optionsWindow.setVisible(true);
        });

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        mainPanel.add(chatScrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);

        sendButton.addActionListener(e -> sendMessage());
        requestChatHistory();
    }

    private void requestChatHistory() {
        if (handler != null) {
            handler.sendMessage("/getchathistory " + chatRoomId);
        }
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) return;
        handler.sendMessage("/chat " + chatRoomId + " " + loginID + " " + message);
        inputField.setText("");
    }

    public void handleChatMessage(String msg) {
        String[] tokens = msg.split(" ", 4);
        if (tokens.length != 4) return;
        String senderLoginID = tokens[2];
        String message = tokens[3];

        if (receivingHistory) {
            tempChatHistory.add(msg);
        } else {
            appendMessage(senderLoginID, message);
        }
    }

    public void handleChatHistoryStart() {
        receivingHistory = true;
        tempChatHistory.clear();
    }

    public void handleChatHistoryEnd() {
        receivingHistory = false;
        for (String chat : tempChatHistory) {
            String[] tokens = chat.split(" ", 4);
            if (tokens.length == 4) {
                appendMessage(tokens[2], tokens[3]);
            }
        }
        tempChatHistory.clear();
    }

    private void appendMessage(String senderLoginID, String message) {
        SwingUtilities.invokeLater(() -> {
            if (senderLoginID.equals(loginID)) {
                htmlContent.append("<div style='text-align: right; color: green;'><b>나</b>: ")
                        .append(message)
                        .append("</div>");
            } else {
                htmlContent.append("<div style='text-align: left; color: blue;'><b>")
                        .append(senderLoginID)
                        .append("</b>: ")
                        .append(message)
                        .append("</div>");
            }
            chatArea.setText(htmlContent.toString());
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
}
