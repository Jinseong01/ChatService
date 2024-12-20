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

    // 채팅창 초기화
    public ChatWindow(String loginID, String chatRoomId, String chatRoomName, ClientHandler handler) {
        this.loginID = loginID;
        this.chatRoomId = chatRoomId;
        this.chatRoomName = chatRoomName;
        this.handler = handler;

        setTitle(chatRoomName);
        setSize(400, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 채팅 영역 설정
        chatArea.setEditable(false);
        chatArea.setContentType("text/plain");
        chatArea.setText("");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
        chatScrollPane.setBorder(BorderFactory.createEmptyBorder());
        chatArea.setBackground(new Color(186, 206, 224));

        // 입력 패널: 입력 필드, 전송 버튼, 추가 옵션 버튼
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

        // 메인 패널 구성
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(chatScrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // 전송 버튼 액션: 메시지 전송
        sendButton.addActionListener(e -> sendMessage());
        requestChatHistory();
    }

    // 채팅 이력 서버에 요청
    private void requestChatHistory() {
        if (handler != null) {
            handler.sendMessage("/getchathistory " + chatRoomId);
        }
    }

    // 입력된 메시지를 서버로 전송
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) return;
        // 시간 포함하여 전송하는 메서드 호출
        handler.sendChatMessage(chatRoomId, message);
        inputField.setText("");
    }

    // 서버로부터 수신한 채팅 메시지를 처리
    public void handleChatMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("[개발용] : 수신된 메시지: " + msg);
            // 이모티콘 처리
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

    // 채팅 이력 수신 시작 시 호출
    public void handleChatHistoryStart() {
        receivingHistory = true;
        tempChatHistory.clear();
    }

    // 채팅 이력 수신 종료 시 호출
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

    // 특정 발신자의 프로필 이미지(Base64)와 표시 이름을 반환
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

    // 프로필 이미지를 캐시하여 ImageIcon으로 반환
    private ImageIcon getProfileImage(String loginID, String base64Image) {
        if (profileImageCache.containsKey(loginID)) {
            return profileImageCache.get(loginID);
        } else {
            ImageIcon icon = base64ToImageIcon(base64Image);
            profileImageCache.put(loginID, icon);
            return icon;
        }
    }

    // 일반 메시지를 채팅 영역에 추가
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

    // 이미지 메시지를 채팅 영역에 추가
    protected void appendImage(String senderLoginID, String time, String base64ImageData) {
        System.out.println("[개발용] : appendImage 호출됨: senderLoginID=" + senderLoginID);

        try {
            // Base64 데이터를 이미지로 변환
            ImageIcon icon = base64ToImageIcon(base64ImageData);
            if (icon == null) {
                System.err.println("이미지 로드 실패: Base64 데이터가 잘못되었습니다.");
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
                insertProfileImage(doc, senderLoginID, base64Image, alignment);
                insertMessageText(doc, senderStyle + " : ", alignment);
                insertImage(doc, scaledIcon, alignment);
            } else {
                insertMessageText(doc, senderStyle + ": ", alignment);
                insertImage(doc, scaledIcon, alignment);
                insertProfileImage(doc, senderLoginID, base64Image, alignment);
            }

            // 줄바꿈 추가
            doc.insertString(doc.getLength(), "\n", null);

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    // 이모티콘 메시지를 채팅 영역에 추가
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
            ImageIcon icon = new ImageIcon(emojiFile.getAbsolutePath());
            if (icon.getIconWidth() == -1) {
                System.err.println("이모티콘 이미지 로드 실패: " + emojiPath);
                return;
            }

            Image image = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(image);

            String alignment = senderLoginID.equals(loginID) ? "right" : "left";

            String[] profile = getSenderProfile(senderLoginID);
            String base64Image = profile[0];
            String senderStyle = profile[1];

            StyledDocument doc = chatArea.getStyledDocument();

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

            doc.insertString(doc.getLength(), "\n", null);

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // 채팅 영역 스크롤 이동
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    // 시간 문자열을 문서에 삽입
    private void insertTime(StyledDocument doc, String time, String alignment) throws BadLocationException {
        doc.insertString(doc.getLength(), "\n", null);

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

    // 프로필 이미지를 문서에 삽입
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

    // 메시지 텍스트를 문서에 삽입
    private void insertMessageText(StyledDocument doc, String message, String alignment) throws BadLocationException {
        doc.insertString(doc.getLength(), message, null);
    }

    // 이미지(일반 이미지)를 문서에 삽입
    private void insertImage(StyledDocument doc, ImageIcon imageIcon, String alignment) throws BadLocationException {
        Style imageStyle = chatArea.addStyle("ImageStyle_" + doc.getLength(), null);
        StyleConstants.setIcon(imageStyle, imageIcon);
        doc.insertString(doc.getLength(), " ", imageStyle);
    }

    // 이모티콘 이미지를 문서에 삽입
    private void insertEmoji(StyledDocument doc, ImageIcon emojiIcon, String alignment) throws BadLocationException {
        Style emojiStyle = chatArea.addStyle("EmojiStyle_" + doc.getLength(), null);
        StyleConstants.setIcon(emojiStyle, emojiIcon);
        doc.insertString(doc.getLength(), " ", emojiStyle);
    }

    // Base64 문자열을 ImageIcon으로 변환
    private ImageIcon base64ToImageIcon(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) return null;
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            ImageIcon icon = new ImageIcon(imageBytes);

            // 이미지 크기를 32x32로 조정
            Image image = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            return new ImageIcon(image);
        } catch (IllegalArgumentException e) {
            System.err.println("Base64 디코딩 실패: " + e.getMessage());
            return null;
        }
    }
}
