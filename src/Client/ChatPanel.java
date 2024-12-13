package Client;

import javax.swing.*;
import java.awt.*;

public class ChatPanel extends JPanel {
    private DefaultListModel<String> chatRoomsListModel = new DefaultListModel<>();
    private JList<String> chatRoomsListUI = new JList<>(chatRoomsListModel);
    private JButton createChatButton = new JButton("채팅방 생성");

    public ChatPanel() {
        setLayout(new BorderLayout());
        JPanel chatRoomsPanel = new JPanel(new BorderLayout());
        chatRoomsPanel.setBorder(BorderFactory.createTitledBorder("채팅방 목록"));
        chatRoomsPanel.add(new JScrollPane(chatRoomsListUI), BorderLayout.CENTER);
        chatRoomsPanel.add(createChatButton, BorderLayout.SOUTH);

        add(chatRoomsPanel, BorderLayout.CENTER);
    }

    public DefaultListModel<String> getChatRoomsListModel() { return chatRoomsListModel; }
    public JList<String> getChatRoomsListUI() { return chatRoomsListUI; }
    public JButton getCreateChatButton() { return createChatButton; }
}
