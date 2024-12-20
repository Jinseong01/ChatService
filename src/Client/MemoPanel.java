package Client;

import javax.swing.*;
import java.awt.*;

public class MemoPanel extends JPanel {
    private DefaultListModel<String> memoListModel = new DefaultListModel<>();
    private JList<String> memoListUI = new JList<>(memoListModel);
    private JButton addMemoButton = new JButton("+"); // 메모 추가 버튼
    private JButton editMemoButton = new JButton("메모 수정");
    private JButton deleteMemoButton = new JButton("메모 삭제");

    // 레이아웃 및 UI 초기화
    public MemoPanel() {
        setLayout(new BorderLayout());

        // 상단 배너 패널 구성
        JPanel bannerPanel = new JPanel(new BorderLayout());
        JLabel bannerLabel = new JLabel("메모", SwingConstants.LEFT); // 배너 제목
        bannerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        bannerLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));

        addMemoButton.setPreferredSize(new Dimension(40, 40)); // 버튼 크기 설정
        addMemoButton.setFocusable(false);

        bannerPanel.add(bannerLabel, BorderLayout.WEST);
        bannerPanel.add(addMemoButton, BorderLayout.EAST);
        bannerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 메모 목록 패널
        JPanel memoListPanel = new JPanel(new BorderLayout());
        memoListPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        memoListPanel.add(new JScrollPane(memoListUI), BorderLayout.CENTER);

        // 메모 수정/삭제 버튼 패널
        JPanel memoButtons = new JPanel(new GridLayout(1, 2, 5, 5));
        memoButtons.add(editMemoButton);
        memoButtons.add(deleteMemoButton);
        memoListPanel.add(memoButtons, BorderLayout.SOUTH);

        // Custom Renderer 설정 (메모 내용만 표시)
        memoListUI.setCellRenderer(new MemoCellRenderer());

        // 전체 패널 구성
        add(bannerPanel, BorderLayout.PAGE_START); // 상단 배너 추가
        add(memoListPanel, BorderLayout.CENTER);  // 메모 목록 추가
    }

    // Getter 메서드들: 외부에서 모델, UI 컴포넌트 접근 가능
    public DefaultListModel<String> getMemoListModel() {
        return memoListModel;
    }

    public JList<String> getMemoListUI() {
        return memoListUI;
    }

    public JButton getAddMemoButton() {
        return addMemoButton;
    }

    public JButton getEditMemoButton() {
        return editMemoButton;
    }

    public JButton getDeleteMemoButton() {
        return deleteMemoButton;
    }

    // 메모 항목 Custom Cell Renderer 클래스
    private static class MemoCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // 기본 렌더러로 JLabel 생성
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String memo = value.toString();

            // "] " 이후의 문자열만 표시하여 인덱스 제거
            if (memo.contains("] ")) {
                memo = memo.substring(memo.indexOf("] ") + 2);
            }

            label.setText(memo);
            label.setFont(new Font("SansSerif", Font.PLAIN, 16));
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(10, 10, 10, 10), // 패딩 추가
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY) // 하단 경계선 추가
            ));
            label.setOpaque(true); // 배경색을 변경하려면 필요
            label.setBackground(isSelected ? new Color(220, 240, 255) : Color.WHITE);
            return label;
        }
    }
}
