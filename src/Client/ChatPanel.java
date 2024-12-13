package Client;

import javax.swing.*;
import java.awt.*;

public class ChatPanel extends JPanel {
    private DefaultListModel<String> chatRoomsListModel = new DefaultListModel<>();
    private JList<String> chatRoomsListUI = new JList<>(chatRoomsListModel);
    private JButton createChatButton = new JButton("+"); // 채팅방 생성 버튼

    public ChatPanel() {
        setLayout(new BorderLayout());

        // 상단 배너
        JPanel bannerPanel = new JPanel(new BorderLayout());
        JLabel bannerLabel = new JLabel("채팅", SwingConstants.LEFT); // 배너 제목
        bannerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        bannerLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));

        createChatButton.setPreferredSize(new Dimension(40, 40)); // 버튼 크기 설정
        createChatButton.setFocusable(false);

        bannerPanel.add(bannerLabel, BorderLayout.WEST);
        bannerPanel.add(createChatButton, BorderLayout.EAST);
        bannerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 채팅방 목록 패널
        JPanel chatRoomsPanel = new JPanel(new BorderLayout());
        chatRoomsPanel.setBorder(BorderFactory.createTitledBorder("채팅방 목록"));
        chatRoomsPanel.add(new JScrollPane(chatRoomsListUI), BorderLayout.CENTER);

        // 전체 구성
        add(bannerPanel, BorderLayout.PAGE_START); // 상단 배너 추가
        add(chatRoomsPanel, BorderLayout.CENTER);  // 채팅방 목록 추가
    }

    // Getter 메서드들
    public DefaultListModel<String> getChatRoomsListModel() {
        return chatRoomsListModel;
    }

    public JList<String> getChatRoomsListUI() {
        return chatRoomsListUI;
    }

    public JButton getCreateChatButton() {
        return createChatButton;
    }
}
