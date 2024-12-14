package Client;

import javax.swing.*;
import java.awt.*;

public class AuthPanel extends JPanel {
    private static final Dimension FIELD_DIMENSION = new Dimension(200, 40); // 텍스트 필드와 버튼 높이 설정
    private CardLayout cardLayout = new CardLayout();
    private JPanel loginPanel = new JPanel();
    private JPanel signupPanel = new JPanel();

    private JTextField loginUserField = createSizedTextField();
    private JPasswordField loginPassField = createSizedPasswordField();
    private JButton loginButton = createSizedButton("로그인");
    private JButton signupButton = createSizedButton("회원가입");

    private JTextField signupLoginIDField = createSizedTextField();
    private JPasswordField signupLoginPWField = createSizedPasswordField();
    private JTextField signupUserNameField = createSizedTextField();
    private JTextField signupBirthdayField = createSizedTextField();
    private JTextField signupNicknameField = createSizedTextField();
    private JTextArea signupInformationArea = new JTextArea(3, 15);
    private JButton registerButton = createSizedButton("회원가입 완료");

    public AuthPanel() {
        setLayout(cardLayout);
        createLoginPanel();
        createSignupPanel();
        add(loginPanel, "login");
        add(signupPanel, "signup");
    }

    private void createLoginPanel() {
        loginPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = createGbc();

        // 제목
        addComponent(loginPanel, gbc, 0, 0, 2, new JLabel("로그인", SwingConstants.CENTER), 20);

        // ID와 PW 필드
        addField(loginPanel, gbc, "ID:", loginUserField, 1);
        addField(loginPanel, gbc, "PW:", loginPassField, 2);

        // 버튼
        addComponent(loginPanel, gbc, 0, 3, 2, loginButton, 30);
        addComponent(loginPanel, gbc, 0, 4, 2, signupButton, 10);

        signupButton.addActionListener(e -> showSignupPanel());
    }

    private void createSignupPanel() {
        signupPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = createGbc();

        // 제목
        addComponent(signupPanel, gbc, 0, 0, 2, new JLabel("회원가입", SwingConstants.CENTER), 20);

        // 입력 필드
        addField(signupPanel, gbc, "로그인 ID:", signupLoginIDField, 1);
        addField(signupPanel, gbc, "비밀번호:", signupLoginPWField, 2);
        addField(signupPanel, gbc, "사용자 이름:", signupUserNameField, 3);
        addField(signupPanel, gbc, "생일(YYYY-MM-DD):", signupBirthdayField, 4);
        addField(signupPanel, gbc, "닉네임:", signupNicknameField, 5);

        // 정보 텍스트 영역
        addComponent(signupPanel, gbc, 0, 6, 2, new JScrollPane(signupInformationArea), 10);
        signupInformationArea.setLineWrap(true);
        signupInformationArea.setWrapStyleWord(true);

        // 버튼
        addComponent(signupPanel, gbc, 0, 7, 2, registerButton, 30);
        JButton backButton = createSizedButton("취소");
        backButton.addActionListener(e -> showLoginPanel());
        addComponent(signupPanel, gbc, 0, 8, 2, backButton, 10);
    }

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field, int yPosition) {
        gbc.gridx = 0;
        gbc.gridy = yPosition;
        gbc.gridwidth = 1;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void addComponent(JPanel panel, GridBagConstraints gbc, int x, int y, int width, JComponent component, int topPadding) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.insets = new Insets(topPadding, 10, 10, 10);
        panel.add(component, gbc);
    }

    private static JTextField createSizedTextField() {
        JTextField textField = new JTextField(15);
        textField.setPreferredSize(FIELD_DIMENSION);
        return textField;
    }

    private static JPasswordField createSizedPasswordField() {
        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setPreferredSize(FIELD_DIMENSION);
        return passwordField;
    }

    private static JButton createSizedButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(FIELD_DIMENSION);
        return button;
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
