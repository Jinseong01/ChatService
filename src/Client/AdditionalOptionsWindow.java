package Client;

import javax.swing.*;
import java.awt.*;

public class AdditionalOptionsWindow extends JFrame {
    private JTextArea memoTextArea; // 메모 입력창
    private JLabel previewLabel; // 기본 미리보기 라벨
    private JButton selectButton; // 하단 버튼 (공용)
    private ClientHandler clientHandler; // ClientHandler 참조

    public AdditionalOptionsWindow(ClientHandler handler) {
        this.clientHandler = handler; // ClientHandler 연결

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

        // 중앙: 미리보기/입력 영역
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder("미리보기"));
        previewLabel = new JLabel("여기에 미리보기가 표시됩니다.", SwingConstants.CENTER);
        memoTextArea = new JTextArea(5, 20);
        memoTextArea.setVisible(false); // 초기에는 숨김
        previewPanel.add(previewLabel, BorderLayout.CENTER);

        // 하단: 선택/저장 버튼
        selectButton = new JButton("선택");

        // 메인 패널 구성
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(previewPanel, BorderLayout.CENTER);
        mainPanel.add(selectButton, BorderLayout.SOUTH);

        // 버튼 클릭 이벤트 설정
        setButtonActions(imageButton, emojiButton, memoButton, previewPanel);

        // 프레임에 메인 패널 추가
        add(mainPanel);
    }

    private void setButtonActions(JButton imageButton, JButton emojiButton, JButton memoButton, JPanel previewPanel) {
        // 이미지 버튼 클릭 시
        imageButton.addActionListener(e -> {
            previewLabel.setText("이미지 업로드 기능 선택됨");
            memoTextArea.setVisible(false); // 메모 입력창 숨기기
            previewLabel.setVisible(true);

            selectButton.setText("업로드");
            selectButton.addActionListener(uploadEvent -> {
                JOptionPane.showMessageDialog(this, "이미지 업로드 기능은 아직 구현되지 않았습니다.");
            });
        });

        // 이모티콘 버튼 클릭 시
        emojiButton.addActionListener(e -> {
            previewLabel.setText("이모티콘 선택 기능 선택됨");
            memoTextArea.setVisible(false); // 메모 입력창 숨기기
            previewLabel.setVisible(true);

            selectButton.setText("선택");
            selectButton.addActionListener(selectEvent -> {
                JOptionPane.showMessageDialog(this, "이모티콘 선택 기능은 아직 구현되지 않았습니다.");
            });
        });

        // 메모 버튼 클릭 시
        memoButton.addActionListener(e -> {
            previewPanel.remove(previewLabel); // 기존 미리보기 라벨 제거
            previewPanel.add(memoTextArea, BorderLayout.CENTER); // 메모 입력창 추가
            memoTextArea.setVisible(true);
            previewLabel.setVisible(false);

            selectButton.setText("저장");
            selectButton.addActionListener(saveEvent -> {
                String memoContent = memoTextArea.getText().trim();
                if (memoContent.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "메모 내용을 입력하세요.", "오류", JOptionPane.WARNING_MESSAGE);
                } else {
                    if (clientHandler != null) {
                        clientHandler.sendMessage("/addmemo " + memoContent.replace(" ", "_"));
                        JOptionPane.showMessageDialog(this, "메모가 저장되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "서버 연결이 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    }
                    // 입력 필드 초기화 및 창 닫기
                    memoTextArea.setText("");
                    dispose();
                }
            });
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 테스트용 (ClientHandler 없이)
            new AdditionalOptionsWindow(null).setVisible(true);
        });
    }
}
