package Client;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ClientUI {
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
    private JTabbedPane leftTabbedPane = new JTabbedPane(JTabbedPane.LEFT);  // 탭 위치 변경

    private JPanel friendsTab = new JPanel(new BorderLayout());
    private DefaultListModel<String> friendsListModel = new DefaultListModel<>();
    private JList<String> friendsListUI = new JList<>(friendsListModel);
    private JButton addFriendButton = new JButton("친구 추가");

    private JPanel chatTab = new JPanel(new BorderLayout());
    private DefaultListModel<String> chatRoomsListModel = new DefaultListModel<>();
    private JList<String> chatRoomsListUI = new JList<>(chatRoomsListModel);
    private JButton createChatButton = new JButton("채팅방 생성");

    private ClientHandler controller;

    public ClientUI(ClientHandler controller) {
        this.controller = controller;
        initializeUI();
        frame.setVisible(true);
    }

    public void initializeUI() {
        frame.setSize(400, 800);
        Font defaultFont = new Font("SansSerif", Font.PLAIN, 16);
        UIManager.put("Label.font", defaultFont);
        UIManager.put("Button.font", defaultFont);
        UIManager.put("TextField.font", defaultFont);
        UIManager.put("TextArea.font", defaultFont);
        UIManager.put("List.font", defaultFont);
        UIManager.put("TabbedPane.font", defaultFont);

        // 로그인 화면 구성
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

        // 회원가입 화면 구성
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

        // 채팅 화면 구성
        initializeFriendsTab();
        initializeChatTab();

        chatPanel.add(leftTabbedPane, BorderLayout.CENTER);
        mainPanel.add(chatPanel, "chat");

        frame.getContentPane().add(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 로그인, 회원가입 버튼 액션 리스너
        loginButton.addActionListener(e -> controller.login(loginUserField.getText(), new String(loginPassField.getPassword())));
        signupButton.addActionListener(e -> controller.switchToSignup());
        registerButton.addActionListener(e -> controller.register(
                signupLoginIDField.getText(),
                new String(signupLoginPWField.getPassword()),
                signupUserNameField.getText(),
                signupBirthdayField.getText(),
                signupNicknameField.getText(),
                signupInformationArea.getText()
        ));
    }

    private JPanel createFieldPanel(String labelText, JComponent field) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(labelText));
        panel.add(field);
        return panel;
    }

    private void initializeFriendsTab() {
        // 친구 목록 화면
        JPanel friendsListPanel = new JPanel(new BorderLayout());
        friendsListPanel.setBorder(BorderFactory.createTitledBorder("친구 목록"));
        friendsListPanel.add(new JScrollPane(friendsListUI), BorderLayout.CENTER);
        friendsListPanel.add(addFriendButton, BorderLayout.SOUTH);
        friendsTab.add(friendsListPanel, BorderLayout.CENTER);

        // 친구 탭에 추가
        leftTabbedPane.addTab("친구", friendsTab);
    }

    private void initializeChatTab() {
        // 채팅방 목록 화면
        JPanel chatRoomsPanel = new JPanel(new BorderLayout());
        chatRoomsPanel.setBorder(BorderFactory.createTitledBorder("채팅방 목록"));
        chatRoomsPanel.add(new JScrollPane(chatRoomsListUI), BorderLayout.CENTER);
        chatRoomsPanel.add(createChatButton, BorderLayout.SOUTH);
        chatTab.add(chatRoomsPanel, BorderLayout.CENTER);

        // 채팅 탭에 추가
        leftTabbedPane.addTab("채팅", chatTab);
    }

    public void showLoginUI() {
        cardLayout.show(mainPanel, "login");
    }

    public void showChatUI() {
        cardLayout.show(mainPanel, "chat");
    }

    public void updateFriendsList(List<String> friends) {
        friendsListModel.clear();
        for (String friend : friends) {
            friendsListModel.addElement(friend);
        }
    }

    public void updateChatRooms(List<String> chatRooms) {
        chatRoomsListModel.clear();
        for (String chatRoom : chatRooms) {
            chatRoomsListModel.addElement(chatRoom);
        }
    }

    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    public void switchToSignup() {
        CardLayout cl = (CardLayout)(authPanel.getLayout());
        cl.show(authPanel, "signup");
    }
}
