package Client;

import javax.swing.*;
import java.awt.*;

public class MemoPanel extends JPanel {
    private DefaultListModel<String> memoListModel = new DefaultListModel<>();
    private JList<String> memoListUI = new JList<>(memoListModel);
    private JButton addMemoButton = new JButton("+"); // 메모 추가 버튼
    private JButton editMemoButton = new JButton("메모 수정");
    private JButton deleteMemoButton = new JButton("메모 삭제");

    public MemoPanel() {
        setLayout(new BorderLayout());

        // 상단 배너
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
        memoListPanel.setBorder(BorderFactory.createTitledBorder("메모 목록"));
        memoListPanel.add(new JScrollPane(memoListUI), BorderLayout.CENTER);

        // 메모 관리 버튼
        JPanel memoButtons = new JPanel(new GridLayout(1, 2, 5, 5));
        memoButtons.add(editMemoButton);
        memoButtons.add(deleteMemoButton);
        memoListPanel.add(memoButtons, BorderLayout.SOUTH);

        // 전체 구성
        add(bannerPanel, BorderLayout.PAGE_START); // 상단 배너 추가
        add(memoListPanel, BorderLayout.CENTER);  // 메모 목록 추가
    }

    // Getter 메서드들
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
}
