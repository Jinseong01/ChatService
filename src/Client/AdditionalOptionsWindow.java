package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AdditionalOptionsWindow extends JFrame {
    private JTextArea memoTextArea; // 메모 입력창
    private JLabel previewLabel; // 기본 미리보기 라벨
    private JButton selectButton; // 하단 버튼 (공용)
    private ClientHandler clientHandler; // ClientHandler 참조
    private String chatRoomId; // 채팅방 ID

    // 이모티콘 디렉토리 설정
    private static final String EMOJI_DIRECTORY = "src/Resources/emojis";
    private JLabel selectedEmojiLabel; // 선택된 이모티콘

    public AdditionalOptionsWindow(ClientHandler handler, String chatRoomId) {
        this.clientHandler = handler; // ClientHandler 연결
        this.chatRoomId = chatRoomId;

        setTitle("추가 옵션");
        setSize(400, 500);
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
            resetPreview(); // 기존 상태 초기화
            previewLabel.setText("이미지 업로드 기능 선택됨");
            previewLabel.setVisible(true);
            memoTextArea.setVisible(false);

            replaceButtonAction(selectButton, sendEvent -> {
                if (selectedEmojiLabel != null && selectedEmojiLabel.getName() != null) {
                    File emojiFile = new File(selectedEmojiLabel.getName());

                    if (!emojiFile.exists()) {
                        JOptionPane.showMessageDialog(this, "선택한 이모티콘 파일이 없습니다.", "오류", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // 서버로 전송
                    clientHandler.sendEmoji(chatRoomId, selectedEmojiLabel.getName());

                    JOptionPane.showMessageDialog(this, "이모티콘이 전송되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "이모티콘을 선택하세요.", "오류", JOptionPane.WARNING_MESSAGE);
                }
            });
        });

        // 이모티콘 버튼 클릭 시
        emojiButton.addActionListener(e -> {
            resetPreview(); // 기존 상태 초기화
            previewLabel.setVisible(false);
            memoTextArea.setVisible(false);

            // 이모티콘 선택 패널
            JPanel emojiPanel = new JPanel(new GridLayout(2, 2, 10, 10));
            List<JLabel> emojiLabels = new ArrayList<>();
            File emojiDir = new File(EMOJI_DIRECTORY);

            if (emojiDir.exists() && emojiDir.isDirectory()) {
                File[] emojiFiles = emojiDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

                if (emojiFiles != null && emojiFiles.length > 0) {
                    for (File emojiFile : emojiFiles) {
                        ImageIcon emojiIcon = new ImageIcon(emojiFile.getAbsolutePath());
                        Image scaledImage = emojiIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                        JLabel emojiLabel = new JLabel(new ImageIcon(scaledImage));
                        emojiLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                        emojiPanel.add(emojiLabel);
                        emojiLabels.add(emojiLabel);

                        emojiLabel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                // 모든 라벨의 강조 표시 초기화
                                for (JLabel label : emojiLabels) {
                                    label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                                }
                                // 선택된 라벨 강조 표시
                                emojiLabel.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
                                selectedEmojiLabel = emojiLabel;

                                // 선택된 이모티콘 파일의 경로를 저장
                                selectedEmojiLabel.setName(emojiFile.getAbsolutePath()); // 절대 경로 설정
                                System.out.println("선택된 이모티콘 경로: " + emojiFile.getAbsolutePath()); // 디버깅 출력
                            }
                        });
                    }

                    previewPanel.removeAll();
                    previewPanel.add(new JScrollPane(emojiPanel), BorderLayout.CENTER);
                    previewPanel.revalidate();
                    previewPanel.repaint();

                    selectButton.setText("전송");
                    replaceButtonAction(selectButton, sendEvent -> {
                        if (selectedEmojiLabel != null && selectedEmojiLabel.getName() != null) {
                            File emojiFile = new File(selectedEmojiLabel.getName());

                            System.out.println("이모티콘 파일 경로: " + emojiFile.getAbsolutePath()); // 디버깅 출력

                            if (!emojiFile.exists()) {
                                JOptionPane.showMessageDialog(this, "선택한 이모티콘 파일이 없습니다.", "오류", JOptionPane.WARNING_MESSAGE);
                                return;
                            }

                            // 서버로 전송
                            clientHandler.sendEmoji(chatRoomId, emojiFile.getName());

                            JOptionPane.showMessageDialog(this, "이모티콘이 전송되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(this, "이모티콘을 선택하세요.", "오류", JOptionPane.WARNING_MESSAGE);
                        }
                    });
                } else {
                    JOptionPane.showMessageDialog(this, "이모티콘 파일이 없습니다.", "오류", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "이모티콘 디렉토리가 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 메모 버튼 클릭 시
        memoButton.addActionListener(e -> {
            resetPreview(); // 기존 상태 초기화
            previewPanel.remove(previewLabel); // 기존 미리보기 라벨 제거
            previewPanel.add(memoTextArea, BorderLayout.CENTER); // 메모 입력창 추가
            memoTextArea.setVisible(true);

            selectButton.setText("저장");
            replaceButtonAction(selectButton, saveEvent -> {
                String memoContent = memoTextArea.getText().trim();
                if (memoContent.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "메모 내용을 입력하세요.", "오류", JOptionPane.WARNING_MESSAGE);
                } else {
                    clientHandler.saveMemo(memoContent); // ClientHandler에 메모 저장 요청
                    JOptionPane.showMessageDialog(this, "메모가 저장되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
                    memoTextArea.setText(""); // 입력 필드 초기화
                }
            });
        });
    }

    private void resetPreview() {
        previewLabel.setText("");
        previewLabel.setIcon(null);
        previewLabel.setVisible(false);
        memoTextArea.setVisible(false);
    }

    private void replaceButtonAction(JButton button, java.awt.event.ActionListener newAction) {
        for (java.awt.event.ActionListener al : button.getActionListeners()) {
            button.removeActionListener(al);
        }
        button.addActionListener(newAction);
    }
}
