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
        loginPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50)); // 여백 추가

        // ID 입력 필드
        JPanel idPanel = new JPanel();
        idPanel.setLayout(new BorderLayout());
        idPanel.setMaximumSize(new Dimension(200, 60)); // 크기 조정
        loginUserField.setFont(new Font("SansSerif", Font.PLAIN, 18)); // 폰트 크기 설정
        loginUserField.setPreferredSize(new Dimension(200, 40)); // 높이 조정
        idPanel.add(new JLabel("ID"), BorderLayout.NORTH);
        idPanel.add(loginUserField, BorderLayout.CENTER);

        // PW 입력 필드
        JPanel pwPanel = new JPanel();
        pwPanel.setLayout(new BorderLayout());
        pwPanel.setMaximumSize(new Dimension(200, 60)); // 크기 조정
        loginPassField.setFont(new Font("SansSerif", Font.PLAIN, 18)); // 폰트 크기 설정
        loginPassField.setPreferredSize(new Dimension(200, 40)); // 높이 조정
        pwPanel.add(new JLabel("PW"), BorderLayout.NORTH);
        pwPanel.add(loginPassField, BorderLayout.CENTER);

        // 버튼 패널
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // 가운데 정렬
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signupButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 버튼에 간격 추가
        buttonPanel.add(loginButton);
        buttonPanel.add(Box.createVerticalStrut(10)); // 버튼 간 간격
        buttonPanel.add(signupButton);

        // 패널에 컴포넌트 추가
        loginPanel.add(Box.createVerticalGlue()); // 상단 여백
        loginPanel.add(idPanel);
        loginPanel.add(Box.createVerticalStrut(20)); // ID와 PW 간격
        loginPanel.add(pwPanel);
        loginPanel.add(Box.createVerticalStrut(40)); // 필드와 버튼 간격
        loginPanel.add(buttonPanel);
        loginPanel.add(Box.createVerticalGlue()); // 하단 여백
    }

    private void createSignupPanel() {
        signupPanel.setLayout(new GridBagLayout()); // GridBagLayout으로 변경
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // 컴포넌트 간의 간격
        gbc.fill = GridBagConstraints.HORIZONTAL; // 컴포넌트 크기 조정

        // 제목 라벨
        JLabel signupTitle = new JLabel("회원가입", SwingConstants.CENTER);
        signupTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // 가로로 두 칸 차지
        signupPanel.add(signupTitle, gbc);

        // 제목과 입력 필드 간격 추가
        gbc.gridy++;
        gbc.insets = new Insets(10, 10, 10, 10); // 위쪽 간격을 늘림
        signupPanel.add(Box.createVerticalStrut(20), gbc);

        // 로그인 ID 필드
        gbc.gridwidth = 1; // 한 칸으로 복원
        gbc.gridy++;
        signupPanel.add(new JLabel("로그인 ID:"), gbc);
        gbc.gridx = 1;
        signupLoginIDField.setPreferredSize(new Dimension(200, 40)); // 크기 조정
        signupPanel.add(signupLoginIDField, gbc);

        // 비밀번호 필드
        gbc.gridx = 0;
        gbc.gridy++;
        signupPanel.add(new JLabel("비밀번호:"), gbc);
        gbc.gridx = 1;
        signupLoginPWField.setPreferredSize(new Dimension(200, 40));
        signupPanel.add(signupLoginPWField, gbc);

        // 사용자 이름 필드
        gbc.gridx = 0;
        gbc.gridy++;
        signupPanel.add(new JLabel("사용자 이름:"), gbc);
        gbc.gridx = 1;
        signupUserNameField.setPreferredSize(new Dimension(200, 40));
        signupPanel.add(signupUserNameField, gbc);

        // 생일 필드
        gbc.gridx = 0;
        gbc.gridy++;
        signupPanel.add(new JLabel("생일(YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        signupBirthdayField.setPreferredSize(new Dimension(200, 40));
        signupPanel.add(signupBirthdayField, gbc);

        // 닉네임 필드
        gbc.gridx = 0;
        gbc.gridy++;
        signupPanel.add(new JLabel("닉네임:"), gbc);
        gbc.gridx = 1;
        signupNicknameField.setPreferredSize(new Dimension(200, 40));
        signupPanel.add(signupNicknameField, gbc);

        // 정보 필드
        gbc.gridx = 0;
        gbc.gridy++;
        signupPanel.add(new JLabel("정보:"), gbc);
        gbc.gridx = 1;
        signupInformationArea.setLineWrap(true); // 텍스트 줄바꿈 설정
        signupInformationArea.setWrapStyleWord(true); // 단어 단위로 줄바꿈
        signupInformationArea.setPreferredSize(new Dimension(200, 100)); // 크기 키움
        signupPanel.add(new JScrollPane(signupInformationArea), gbc);

        // 회원가입 버튼
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2; // 가로로 두 칸 차지
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(30, 10, 5, 10); // 위쪽 간격 줄임 (기본 10 → 5)
        registerButton.setPreferredSize(new Dimension(200, 40)); // 크기 설정
        signupPanel.add(registerButton, gbc);

        // 뒤로가기 버튼
        gbc.gridy++; // 회원가입 완료 버튼 아래로 이동
        gbc.insets = new Insets(5, 10, 10, 10); // 위쪽 간격 줄임 (기본 10 → 5)
        JButton backButton = new JButton("취소");
        backButton.setPreferredSize(new Dimension(200, 40)); // 크기 설정
        backButton.addActionListener(e -> showLoginPanel()); // 로그인 창으로 이동
        signupPanel.add(backButton, gbc);
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
