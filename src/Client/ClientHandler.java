package Client;

import Model.User;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler {
    private ClientUI ui;
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private User loginUser = null;

    // 채팅방ID -> ChatWindow 맵
    private Map<String, ChatWindow> chatWindows = new HashMap<>();

    public ClientHandler(ClientUI ui) {
        this.ui = ui;
    }

    public void connectToServer(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        try {
            socket = new Socket(serverAddress, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            new Thread(new Listener()).start();
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(ui.getFrame(), "서버에 연결할 수 없습니다. UI는 그대로 표시합니다.");
            });
        }
    }

    public void sendMessage(String message) {
        if (out != null) out.println(message);
    }

    public void addChatWindow(String chatRoomId, ChatWindow cw) {
        chatWindows.put(chatRoomId, cw);
    }

    public ChatWindow getChatWindow(String chatRoomId) {
        return chatWindows.get(chatRoomId);
    }

    // 이미지 업로드 요청
    public void uploadImage(File imageFile) {
        if (imageFile != null && imageFile.exists()) {
            sendMessage("/uploadimage " + imageFile.getAbsolutePath());
        } else {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(ui.getFrame(), "이미지 파일이 존재하지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    // 이모티콘 전송 요청
    public void sendEmoji(String chatRoomId, String emojiFileName) {
        if (chatRoomId == null || emojiFileName == null || emojiFileName.isEmpty()) {
            System.out.println("[개발용] : sendEmoji 실패: chatRoomId 또는 emojiFileName이 null/비어 있음");
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(ui.getFrame(), "유효하지 않은 이모티콘입니다.", "오류", JOptionPane.ERROR_MESSAGE);
            });
            return;
        }

        // 서버로 전송
        String command = "/sendemoji " + chatRoomId + " " + loginUser.getLoginID() + " " + emojiFileName;
        System.out.println("[개발용] : 전송 명령어: " + command); // 디버깅 출력

        // 정확히 한 줄만 전송
        out.print(command + "\n");
        out.flush(); // 스트림 강제 플러시
    }

    // 메모 저장 요청
    public void saveMemo(String memoContent) {
        if (memoContent != null && !memoContent.trim().isEmpty()) {
            sendMessage("/addmemo " + memoContent.replace(" ", "_"));
        } else {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(ui.getFrame(), "메모 내용이 비어 있습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    public void sendImage(String chatRoomId, String imagePath) {
        File imageFile = new File(imagePath);

        if (!imageFile.exists()) {
            System.err.println("전송하려는 이미지 파일이 존재하지 않습니다: " + imagePath);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(ui.getFrame(), "이미지 파일이 존재하지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            });
            return;
        }

        try {
            // 서버에 이미지 전송 명령어
            String command = "/sendimage " + chatRoomId + " " + loginUser.getLoginID() + " " + imageFile.getAbsolutePath();
            out.println(command);
            System.out.println("전송 명령어: " + command);
        } catch (Exception e) {
            System.err.println("이미지 전송 중 오류 발생: " + e.getMessage());
        }
    }

    private class Listener implements Runnable {
        @Override
        public void run() {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println("[서버 메시지] : " + msg);
                    if (msg.startsWith("/chathistorystart ")) {
                        String[] tokens = msg.split(" ", 2);
                        String chatRoomId = tokens[1];
                        ChatWindow cw = chatWindows.get(chatRoomId);
                        if (cw != null) {
                            cw.handleChatHistoryStart();
                        }
                    } else if (msg.startsWith("/chathistoryend ")) {
                        String[] tokens = msg.split(" ", 2);
                        String chatRoomId = tokens[1];
                        ChatWindow cw = chatWindows.get(chatRoomId);
                        if (cw != null) {
                            cw.handleChatHistoryEnd();
                        }
                    } else if (msg.startsWith("/chat ")) {
                        String[] tokens = msg.split(" ", 4);
                        if (tokens.length == 4) {
                            String chatRoomId = tokens[1];
                            ChatWindow cw = chatWindows.get(chatRoomId);
                            if (cw != null) {
                                cw.handleChatMessage(msg);
                            }
                        } else {
                            System.out.println("수신된 /chat 명령어의 형식이 잘못되었습니다: " + msg);
                        }
                    } else if (msg.startsWith("/sendemoji ")) {
                        String[] tokens = msg.split(" ", 4);
                        if (tokens.length == 4) {
                            String chatRoomId = tokens[1];
                            String senderLoginID = tokens[2];
                            String emojiFilePath = tokens[3];

                            SwingUtilities.invokeLater(() -> {
                                ChatWindow cw = chatWindows.get(chatRoomId);
                                if (cw != null) {
                                    cw.appendEmoji(senderLoginID, emojiFilePath);
                                }
                            });
                        } else {
                            System.out.println("수신된 /sendemoji 명령어의 형식이 잘못되었습니다: " + msg);
                        }
                    } else if (msg.startsWith("/sendimage")) {
                        String[] tokens = msg.split(" ", 4);
                        if (tokens.length == 4) {
                            String chatRoomId = tokens[1];
                            String senderLoginID = tokens[2];
                            String imagePath = tokens[3];

                            SwingUtilities.invokeLater(() -> {
                                ChatWindow cw = chatWindows.get(chatRoomId); // 현재 채팅방 가져오기
                                if (cw != null) {
                                    cw.appendImage(senderLoginID, imagePath);
                                }
                            });
                        } else {
                            System.out.println("수신된 이미지 메시지의 형식이 잘못되었습니다: " + msg);
                        }
                    } else if (msg.startsWith("/login")) {
                        handleLoginResponse(msg);
                    } else if (msg.startsWith("/signup")) {
                        handleSignupResponse(msg);
                    } else if (msg.startsWith("/error")) {
                        handleError(msg);
                    } else if (msg.startsWith("/friends")) {
                        handleFriendsList(msg);
                    } else if (msg.startsWith("/friendrequest")) {
                        handleFriendRequest(msg);
                    } else if (msg.startsWith("/friendaccepted")) {
                        handleFriendAccepted(msg);
                    } else if (msg.startsWith("/friendrejected")) {
                        handleFriendRejected(msg);
                    } else if (msg.startsWith("/createchat ")) {
                        handleCreateChat(msg);
                    } else if (msg.startsWith("/createchat_success")) {
                        handleCreateChatSuccess(msg);
                    } else if (msg.startsWith("/logout")) {
                        handleLogoutResponse(msg);
                    } else if (msg.startsWith("/memosstart")) {
                        handleMemosStart(msg);
                    } else if (msg.startsWith("/memosend")) {
                        handleMemosEnd(msg);
                    } else if (msg.startsWith("/memo")) {
                        handleMemo(msg);
                    } else if (msg.startsWith("/addmemo")) {
                        handleAddMemoResponse(msg);
                    } else if (msg.startsWith("/editmemo")) {
                        handleEditMemoResponse(msg);
                    } else if (msg.startsWith("/deletememo")) {
                        handleDeleteMemoResponse(msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ui.getFrame(), "서버 연결이 끊어졌습니다.");
                });
            }
        }

        private void handleEmojiMessage(String msg) {
            String[] tokens = msg.split(" ", 4); // chatRoomId 추가
            if (tokens.length == 4) {
                String chatRoomId = tokens[1];  // 채팅방 ID
                String senderLoginID = tokens[2];  // 보낸 사람의 ID
                String emojiFileName = tokens[3]; // 이모티콘 파일 이름

                ChatWindow cw = chatWindows.get(chatRoomId); // 특정 채팅창 가져오기
                if (cw != null) {
                    SwingUtilities.invokeLater(() -> cw.appendEmoji(senderLoginID, emojiFileName));
                }
            }
        }

        private void handleLoginResponse(String msg) {
            String[] tokens = msg.split(" ", 8);

            if (tokens.length < 8) return;

            if (tokens[1].equals("success")) {
                String loginID = tokens[2];
                String loginPW = tokens[3];
                String userName = tokens[4];
                String birthday = tokens[5];
                String nickname = tokens[6];
                String information = tokens[7];
                // loginUser 객체 생성
                loginUser = new User(loginID, loginPW, userName, birthday, nickname, information);

                System.out.println("[개발용] : " + loginUser);

                SwingUtilities.invokeLater(() -> {
                    ui.handleLoginSuccess(loginUser);
                });
            } else {
                String errorMsg = tokens.length >= 8 ? tokens[2] : "로그인 실패";
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ui.getFrame(), "로그인 실패: " + errorMsg);
                });
            }
        }

        private void handleSignupResponse(String msg) {
            String[] tokens = msg.split(" ", 3);
            if (tokens.length < 2) return;
            if (tokens[1].equals("success")) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ui.getFrame(), "회원가입 성공! 로그인하세요.");
                    ui.switchToLogin();
                });
            } else {
                String errorMsg = tokens.length >= 3 ? tokens[2] : "회원가입 실패";
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ui.getFrame(), "회원가입 실패: " + errorMsg);
                });
            }
        }

        private void handleError(String msg) {
            String errorMsg = msg.length() > 7 ? msg.substring(7) : "알 수 없는 오류";
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(ui.getFrame(), "오류: " + errorMsg);
            });
        }

        private void handleFriendsList(String msg) {
            // 형식: /friends friend1 friend2 friend3 ...
            String[] tokens = msg.split(" ");
            if (loginUser != null) {
                for (int i = 1; i < tokens.length; i++) {
                    loginUser.addFriend(tokens[i]);
                }
                SwingUtilities.invokeLater(() -> {
                    ui.updateFriendsList(new ArrayList<>(loginUser.getFriends()));
                });
            }
            System.out.println("[개발용] 클라이언트 " + loginUser.getLoginID() + "의 친구 목록 : " + loginUser.getFriends());
        }

        private void handleFriendRequest(String msg) {
            String[] tokens = msg.split(" ", 2);
            if (tokens.length != 2) return;
            String requesterLoginID = tokens[1];
            SwingUtilities.invokeLater(() -> {
                ui.addFriendRequest(requesterLoginID);
            });
        }

        private void handleFriendAccepted(String msg) {
            // 형식: /friendaccepted accepterLoginID
            String[] tokens = msg.split(" ", 2);
            if (tokens.length != 2) return;
            String accepterLoginID = tokens[1];
            if (loginUser != null) {
                loginUser.addFriend(accepterLoginID);
                SwingUtilities.invokeLater(() -> {
                    ui.updateFriendsList(new ArrayList<>(loginUser.getFriends()));
                    JOptionPane.showMessageDialog(ui.getFrame(), accepterLoginID + "님이 친구 요청을 수락했습니다.");
                });
            }
        }

        private void handleFriendRejected(String msg) {
            String[] tokens = msg.split(" ", 2);
            if (tokens.length != 2) return;
            String rejecterLoginID = tokens[1];
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(ui.getFrame(), rejecterLoginID + "님이 친구 요청을 거절했습니다.");
            });
        }

        private void handleCreateChat(String msg) {
            String[] tokens = msg.split(" ", 3);
            if (tokens.length != 3) return;
            String chatRoomId = tokens[1];
            String chatRoomName = tokens[2];
            String chatRoomDisplay = chatRoomName + " (ID: " + chatRoomId + ")";
            SwingUtilities.invokeLater(() -> {
                ui.addChatRoom(chatRoomDisplay);
            });
        }

        private void handleCreateChatSuccess(String msg) {
            String[] tokens = msg.split(" ", 2);
            if (tokens.length != 2) return;
            String successMsg = tokens[1];
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(ui.getFrame(), successMsg);
            });
        }

        private void handleLogoutResponse(String msg) {
            String[] tokens = msg.split(" ", 3);
            if (tokens.length < 2) return;
            if (tokens[1].equals("success")) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ui.getFrame(), "로그아웃 되었습니다.");
                    ui.resetUI();
                });
            } else {
                String errorMsg = tokens.length >= 3 ? tokens[2] : "로그아웃 실패";
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ui.getFrame(), "로그아웃 실패: " + errorMsg);
                });
            }
        }

        private void handleMemosStart(String msg) {
            System.out.println("[개발용] : 사용자 메모 초기화");
            loginUser.getMemos().clear();
            SwingUtilities.invokeLater(() -> {
                ui.clearMemos();
            });
        }

        private void handleMemo(String msg) {
            // 형식: /memo index memoContent
            String[] tokens = msg.split(" ", 3);
            if (tokens.length != 3) return;
            String index = tokens[1];
            String memoContent = tokens[2].replace("_", " ");
            if (loginUser != null) {
                loginUser.addMemo(memoContent);
                SwingUtilities.invokeLater(() -> {
                    ui.addMemo("[" + index + "] " + memoContent);
                });
            }
        }

        private void handleMemosEnd(String msg) {
            // 메모 수신 종료 시 특별 처리 필요 시 여기에
            System.out.println("[개발용] : " + loginUser.getLoginID() + "의 메모 목록 : " + loginUser.getMemos());
        }

        private void handleAddMemoResponse(String msg) {
            if (msg.contains("success")) {
                sendMessage("/getmemos " + loginUser.getLoginID());
            } else {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ui.getFrame(), "메모 추가 실패.");
                });
            }
        }

        private void handleEditMemoResponse(String msg) {
            if (msg.contains("success")) {
                sendMessage("/getmemos " + loginUser.getLoginID());
            } else {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ui.getFrame(), "메모 수정 실패.");
                });
            }
        }

        private void handleDeleteMemoResponse(String msg) {
            if (msg.contains("success")) {
                sendMessage("/getmemos " + loginUser.getLoginID());
            } else {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ui.getFrame(), "메모 삭제 실패.");
                });
            }
        }
    }
}
