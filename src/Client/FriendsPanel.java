package Client;

import javax.swing.*;
import java.awt.*;

public class FriendsPanel extends JPanel {
    private DefaultListModel<String> friendsListModel = new DefaultListModel<>();
    private JList<String> friendsListUI = new JList<>(friendsListModel);
    private JButton addFriendButton = new JButton("친구 추가");

    private DefaultListModel<String> friendRequestsModel = new DefaultListModel<>();
    private JList<String> friendRequestsListUI = new JList<>(friendRequestsModel);
    private JButton acceptFriendButton = new JButton("수락");
    private JButton rejectFriendButton = new JButton("거절");

    public FriendsPanel() {
        setLayout(new BorderLayout());
        JPanel friendsListPanel = new JPanel(new BorderLayout());
        friendsListPanel.setBorder(BorderFactory.createTitledBorder("친구 목록"));
        friendsListPanel.add(new JScrollPane(friendsListUI), BorderLayout.CENTER);
        friendsListPanel.add(addFriendButton, BorderLayout.SOUTH);

        JPanel friendRequestsPanel = new JPanel(new BorderLayout());
        friendRequestsPanel.setBorder(BorderFactory.createTitledBorder("친구 요청"));
        friendRequestsPanel.add(new JScrollPane(friendRequestsListUI), BorderLayout.CENTER);
        JPanel friendRequestButtons = new JPanel(new GridLayout(1, 2, 5, 5));
        friendRequestButtons.add(acceptFriendButton);
        friendRequestButtons.add(rejectFriendButton);
        friendRequestsPanel.add(friendRequestButtons, BorderLayout.SOUTH);

        add(friendsListPanel, BorderLayout.CENTER);
        add(friendRequestsPanel, BorderLayout.SOUTH);
    }

    public DefaultListModel<String> getFriendsListModel() { return friendsListModel; }
    public DefaultListModel<String> getFriendRequestsModel() { return friendRequestsModel; }

    public JButton getAddFriendButton() { return addFriendButton; }
    public JButton getAcceptFriendButton() { return acceptFriendButton; }
    public JButton getRejectFriendButton() { return rejectFriendButton; }

    public JList<String> getFriendsListUI() { return friendsListUI; }
    public JList<String> getFriendRequestsListUI() { return friendRequestsListUI; }
}
