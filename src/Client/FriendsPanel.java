package Client;

import Model.User;

import javax.swing.*;
import java.awt.*;
import java.util.Base64;

public class FriendsPanel extends JPanel {
    private DefaultListModel<String> friendsListModel = new DefaultListModel<>();
    private JList<String> friendsListUI = new JList<>(friendsListModel);

    private DefaultListModel<String> friendRequestsModel = new DefaultListModel<>();
    private JList<String> friendRequestsListUI = new JList<>(friendRequestsModel);

    private JButton addFriendButton = new JButton("+"); // 친구 추가 버튼
    private JButton acceptFriendButton = new JButton("수락"); // 친구 요청 수락 버튼
    private JButton rejectFriendButton = new JButton("거절"); // 친구 요청 거절 버튼

    // 사용자 정보 표시를 위한 컴포넌트
    private JLabel profileImageLabel = new JLabel();
    private JLabel nameValueLabel = new JLabel();
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

        // 사용자 정보 패널 구성
        // 첫 번째 줄: 프로필 이미지 + 이름
        JPanel panelOne = new JPanel(new BorderLayout()); // BorderLayout을 사용해 프로필 이미지와 이름을 수평으로 배치

        // 프로필 이미지와 이름을 왼쪽 정렬로 배치
        profileImageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 프로필 이미지와 이름 사이에 공백을 추가
        profileImageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // 이미지와 이름 사이 공백

        // 프로필 이미지와 이름을 BorderLayout으로 배치
        panelOne.add(profileImageLabel, BorderLayout.WEST); // 프로필 이미지를 왼쪽에 배치
        panelOne.add(nameValueLabel, BorderLayout.CENTER); // 이름은 프로필 이미지 옆에 배치

        // 두 번째 줄: 상태 메시지
        JPanel panelTwo = new JPanel(new BorderLayout());
        panelTwo.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        panelTwo.add(statusMessageValueLabel, BorderLayout.CENTER);

        // 전체 유저 정보 패널
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setBorder(BorderFactory.createTitledBorder("내 정보"));

        // panelOne을 유저 정보 패널에 추가
        userInfoPanel.add(panelOne);
        // panelTwo를 유저 정보 패널에 추가
        userInfoPanel.add(panelTwo);


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

    private ImageIcon base64ToImageIcon(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) return null;
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            ImageIcon icon = new ImageIcon(imageBytes);

            // 필요하다면 아이콘 크기 조정
            Image image = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            return new ImageIcon(image);
        } catch (IllegalArgumentException e) {
            System.err.println("Base64 디코딩 실패: " + e.getMessage());
            return null;
        }
    }

    // 사용자 정보 업데이트 메서드
    public void updateUserInfo(User loginUser) {
        SwingUtilities.invokeLater(() -> {
            nameValueLabel.setText(loginUser.getUserName());
            statusMessageValueLabel.setText(loginUser.getInformation());

            // 프로필 이미지 표시
            ImageIcon profileIcon = base64ToImageIcon(loginUser.getProfileImage());
            if (profileIcon != null) {
                profileImageLabel.setIcon(profileIcon);
                profileImageLabel.setText("");
            } else {
                profileImageLabel.setIcon(null);
                profileImageLabel.setText("이미지를 불러올 수 없습니다.");
            }
        });
    }

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
