package Client;

import Model.User;

import javax.swing.*;
import java.awt.*;

public class FriendsPanel extends JPanel {
    private DefaultListModel<String> friendsListModel = new DefaultListModel<>();
    private JList<String> friendsListUI = new JList<>(friendsListModel);

    private DefaultListModel<String> friendRequestsModel = new DefaultListModel<>();
    private JList<String> friendRequestsListUI = new JList<>(friendRequestsModel);

    private JButton addFriendButton = new JButton("+"); // 친구 추가 버튼
    private JButton acceptFriendButton = new JButton("수락"); // 친구 요청 수락 버튼
    private JButton rejectFriendButton = new JButton("거절"); // 친구 요청 거절 버튼

    // 사용자 정보 표시를 위한 레이블들
    private JLabel idValueLabel = new JLabel();
    private JLabel nameValueLabel = new JLabel();
    private JLabel birthdayValueLabel = new JLabel();
    private JLabel nicknameValueLabel = new JLabel();
    private JLabel statusMessageValueLabel = new JLabel();

    public FriendsPanel() {
        setLayout(new BorderLayout());

        // 상단 배너 패널
        JPanel bannerPanel = new JPanel(new BorderLayout());
        JLabel bannerLabel = new JLabel("친구", SwingConstants.LEFT); // 배너 제목
        bannerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        bannerLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));

        addFriendButton.setPreferredSize(new Dimension(40, 40)); // 버튼 크기 설정
        addFriendButton.setFocusable(false);

        bannerPanel.add(bannerLabel, BorderLayout.WEST);
        bannerPanel.add(addFriendButton, BorderLayout.EAST);
        bannerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 사용자 정보 패널
        JPanel userInfoPanel = new JPanel(new GridLayout(5, 2, 5, 5)); // 5행 2열, 가로/세로 간격 5px
        userInfoPanel.setBorder(BorderFactory.createTitledBorder("내 정보"));

        userInfoPanel.add(new JLabel("ID: "));
        userInfoPanel.add(idValueLabel);

        userInfoPanel.add(new JLabel("이름: "));
        userInfoPanel.add(nameValueLabel);

        userInfoPanel.add(new JLabel("생일: "));
        userInfoPanel.add(birthdayValueLabel);

        userInfoPanel.add(new JLabel("닉네임: "));
        userInfoPanel.add(nicknameValueLabel);

        userInfoPanel.add(new JLabel("상태 메시지: "));
        userInfoPanel.add(statusMessageValueLabel);

        // 상단 컨테이너 패널 생성
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(bannerPanel);
        topPanel.add(userInfoPanel);

        // 친구 목록 패널
        JPanel friendsListPanel = new JPanel(new BorderLayout());
        friendsListPanel.setBorder(BorderFactory.createTitledBorder("친구 목록"));

        // 친구 목록 UI에 커스텀 셀 렌더러 추가
        friendsListUI.setCellRenderer(new CustomListCellRenderer());
        friendsListPanel.add(new JScrollPane(friendsListUI), BorderLayout.CENTER);

        // 친구 요청 패널
        JPanel friendRequestsPanel = new JPanel(new BorderLayout());
        friendRequestsPanel.setBorder(BorderFactory.createTitledBorder("친구 요청"));
        friendRequestsPanel.add(new JScrollPane(friendRequestsListUI), BorderLayout.CENTER);

        JPanel friendRequestButtons = new JPanel(new GridLayout(1, 2, 5, 5));
        friendRequestButtons.add(acceptFriendButton);
        friendRequestButtons.add(rejectFriendButton);

        friendRequestsPanel.add(friendRequestButtons, BorderLayout.SOUTH);

        // 전체 구성
        add(topPanel, BorderLayout.PAGE_START); // 상단 컨테이너 패널 추가
        add(friendsListPanel, BorderLayout.CENTER); // 친구 목록 추가
        add(friendRequestsPanel, BorderLayout.SOUTH); // 친구 요청 추가
    }

    // Getter 메서드들
    public DefaultListModel<String> getFriendsListModel() {
        return friendsListModel;
    }

    public DefaultListModel<String> getFriendRequestsModel() {
        return friendRequestsModel;
    }

    public JButton getAddFriendButton() {
        return addFriendButton;
    }

    public JButton getAcceptFriendButton() {
        return acceptFriendButton;
    }

    public JButton getRejectFriendButton() {
        return rejectFriendButton;
    }

    public JList<String> getFriendsListUI() {
        return friendsListUI;
    }

    public JList<String> getFriendRequestsListUI() {
        return friendRequestsListUI;
    }

    // 사용자 정보 업데이트 메서드
    public void updateUserInfo(User loginUser) {
        SwingUtilities.invokeLater(() -> {
            idValueLabel.setText(loginUser.getLoginID());
            nameValueLabel.setText(loginUser.getUserName());
            birthdayValueLabel.setText(loginUser.getBirthday());
            nicknameValueLabel.setText(loginUser.getNickname());
            statusMessageValueLabel.setText(loginUser.getInformation());
        });
    }

    // 커스텀 셀 렌더러 클래스
    // 커스텀 셀 렌더러 클래스
    private static class CustomListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setFont(new Font("SansSerif", Font.PLAIN, 16)); // 폰트 크기 설정
            label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY)); // 경계선 추가
            label.setPreferredSize(new Dimension(label.getWidth(), 30)); // 높이 설정
            return label;
        }
    }
}
