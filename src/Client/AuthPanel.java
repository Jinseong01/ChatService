package Client;

import javax.swing.*;
import java.awt.*;

public class AuthPanel extends JPanel {
    private CardLayout cardLayout = new CardLayout();
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

    public AuthPanel() {
        setLayout(cardLayout);
        createLoginPanel();
        createSignupPanel();
        add(loginPanel, "login");
        add(signupPanel, "signup");
    }

    private void createLoginPanel() {
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
    }

    private void createSignupPanel() {
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
    }

    private JPanel createFieldPanel(String labelText, JComponent field) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(labelText));
        panel.add(field);
        return panel;
    }

    public void showLoginPanel() {
        cardLayout.show(this, "login");
    }

    public void showSignupPanel() {
        cardLayout.show(this, "signup");
    }

    // Getters
    public JTextField getLoginUserField() { return loginUserField; }
    public JPasswordField getLoginPassField() { return loginPassField; }
    public JButton getLoginButton() { return loginButton; }
    public JButton getSignupButton() { return signupButton; }

    public JTextField getSignupLoginIDField() { return signupLoginIDField; }
    public JPasswordField getSignupLoginPWField() { return signupLoginPWField; }
    public JTextField getSignupUserNameField() { return signupUserNameField; }
    public JTextField getSignupBirthdayField() { return signupBirthdayField; }
    public JTextField getSignupNicknameField() { return signupNicknameField; }
    public JTextArea getSignupInformationArea() { return signupInformationArea; }
    public JButton getRegisterButton() { return registerButton; }
}
