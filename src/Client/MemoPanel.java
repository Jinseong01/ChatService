package Client;

import javax.swing.*;
import java.awt.*;

public class MemoPanel extends JPanel {
    private DefaultListModel<String> memoListModel = new DefaultListModel<>();
    private JList<String> memoListUI = new JList<>(memoListModel);
    private JButton addMemoButton = new JButton("메모 추가");
    private JButton editMemoButton = new JButton("메모 수정");
    private JButton deleteMemoButton = new JButton("메모 삭제");

    public MemoPanel() {
        setLayout(new BorderLayout());
        JPanel memoListPanel = new JPanel(new BorderLayout());
        memoListPanel.setBorder(BorderFactory.createTitledBorder("메모 목록"));
        memoListPanel.add(new JScrollPane(memoListUI), BorderLayout.CENTER);
        JPanel memoButtons = new JPanel(new GridLayout(1, 3, 5, 5));
        memoButtons.add(addMemoButton);
        memoButtons.add(editMemoButton);
        memoButtons.add(deleteMemoButton);
        memoListPanel.add(memoButtons, BorderLayout.SOUTH);

        add(memoListPanel, BorderLayout.CENTER);
    }

    public DefaultListModel<String> getMemoListModel() { return memoListModel; }
    public JList<String> getMemoListUI() { return memoListUI; }
    public JButton getAddMemoButton() { return addMemoButton; }
    public JButton getEditMemoButton() { return editMemoButton; }
    public JButton getDeleteMemoButton() { return deleteMemoButton; }
}
