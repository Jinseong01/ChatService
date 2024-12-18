package Client;

import Model.UserSummary;
import Model.User;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
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
    // 채팅방ID -> 채팅방 멤버 Set<Friend>
    private Map<String, Set<UserSummary>> chatRoomMembers = new HashMap<>();

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


    public void sendChatMessage(String chatRoomId, String message) {
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        // 형식: /chat chatRoomId senderLoginID time message
        String command = "/chat " + chatRoomId + " " + loginUser.getLoginID() + " " + time + " " + message;
        sendMessage(command);
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
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        String command = "/sendemoji " + chatRoomId + " " + loginUser.getLoginID() + " " + time + " " + emojiFileName;
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
            // 이미지를 바이트 배열로 읽기
            byte[] imageBytes = Files.readAllBytes(Paths.get(imageFile.getAbsolutePath()));

            // Base64로 인코딩
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String time = new SimpleDateFormat("HH:mm").format(new Date());

            // 서버에 Base64 데이터 전송
            String command = "/sendimage " + chatRoomId + " " + loginUser.getLoginID() + " " + time + " " + base64Image;
            out.println(command);
            out.flush();
            System.out.println("이미지 전송 명령어: " + command);
        } catch (IOException e) {
            System.err.println("이미지 읽기 및 전송 중 오류 발생: " + e.getMessage());
        }
    }

    public void updateProfileImage(String base64Image) {
        // 형식: /updateprofileimage loginID base64ImageData
        String command = "/updateprofileimage " + loginUser.getLoginID() + " " + base64Image;
        sendMessage(command);
    }

    public void updateStatusMessage(String newStatus) {
        if (loginUser == null) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(ui.getFrame(), "로그인이 필요합니다.", "오류", JOptionPane.ERROR_MESSAGE);
            });
            return;
        }

        // /updatestatus loginID newStatus
        String command = "/updatestatus " + loginUser.getLoginID() + " " + newStatus;
        sendMessage(command);
    }

    public User getLoginUser() {
        return loginUser;
    }

    public Set<UserSummary> getChatRoomMembers(String chatRoomId) {
        return chatRoomMembers.get(chatRoomId);
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
                        String[] tokens = msg.split(" ", 5);
                        if (tokens.length == 5) {
                            String chatRoomId = tokens[1];
                            ChatWindow cw = chatWindows.get(chatRoomId);
                            if (cw != null) {
                                cw.handleChatMessage(msg);
                            }
                        } else {
                            System.out.println("수신된 /chat 명령어의 형식이 잘못되었습니다: " + msg);
                        }
                    } else if (msg.startsWith("/sendemoji ")) {
                        String[] tokens = msg.split(" ", 5);
                        if (tokens.length == 5) {
                            String chatRoomId = tokens[1];
                            String senderLoginID = tokens[2];
                            String time = tokens[3];
                            String emojiFilePath = tokens[4];

                            SwingUtilities.invokeLater(() -> {
                                ChatWindow cw = chatWindows.get(chatRoomId);
                                if (cw != null) {
                                    cw.appendEmoji(senderLoginID, time, emojiFilePath);
                                }
                            });
                        } else {
                            System.out.println("수신된 /sendemoji 명령어의 형식이 잘못되었습니다: " + msg);
                        }
                    } else if (msg.startsWith("/sendimage")) {
                        String[] tokens = msg.split(" ", 5);
                        if (tokens.length == 5) {
                            String chatRoomId = tokens[1];
                            String senderLoginID = tokens[2];
                            String time = tokens[3];
                            String imagePath = tokens[4];

                            SwingUtilities.invokeLater(() -> {
                                ChatWindow cw = chatWindows.get(chatRoomId); // 현재 채팅방 가져오기
                                if (cw != null) {
                                    cw.appendImage(senderLoginID, time, imagePath);
                                }
                            });
                        } else {
                            System.out.println("수신된 이미지 메시지의 형식이 잘못되었습니다: " + msg);
                        }
                    } else if (msg.startsWith("/login")) {
                        handleLoginResponse(msg);
                    } else if (msg.startsWith("/checkonline")) {
                        handleCheckOnlineResponse(msg);
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
                    } else if (msg.startsWith("/createchat_members")) {
                        handleCreateChatMembers(msg);
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
                    else if (msg.startsWith("/profileimageupdate ")) {
                        handleProfileImageUpdate(msg);
                    }
                    else if (msg.startsWith("/statusupdate ")) {
                        handleStatusUpdate(msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ui.getFrame(), "서버 연결이 끊어졌습니다.");
                });
            }
        }

        private void handleCheckOnlineResponse(String msg) {
            String[] tokens = msg.split(" ");
            if (tokens.length < 2) {
                JOptionPane.showMessageDialog(ui.getFrame(), "응답 메시지 형식 오류: " + msg);
                return;
            }

            String chatRoomName = tokens[1];
            List<String> onlineLoginIDs = tokens.length > 2
                    ? Arrays.asList(tokens).subList(2, tokens.length)
                    : Collections.emptyList();

            if (!onlineLoginIDs.isEmpty()) {
                List<UserSummary> userSummaries = new ArrayList<>(loginUser.getFriends());
                StringBuilder sb = new StringBuilder("/createchat " + chatRoomName);

                for (String loginID : onlineLoginIDs) {
                    userSummaries.stream()
                            .filter(friend -> friend.getLoginID().equals(loginID))
                            .findFirst()
                            .ifPresent(friend -> sb.append(" ").append(friend.getLoginID()));
                }

                ClientHandler.this.sendMessage(sb.toString()); // 채팅방 생성 요청
            } else {
                JOptionPane.showMessageDialog(ui.getFrame(), "선택된 친구 중 접속 중인 사용자가 없습니다.");
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
                String information = tokens[6];
                String profileImage = tokens[7];

                // loginUser 객체 생성
                loginUser = new User(loginID, loginPW, userName, birthday, information);
                loginUser.setProfileImage(profileImage);

                System.out.println("[개발용] : " + loginUser);

                SwingUtilities.invokeLater(() -> {
                    ui.handleLoginSuccess(loginUser);
                });
            } else {
                String errorMsg = tokens.length >= 9 ? tokens[2] : "로그인 실패";
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
            // 형식: /friends loginID|userName|info|profileImage ...
            String[] tokens = msg.split(" ");
            if (tokens.length < 2) return;

            Set<UserSummary> userSummaries = new HashSet<>();
            for (int i = 1; i < tokens.length; i++) {
                String[] friendData = tokens[i].split("\\|");
                if (friendData.length == 4) {
                    String loginID = friendData[0];
                    String userName = friendData[1].replace("_", " ");
                    String info = friendData[2].replace("_", " ");
                    String profileImage = friendData[3];
                    userSummaries.add(new UserSummary(loginID, userName, info, profileImage));
                }
            }

            if (loginUser != null) {
                loginUser.setFriends(userSummaries);
                SwingUtilities.invokeLater(() -> {
                    ui.updateFriendsList(new ArrayList<>(userSummaries));
                });
            }
            System.out.println("[개발용] : " + loginUser.getLoginID() + "의 친구 목록 업데이트 완료: " + userSummaries);
        }

        private void handleFriendRequest(String msg) {
            // 형식: /friendrequest loginID|userName|info|profileImage
            String[] tokens = msg.split(" ", 2);
            if (tokens.length != 2) return;

            String[] friendData = tokens[1].split("\\|");
            if (friendData.length == 4) {
                String loginID = friendData[0];
                String userName = friendData[1].replace("_", " ");
                String info = friendData[2].replace("_", " ");
                String profileImage = friendData[3];

                UserSummary requester = new UserSummary(loginID, userName, info, profileImage);
                SwingUtilities.invokeLater(() -> {
                    ui.addFriendRequest(requester);
                });
            }
        }

        private void handleFriendAccepted(String msg) {
            // 형식: /friendaccepted loginID|userName|info|profileImage
            String[] tokens = msg.split(" ", 2);
            if (tokens.length != 2) return;

            String[] friendData = tokens[1].split("\\|");
            if (friendData.length == 4) {
                String loginID = friendData[0];
                String userName = friendData[1].replace("_", " ");
                String info = friendData[2].replace("_", " ");
                String profileImage = friendData[3];

                UserSummary newUserSummary = new UserSummary(loginID, userName, info, profileImage);
                if (loginUser != null) {
                    loginUser.addFriend(newUserSummary);
                    SwingUtilities.invokeLater(() -> {
                        ui.updateFriendsList(new ArrayList<>(loginUser.getFriends()));
                        JOptionPane.showMessageDialog(ui.getFrame(), userName + "님이 친구 요청을 수락했습니다.");
                    });
                }
            }
        }

        private void handleFriendRejected(String msg) {
            // 형식: /friendrejected loginID
            String[] tokens = msg.split(" ", 2);
            if (tokens.length != 2) return;

            String rejectedLoginID = tokens[1];
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(ui.getFrame(), rejectedLoginID + "님이 친구 요청을 거절했습니다.");
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

        private Set<UserSummary> parseChatRoomMembers(String membersData) {
            Set<UserSummary> members = new HashSet<>();
            String[] memberList = membersData.split(" ");
            for (String memberInfo : memberList) {
                String[] info = memberInfo.split("\\|");
                if (info.length == 4) { // loginID, userName, profileImage, statusMessage
                    String loginID = info[0];
                    String userName = info[1].replace("_", " "); // 공백 복구
                    String profileImage = info[2];
                    String statusMessage = info[3].replace("_", " "); // 상태 메시지도 공백 복구
                    members.add(new UserSummary(loginID, userName, statusMessage, profileImage));
                }
            }
            return members;
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

        private void handleProfileImageUpdate(String msg) {
            String[] tokens = msg.split(" ", 3);
            if (tokens.length == 3) {
                String updatedLoginID = tokens[1];
                String newProfileImage = tokens[2];

                SwingUtilities.invokeLater(() -> {
                    if (loginUser != null && updatedLoginID.equals(loginUser.getLoginID())) {
                        // 자신의 프로필 이미지 업데이트
                        ui.updateOwnProfileImage(newProfileImage);
                    } else {
                        // 친구 목록 업데이트
                        ui.updateFriendProfileImage(updatedLoginID, newProfileImage);

                        // 친구 요청 목록 업데이트
                        ui.updateFriendRequestProfileImage(updatedLoginID, newProfileImage);
                    }

                    // 채팅방 멤버 목록 업데이트
                    updateChatRoomMembersProfileImage(updatedLoginID, newProfileImage);
                });
            }
        }


        private void updateChatRoomMembersProfileImage(String updatedLoginID, String newProfileImage) {
            for (Set<UserSummary> members : chatRoomMembers.values()) {
                for (UserSummary member : members) {
                    if (member.getLoginID().equals(updatedLoginID)) {
                        member.setProfileImage(newProfileImage);
                    }
                }
            }
        }

        private void handleStatusUpdate(String msg) {
            String[] tokens = msg.split(" ", 3);
            if (tokens.length == 3) {
                String updatedLoginID = tokens[1];
                String newStatus = tokens[2];

                SwingUtilities.invokeLater(() -> {
                    if (loginUser != null && updatedLoginID.equals(loginUser.getLoginID())) {
                        // 자신의 상태 메시지 업데이트
                        ui.updateOwnStatusMessage(newStatus);
                    } else {
                        // 친구 목록 업데이트
                        ui.updateFriendStatus(updatedLoginID, newStatus);

                        // 친구 요청 목록 업데이트
                        ui.updateFriendRequestStatus(updatedLoginID, newStatus);
                    }
                });
            }
        }
    }

    // 채팅방 멤버 데이터를 파싱하여 저장
    private void handleCreateChatMembers(String msg) {
        String[] tokens = msg.split(" ", 3);
        if (tokens.length < 3) return;

        String chatRoomId = tokens[1];
        String membersData = tokens[2];

        Set<UserSummary> members = parseChatRoomMembers(membersData);
        chatRoomMembers.put(chatRoomId, members);

        System.out.println("[개발용] 채팅방 멤버 저장 완료: " + chatRoomId);
    }

    // 기존 멤버 파싱 메서드 재사용
    private Set<UserSummary> parseChatRoomMembers(String membersData) {
        Set<UserSummary> members = new HashSet<>();
        String[] memberList = membersData.split(" ");
        for (String memberInfo : memberList) {
            String[] info = memberInfo.split("\\|");
            if (info.length == 4) { // loginID, userName, profileImage, statusMessage
                String loginID = info[0];
                String userName = info[1].replace("_", " ");
                String profileImage = info[2];
                String statusMessage = info[3].replace("_", " ");
                members.add(new UserSummary(loginID, userName, statusMessage, profileImage));
            }
        }
        return members;
    }
}
