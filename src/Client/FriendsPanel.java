package Client;

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

    public FriendsPanel() {
        setLayout(new BorderLayout());

        // 상단 배너
        JPanel bannerPanel = new JPanel(new BorderLayout());
        JLabel bannerLabel = new JLabel("친구", SwingConstants.LEFT); // 배너 제목
        bannerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        bannerLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));

        addFriendButton.setPreferredSize(new Dimension(40, 40)); // 버튼 크기 설정
        addFriendButton.setFocusable(false);

        bannerPanel.add(bannerLabel, BorderLayout.WEST);
        bannerPanel.add(addFriendButton, BorderLayout.EAST);
        bannerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 친구 목록 패널
        JPanel friendsListPanel = new JPanel(new BorderLayout());
        friendsListPanel.setBorder(BorderFactory.createTitledBorder("친구 목록"));
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
        add(bannerPanel, BorderLayout.PAGE_START); // 상단 배너 추가
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
}
