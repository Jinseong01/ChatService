package Client;

import javax.swing.*;
import java.awt.*;

public class AdditionalOptionsWindow extends JFrame {
    public AdditionalOptionsWindow() {
        setTitle("추가 옵션");
        setSize(300, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // 창을 화면 중앙에 위치

        // 메인 패널 설정
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 상단: 이미지, 이모티콘, 메모 버튼
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        JButton imageButton = new JButton("이미지");
        JButton emojiButton = new JButton("이모티콘");
        JButton memoButton = new JButton("메모");

        buttonPanel.add(imageButton);
        buttonPanel.add(emojiButton);
        buttonPanel.add(memoButton);

        // 중앙: 동작 미리보기 영역 (예: 이미지 업로드, 이모티콘 선택, 메모 작성)
        JPanel previewPanel = new JPanel();
        previewPanel.setBorder(BorderFactory.createTitledBorder("미리보기"));
        JLabel previewLabel = new JLabel("여기에 미리보기가 표시됩니다.");
        previewPanel.add(previewLabel);

        // 하단: "선택" 버튼
        JButton selectButton = new JButton("선택");

        // 메인 패널 구성
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(previewPanel, BorderLayout.CENTER);
        mainPanel.add(selectButton, BorderLayout.SOUTH);

        // 프레임에 메인 패널 추가
        add(mainPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AdditionalOptionsWindow().setVisible(true);
        });
    }
}
