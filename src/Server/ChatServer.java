package Server;

import Model.ChatRoom;
import Model.User;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 12345;
    // 사용자 정보: loginID -> Model.User
    private static Map<String, User> userCredentials = new ConcurrentHashMap<>();

    // 사용자 상태: loginID -> ClientHandler
    private static Map<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();

    // 채팅방: chatRoomId -> Model.ChatRoom
    private static Map<String, ChatRoom> chatRooms = new ConcurrentHashMap<>();

    // 친구 목록: loginID -> Set<loginID>
    // test : ["사용자 로그인 ID1", "사용자 로그인 ID2", "사용자 로그인 ID3", ...]
    private static Map<String, Set<String>> friendsList = new ConcurrentHashMap<>();

    // 친구 요청: loginID -> Set<loginID>
    // test : ["사용자 로그인 ID1", "사용자 로그인 ID2", "사용자 로그인 ID3", ...]
    private static Map<String, Set<String>> friendRequests = new ConcurrentHashMap<>();

    // 메모: loginID -> List<memoContent>
    // test : ["메모1", "메모2"]
    private static Map<String, List<String>> userMemos = new ConcurrentHashMap<>();


    public static void main(String[] args) {
        System.out.println("서버 시작 중...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("클라이언트 연결: " + socket.getInetAddress());
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String loginID;
        private User user;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String msg) {
            out.println(msg);
        }

        @Override
        public void run() {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println("수신: " + msg);
                    if (msg.startsWith("/signup")) {
                        handleSignup(msg);
                    } else if (msg.startsWith("/login")) {
                        handleLogin(msg);
                    } else if (msg.startsWith("/addfriend")) {
                        handleAddFriend(msg);
                    } else if (msg.startsWith("/acceptfriend")) {
                        handleAcceptFriend(msg);
                    } else if (msg.startsWith("/rejectfriend")) {
                        handleRejectFriend(msg);
                    } else if (msg.startsWith("/createchat")) {
                        handleCreateChat(msg);
                    } else if (msg.startsWith("/getchathistory")) {
                        handleGetChatHistory(msg);
                    } else if (msg.startsWith("/chat")) {
                        handleChat(msg);
                    } else if (msg.startsWith("/logout")) {
                        handleLogout();
                        break;
                    } else if (msg.startsWith("/addmemo")) {
                        handleAddMemo(msg);
                    } else if (msg.startsWith("/editmemo")) {
                        handleEditMemo(msg);
                    } else if (msg.startsWith("/deletememo")) {
                        handleDeleteMemo(msg);
                    } else if (msg.startsWith("/getmemos")) {
                        handleGetMemos(msg);
                    } else {
                        out.println("/error 알 수 없는 명령어입니다.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (loginID != null) {
                    onlineUsers.remove(loginID);
                    System.out.println(loginID + " 로그아웃");
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 회원가입 처리
        private void handleSignup(String msg) {
            // 형식: /signup loginID loginPW userName birthday nickname information
            String[] tokens = msg.split(" ", 7);
            if (tokens.length != 7) {
                out.println("/signup fail 잘못된 형식입니다.");
                return;
            }
            String userLoginID = tokens[1];
            String loginPW = tokens[2];
            String userName = tokens[3];
            String birthday = tokens[4];
            String nickname = tokens[5];
            String information = tokens[6].replace("_", " ");

            if (userCredentials.containsKey(userLoginID)) {
                out.println("/signup fail 이미 존재하는 사용자입니다.");
            } else {
                User newUser = new User(userLoginID, loginPW, userName, birthday, nickname, information);
                userCredentials.put(userLoginID, newUser);
                friendsList.put(userLoginID, ConcurrentHashMap.newKeySet());
                friendRequests.put(userLoginID, ConcurrentHashMap.newKeySet());
                userMemos.put(userLoginID, new ArrayList<>());
                out.println("/signup success");
                System.out.println("회원가입 완료: " + userLoginID);
            }
        }

        // 로그인 처리
        private void handleLogin(String msg) {
            // 형식: /login loginID loginPW
            String[] tokens = msg.split(" ", 3);
            if (tokens.length != 3) {
                out.println("/login fail 잘못된 형식입니다.");
                return;
            }
            String userLoginID = tokens[1];
            String pass = tokens[2];

            if (!userCredentials.containsKey(userLoginID)) {
                out.println("/login fail 존재하지 않는 사용자입니다.");
            } else if (!userCredentials.get(userLoginID).getLoginPW().equals(pass)) {
                out.println("/login fail 비밀번호가 틀렸습니다.");
            } else if (onlineUsers.containsKey(userLoginID)) {
                out.println("/login fail 이미 로그인 된 사용자입니다.");
            } else {
                this.loginID = userLoginID;
                this.user = userCredentials.get(userLoginID);
                onlineUsers.put(userLoginID, this);
                out.println("/login success");
                System.out.println(userLoginID + " 로그인 성공");
                // 친구 목록 동기화
                handleGetFriends();
                // 메모 목록 동기화
                handleGetMemos("/getmemos " + loginID);
            }
        }

        // 친구 목록 전송 처리
        private void handleGetFriends() {
            Set<String> friends = friendsList.get(loginID);
            StringBuilder sb = new StringBuilder("/friends");
            for (String friend : friends) {
                sb.append(" ").append(friend);
            }
            out.println(sb.toString());
        }

        // 친구 요청 처리
        private void handleAddFriend(String msg) {
            // 형식: /addfriend friendLoginID
            String[] tokens = msg.split(" ", 2);
            if (tokens.length != 2) {
                out.println("/addfriend fail 잘못된 형식입니다.");
                return;
            }
            String friendLoginID = tokens[1];
            if (!userCredentials.containsKey(friendLoginID)) {
                out.println("/addfriend fail 존재하지 않는 사용자입니다.");
                return;
            }
            if (friendsList.get(loginID).contains(friendLoginID)) {
                out.println("/addfriend fail 이미 친구입니다.");
                return;
            }
            if (friendRequests.get(friendLoginID).contains(loginID)) {
                out.println("/addfriend fail 이미 친구 요청을 보냈습니다.");
                return;
            }
            friendRequests.get(friendLoginID).add(loginID);
            // 실시간으로 친구 요청을 받는 사용자에게 전송
            if (onlineUsers.containsKey(friendLoginID)) {
                onlineUsers.get(friendLoginID).sendMessage("/friendrequest " + loginID);
            }
            out.println("/addfriend success 친구 요청을 보냈습니다.");
            System.out.println(loginID + "님이 " + friendLoginID + "님에게 친구 요청을 보냈습니다.");
        }

        // 친구 요청 수락 처리
        private void handleAcceptFriend(String msg) {
            String[] tokens = msg.split(" ", 2);
            if (tokens.length != 2) {
                out.println("/acceptfriend fail 잘못된 형식입니다.");
                return;
            }
            String requester = tokens[1];
            if (!friendRequests.get(loginID).contains(requester)) {
                out.println("/acceptfriend fail 친구 요청이 없습니다.");
                return;
            }
            friendRequests.get(loginID).remove(requester);
            friendsList.get(loginID).add(requester);
            friendsList.get(requester).add(loginID);
            out.println("/acceptfriend success 친구가 되었습니다.");
            System.out.println(loginID + "님과 " + requester + "님이 친구가 되었습니다.");
            // 친구에게 알림 전송
            if (onlineUsers.containsKey(requester)) {
                onlineUsers.get(requester).sendMessage("/friendaccepted " + loginID);
                // 친구의 최신 친구 목록을 동기화
                onlineUsers.get(requester).sendMessage("/friends " + String.join(" ", friendsList.get(requester)));
            }
            // 현재 사용자의 최신 친구 목록을 동기화
            out.println("/friends " + String.join(" ", friendsList.get(loginID)));
        }

        // 친구 요청 거절 처리
        private void handleRejectFriend(String msg) {
            String[] tokens = msg.split(" ", 2);
            if (tokens.length != 2) {
                out.println("/rejectfriend fail 잘못된 형식입니다.");
                return;
            }
            String requester = tokens[1];
            if (!friendRequests.get(loginID).contains(requester)) {
                out.println("/rejectfriend fail 친구 요청이 없습니다.");
                return;
            }
            friendRequests.get(loginID).remove(requester);
            out.println("/rejectfriend success 친구 요청을 거절했습니다.");
            System.out.println(loginID + "님이 " + requester + "님의 친구 요청을 거절했습니다.");
            // 친구 요청 보낸 사용자에게 알림
            if (onlineUsers.containsKey(requester)) {
                onlineUsers.get(requester).sendMessage("/friendrejected " + loginID);
            }
        }

        // 채팅방 생성 처리
        private void handleCreateChat(String msg) {
            // /createchat chatRoomName friend1 friend2 ...
            String[] tokens = msg.split(" ", 3);
            if (tokens.length < 3) {
                out.println("/createchat fail 잘못된 형식입니다.");
                return;
            }
            String chatRoomName = tokens[1];
            Set<User> members = new HashSet<>();
            members.add(user);
            String[] friends = tokens[2].split(" ");
            for (String friend : friends) {
                if (friendsList.get(loginID).contains(friend)) {
                    User f = userCredentials.get(friend);
                    members.add(f);
                } else {
                    out.println("/createchat fail " + friend + "은(는) 친구가 아닙니다.");
                    return;
                }
            }
            String chatRoomId = UUID.randomUUID().toString();
            ChatRoom chatRoom = new ChatRoom(chatRoomId, chatRoomName, members);
            chatRooms.put(chatRoomId, chatRoom);
            // 채팅방 참여자에게 알림
            for (User member : members) {
                if (onlineUsers.containsKey(member.getLoginID())) {
                    onlineUsers.get(member.getLoginID()).sendMessage("/createchat " + chatRoomId + " " + chatRoomName);
                }
            }
            out.println("/createchat_success 채팅방이 생성되었습니다. ID: " + chatRoomId);
        }

        // 채팅 기록 요청 처리
        private void handleGetChatHistory(String msg) {
            String[] tokens = msg.split(" ", 2);
            if (tokens.length != 2) {
                out.println("/getchathistory fail 잘못된 형식입니다.");
                return;
            }
            String chatRoomId = tokens[1];
            if (!chatRooms.containsKey(chatRoomId)) {
                out.println("/getchathistory fail 존재하지 않는 채팅방입니다.");
                return;
            }
            ChatRoom chatRoom = chatRooms.get(chatRoomId);
            // 멤버 확인
            boolean isMember = chatRoom.getMembers().stream().anyMatch(u -> u.getLoginID().equals(loginID));
            if (!isMember) {
                out.println("/getchathistory fail 채팅방에 참여하고 있지 않습니다.");
                return;
            }
            List<String> history = chatRoom.getChatHistory();
            out.println("/chathistorystart " + chatRoomId);
            for (String h : history) {
                out.println(h);
            }
            out.println("/chathistoryend " + chatRoomId);
        }

        // 채팅 메시지 처리
        private void handleChat(String msg) {
            String[] tokens = msg.split(" ", 4);
            if (tokens.length != 4) {
                out.println("/chat fail 잘못된 형식입니다.");
                return;
            }
            String chatRoomId = tokens[1];
            String sender = tokens[2];
            String message = tokens[3];
            if (!chatRooms.containsKey(chatRoomId)) {
                out.println("/chat fail 존재하지 않는 채팅방입니다.");
                return;
            }
            ChatRoom chatRoom = chatRooms.get(chatRoomId);
            boolean isMember = chatRoom.getMembers().stream().anyMatch(u -> u.getLoginID().equals(sender));
            if (!isMember) {
                out.println("/chat fail 발신자가 멤버가 아닙니다.");
                return;
            }
            String formattedMessage = "/chat " + chatRoomId + " " + sender + " " + message;
            chatRoom.addMessage(formattedMessage);
            for (User member : chatRoom.getMembers()) {
                if (onlineUsers.containsKey(member.getLoginID())) {
                    onlineUsers.get(member.getLoginID()).sendMessage(formattedMessage);
                }
            }
        }

        // 로그아웃 처리
        private void handleLogout() {
            onlineUsers.remove(loginID);
            out.println("/logout success 로그아웃 되었습니다.");
        }

        // 메모 추가 처리
        private void handleAddMemo(String msg) {
            // /addmemo memoContent
            String[] tokens = msg.split(" ", 2);
            if (tokens.length != 2) {
                out.println("/addmemo fail 잘못된 형식");
                return;
            }
            String memoContent = tokens[1].replace("_", " ");
            userMemos.putIfAbsent(loginID, new ArrayList<>());
            userMemos.get(loginID).add(memoContent);
            out.println("/addmemo success");
        }

        // 메모 수정 처리
        private void handleEditMemo(String msg) {
            // /editmemo index newContent
            String[] tokens = msg.split(" ", 3);
            if (tokens.length != 3) {
                out.println("/editmemo fail 잘못된 형식");
                return;
            }
            int index;
            try {
                index = Integer.parseInt(tokens[1]);
            } catch (NumberFormatException e) {
                out.println("/editmemo fail 인덱스 오류");
                return;
            }
            String newContent = tokens[2].replace("_", " ");
            List<String> memos = userMemos.get(loginID);
            if (memos == null || index < 0 || index >= memos.size()) {
                out.println("/editmemo fail 유효하지 않은 인덱스");
                return;
            }
            memos.set(index, newContent);
            out.println("/editmemo success");
        }

        // 메모 삭제 처리
        private void handleDeleteMemo(String msg) {
            // /deletememo index
            String[] tokens = msg.split(" ", 2);
            if (tokens.length != 2) {
                out.println("/deletememo fail 잘못된 형식");
                return;
            }
            int index;
            try {
                index = Integer.parseInt(tokens[1]);
            } catch (NumberFormatException e) {
                out.println("/deletememo fail 인덱스 오류");
                return;
            }
            List<String> memos = userMemos.get(loginID);
            if (memos == null || index < 0 || index >= memos.size()) {
                out.println("/deletememo fail 유효하지 않은 인덱스");
                return;
            }
            memos.remove(index);
            out.println("/deletememo success");
        }

        // 메모 목록 요청 처리
        private void handleGetMemos(String msg) {
            // /getmemos userId
            String[] tokens = msg.split(" ", 2);
            if (tokens.length != 2) {
                out.println("/getmemos fail 잘못된 형식");
                return;
            }
            String requestedLoginID = tokens[1];
            if (!requestedLoginID.equals(loginID)) {
                out.println("/getmemos fail 다른 사용자 메모 조회 불가");
                return;
            }
            List<String> memos = userMemos.getOrDefault(loginID, new ArrayList<>());
            out.println("/memosstart");
            for (int i = 0; i < memos.size(); i++) {
                out.println("/memo " + i + " " + memos.get(i).replace(" ", "_"));
            }
            out.println("/memosend");
        }
    }
}
