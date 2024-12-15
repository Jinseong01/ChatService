package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
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
            if (chatRoomId != null && handler != null) {
                AdditionalOptionsWindow optionsWindow = new AdditionalOptionsWindow(handler, chatRoomId);
                optionsWindow.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "핸들러 또는 채팅방 ID가 유효하지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
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
        System.out.println("[개발용] : 수신된 메시지: " + msg);

        if (msg.startsWith("[이모티콘] ")) {
            String[] tokens = msg.split(" ", 3);
            if (tokens.length == 3) {
                String senderLoginID = tokens[1]; // 보낸 사람의 ID
                String emojiFileName = tokens[2]; // 이모티콘 파일 이름
                System.out.println("[개발용] : 이모티콘 메시지로 인식됨: " + senderLoginID + ", 파일: " + emojiFileName);
                appendEmoji(senderLoginID, emojiFileName); // 두 개의 매개변수 전달
            } else {
                System.err.println("[개발용] : 이모티콘 메시지 형식이 잘못되었습니다.");
            }
        } else {
            // 일반 메시지 처리
            String[] tokens = msg.split(" ", 4);
            if (tokens.length == 4) {
                String senderLoginID = tokens[2];
                String message = tokens[3];
                appendMessage(senderLoginID, message);
            } else {
                System.err.println("일반 메시지 형식이 잘못되었습니다: " + msg);
            }
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

    private void appendToChat(String content, String alignment, String senderStyle) {
        SwingUtilities.invokeLater(() -> {
            htmlContent.append("<div style='text-align: ")
                    .append(alignment)
                    .append(";'><b>")
                    .append(senderStyle)
                    .append("</b>:<br>")
                    .append(content)
                    .append("</div>");
            chatArea.setText(htmlContent.toString());
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private void appendMessage(String senderLoginID, String message) {
        String alignment = senderLoginID.equals(loginID) ? "right" : "left";
        String senderStyle = senderLoginID.equals(loginID) ? "나" : senderLoginID;
        appendToChat(message, alignment, senderStyle);
    }

    protected void appendImage(String senderLoginID, String imagePath) {
        File imageFile = new File(imagePath);

        if (!imageFile.exists()) {
            System.err.println("이미지 파일이 존재하지 않습니다: " + imagePath);
            return;
        }

        try {
            String imgTag = "<img src='file://" + imageFile.getAbsolutePath().replace("\\", "/") + "' width='128' height='128'>";
            String alignment = senderLoginID.equals(loginID) ? "right" : "left";
            String senderStyle = senderLoginID.equals(loginID) ? "나" : senderLoginID;

            appendToChat(imgTag, alignment, senderStyle);

            System.out.println("이미지 추가 성공: " + imageFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("이미지 추가 중 오류 발생: " + e.getMessage());
        }
    }

    protected void appendEmoji(String senderLoginID, String emojiFileName) {
        System.out.println("[개발용] : appendEmoji 호출됨: senderLoginID=" + senderLoginID + ", emojiFileName=" + emojiFileName);
        String emojiPath = emojiFileName;
        File emojiFile = new File(emojiPath);

        System.out.println("[개발용] : 이모티콘 경로 확인: " + emojiPath);

        if (!emojiFile.exists()) {
            System.err.println("[개발용] : 이모티콘 파일이 존재하지 않습니다: " + emojiPath);
            return;
        }

        try {
            String imgTag = "<img src='file://" + emojiFile.getAbsolutePath().replace("\\", "/") + "' width='64' height='64'>";
            System.out.println("[개발용] : 생성된 img 태그: " + imgTag);

            String alignment = senderLoginID.equals(loginID) ? "right" : "left";
            String senderStyle = senderLoginID.equals(loginID) ? "나" : senderLoginID;

            appendToChat(imgTag, alignment, senderStyle);
            System.out.println("[개발용] : 이모티콘 추가 성공: " + emojiFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("[개발용] : 이모티콘 추가 중 오류 발생: " + e.getMessage());
        }
    }
}
