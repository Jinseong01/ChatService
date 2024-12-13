package Client;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class ClientUI extends JFrame {
    private ClientHandler clientHandler;

    private JFrame frame = new JFrame("Java Chat (Mobile Style)");
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    private JPanel authPanel = new JPanel(new CardLayout());
    private JPanel loginPanel = new JPanel();
    private JPanel signupPanel = new JPanel();

    private JTextField loginUserField = new JTextField(15);
    private JPasswordField loginPassField = new JPasswordField(15);
    private JButton loginButton = new JButton("로그인");
    private JButton signupButton = new JButton("회원가입");

    private JTextField signupLoginIDField = new JTextField(15);
    private JPasswordField signupLoginPWField = new JPasswordField(15);
    private JTextField signupUserNameField = new JTextField(15);
    private JTextField signupBirthdayField = new JTextField(15);
    private JTextField signupNicknameField = new JTextField(15);
    private JTextArea signupInformationArea = new JTextArea(3, 15);
    private JButton registerButton = new JButton("회원가입 완료");

    private JPanel chatPanel = new JPanel(new BorderLayout());

    private JTabbedPane leftTabbedPane = new JTabbedPane(JTabbedPane.TOP);

    private JPanel friendsTab = new JPanel(new BorderLayout());
    private DefaultListModel<String> friendsListModel = new DefaultListModel<>();
    private JList<String> friendsListUI = new JList<>(friendsListModel);
    private JButton addFriendButton = new JButton("친구 추가");
    private DefaultListModel<String> friendRequestsModel = new DefaultListModel<>();
    private JList<String> friendRequestsListUI = new JList<>(friendRequestsModel);
    private JButton acceptFriendButton = new JButton("수락");
    private JButton rejectFriendButton = new JButton("거절");

    private JPanel chatTab = new JPanel(new BorderLayout());
    private DefaultListModel<String> chatRoomsListModel = new DefaultListModel<>();
    private JList<String> chatRoomsListUI = new JList<>(chatRoomsListModel);
    private JButton createChatButton = new JButton("채팅방 생성");

    private JPanel memoTab = new JPanel(new BorderLayout());
    private DefaultListModel<String> memoListModel = new DefaultListModel<>();
    private JList<String> memoListUI = new JList<>(memoListModel);
    private JButton addMemoButton = new JButton("메모 추가");
    private JButton editMemoButton = new JButton("메모 수정");
    private JButton deleteMemoButton = new JButton("메모 삭제");

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

    public String getLoginIDFromField() {
        return loginUserField.getText().trim();
    }

    private void initializeUI() {
        frame.setSize(400, 800);
        Font defaultFont = new Font("SansSerif", Font.PLAIN, 16);
        UIManager.put("Label.font", defaultFont);
        UIManager.put("Button.font", defaultFont);
        UIManager.put("TextField.font", defaultFont);
        UIManager.put("TextArea.font", defaultFont);
        UIManager.put("List.font", defaultFont);
        UIManager.put("TabbedPane.font", defaultFont);

        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        JLabel loginTitle = new JLabel("로그인", SwingConstants.CENTER);
        loginTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        loginTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPanel.add(loginTitle);
        loginPanel.add(Box.createVerticalStrut(20));
        loginPanel.add(createFieldPanel("로그인 ID:", loginUserField));
        loginPanel.add(createFieldPanel("비밀번호:", loginPassField));
        JPanel loginBtnPanel = new JPanel(new FlowLayout());
        loginBtnPanel.add(loginButton);
        loginBtnPanel.add(signupButton);
        loginPanel.add(Box.createVerticalStrut(20));
        loginPanel.add(loginBtnPanel);

        signupPanel.setLayout(new BoxLayout(signupPanel, BoxLayout.Y_AXIS));
        signupPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        JLabel signupTitle = new JLabel("회원가입", SwingConstants.CENTER);
        signupTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        signupTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        signupPanel.add(signupTitle);
        signupPanel.add(Box.createVerticalStrut(20));
        signupPanel.add(createFieldPanel("로그인 ID:", signupLoginIDField));
        signupPanel.add(createFieldPanel("비밀번호:", signupLoginPWField));
        signupPanel.add(createFieldPanel("사용자 이름:", signupUserNameField));
        signupPanel.add(createFieldPanel("생일(YYYY-MM-DD):", signupBirthdayField));
        signupPanel.add(createFieldPanel("닉네임:", signupNicknameField));
        JPanel infoPanel = new JPanel(new BorderLayout(5,5));
        infoPanel.add(new JLabel("정보:"), BorderLayout.WEST);
        infoPanel.add(new JScrollPane(signupInformationArea), BorderLayout.CENTER);
        signupPanel.add(infoPanel);
        signupPanel.add(Box.createVerticalStrut(20));
        JPanel registerBtnPanel = new JPanel(new FlowLayout());
        registerBtnPanel.add(registerButton);
        signupPanel.add(registerBtnPanel);

        authPanel.add(loginPanel, "login");
        authPanel.add(signupPanel, "signup");
        mainPanel.add(authPanel, "auth");

        JPanel friendsListPanel = new JPanel(new BorderLayout());
        friendsListPanel.setBorder(BorderFactory.createTitledBorder("친구 목록"));
        friendsListPanel.add(new JScrollPane(friendsListUI), BorderLayout.CENTER);
        friendsListPanel.add(addFriendButton, BorderLayout.SOUTH);

        JPanel friendRequestsPanel = new JPanel(new BorderLayout());
        friendRequestsPanel.setBorder(BorderFactory.createTitledBorder("친구 요청"));
        friendRequestsPanel.add(new JScrollPane(friendRequestsListUI), BorderLayout.CENTER);
        JPanel friendRequestButtons = new JPanel(new GridLayout(1, 2, 5, 5));
        friendRequestButtons.add(acceptFriendButton);
        friendRequestButtons.add(rejectFriendButton);
        friendRequestsPanel.add(friendRequestButtons, BorderLayout.SOUTH);

        friendsTab.add(friendsListPanel, BorderLayout.CENTER);
        friendsTab.add(friendRequestsPanel, BorderLayout.SOUTH);

        JPanel chatRoomsPanel = new JPanel(new BorderLayout());
        chatRoomsPanel.setBorder(BorderFactory.createTitledBorder("채팅방 목록"));
        chatRoomsPanel.add(new JScrollPane(chatRoomsListUI), BorderLayout.CENTER);
        chatRoomsPanel.add(createChatButton, BorderLayout.SOUTH);
        chatTab.add(chatRoomsPanel, BorderLayout.CENTER);

        JPanel memoListPanel = new JPanel(new BorderLayout());
        memoListPanel.setBorder(BorderFactory.createTitledBorder("메모 목록"));
        memoListPanel.add(new JScrollPane(memoListUI), BorderLayout.CENTER);
        JPanel memoButtons = new JPanel(new GridLayout(1, 3, 5, 5));
        memoButtons.add(addMemoButton);
        memoButtons.add(editMemoButton);
        memoButtons.add(deleteMemoButton);
        memoListPanel.add(memoButtons, BorderLayout.SOUTH);
        memoTab.add(memoListPanel, BorderLayout.CENTER);

        leftTabbedPane.addTab("친구", friendsTab);
        leftTabbedPane.addTab("채팅", chatTab);
        leftTabbedPane.addTab("메모", memoTab);
        leftTabbedPane.setSelectedIndex(1);

        chatPanel.add(leftTabbedPane, BorderLayout.CENTER);
        mainPanel.add(chatPanel, "chat");

        frame.getContentPane().add(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 이벤트 리스너
        loginButton.addActionListener(e -> attemptLogin());
        signupButton.addActionListener(e -> switchToSignup());
        registerButton.addActionListener(e -> attemptSignup());
        addFriendButton.addActionListener(e -> sendFriendRequest());
        acceptFriendButton.addActionListener(e -> acceptFriendRequest());
        rejectFriendButton.addActionListener(e -> rejectFriendRequest());
        createChatButton.addActionListener(e -> createChatRoom());
        addMemoButton.addActionListener(e -> addMemo());
        editMemoButton.addActionListener(e -> editSelectedMemo());
        deleteMemoButton.addActionListener(e -> deleteSelectedMemo());

        chatRoomsListUI.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    openChatWindow();
                }
            }
        });

        memoListUI.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editSelectedMemo();
                }
            }
        });
    }

    private JPanel createFieldPanel(String labelText, JComponent field) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(labelText));
        panel.add(field);
        return panel;
    }

    private void attemptLogin() {
        String id = loginUserField.getText().trim();
        String pw = new String(loginPassField.getPassword()).trim();
        if (id.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "로그인 ID와 비밀번호를 입력하세요.");
            return;
        }
        if (clientHandler != null) clientHandler.sendMessage("/login " + id + " " + pw);
    }

    private void attemptSignup() {
        String id = signupLoginIDField.getText().trim();
        String pw = new String(signupLoginPWField.getPassword()).trim();
        String userName = signupUserNameField.getText().trim();
        String birthday = signupBirthdayField.getText().trim();
        String nickname = signupNicknameField.getText().trim();
        String information = signupInformationArea.getText().trim().replace(" ", "_");

        if (id.isEmpty() || pw.isEmpty() || userName.isEmpty() || birthday.isEmpty() || nickname.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "모든 필드를 입력하세요.");
            return;
        }

        if (clientHandler != null) clientHandler.sendMessage("/signup " + id + " " + pw + " " + userName + " " + birthday + " " + nickname + " " + information);
    }

    public void switchToSignup() {
        CardLayout cl = (CardLayout) (authPanel.getLayout());
        cl.show(authPanel, "signup");
    }

    public void switchToLogin() {
        CardLayout cl = (CardLayout) (authPanel.getLayout());
        cl.show(authPanel, "login");
    }

    public void switchToChatPanel() {
        CardLayout cl = (CardLayout) (mainPanel.getLayout());
        cl.show(mainPanel, "chat");
    }

    private void sendFriendRequest() {
        String friendLoginID = JOptionPane.showInputDialog(frame, "친구의 로그인 ID를 입력하세요:");
        if (friendLoginID != null && !friendLoginID.trim().isEmpty()) {
            if (clientHandler != null) clientHandler.sendMessage("/addfriend " + friendLoginID.trim());
        }
    }

    private void acceptFriendRequest() {
        String selectedFriend = friendRequestsListUI.getSelectedValue();
        if (selectedFriend == null) {
            JOptionPane.showMessageDialog(frame, "수락할 친구 요청을 선택하세요.");
            return;
        }
        if (clientHandler != null) clientHandler.sendMessage("/acceptfriend " + selectedFriend);
        friendRequestsModel.removeElement(selectedFriend);
    }

    private void rejectFriendRequest() {
        String selectedFriend = friendRequestsListUI.getSelectedValue();
        if (selectedFriend == null) {
            JOptionPane.showMessageDialog(frame, "거절할 친구 요청을 선택하세요.");
            return;
        }
        if (clientHandler != null) clientHandler.sendMessage("/rejectfriend " + selectedFriend);
        friendRequestsModel.removeElement(selectedFriend);
    }

    private void createChatRoom() {
        String chatRoomName = JOptionPane.showInputDialog(frame, "채팅방 이름을 입력하세요:");
        if (chatRoomName == null || chatRoomName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "채팅방 이름을 입력해야 합니다.");
            return;
        }

        List<String> friends = Collections.list(friendsListModel.elements());
        if (friends.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "친구가 없습니다. 친구를 추가하세요.");
            return;
        }

        JList<String> friendsJList = new JList<>(friendsListModel);
        friendsJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(friendsJList);
        scrollPane.setPreferredSize(new Dimension(200, 150));
        int result = JOptionPane.showConfirmDialog(frame, scrollPane,
                "채팅방에 추가할 친구를 선택하세요", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            List<String> selectedFriends = friendsJList.getSelectedValuesList();
            if (selectedFriends.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "최소 하나의 친구를 선택하세요.");
                return;
            }
            StringBuilder sb = new StringBuilder("/createchat " + chatRoomName);
            for (String friend : selectedFriends) {
                sb.append(" ").append(friend);
            }
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
        String selectedMemo = memoListUI.getSelectedValue();
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
        String selectedMemo = memoListUI.getSelectedValue();
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
        friendsListModel.clear();
        for (String friend : friends) {
            friendsListModel.addElement(friend);
        }
    }

    public void addFriendRequest(String requesterLoginID) {
        friendRequestsModel.addElement(requesterLoginID);
        JOptionPane.showMessageDialog(frame, requesterLoginID + "님이 친구 요청을 보냈습니다.");
    }

    public void addChatRoom(String chatRoomDisplay) {
        chatRoomsListModel.addElement(chatRoomDisplay);
    }

    public void resetUI() {
        switchToLogin();
        // Clear lists
        friendsListModel.clear();
        friendRequestsModel.clear();
        chatRoomsListModel.clear();
        memoListModel.clear();
    }

    public void clearMemos() {
        memoListModel.clear();
    }

    public void addMemo(String memo) {
        memoListModel.addElement(memo);
    }

    private void openChatWindow() {
        String selectedChatRoom = chatRoomsListUI.getSelectedValue();
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

        ChatWindow chatWindow = new ChatWindow(getLoginIDFromField(), chatRoomId, clientHandler);
        clientHandler.addChatWindow(chatRoomId, chatWindow);
        chatWindow.setVisible(true);
    }

    // ChatWindow 이너 클래스
    public class ChatWindow extends JFrame {
        private String chatRoomId;
        private ClientHandler handler;
        private String loginID;

        private JTextPane chatArea = new JTextPane();
        private JScrollPane chatScrollPane = new JScrollPane(chatArea);
        private JTextField inputField = new JTextField(15);
        private JButton sendButton = new JButton("전송");
        private StringBuilder htmlContent = new StringBuilder("<html><body>");

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
}
