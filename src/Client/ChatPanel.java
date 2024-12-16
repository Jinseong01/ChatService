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
        chatRoomsPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // Custom Renderer 설정
        chatRoomsListUI.setCellRenderer(new ChatRoomCellRenderer());
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

    // Custom Cell Renderer 클래스
    private static class ChatRoomCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            // 채팅방 이름 추출
            String chatRoomName = value.toString();

            // 스타일 변경
            label.setText(chatRoomName); // 이름만 표시
            label.setFont(new Font("SansSerif", Font.PLAIN, 16)); // 폰트 크기 설정
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(10, 10, 10, 10), // 패딩 추가
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY) // 경계선 추가
            ));
            label.setOpaque(true); // 배경색 활성화
            label.setBackground(isSelected ? new Color(220, 240, 255) : Color.WHITE); // 선택된 항목 배경색
            return label;
        }
    }
}
