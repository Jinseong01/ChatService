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
        JLabel bannerLabel = new JLabel("친구", SwingConstants.LEFT);
        bannerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        bannerLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));

        addFriendButton.setPreferredSize(new Dimension(40, 40));
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
        friendsListUI.setCellRenderer(new CustomListCellRenderer());
        friendsListPanel.add(new JScrollPane(friendsListUI), BorderLayout.CENTER);

        // 친구 요청 패널
        JPanel friendRequestsPanel = new JPanel(new BorderLayout());
        friendRequestsPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        friendRequestsPanel.add(new JScrollPane(friendRequestsListUI), BorderLayout.CENTER);

        JPanel friendRequestButtons = new JPanel(new GridLayout(1, 2, 5, 5));
        friendRequestButtons.add(acceptFriendButton);
        friendRequestButtons.add(rejectFriendButton);
        friendRequestsPanel.add(friendRequestButtons, BorderLayout.SOUTH);

        // 전체 구성
        add(topPanel, BorderLayout.PAGE_START);
        add(friendsListPanel, BorderLayout.CENTER);
        add(friendRequestsPanel, BorderLayout.SOUTH);
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

    private static class CustomListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setFont(new Font("SansSerif", Font.PLAIN, 16));
            label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
            label.setPreferredSize(new Dimension(label.getWidth(), 30));
            return label;
        }
    }
}
