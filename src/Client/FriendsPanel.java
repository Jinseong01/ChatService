package Client;

import Model.Friend;
import Model.User;

import javax.swing.*;
import java.awt.*;
import java.util.Base64;

public class FriendsPanel extends JPanel {
    private DefaultListModel<Friend> friendsListModel = new DefaultListModel<>();
    private JList<Friend> friendsListUI = new JList<>(friendsListModel);

    private DefaultListModel<Friend> friendRequestsModel = new DefaultListModel<>();
    private JList<Friend> friendRequestsListUI = new JList<>(friendRequestsModel);

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

        // 사용자 정보 패널 (이미지와 이름 및 상태 메시지)
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // 프로필 이미지 설정
        profileImageLabel.setPreferredSize(new Dimension(64, 64));
        userInfoPanel.add(profileImageLabel);

        // 이름과 상태 메시지를 하나의 패널에 추가
        JPanel nameStatusPanel = new JPanel();
        nameStatusPanel.setLayout(new BoxLayout(nameStatusPanel, BoxLayout.Y_AXIS));

        nameValueLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        statusMessageValueLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusMessageValueLabel.setForeground(Color.GRAY);

        nameStatusPanel.add(nameValueLabel);
        nameStatusPanel.add(statusMessageValueLabel);

        userInfoPanel.add(nameStatusPanel);

        // 상단 패널
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(bannerPanel);
        topPanel.add(userInfoPanel);

        // 친구 목록 패널
        JPanel friendsListPanel = new JPanel(new BorderLayout());
        friendsListPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        friendsListUI.setCellRenderer(new CustomFriendListCellRenderer());
        friendsListPanel.add(new JScrollPane(friendsListUI), BorderLayout.CENTER);

        // 친구 요청 라벨
        JLabel friendRequestLabel = new JLabel("친구 요청");
        friendRequestLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        friendRequestLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // 친구 요청 패널
        JPanel friendRequestsPanel = new JPanel(new BorderLayout());
        friendRequestsPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        friendRequestsListUI.setCellRenderer(new CustomFriendListCellRenderer());
        friendRequestsPanel.add(new JScrollPane(friendRequestsListUI), BorderLayout.CENTER);

        JPanel friendRequestButtons = new JPanel(new GridLayout(1, 2, 5, 5));
        friendRequestButtons.add(acceptFriendButton);
        friendRequestButtons.add(rejectFriendButton);

        friendRequestsPanel.add(friendRequestButtons, BorderLayout.SOUTH);

        // 라벨과 친구 요청 패널을 감싸는 새로운 패널
        JPanel friendRequestContainer = new JPanel();
        friendRequestContainer.setLayout(new BorderLayout());
        friendRequestContainer.add(friendRequestLabel, BorderLayout.NORTH);
        friendRequestContainer.add(friendRequestsPanel, BorderLayout.CENTER);

        // 전체 구성
        add(topPanel, BorderLayout.PAGE_START);
        add(friendsListPanel, BorderLayout.CENTER);
        add(friendRequestContainer, BorderLayout.SOUTH);
    }

    public DefaultListModel<Friend> getFriendsListModel() {
        return friendsListModel;
    }

    public DefaultListModel<Friend> getFriendRequestsModel() {
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

    public JList<Friend> getFriendsListUI() {
        return friendsListUI;
    }

    public JList<Friend> getFriendRequestsListUI() {
        return friendRequestsListUI;
    }

    public JLabel getProfileImageLabel() {
        return profileImageLabel;
    }

    public JLabel getStatusMessageValueLabel() {
        return statusMessageValueLabel;
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

    public void updateUserInfo(User loginUser) {
        SwingUtilities.invokeLater(() -> {
            nameValueLabel.setText(loginUser.getUserName());
            statusMessageValueLabel.setText(loginUser.getInformation());

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
    private static class CustomFriendListCellRenderer extends JPanel implements ListCellRenderer<Friend> {
        private JLabel nameLabel;
        private JLabel statusMessageLabel;
        private JLabel profileImageLabel;

        public CustomFriendListCellRenderer() {
            setLayout(new BorderLayout());
            setOpaque(true); // 배경색을 적용할 수 있도록 설정

            // 왼쪽: 이미지와 이름
            profileImageLabel = new JLabel();
            profileImageLabel.setPreferredSize(new Dimension(40, 40));

            nameLabel = new JLabel();
            nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));

            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            leftPanel.setOpaque(false); // 부모 배경색을 따름
            leftPanel.add(profileImageLabel);
            leftPanel.add(nameLabel);

            // 오른쪽: 상태 메시지
            statusMessageLabel = new JLabel();
            statusMessageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            statusMessageLabel.setForeground(Color.GRAY);
            statusMessageLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            statusMessageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // 오른쪽 여백 추가

            add(leftPanel, BorderLayout.WEST);
            add(statusMessageLabel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Friend> list, Friend friend, int index, boolean isSelected, boolean cellHasFocus) {
            // 이름 설정
            nameLabel.setText(friend.getUserName());

            // 상태 메시지 설정
            String statusMessage = friend.getInformation();
            statusMessageLabel.setText(statusMessage != null ? statusMessage : "");

            // 프로필 이미지 설정
            if (friend.getProfileImage() != null && !friend.getProfileImage().isEmpty()) {
                try {
                    byte[] imageBytes = Base64.getDecoder().decode(friend.getProfileImage());
                    ImageIcon profileIcon = new ImageIcon(imageBytes);
                    Image image = profileIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                    profileImageLabel.setIcon(new ImageIcon(image));
                } catch (IllegalArgumentException e) {
                    profileImageLabel.setIcon(null);
                }
            } else {
                profileImageLabel.setIcon(null);
            }

            // 선택된 항목의 배경색 설정
            if (isSelected) {
                setBackground(new Color(220, 240, 255)); // 선택된 항목 배경색
            } else {
                setBackground(Color.WHITE); // 기본 배경색
            }

            return this;
        }
    }
}
