package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

public class ClientApp {
    private String serverAddress = "localhost";
    private int serverPort = 12345;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private String loginID = null;

    private JFrame frame = new JFrame("Java Chat (Mobile Style)");
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    private JPanel authPanel = new JPanel(cardLayout);
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

    // 채팅방ID -> ChatWindow 맵
    private Map<String, ChatWindow> chatWindows = new HashMap<>();

    public ClientApp() {
        initializeUI();
        frame.setVisible(true);
        connectToServer();
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
        deleteMemoButton = new JButton("메모 삭제");
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

        loginButton.addActionListener(e -> login());
        signupButton.addActionListener(e -> switchToSignup());
        registerButton.addActionListener(e -> register());
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

    private void switchToSignup() {
        CardLayout cl = (CardLayout) (authPanel.getLayout());
        cl.show(authPanel, "signup");
    }

    private void switchToLogin() {
        CardLayout cl = (CardLayout) (authPanel.getLayout());
        cl.show(authPanel, "login");
    }

    private void connectToServer() {
        try {
            socket = new Socket(serverAddress, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            new Thread(new Listener()).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "서버에 연결할 수 없습니다. UI는 그대로 표시합니다.");
        }
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

        ChatWindow chatWindow = new ChatWindow(loginID, chatRoomId, out);
        chatWindows.put(chatRoomId, chatWindow); // 맵에 등록
        chatWindow.setVisible(true);
    }

    private void login() {
        String id = loginUserField.getText().trim();
        String pw = new String(loginPassField.getPassword()).trim();
        if (id.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "로그인 ID와 비밀번호를 입력하세요.");
            return;
        }
        if (out != null) out.println("/login " + id + " " + pw);
    }

    private void register() {
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

        if (out != null) out.println("/signup " + id + " " + pw + " " + userName + " " + birthday + " " + nickname + " " + information);
    }

    private void sendFriendRequest() {
        String friendLoginID = JOptionPane.showInputDialog(frame, "친구의 로그인 ID를 입력하세요:");
        if (friendLoginID != null && !friendLoginID.trim().isEmpty()) {
            if (out != null) out.println("/addfriend " + friendLoginID.trim());
        }
    }

    private void acceptFriendRequest() {
        String selectedFriend = friendRequestsListUI.getSelectedValue();
        if (selectedFriend == null) {
            JOptionPane.showMessageDialog(frame, "수락할 친구 요청을 선택하세요.");
            return;
        }
        if (out != null) out.println("/acceptfriend " + selectedFriend);
        friendRequestsModel.removeElement(selectedFriend);
    }

