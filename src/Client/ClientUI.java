package Client;

import Model.UserSummary;
import Model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ClientUI extends JFrame {
    private ClientHandler clientHandler;
    private JFrame frame = new JFrame("Java Chat (Mobile Style)");
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    // 패널들
    private AuthPanel authPanel;
    private FriendsPanel friendsPanel;
    private ChatPanel chatPanel;
    private MemoPanel memoPanel;
    private JPanel menuPanel;

    private JTabbedPane leftTabbedPane;

    // 로그인한 사용자 정보
    private User loginUser = null;

    public ClientUI() {
        initializeUI();
    }

    public JFrame getFrame() {
        return frame;
    }

    public void setClientHandler(ClientHandler handler) {
        this.clientHandler = handler;
    }

    public void showFrame() {
        frame.setVisible(true);
    }

    private void initializeUI() {
        frame.setSize(500, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Font defaultFont = new Font("SansSerif", Font.PLAIN, 16);
        UIManager.put("Label.font", defaultFont);
        UIManager.put("Button.font", defaultFont);

        authPanel = new AuthPanel();
        friendsPanel = new FriendsPanel();
        chatPanel = new ChatPanel();
        memoPanel = new MemoPanel();

        // 왼쪽 버튼 메뉴
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS)); // 세로 배치
        menuPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8)); // 패널 여백 추가

        JButton friendsButton = new JButton("친구");
        JButton chatButton = new JButton("채팅");
        JButton memoButton = new JButton("메모");

        // 버튼 크기 조정
        Dimension buttonSize = new Dimension(80, 40); // 버튼 크기 설정
        friendsButton.setMaximumSize(buttonSize);
        chatButton.setMaximumSize(buttonSize);
        memoButton.setMaximumSize(buttonSize);

        // 버튼 상단 정렬
        friendsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        chatButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        memoButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 메뉴 패널에 상단 여백 추가
        menuPanel.add(Box.createVerticalStrut(15));

        // 메뉴 패널에 버튼 추가
        menuPanel.add(friendsButton);
        menuPanel.add(Box.createVerticalStrut(8)); // 버튼 간 여백
        menuPanel.add(chatButton);
        menuPanel.add(Box.createVerticalStrut(8));
        menuPanel.add(memoButton);
        menuPanel.add(Box.createVerticalGlue()); // 하단 여백

        // 초기 상태에서는 숨김
        menuPanel.setVisible(false);

        // 중앙 화면 패널
        mainPanel.add(authPanel, "auth");
        mainPanel.add(friendsPanel, "friends");
        mainPanel.add(chatPanel, "chat");
        mainPanel.add(memoPanel, "memo");

        // 버튼 클릭 이벤트
        friendsButton.addActionListener(e -> cardLayout.show(mainPanel, "friends"));
        chatButton.addActionListener(e -> cardLayout.show(mainPanel, "chat"));
        memoButton.addActionListener(e -> cardLayout.show(mainPanel, "memo"));

        // 전체 레이아웃
        frame.setLayout(new BorderLayout());

        // 경계선이 포함된 왼쪽 메뉴 패널
        menuPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY)); // 오른쪽 경계선 추가
        frame.add(menuPanel, BorderLayout.WEST);

        // 중앙 화면 패널
        frame.add(mainPanel, BorderLayout.CENTER);

        registerEventListeners();
    }

    private void registerEventListeners() {
        // AuthPanel 이벤트
        authPanel.getLoginButton().addActionListener(e -> attemptLogin());
        authPanel.getSignupButton().addActionListener(e -> switchToSignup());
        authPanel.getRegisterButton().addActionListener(e -> attemptSignup());

        // FriendsPanel 이벤트
        friendsPanel.getAddFriendButton().addActionListener(e -> sendFriendRequest());
        friendsPanel.getAcceptFriendButton().addActionListener(e -> acceptFriendRequest());
        friendsPanel.getRejectFriendButton().addActionListener(e -> rejectFriendRequest());
        friendsPanel.getProfileImageLabel().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                editProfileImageClick(); // 별도 메서드 호출만 수행
            }
        });
        friendsPanel.getStatusMessageValueLabel().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                editStatusMessageClick(); // 별도 메서드 호출만 수행
            }
        });

        // ChatPanel 이벤트
        chatPanel.getCreateChatButton().addActionListener(e -> createChatRoom());
        chatPanel.getChatRoomsListUI().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    openChatWindow();
                }
            }
        });

        // MemoPanel 이벤트
        memoPanel.getAddMemoButton().addActionListener(e -> addMemo());
        memoPanel.getEditMemoButton().addActionListener(e -> editSelectedMemo());
        memoPanel.getDeleteMemoButton().addActionListener(e -> deleteSelectedMemo());
        memoPanel.getMemoListUI().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editSelectedMemo();
                }
            }
        });
    }

    private void editStatusMessageClick() {
        String newStatus = JOptionPane.showInputDialog(getFrame(), "새 상태메시지를 입력하세요:");
        if (newStatus != null && !newStatus.trim().isEmpty()) {
            clientHandler.updateStatusMessage(newStatus.trim());
        }
    }

    private void editProfileImageClick() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(getFrame());

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null && selectedFile.exists()) {
                try {
                    byte[] fileBytes = java.nio.file.Files.readAllBytes(selectedFile.toPath());
                    String base64Image = Base64.getEncoder().encodeToString(fileBytes);

                    if (clientHandler != null) {
                        clientHandler.updateProfileImage(base64Image);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(getFrame(), "이미지 파일을 읽는 동안 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public void switchToSignup() {
        authPanel.showSignupPanel();
        menuPanel.setVisible(false); // 메뉴 숨기기
    }

    public void switchToLogin() {
        authPanel.showLoginPanel();
        menuPanel.setVisible(false); // 메뉴 숨기기
    }

    public void switchToFriendsPanel() {
        cardLayout.show(mainPanel, "friends");
        menuPanel.setVisible(true); // 메뉴 표시
    }

    private void attemptLogin() {
        String id = authPanel.getLoginUserField().getText().trim();
        String pw = new String(authPanel.getLoginPassField().getPassword()).trim();
        if (id.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "로그인 ID와 비밀번호를 입력하세요.");
            return;
        }
        if (clientHandler != null) clientHandler.sendMessage("/login " + id + " " + pw);
    }

    private void attemptSignup() {
        String id = authPanel.getSignupLoginIDField().getText().trim();
        String pw = new String(authPanel.getSignupLoginPWField().getPassword()).trim();
        String userName = authPanel.getSignupUserNameField().getText().trim();
        String birthday = authPanel.getSignupBirthdayField().getText().trim();
        String information = authPanel.getSignupStatusMessageField().getText().trim().replace(" ", "_");

        if (id.isEmpty() || pw.isEmpty() || userName.isEmpty() || birthday.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "모든 필드를 입력하세요.");
            return;
        }

        if (clientHandler != null) {
            clientHandler.sendMessage("/signup " + id + " " + pw + " " + userName + " " + birthday + " " + information);

            // 서버 응답 처리
            SwingUtilities.invokeLater(() -> {
                // 회원가입 실패를 가정하고 버튼만 초기화
                authPanel.getRegisterButton().setEnabled(true);
            });
        }
    }

    private void sendFriendRequest() {
        String friendLoginID = JOptionPane.showInputDialog(frame, "친구의 로그인 ID를 입력하세요:");
        if (friendLoginID != null && !friendLoginID.trim().isEmpty()) {
            if (clientHandler != null) clientHandler.sendMessage("/addfriend " + friendLoginID.trim());
        }
    }

    private void acceptFriendRequest() {
        UserSummary selectedUserSummary = friendsPanel.getFriendRequestsListUI().getSelectedValue();
        if (selectedUserSummary == null) {
            JOptionPane.showMessageDialog(frame, "수락할 친구 요청을 선택하세요.");
            return;
        }

        String loginID = selectedUserSummary.getLoginID(); // Friend 객체에서 직접 로그인 ID 가져오기
        if (clientHandler != null) {
            clientHandler.sendMessage("/acceptfriend " + loginID);
        }
        friendsPanel.getFriendRequestsModel().removeElement(selectedUserSummary); // 리스트에서 제거
    }

    private void rejectFriendRequest() {
        UserSummary selectedUserSummary = friendsPanel.getFriendRequestsListUI().getSelectedValue();
        if (selectedUserSummary == null) {
            JOptionPane.showMessageDialog(frame, "거절할 친구 요청을 선택하세요.");
            return;
        }

        String loginID = selectedUserSummary.getLoginID(); // Friend 객체에서 직접 로그인 ID 가져오기
        if (clientHandler != null) {
            clientHandler.sendMessage("/rejectfriend " + loginID);
        }
        friendsPanel.getFriendRequestsModel().removeElement(selectedUserSummary); // 리스트에서 제거
    }

    private void createChatRoom() {
        String chatRoomName = JOptionPane.showInputDialog(frame, "채팅방 이름을 입력하세요:");
        if (chatRoomName == null || chatRoomName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "채팅방 이름을 입력해야 합니다.");
            return;
        }

        List<UserSummary> userSummaries = Collections.list(friendsPanel.getFriendsListModel().elements());
        if (userSummaries.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "친구가 없습니다. 친구를 추가하세요.");
            return;
        }

        CheckBoxListModel model = new CheckBoxListModel(userSummaries.stream()
                .map(UserSummary::getUserName)
                .toList());
        JList<String> friendsJList = new JList<>(model);
        friendsJList.setCellRenderer(new CheckBoxListRenderer());

        friendsJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = friendsJList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    model.setChecked(index, !model.isChecked(index));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(friendsJList);
        scrollPane.setPreferredSize(new Dimension(200, 150));
        int result = JOptionPane.showConfirmDialog(frame, scrollPane,
                "채팅방에 추가할 친구를 선택하세요", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            List<String> selectedUserNames = model.getCheckedItems();
            if (selectedUserNames.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "최소 하나의 친구를 선택하세요.");
                return;
            }

            // 선택된 userName을 loginID로 변환하여 명령 생성
            StringBuilder sb = new StringBuilder("/checkonline " + chatRoomName);
            for (String userName : selectedUserNames) {
                userSummaries.stream()
                        .filter(friend -> friend.getUserName().equals(userName))
                        .findFirst()
                        .ifPresent(friend -> sb.append(" ").append(friend.getLoginID()));
            }

            if (clientHandler != null) {
                clientHandler.sendMessage(sb.toString());
            }
        }
    }

    private void addMemo() {
        String memoContent = JOptionPane.showInputDialog(frame, "추가할 메모 내용을 입력하세요:");
        if (memoContent != null && !memoContent.trim().isEmpty()) {
            memoContent = memoContent.replace(" ", "_");
            if (clientHandler != null) clientHandler.sendMessage("/addmemo " + memoContent.trim());
        }
    }

    private void editSelectedMemo() {
        String selectedMemo = memoPanel.getMemoListUI().getSelectedValue();
        if (selectedMemo == null) {
            JOptionPane.showMessageDialog(frame, "수정할 메모를 선택하세요.");
            return;
        }

        int start = selectedMemo.indexOf("[");
        int end = selectedMemo.indexOf("]");
        if (start == -1 || end == -1) {
            JOptionPane.showMessageDialog(frame, "유효하지 않은 메모 형식입니다.");
            return;
        }

        String indexStr = selectedMemo.substring(start + 1, end);
        int index;
        try {
            index = Integer.parseInt(indexStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "메모 인덱스가 유효하지 않습니다.");
            return;
        }

        String existingContent = selectedMemo.substring(end + 2);
        String newContent = (String) JOptionPane.showInputDialog(
                frame,
                "메모 내용을 수정하세요:",
                "메모 수정",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                existingContent
        );

        if (newContent != null && !newContent.trim().isEmpty()) {
            newContent = newContent.replace(" ", "_");
            if (clientHandler != null) clientHandler.sendMessage("/editmemo " + index + " " + newContent.trim());
        }
    }

    private void deleteSelectedMemo() {
        String selectedMemo = memoPanel.getMemoListUI().getSelectedValue();
        if (selectedMemo == null) {
            JOptionPane.showMessageDialog(frame, "삭제할 메모를 선택하세요.");
            return;
        }

        int start = selectedMemo.indexOf("[");
        int end = selectedMemo.indexOf("]");
        if (start == -1 || end == -1) {
            JOptionPane.showMessageDialog(frame, "유효하지 않은 메모 형식입니다.");
            return;
        }

        String indexStr = selectedMemo.substring(start + 1, end);
        int index;
        try {
            index = Integer.parseInt(indexStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "메모 인덱스가 유효하지 않습니다.");
            return;
        }

        if (clientHandler != null) clientHandler.sendMessage("/deletememo " + index);
    }

    public void updateFriendsList(List<UserSummary> userSummaries) {
        friendsPanel.getFriendsListModel().clear();
        for (UserSummary userSummary : userSummaries) {
            friendsPanel.getFriendsListModel().addElement(userSummary);
        }
    }

    public void addFriendRequest(UserSummary requester) {
        friendsPanel.getFriendRequestsModel().addElement(requester);
        JOptionPane.showMessageDialog(frame, requester.getUserName() + "님이 친구 요청을 보냈습니다.");
    }


    private String formatFriendDisplayName(UserSummary userSummary) {
        // e.g., "홍길동 (loginID: hong123)"
        return String.format("%s (ID: %s)", userSummary.getUserName(), userSummary.getLoginID());
    }

    private String extractLoginIDFromDisplayName(String displayName) {
        // Extract loginID from display name, e.g., "홍길동 (ID: hong123)"
        int idStart = displayName.indexOf("(ID: ");
        int idEnd = displayName.indexOf(")", idStart);
        if (idStart != -1 && idEnd != -1) {
            return displayName.substring(idStart + 5, idEnd);
        }
        return null;
    }

    public void addChatRoom(String chatRoomDisplay) {
        chatPanel.getChatRoomsListModel().addElement(chatRoomDisplay);
    }

    public void resetUI() {
        switchToLogin();
        // Clear lists
        friendsPanel.getFriendsListModel().clear();
        friendsPanel.getFriendRequestsModel().clear();
        chatPanel.getChatRoomsListModel().clear();
        memoPanel.getMemoListModel().clear();
    }

    public void clearMemos() {
        memoPanel.getMemoListModel().clear();
    }

    public void addMemo(String memo) {
        memoPanel.getMemoListModel().addElement(memo);
    }

    private void openChatWindow() {
        String selectedChatRoom = chatPanel.getChatRoomsListUI().getSelectedValue();
        if (selectedChatRoom == null) {
            JOptionPane.showMessageDialog(frame, "채팅방을 선택하세요.");
            return;
        }
        int idStart = selectedChatRoom.indexOf("ID: ");
        int idEnd = selectedChatRoom.indexOf(")", idStart);
        if (idStart == -1 || idEnd == -1) {
            JOptionPane.showMessageDialog(frame, "유효하지 않은 채팅방 형식입니다.");
            return;
        }
        String chatRoomId = selectedChatRoom.substring(idStart + 4, idEnd);
        String chatRoomName = selectedChatRoom.substring(0, idStart).replace("(", "").trim();

        if (loginUser == null) {
            JOptionPane.showMessageDialog(frame, "로그인 정보가 없습니다.");
            return;
        }

        ChatWindow chatWindow = new ChatWindow(loginUser.getLoginID(), chatRoomId, chatRoomName, clientHandler);
        clientHandler.addChatWindow(chatRoomId, chatWindow);
        chatWindow.setVisible(true);
    }

    // 로그인 성공 시 호출되는 메서드
    public void handleLoginSuccess(User loginUser) {
        this.loginUser = loginUser; // 로그인한 사용자 정보 저장
        // FriendsPanel의 사용자 정보 업데이트
        friendsPanel.updateUserInfo(loginUser);
        switchToFriendsPanel();
    }

    // 친구의 프로필 이미지 업데이트
    public void updateFriendProfileImage(String loginID, String newProfileImage) {
        DefaultListModel<UserSummary> model = friendsPanel.getFriendsListModel();
        boolean updated = false;

        // 친구 목록에서 업데이트 시도
        for (int i = 0; i < model.size(); i++) {
            UserSummary userSummary = model.getElementAt(i);
            if (userSummary.getLoginID().equals(loginID)) {
                System.out.println("[개발용] : 친구 목록에서 프로필 이미지 업데이트: " + loginID);
                userSummary.setProfileImage(newProfileImage);
                model.setElementAt(userSummary, i); // 리스트 모델을 업데이트하여 변경 사항 반영
                updated = true;
                break;
            }
        }

        // loginUser 안에 저장된 친구 정보 업데이트
        if (loginUser != null) {
            Set<UserSummary> userSummaries = loginUser.getFriends();
            for (UserSummary userSummary : userSummaries) {
                if (userSummary.getLoginID().equals(loginID)) {
                    userSummary.setProfileImage(newProfileImage);
                    System.out.println("[개발용] : loginUser의 친구 프로필 이미지 업데이트: " + loginID);
                    break;
                }
            }
        }

        // 친구 목록에 없을 경우, 친구 요청 목록에서 업데이트 시도
        if (!updated) {
            System.out.println("[개발용] : 친구 요청 목록에서 프로필 이미지 업데이트 시도: " + loginID);
            updateFriendRequestProfileImage(loginID, newProfileImage);
        }
    }

    // 친구의 상태 메시지 업데이트
    public void updateFriendStatus(String loginID, String newStatus) {
        DefaultListModel<UserSummary> model = friendsPanel.getFriendsListModel();
        boolean updated = false;

        // 친구 목록에서 업데이트 시도
        for (int i = 0; i < model.size(); i++) {
            UserSummary userSummary = model.getElementAt(i);
            if (userSummary.getLoginID().equals(loginID)) {
                System.out.println("[개발용] : 친구 목록에서 상태 메시지 업데이트: " + loginID);
                userSummary.setInformation(newStatus);
                model.setElementAt(userSummary, i); // 리스트 모델을 업데이트하여 변경 사항 반영
                updated = true;
                break;
            }
        }

        // loginUser 안에 저장된 친구 정보 업데이트
        if (loginUser != null) {
            Set<UserSummary> userSummaries = loginUser.getFriends();
            for (UserSummary userSummary : userSummaries) {
                if (userSummary.getLoginID().equals(loginID)) {
                    userSummary.setInformation(newStatus);
                    System.out.println("[개발용] : loginUser의 친구 상태 메시지 업데이트: " + loginID);
                    break;
                }
            }
        }

        // 친구 목록에 없을 경우, 친구 요청 목록에서 업데이트 시도
        if (!updated) {
            System.out.println("[개발용] : 친구 요청 목록에서 상태 메시지 업데이트 시도: " + loginID);
            updateFriendRequestStatus(loginID, newStatus);
        }
    }

    // 자신의 프로필 이미지 업데이트
    public void updateOwnProfileImage(String newProfileImage) {
        // 프로필 이미지 UI 업데이트
        ImageIcon profileIcon = null;
        if (newProfileImage != null && !newProfileImage.isEmpty()) {
            try {
                byte[] imageBytes = Base64.getDecoder().decode(newProfileImage);
                Image image = Toolkit.getDefaultToolkit().createImage(imageBytes);
                Image scaledImage = image.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                profileIcon = new ImageIcon(scaledImage);
            } catch (IllegalArgumentException e) {
                System.err.println("Base64 디코딩 실패: " + e.getMessage());
            }
        }

        if (profileIcon != null) {
            friendsPanel.getProfileImageLabel().setIcon(profileIcon);
            friendsPanel.getProfileImageLabel().setText("");
        } else {
            friendsPanel.getProfileImageLabel().setIcon(null);
            friendsPanel.getProfileImageLabel().setText("이미지를 불러올 수 없습니다.");
        }

        // User 객체의 프로필 이미지 업데이트
        if (loginUser != null) {
            loginUser.setProfileImage(newProfileImage);
        }
    }

    // 자신의 상태 메시지 업데이트
    public void updateOwnStatusMessage(String newStatus) {
        // 상태 메시지 UI 업데이트
        friendsPanel.getStatusMessageValueLabel().setText(newStatus);

        // User 객체의 상태 메시지 업데이트
        if (loginUser != null) {
            loginUser.setInformation(newStatus);
        }
    }

    public void updateFriendRequestProfileImage(String loginID, String newProfileImage) {
        DefaultListModel<UserSummary> model = friendsPanel.getFriendRequestsModel();
        for (int i = 0; i < model.size(); i++) {
            UserSummary userSummary = model.get(i);
            if (userSummary.getLoginID().equals(loginID)) {
                userSummary.setProfileImage(newProfileImage);
                model.set(i, userSummary); // 모델 업데이트
                break;
            }
        }
    }

    public void updateFriendRequestStatus(String loginID, String newStatus) {
        DefaultListModel<UserSummary> model = friendsPanel.getFriendRequestsModel();
        for (int i = 0; i < model.size(); i++) {
            UserSummary userSummary = model.get(i);
            if (userSummary.getLoginID().equals(loginID)) {
                userSummary.setInformation(newStatus);
                model.set(i, userSummary); // 모델 업데이트
                break;
            }
        }
    }
}
