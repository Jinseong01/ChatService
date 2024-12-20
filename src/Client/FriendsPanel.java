package Client;

import Model.UserSummary;
import Model.User;

import javax.swing.*;
import java.awt.*;
import java.util.Base64;

public class FriendsPanel extends JPanel {
    private DefaultListModel<UserSummary> friendsListModel = new DefaultListModel<>();
    private JList<UserSummary> friendsListUI = new JList<>(friendsListModel);

    private DefaultListModel<UserSummary> friendRequestsModel = new DefaultListModel<>();
    private JList<UserSummary> friendRequestsListUI = new JList<>(friendRequestsModel);

    private JButton addFriendButton = new JButton("+"); // 친구 추가 버튼
    private JButton acceptFriendButton = new JButton("수락"); // 친구 요청 수락 버튼
    private JButton rejectFriendButton = new JButton("거절"); // 친구 요청 거절 버튼

    // 사용자 정보 표시를 위한 컴포넌트
    private JLabel profileImageLabel = new JLabel();
    private JLabel nameValueLabel = new JLabel();
    private JLabel statusMessageValueLabel = new JLabel();

    // 패널 레이아웃 및 UI 컴포넌트 초기화
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

        // 사용자 정보 패널 (프로필 이미지, 이름, 상태 메시지)
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        profileImageLabel.setPreferredSize(new Dimension(64, 64));
        userInfoPanel.add(profileImageLabel);

        JPanel nameStatusPanel = new JPanel();
        nameStatusPanel.setLayout(new BoxLayout(nameStatusPanel, BoxLayout.Y_AXIS));

        nameValueLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        statusMessageValueLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusMessageValueLabel.setForeground(Color.GRAY);

        nameStatusPanel.add(nameValueLabel);
        nameStatusPanel.add(statusMessageValueLabel);

        userInfoPanel.add(nameStatusPanel);

        // 상단 패널(배너+사용자정보)
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

        // 친구 요청 처리 버튼 패널 (수락/거절)
        JPanel friendRequestButtons = new JPanel(new GridLayout(1, 2, 5, 5));
        friendRequestButtons.add(acceptFriendButton);
        friendRequestButtons.add(rejectFriendButton);
        friendRequestsPanel.add(friendRequestButtons, BorderLayout.SOUTH);

        // 친구 요청 레이블과 요청 패널을 담는 컨테이너
        JPanel friendRequestContainer = new JPanel();
        friendRequestContainer.setLayout(new BorderLayout());
        friendRequestContainer.add(friendRequestLabel, BorderLayout.NORTH);
        friendRequestContainer.add(friendRequestsPanel, BorderLayout.CENTER);

        // 전체 구성
        add(topPanel, BorderLayout.PAGE_START); // 상단(배너+유저정보)
        add(friendsListPanel, BorderLayout.CENTER); // 친구 목록
        add(friendRequestContainer, BorderLayout.SOUTH); // 친구 요청 부분
    }

    // Getter 메서드들: 외부에서 모델이나 UI 컴포넌트에 접근할 수 있도록 제공
    public DefaultListModel<UserSummary> getFriendsListModel() {
        return friendsListModel;
    }

    public DefaultListModel<UserSummary> getFriendRequestsModel() {
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

    public JList<UserSummary> getFriendsListUI() {
        return friendsListUI;
    }

    public JList<UserSummary> getFriendRequestsListUI() {
        return friendRequestsListUI;
    }

    public JLabel getProfileImageLabel() {
        return profileImageLabel;
    }

    public JLabel getStatusMessageValueLabel() {
        return statusMessageValueLabel;
    }

    // Base64 문자열을 디코딩한 뒤 ImageIcon으로 변환
    // 프로필 이미지를 표시할 때 사용
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

    // 사용자 정보(이름, 상태 메시지, 프로필 이미지)를 업데이트
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

    // 친구 목록 및 친구 요청 목록의 항목을 렌더링하는 Custom Cell Renderer 클래스
    private static class CustomFriendListCellRenderer extends JPanel implements ListCellRenderer<UserSummary> {
        private JLabel nameLabel;
        private JLabel statusMessageLabel;
        private JLabel profileImageLabel;

        public CustomFriendListCellRenderer() {
            setLayout(new BorderLayout());
            setOpaque(true);

            profileImageLabel = new JLabel();
            profileImageLabel.setPreferredSize(new Dimension(40, 40));

            nameLabel = new JLabel();
            nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));

            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            leftPanel.setOpaque(false); // 배경색 투명 처리(상위 배경 사용)
            leftPanel.add(profileImageLabel);
            leftPanel.add(nameLabel);

            statusMessageLabel = new JLabel();
            statusMessageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            statusMessageLabel.setForeground(Color.GRAY);
            statusMessageLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            statusMessageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

            add(leftPanel, BorderLayout.WEST);
            add(statusMessageLabel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends UserSummary> list, UserSummary userSummary, int index, boolean isSelected, boolean cellHasFocus) {
            // 이름 설정
            nameLabel.setText(userSummary.getUserName());

            // 상태 메시지 설정
            String statusMessage = userSummary.getInformation();
            statusMessageLabel.setText(statusMessage != null ? statusMessage : "");

            // 프로필 이미지 설정 (Base64 -> ImageIcon)
            if (userSummary.getProfileImage() != null && !userSummary.getProfileImage().isEmpty()) {
                try {
                    byte[] imageBytes = Base64.getDecoder().decode(userSummary.getProfileImage());
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
