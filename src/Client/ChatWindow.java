package Client;

import Model.UserSummary;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatWindow extends JFrame {
    private String chatRoomId;
    private String chatRoomName;
    private ClientHandler handler;
    private String loginID;

    private JTextPane chatArea = new JTextPane();
    private JScrollPane chatScrollPane = new JScrollPane(chatArea);
    private JTextField inputField = new JTextField(15);
    private JButton sendButton = new JButton("전송");
    private JButton additionalOptionsButton = new JButton("+"); // 추가 옵션 버튼

    private boolean receivingHistory = false;
    private List<String> tempChatHistory = new ArrayList<>();

    // 기본 이미지 Base64 문자열 (32x32 PNG 예시)
    private static final String DEFAULT_BASE64_IMAGE = "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAABKElEQVRYR+2XMQrCQBBFf3ZQ3ZCQVUgVUAVSgVWgJSgFJQgVQAVUgV6gJ0gJVKQFwGfrhcTHznv7q3Vra7r/X3FhFkBmCNAGYDWIBRwF3gG8A9wApAU8A+qAg4AtAMOAeVZxDJmBnK6AK8BtAAcQfqYBN0D5oAf4b1Fv7OuwAQy4FSgBbgE3oEeI7XGYI8oPczAm2JmKQd4BtIh2gHeqAewCOQFvALdgXKA3sCtwO/B/4GoAVUAmcAj4AjYBEwBbgGsF7qTjgHoBvwAfQJtwKZADGMM7kXvThkAX0HpwGwBTwGjAHzAbsAMeA/gAXAKXAl0BkgLWgAAAABJRU5ErkJggg=="; // 32x32 PNG 이미지의 Base64 문자열 예시

    // 프로필 이미지 캐싱
    private Map<String, ImageIcon> profileImageCache = new HashMap<>();

    public ChatWindow(String loginID, String chatRoomId, String chatRoomName, ClientHandler handler) {
        this.loginID = loginID;
        this.chatRoomId = chatRoomId;
        this.chatRoomName = chatRoomName;
        this.handler = handler;

        setTitle(chatRoomName);
        setSize(400, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        chatArea.setEditable(false);
        chatArea.setContentType("text/plain");
        chatArea.setText("");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
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
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
        // 시간 포함하여 전송하는 메서드 호출
        handler.sendChatMessage(chatRoomId, message);
        inputField.setText("");
    }

    public void handleChatMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("[개발용] : 수신된 메시지: " + msg);

            if (msg.startsWith("[이모티콘] ")) {
                String[] tokens = msg.split(" ", 4);
                if (tokens.length == 4) {
                    String senderLoginID = tokens[1]; // 보낸 사람의 ID
                    String time = tokens[2];
                    String emojiFileName = tokens[3]; // 이모티콘 파일 이름
                    appendEmoji(senderLoginID, time, emojiFileName); // 프로필 이미지 포함
                } else {
                    System.err.println("[개발용] : 이모티콘 메시지 형식이 잘못되었습니다.");
                }
            } else {
                // 일반 메시지 처리
                String[] tokens = msg.split(" ", 5);
                if (tokens.length == 5) {
                    String senderLoginID = tokens[2];
                    String time = tokens[3];
                    String message = tokens[4];
                    appendMessage(senderLoginID, time, message); // 프로필 이미지 포함
                } else {
                    System.err.println("일반 메시지 형식이 잘못되었습니다: " + msg);
                }
            }
        });
    }

    public void handleChatHistoryStart() {
        receivingHistory = true;
        tempChatHistory.clear();
    }

    public void handleChatHistoryEnd() {
        receivingHistory = false;
        for (String chat : tempChatHistory) {
            String[] tokens = chat.split(" ", 5);
            if (tokens.length == 5) {
                appendMessage(tokens[2], tokens[3], tokens[4]);
            }
        }
        tempChatHistory.clear();
    }

    private String[] getSenderProfile(String senderLoginID) {
        String base64Image;
        String senderStyle;

        // 채팅방 멤버 목록에서 sender 찾기
        Set<UserSummary> members = handler.getChatRoomMembers(chatRoomId);
        UserSummary sender = null;

        if (members != null) {
            for (UserSummary member : members) {
                if (member.getLoginID().equals(senderLoginID)) {
                    sender = member;
                    break;
                }
            }
        }

        // 디버깅용 로그
        if (sender == null) {
            System.out.println("[디버깅] sender가 null입니다. senderLoginID: " + senderLoginID);
        } else {
            System.out.println("[디버깅] sender가 존재합니다. UserName: " + sender.getUserName() +
                    ", ProfileImage: " + (sender.getProfileImage() == null ? "null" : "존재"));
        }

        if (sender != null && sender.getProfileImage() != null && !sender.getProfileImage().isEmpty()) {
            // sender의 프로필 이미지 사용
            base64Image = sender.getProfileImage();
            senderStyle = sender.getUserName();
        } else {
            if (senderLoginID.equals(loginID)) {
                // 발신자가 본인인 경우 자신의 프로필 이미지 사용
                base64Image = handler.getLoginUser().getProfileImage();
                senderStyle = "나";
            } else {
                // 발신자가 친구가 아닌 경우 기본 이미지 사용
                base64Image = DEFAULT_BASE64_IMAGE;
                senderStyle = senderLoginID;
            }
        }

        // 디버깅용 로그
        System.out.println("[디버깅] 사용되는 Base64 이미지 문자열: " + base64Image);

        return new String[]{base64Image, senderStyle};
    }

    private ImageIcon getProfileImage(String loginID, String base64Image) {
        if (profileImageCache.containsKey(loginID)) {
            return profileImageCache.get(loginID);
        } else {
            ImageIcon icon = base64ToImageIcon(base64Image);
            profileImageCache.put(loginID, icon);
            return icon;
        }
    }

    private void appendMessage(String senderLoginID, String time, String message) {
        String alignment = senderLoginID.equals(loginID) ? "right" : "left";

        // 발신자의 프로필 이미지와 스타일 가져오기
        String[] profile = getSenderProfile(senderLoginID);
        String base64Image = profile[0];
        String senderStyle = profile[1];

        // StyledDocument에 이미지와 텍스트 삽입
        StyledDocument doc = chatArea.getStyledDocument();

        try {
            // 시간 정보 삽입
            insertTime(doc, time, alignment);

            // 프로필 이미지 삽입과 메시지 텍스트 삽입
            if (alignment.equals("left")) {
                // 발신자가 다른 사용자일 경우: 이미지 먼저, 메시지 나중에
                insertProfileImage(doc, senderLoginID, base64Image, alignment);
                insertMessageText(doc, senderStyle + ": " + message + " ", alignment);
            } else {
                // 발신자가 본인인 경우: 메시지 먼저, 이미지 나중에
                insertMessageText(doc, senderStyle + ": " + message + " ", alignment);
                insertProfileImage(doc, senderLoginID, base64Image, alignment);
            }

            // 줄바꿈 추가
            doc.insertString(doc.getLength(), "\n", null);

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // 채팅 영역 스크롤 이동
        chatArea.setCaretPosition(doc.getLength());
    }

    protected void appendImage(String senderLoginID, String time, String imagePath) {
        System.out.println("[개발용] : appendImage 호출됨: senderLoginID=" + senderLoginID + ", imagePath=" + imagePath);
        String os = System.getProperty("os.name").toLowerCase();
        System.out.println("[개발용] : 클라이언트 OS : " + os);
        File imageFile = new File(imagePath);

        if (!imageFile.exists()) {
            System.err.println("이미지 파일이 존재하지 않습니다: " + imagePath);
            return;
        }

        try {
            // 이미지 로드
            ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
            if (icon.getIconWidth() == -1) {
                System.err.println("이미지 로드 실패: " + imagePath);
                return;
            }

            // 이미지 크기 조정
            Image image = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(image);

            // 단락 정렬 설정
            String alignment = senderLoginID.equals(loginID) ? "right" : "left";

            // 발신자의 프로필 이미지와 스타일 가져오기
            String[] profile = getSenderProfile(senderLoginID);
            String base64Image = profile[0];
            String senderStyle = profile[1];

            StyledDocument doc = chatArea.getStyledDocument();

            // 시간 정보 삽입
            insertTime(doc, time, alignment);

            // 프로필 이미지 삽입과 이미지 삽입
            if (alignment.equals("left")) {
                // 발신자가 다른 사용자일 경우: 이미지 먼저, 메시지 나중에
                insertProfileImage(doc, senderLoginID, base64Image, alignment);
                insertMessageText(doc, senderStyle + " : ", alignment);
                insertImage(doc, scaledIcon, alignment);
            } else {
                // 발신자가 본인인 경우: 메시지 먼저, 이미지 나중에
                insertMessageText(doc, senderStyle + ": ", alignment);
                insertImage(doc, scaledIcon, alignment);
                insertProfileImage(doc, senderLoginID, base64Image, alignment);
            }

            // 줄바꿈 추가
            doc.insertString(doc.getLength(), "\n", null);

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // 채팅 영역 스크롤 이동
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    protected void appendEmoji(String senderLoginID, String time, String emojiFileName) {
        System.out.println("[개발용] : appendEmoji 호출됨: senderLoginID=" + senderLoginID + ", emojiFileName=" + emojiFileName);
        String os = System.getProperty("os.name").toLowerCase();
        System.out.println("[개발용] : 클라이언트 OS : " + os);
        String emojiPath = emojiFileName;
        File emojiFile = new File(emojiPath);

        System.out.println("[개발용] : 이모티콘 경로 확인: " + emojiPath);

        if (!emojiFile.exists()) {
            System.err.println("[개발용] : 이모티콘 파일이 존재하지 않습니다: " + emojiPath);
            return;
        }

        try {
            // 이모티콘 로드
            ImageIcon icon = new ImageIcon(emojiFile.getAbsolutePath());
            if (icon.getIconWidth() == -1) {
                System.err.println("이모티콘 이미지 로드 실패: " + emojiPath);
                return;
            }

            // 이모티콘 크기 조정
            Image image = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(image);

            // 단락 정렬 설정
            String alignment = senderLoginID.equals(loginID) ? "right" : "left";

            // 발신자의 프로필 이미지와 스타일 가져오기
            String[] profile = getSenderProfile(senderLoginID);
            String base64Image = profile[0];
            String senderStyle = profile[1];

            StyledDocument doc = chatArea.getStyledDocument();

            // 시간 정보 삽입
            insertTime(doc, time, alignment);

            // 프로필 이미지 삽입과 이모티콘 삽입
            if (alignment.equals("left")) {
                // 발신자가 다른 사용자일 경우: 이미지 먼저, 메시지 나중에
                insertProfileImage(doc, senderLoginID, base64Image, alignment);
                insertMessageText(doc, senderStyle + " : ", alignment);
                insertEmoji(doc, scaledIcon, alignment);
            } else {
                // 발신자가 본인인 경우: 메시지 먼저, 이모티콘 나중에
                insertMessageText(doc, senderStyle + ": ", alignment);
                insertEmoji(doc, scaledIcon, alignment);
                insertProfileImage(doc, senderLoginID, base64Image, alignment);
            }

            // 줄바꿈 추가
            doc.insertString(doc.getLength(), "\n", null);

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // 채팅 영역 스크롤 이동
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void insertTime(StyledDocument doc, String time, String alignment) throws BadLocationException {
        // 시간 정보 삽입
        doc.insertString(doc.getLength(), "\n", null);

        // 시간 스타일 생성
        Style timeStyle = chatArea.addStyle("TimeStyle_" + doc.getLength(), null);
        StyleConstants.setFontSize(timeStyle, 10);
        StyleConstants.setItalic(timeStyle, true);
        if (alignment.equals("right")) {
            StyleConstants.setAlignment(timeStyle, StyleConstants.ALIGN_RIGHT);
        } else {
            StyleConstants.setAlignment(timeStyle, StyleConstants.ALIGN_LEFT);
        }
        doc.setParagraphAttributes(doc.getLength(), 1, timeStyle, false);
        doc.insertString(doc.getLength(), time + "\n", timeStyle);
    }

    private void insertProfileImage(StyledDocument doc, String senderLoginID, String base64Image, String alignment) throws BadLocationException {
        if (base64Image != null && !base64Image.isEmpty()) {
            ImageIcon profileIcon = getProfileImage(senderLoginID, base64Image);
            if (profileIcon != null) {
                Style imageStyle = chatArea.addStyle("ProfileImage_" + doc.getLength(), null);
                StyleConstants.setIcon(imageStyle, profileIcon);
                doc.insertString(doc.getLength(), " ", imageStyle);
            }
        }
    }

    private void insertMessageText(StyledDocument doc, String message, String alignment) throws BadLocationException {
        // 메시지 텍스트 삽입
        doc.insertString(doc.getLength(), message, null);
    }

    private void insertImage(StyledDocument doc, ImageIcon imageIcon, String alignment) throws BadLocationException {
        Style imageStyle = chatArea.addStyle("ImageStyle_" + doc.getLength(), null);
        StyleConstants.setIcon(imageStyle, imageIcon);
        doc.insertString(doc.getLength(), " ", imageStyle);
    }

    private void insertEmoji(StyledDocument doc, ImageIcon emojiIcon, String alignment) throws BadLocationException {
        Style emojiStyle = chatArea.addStyle("EmojiStyle_" + doc.getLength(), null);
        StyleConstants.setIcon(emojiStyle, emojiIcon);
        doc.insertString(doc.getLength(), " ", emojiStyle);
    }

    private ImageIcon base64ToImageIcon(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) return null;
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            ImageIcon icon = new ImageIcon(imageBytes);

            // 필요에 따라 이미지 크기 조정
            Image image = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            return new ImageIcon(image);
        } catch (IllegalArgumentException e) {
            System.err.println("Base64 디코딩 실패: " + e.getMessage());
            return null;
        }
    }
}
