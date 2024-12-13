package Client;

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
    private String loginID = null;

    // 채팅방ID -> ChatWindow 맵
    private Map<String, ClientUI.ChatWindow> chatWindows = new HashMap<>();

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

    public String getLoginID() {
        return loginID;
    }

    public void setLoginID(String loginID) {
        this.loginID = loginID;
    }

    public void addChatWindow(String chatRoomId, ClientUI.ChatWindow cw) {
        chatWindows.put(chatRoomId, cw);
    }

    public ClientUI.ChatWindow getChatWindow(String chatRoomId) {
        return chatWindows.get(chatRoomId);
    }

    private class Listener implements Runnable {
        @Override
        public void run() {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println("서버 메시지: " + msg);
                    if (msg.startsWith("/chathistorystart ")) {
                        String[] tokens = msg.split(" ", 2);
                        String chatRoomId = tokens[1];
                        ClientUI.ChatWindow cw = chatWindows.get(chatRoomId);
                        if (cw != null) {
                            cw.handleChatHistoryStart();
                        }
                    } else if (msg.startsWith("/chathistoryend ")) {
                        String[] tokens = msg.split(" ", 2);
                        String chatRoomId = tokens[1];
                        ClientUI.ChatWindow cw = chatWindows.get(chatRoomId);
                        if (cw != null) {
                            cw.handleChatHistoryEnd();
                        }
                    } else if (msg.startsWith("/chat ")) {
                        String[] tokens = msg.split(" ", 4);
                        if (tokens.length == 4) {
                            String chatRoomId = tokens[1];
                            ClientUI.ChatWindow cw = chatWindows.get(chatRoomId);
                            if (cw != null) {
                                cw.handleChatMessage(msg);
                            }
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
                    } else if (msg.startsWith("/memo ")) {
                        handleMemo(msg);
                    } else if (msg.startsWith("/memosend")) {
                        handleMemosEnd(msg);
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

        private void handleLoginResponse(String msg) {
            String[] tokens = msg.split(" ", 3);
            if (tokens.length < 2) return;
            if (tokens[1].equals("success")) {
                setLoginID(ui.getLoginIDFromField());
                SwingUtilities.invokeLater(() -> {
                    ui.switchToChatPanel();
                });
            } else {
                String errorMsg = tokens.length >= 3 ? tokens[2] : "로그인 실패";
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
            String[] tokens = msg.split(" ");
            SwingUtilities.invokeLater(() -> {
                ui.updateFriendsList(Arrays.asList(tokens).subList(1, tokens.length));
            });
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
            String[] tokens = msg.split(" ", 2);
            if (tokens.length != 2) return;
            String accepterLoginID = tokens[1];
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(ui.getFrame(), accepterLoginID + "님이 친구 요청을 수락했습니다.");
            });
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
            SwingUtilities.invokeLater(() -> {
                ui.clearMemos();
            });
        }

        private void handleMemo(String msg) {
            String[] tokens = msg.split(" ", 3);
            if (tokens.length != 3) return;
            String index = tokens[1];
            String memoContent = tokens[2].replace("_", " ");
            SwingUtilities.invokeLater(() -> {
                ui.addMemo("[" + index + "] " + memoContent);
            });
        }

        private void handleMemosEnd(String msg) {
            // 메모 수신 종료 시 특별 처리 필요 시 여기에
        }

        private void handleAddMemoResponse(String msg) {
            if (msg.contains("success")) {
                sendMessage("/getmemos " + loginID);
            } else {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ui.getFrame(), "메모 추가 실패.");
                });
            }
        }

        private void handleEditMemoResponse(String msg) {
            if (msg.contains("success")) {
                sendMessage("/getmemos " + loginID);
            } else {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ui.getFrame(), "메모 수정 실패.");
                });
            }
        }

        private void handleDeleteMemoResponse(String msg) {
            if (msg.contains("success")) {
                sendMessage("/getmemos " + loginID);
            } else {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ui.getFrame(), "메모 삭제 실패.");
                });
            }
        }
    }
}