    private void rejectFriendRequest() {
        String selectedFriend = friendRequestsListUI.getSelectedValue();
        if (selectedFriend == null) {
            JOptionPane.showMessageDialog(frame, "거절할 친구 요청을 선택하세요.");
            return;
        }
        if (out != null) out.println("/rejectfriend " + selectedFriend);
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
            if (out != null) out.println(sb.toString());
        }
    }

    private void addMemo() {
        String memoContent = JOptionPane.showInputDialog(frame, "추가할 메모 내용을 입력하세요:");
        if (memoContent != null && !memoContent.trim().isEmpty()) {
            memoContent = memoContent.replace(" ", "_");
            if (out != null) out.println("/addmemo " + memoContent.trim());
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
            if (out != null) out.println("/editmemo " + index + " " + newContent.trim());
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

        if (out != null) out.println("/deletememo " + index);
    }

    // ChatWindow 이너 클래스
    private class ChatWindow extends JFrame {
        private String chatRoomId;
        private PrintWriter out;
        private String loginID;

        private JTextPane chatArea = new JTextPane();
        private JScrollPane chatScrollPane = new JScrollPane(chatArea);
        private JTextField inputField = new JTextField(15);
        private JButton sendButton = new JButton("전송");
        private StringBuilder htmlContent = new StringBuilder("<html><body>");

        private boolean receivingHistory = false;
        private List<String> tempChatHistory = new ArrayList<>();

        public ChatWindow(String loginID, String chatRoomId, PrintWriter out) {
            this.loginID = loginID;
            this.chatRoomId = chatRoomId;
            this.out = out;

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
            if (out != null) {
                out.println("/getchathistory " + chatRoomId);
            }
        }

        private void sendMessage() {
            String message = inputField.getText().trim();
            if (message.isEmpty()) return;
            out.println("/chat " + chatRoomId + " " + loginID + " " + message);
            inputField.setText("");
        }

        public void handleChatMessage(String msg) {
            String[] tokens = msg.split(" ", 4);
            if (tokens.length != 4) return;
            String senderLoginID = tokens[2];
            String message = tokens[3];
            appendMessage(senderLoginID, message);
        }

        public void handleChatHistoryStart() {
            receivingHistory = true;
            tempChatHistory.clear();
        }

        public void handleChatHistoryEnd() {
            receivingHistory = false;
            for (String chat : tempChatHistory) {
                handleChatMessage(chat);
            }
            tempChatHistory.clear();
        }

        public void handleHistoryLine(String msg) {
            tempChatHistory.add(msg);
        }

        private void appendMessage(String senderLoginID, String message) {
            SwingUtilities.invokeLater(() -> {
                if (senderLoginID.equals(loginID)) {
                    htmlContent.append("<div style='text-align: right; color: green;'><b>")
                            .append("나")
                            .append("</b>: ")
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

    private class Listener implements Runnable {
        @Override
        public void run() {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println("서버 메시지: " + msg);

                    // 메시지를 파싱해 chatRoomId 추출하는 로직
                    if (msg.startsWith("/chathistorystart ")) {
                        String[] tokens = msg.split(" ", 2);
                        String chatRoomId = tokens[1];
                        ChatWindow cw = chatWindows.get(chatRoomId);
                        if (cw != null) {
                            cw.handleChatHistoryStart();
                        }
                    } else if (msg.startsWith("/chathistoryend ")) {
                        String[] tokens = msg.split(" ", 2);
                        String chatRoomId = tokens[1];
                        ChatWindow cw = chatWindows.get(chatRoomId);
                        if (cw != null) {
                            cw.handleChatHistoryEnd();
                        }
                    } else if (msg.startsWith("/chat ")) {
                        // /chat chatRoomId sender message
                        String[] tokens = msg.split(" ", 4);
                        if (tokens.length == 4) {
                            String chatRoomId = tokens[1];
                            ChatWindow cw = chatWindows.get(chatRoomId);
                            if (cw != null) {
                                // 히스토리 수신 중인지 판단하기 위해 토큰 4개 중 sender, message가 이미 있음
                                // 단순히 handleChatMessage 호출
                                // 만약 히스토리 수신 중이라면 handleHistoryLine으로 먼저 저장 후 end 시점에 표시하는 로직 필요
                                // 여기서는 단순히 바로 표시
                                cw.handleChatMessage(msg);
                            }
                        }
                    } else if (msg.startsWith("/login")) {
                        handleLoginResponse(msg);
                    } else if (msg.startsWith("/signup")) {
                        handleSignupResponse(msg);
                    } else if (msg.startsWith("/error")) {
                        handleError(msg);
                    } else if (msg.startsWith("/friends")) {
                        handleFriendsList(msg);
                    } else if (msg.startsWith("/friendrequest")) {
                        handleFriendRequest(msg);
                    } else if (msg.startsWith("/friendaccepted")) {
                        handleFriendAccepted(msg);
                    } else if (msg.startsWith("/friendrejected")) {
                        handleFriendRejected(msg);
                    } else if (msg.startsWith("/createchat ")) {
                        handleCreateChat(msg);
                    } else if (msg.startsWith("/createchat_success")) {
                        handleCreateChatSuccess(msg);
                    } else if (msg.startsWith("/logout")) {
                        handleLogoutResponse(msg);
                    } else if (msg.startsWith("/memosstart")) {
                        handleMemosStart(msg);
                    } else if (msg.startsWith("/memo ")) {
                        handleMemo(msg);
                    } else if (msg.startsWith("/memosend")) {
                        handleMemosEnd(msg);
                    } else if (msg.startsWith("/addmemo")) {
                        handleAddMemoResponse(msg);
                    } else if (msg.startsWith("/editmemo")) {
                        handleEditMemoResponse(msg);
                    } else if (msg.startsWith("/deletememo")) {
                        handleDeleteMemoResponse(msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "서버 연결이 끊어졌습니다.");
                });
            }
        }

        private void handleLoginResponse(String msg) {
            String[] tokens = msg.split(" ", 3);
            if (tokens.length < 2) return;
            if (tokens[1].equals("success")) {
                loginID = loginUserField.getText().trim();
                SwingUtilities.invokeLater(() -> {
                    cardLayout.show(mainPanel, "chat");
                });
            } else {
                String errorMsg = tokens.length >= 3 ? tokens[2] : "로그인 실패";
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "로그인 실패: " + errorMsg);
                });
            }
        }

        private void handleSignupResponse(String msg) {
            String[] tokens = msg.split(" ", 3);
            if (tokens.length < 2) return;
            if (tokens[1].equals("success")) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "회원가입 성공! 로그인하세요.");
                    switchToLogin();
                });
            } else {
                String errorMsg = tokens.length >= 3 ? tokens[2] : "회원가입 실패";
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "회원가입 실패: " + errorMsg);
                });
            }
        }

        private void handleError(String msg) {
            String errorMsg = msg.length() > 7 ? msg.substring(7) : "알 수 없는 오류";
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(frame, "오류: " + errorMsg);
            });
        }

        private void handleFriendsList(String msg) {
            String[] tokens = msg.split(" ");
            SwingUtilities.invokeLater(() -> {
                friendsListModel.clear();
                for (int i = 1; i < tokens.length; i++) {
                    friendsListModel.addElement(tokens[i]);
                }
            });
        }

        private void handleFriendRequest(String msg) {
            String[] tokens = msg.split(" ", 2);
            if (tokens.length != 2) return;
            String requesterLoginID = tokens[1];
            SwingUtilities.invokeLater(() -> {
                friendRequestsModel.addElement(requesterLoginID);
                JOptionPane.showMessageDialog(frame, requesterLoginID + "님이 친구 요청을 보냈습니다.");
            });
        }

        private void handleFriendAccepted(String msg) {
            String[] tokens = msg.split(" ", 2);
            if (tokens.length != 2) return;
            String accepterLoginID = tokens[1];
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(frame, accepterLoginID + "님이 친구 요청을 수락했습니다.");
            });
        }

        private void handleFriendRejected(String msg) {
            String[] tokens = msg.split(" ", 2);
            if (tokens.length != 2) return;
            String rejecterLoginID = tokens[1];
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(frame, rejecterLoginID + "님이 친구 요청을 거절했습니다.");
            });
        }

        private void handleCreateChat(String msg) {
            String[] tokens = msg.split(" ", 3);
            if (tokens.length != 3) return;
            String chatRoomId = tokens[1];
            String chatRoomName = tokens[2];
            String chatRoomDisplay = chatRoomName + " (ID: " + chatRoomId + ")";
            SwingUtilities.invokeLater(() -> {
                chatRoomsListModel.addElement(chatRoomDisplay);
            });
        }

        private void handleCreateChatSuccess(String msg) {
            String[] tokens = msg.split(" ", 2);
            if (tokens.length != 2) return;
            String successMsg = tokens[1];
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(frame, successMsg);
            });
        }

        private void handleLogoutResponse(String msg) {
            String[] tokens = msg.split(" ", 3);
            if (tokens.length < 2) return;
            if (tokens[1].equals("success")) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "로그아웃 되었습니다.");
                    cardLayout.show(mainPanel, "auth");
                    loginID = null;
                    friendsListModel.clear();
                    friendRequestsModel.clear();
                    chatRoomsListModel.clear();
                    memoListModel.clear();
                });
            } else {
                String errorMsg = tokens.length >= 3 ? tokens[2] : "로그아웃 실패";
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "로그아웃 실패: " + errorMsg);
                });
            }
        }

        private void handleMemosStart(String msg) {
            SwingUtilities.invokeLater(() -> {
                memoListModel.clear();
            });
        }

        private void handleMemo(String msg) {
            String[] tokens = msg.split(" ", 3);
            if (tokens.length != 3) return;
            String index = tokens[1];
            String memoContent = tokens[2].replace("_", " ");
            SwingUtilities.invokeLater(() -> {
                memoListModel.addElement("[" + index + "] " + memoContent);
            });
        }

        private void handleMemosEnd(String msg) {
        }

        private void handleAddMemoResponse(String msg) {
            if (msg.contains("success")) {
                if (out != null) out.println("/getmemos " + loginID);
            } else {
                JOptionPane.showMessageDialog(frame, "메모 추가 실패.");
            }
        }

        private void handleEditMemoResponse(String msg) {
            if (msg.contains("success")) {
                if (out != null) out.println("/getmemos " + loginID);
            } else {
                JOptionPane.showMessageDialog(frame, "메모 수정 실패.");
            }
        }

        private void handleDeleteMemoResponse(String msg) {
            if (msg.contains("success")) {
                if (out != null) out.println("/getmemos " + loginID);
            } else {
                JOptionPane.showMessageDialog(frame, "메모 삭제 실패.");
            }
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientApp::new);
    }
}
