package Client;

import Model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;
import java.util.List;

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
        frame.add(menuPanel, BorderLayout.WEST);
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
        String nickname = authPanel.getSignupNicknameField().getText().trim();
        String information = authPanel.getSignupInformationArea().getText().trim().replace(" ", "_");

        if (id.isEmpty() || pw.isEmpty() || userName.isEmpty() || birthday.isEmpty() || nickname.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "모든 필드를 입력하세요.");
            return;
        }

        if (clientHandler != null) clientHandler.sendMessage("/signup " + id + " " + pw + " " + userName + " " + birthday + " " + nickname + " " + information);
    }

    private void sendFriendRequest() {
        String friendLoginID = JOptionPane.showInputDialog(frame, "친구의 로그인 ID를 입력하세요:");
        if (friendLoginID != null && !friendLoginID.trim().isEmpty()) {
            if (clientHandler != null) clientHandler.sendMessage("/addfriend " + friendLoginID.trim());
        }
    }

    private void acceptFriendRequest() {
        String selectedFriend = friendsPanel.getFriendRequestsListUI().getSelectedValue();
        if (selectedFriend == null) {
            JOptionPane.showMessageDialog(frame, "수락할 친구 요청을 선택하세요.");
            return;
        }
        if (clientHandler != null) clientHandler.sendMessage("/acceptfriend " + selectedFriend);
        friendsPanel.getFriendRequestsModel().removeElement(selectedFriend);
    }

    private void rejectFriendRequest() {
        String selectedFriend = friendsPanel.getFriendRequestsListUI().getSelectedValue();
        if (selectedFriend == null) {
            JOptionPane.showMessageDialog(frame, "거절할 친구 요청을 선택하세요.");
            return;
        }
        if (clientHandler != null) clientHandler.sendMessage("/rejectfriend " + selectedFriend);
        friendsPanel.getFriendRequestsModel().removeElement(selectedFriend);
    }

    private void createChatRoom() {
        String chatRoomName = JOptionPane.showInputDialog(frame, "채팅방 이름을 입력하세요:");
        if (chatRoomName == null || chatRoomName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "채팅방 이름을 입력해야 합니다.");
            return;
        }

        List<String> friends = Collections.list(friendsPanel.getFriendsListModel().elements());
        if (friends.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "친구가 없습니다. 친구를 추가하세요.");
            return;
        }

        // CheckBoxListModel 및 CheckBoxListRenderer 사용
        CheckBoxListModel model = new CheckBoxListModel(friends);
        JList<String> friendsJList = new JList<>(model);
        friendsJList.setCellRenderer(new CheckBoxListRenderer());
        friendsJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // 다중 선택 모드 설정

        // 마우스 클릭 이벤트로 체크박스 상태 변경
        friendsJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = friendsJList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    // 클릭한 항목의 선택 상태를 반전시킴
                    model.setChecked(index, !model.isChecked(index));
                }
            }
        });

        // 선택 창 표시
        JScrollPane scrollPane = new JScrollPane(friendsJList);
        scrollPane.setPreferredSize(new Dimension(200, 150));
        int result = JOptionPane.showConfirmDialog(frame, scrollPane,
                "채팅방에 추가할 친구를 선택하세요", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            List<String> selectedFriends = model.getCheckedItems();
            if (selectedFriends.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "최소 하나의 친구를 선택하세요.");
                return;
            }

            // 선택된 친구를 기반으로 채팅방 생성 명령어 생성
            StringBuilder sb = new StringBuilder("/createchat " + chatRoomName);
            for (String friend : selectedFriends) {
                sb.append(" ").append(friend);
            }

            // 서버로 명령 전송
            if (clientHandler != null) clientHandler.sendMessage(sb.toString());
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

    public void updateFriendsList(List<String> friends) {
        friendsPanel.getFriendsListModel().clear();
        for (String friend : friends) {
            friendsPanel.getFriendsListModel().addElement(friend);
        }
    }

    public void addFriendRequest(String requesterLoginID) {
        friendsPanel.getFriendRequestsModel().addElement(requesterLoginID);
        JOptionPane.showMessageDialog(frame, requesterLoginID + "님이 친구 요청을 보냈습니다.");
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

        if (loginUser == null) {
            JOptionPane.showMessageDialog(frame, "로그인 정보가 없습니다.");
            return;
        }

        ChatWindow chatWindow = new ChatWindow(loginUser.getLoginID(), chatRoomId, clientHandler);
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
}
