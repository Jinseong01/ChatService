package Server;

import Model.ChatRoom;
import Model.User;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String loginID;
    private User user;

    public UserHandler(Socket socket) {
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
                ServerApp.onlineUsers.remove(loginID);
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

        if (ServerApp.userCredentials.containsKey(userLoginID)) {
            out.println("/signup fail 이미 존재하는 사용자입니다.");
        } else {
            User newUser = new User(userLoginID, loginPW, userName, birthday, nickname, information);
            ServerApp.userCredentials.put(userLoginID, newUser);
            ServerApp.friendRequests.put(userLoginID, ConcurrentHashMap.newKeySet());
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

        if (!ServerApp.userCredentials.containsKey(userLoginID)) {
            out.println("/login fail 존재하지 않는 사용자입니다.");
        } else if (!ServerApp.userCredentials.get(userLoginID).getLoginPW().equals(pass)) {
            out.println("/login fail 비밀번호가 틀렸습니다.");
        } else if (ServerApp.onlineUsers.containsKey(userLoginID)) {
            out.println("/login fail 이미 로그인 된 사용자입니다.");
        } else {
            this.loginID = userLoginID;
            this.user = ServerApp.userCredentials.get(userLoginID);
            ServerApp.onlineUsers.put(userLoginID, this);
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
        Set<String> friends = user.getFriends();
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
        if (!ServerApp.userCredentials.containsKey(friendLoginID)) {
            out.println("/addfriend fail 존재하지 않는 사용자입니다.");
            return;
        }
        if (user.getFriends().contains(friendLoginID)) {
            out.println("/addfriend fail 이미 친구입니다.");
            return;
        }
        Set<String> requests = ServerApp.friendRequests.get(friendLoginID);
        if (requests.contains(loginID)) {
            out.println("/addfriend fail 이미 친구 요청을 보냈습니다.");
            return;
        }
        requests.add(loginID);
        // 실시간으로 친구 요청을 받는 사용자에게 전송
        if (ServerApp.onlineUsers.containsKey(friendLoginID)) {
            ServerApp.onlineUsers.get(friendLoginID).sendMessage("/friendrequest " + loginID);
        }
        out.println("/addfriend success 친구 요청을 보냈습니다.");
        System.out.println(loginID + "님이 " + friendLoginID + "님에게 친구 요청을 보냈습니다.");
    }

    // 친구 요청 수락 처리
    private void handleAcceptFriend(String msg) {
        // 형식: /acceptfriend requesterLoginID
        String[] tokens = msg.split(" ", 2);
        if (tokens.length != 2) {
            out.println("/acceptfriend fail 잘못된 형식입니다.");
            return;
        }
        String requester = tokens[1];
        Set<String> requests = ServerApp.friendRequests.get(loginID);
        if (!requests.contains(requester)) {
            out.println("/acceptfriend fail 친구 요청이 없습니다.");
            return;
        }
        // 친구 요청 제거 및 친구 목록 추가
        requests.remove(requester);
        user.addFriend(requester);
        User requesterUser = ServerApp.userCredentials.get(requester);
        if (requesterUser != null) {
            requesterUser.addFriend(loginID);
        }
        out.println("/acceptfriend success 친구가 되었습니다.");
        System.out.println(loginID + "님과 " + requester + "님이 친구가 되었습니다.");
        // 친구에게 알림 전송
        if (ServerApp.onlineUsers.containsKey(requester)) {
            ServerApp.onlineUsers.get(requester).sendMessage("/friendaccepted " + loginID);
            // 친구의 최신 친구 목록을 동기화
            ServerApp.onlineUsers.get(requester).sendMessage("/friends " + String.join(" ", requesterUser.getFriends()));
        }
        // 현재 사용자의 최신 친구 목록을 동기화
        out.println("/friends " + String.join(" ", user.getFriends()));
    }

    // 친구 요청 거절 처리
    private void handleRejectFriend(String msg) {
        // 형식: /rejectfriend requesterLoginID
        String[] tokens = msg.split(" ", 2);
        if (tokens.length != 2) {
            out.println("/rejectfriend fail 잘못된 형식입니다.");
            return;
        }
        String requester = tokens[1];
        Set<String> requests = ServerApp.friendRequests.get(loginID);
        if (!requests.contains(requester)) {
            out.println("/rejectfriend fail 친구 요청이 없습니다.");
            return;
        }
        // 친구 요청 제거
        requests.remove(requester);
        out.println("/rejectfriend success 친구 요청을 거절했습니다.");
        System.out.println(loginID + "님이 " + requester + "님의 친구 요청을 거절했습니다.");
        // 친구 요청 보낸 사용자에게 알림
        if (ServerApp.onlineUsers.containsKey(requester)) {
            ServerApp.onlineUsers.get(requester).sendMessage("/friendrejected " + loginID);
        }
    }

    // 채팅방 생성 처리
    private void handleCreateChat(String msg) {
        // 형식: /createchat chatRoomName friend1 friend2 ...
        String[] tokens = msg.split(" ", 3);
        if (tokens.length < 3) {
            out.println("/createchat fail 잘못된 형식입니다.");
            return;
        }
        String chatRoomName = tokens[1];
        Set<User> members = ConcurrentHashMap.newKeySet();
        members.add(user);
        String[] friends = tokens[2].split(" ");
        for (String friend : friends) {
            if (user.getFriends().contains(friend)) {
                User f = ServerApp.userCredentials.get(friend);
                if (f != null) {
                    members.add(f);
                } else {
                    out.println("/createchat fail " + friend + "은(는) 존재하지 않는 사용자입니다.");
                    return;
                }
            } else {
                out.println("/createchat fail " + friend + "은(는) 친구가 아닙니다.");
                return;
            }
        }
        String chatRoomId = UUID.randomUUID().toString();
        ChatRoom chatRoom = new ChatRoom(chatRoomId, chatRoomName, members);
        ServerApp.chatRooms.put(chatRoomId, chatRoom);
        // 채팅방 참여자에게 알림
        for (User member : members) {
            if (ServerApp.onlineUsers.containsKey(member.getLoginID())) {
                ServerApp.onlineUsers.get(member.getLoginID()).sendMessage("/createchat " + chatRoomId + " " + chatRoomName);
            }
        }
        out.println("/createchat_success 채팅방이 생성되었습니다. ID: " + chatRoomId);
    }

    // 채팅 기록 요청 처리
    private void handleGetChatHistory(String msg) {
        // 형식: /getchathistory chatRoomId
        String[] tokens = msg.split(" ", 2);
        if (tokens.length != 2) {
            out.println("/getchathistory fail 잘못된 형식입니다.");
            return;
        }
        String chatRoomId = tokens[1];
        if (!ServerApp.chatRooms.containsKey(chatRoomId)) {
            out.println("/getchathistory fail 존재하지 않는 채팅방입니다.");
            return;
        }
        ChatRoom chatRoom = ServerApp.chatRooms.get(chatRoomId);
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
        // 형식: /chat chatRoomId senderLoginID message
        String[] tokens = msg.split(" ", 4);
        if (tokens.length != 4) {
            out.println("/chat fail 잘못된 형식입니다.");
            return;
        }
        String chatRoomId = tokens[1];
        String sender = tokens[2];
        String message = tokens[3];
        if (!ServerApp.chatRooms.containsKey(chatRoomId)) {
            out.println("/chat fail 존재하지 않는 채팅방입니다.");
            return;
        }
        ChatRoom chatRoom = ServerApp.chatRooms.get(chatRoomId);
        boolean isMember = chatRoom.getMembers().stream().anyMatch(u -> u.getLoginID().equals(sender));
        if (!isMember) {
            out.println("/chat fail 발신자가 멤버가 아닙니다.");
            return;
        }
        String formattedMessage = "/chat " + chatRoomId + " " + sender + " " + message;
        chatRoom.addMessage(formattedMessage);
        for (User member : chatRoom.getMembers()) {
            if (ServerApp.onlineUsers.containsKey(member.getLoginID())) {
                ServerApp.onlineUsers.get(member.getLoginID()).sendMessage(formattedMessage);
            }
        }
    }

    // 로그아웃 처리
    private void handleLogout() {
        ServerApp.onlineUsers.remove(loginID);
        out.println("/logout success 로그아웃 되었습니다.");
    }

    // 메모 추가 처리
    private void handleAddMemo(String msg) {
        // 형식: /addmemo memoContent
        String[] tokens = msg.split(" ", 2);
        if (tokens.length != 2) {
            out.println("/addmemo fail 잘못된 형식");
            return;
        }
        String memoContent = tokens[1].replace("_", " ");
        user.addMemo(memoContent);
        out.println("/addmemo success");
    }

    // 메모 수정 처리
    private void handleEditMemo(String msg) {
        // 형식: /editmemo index newContent
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
        boolean success = user.editMemo(index, newContent);
        if (success) {
            out.println("/editmemo success");
        } else {
            out.println("/editmemo fail 유효하지 않은 인덱스");
        }
    }

    // 메모 삭제 처리
    private void handleDeleteMemo(String msg) {
        // 형식: /deletememo index
        String[] tokens = msg.split(" ", 2);
        if (tokens.length != 2) {
            out.println("/deletememo fail 잘못된 형식입니다.");
            return;
        }
        int index;
        try {
            index = Integer.parseInt(tokens[1]);
        } catch (NumberFormatException e) {
            out.println("/deletememo fail 인덱스 오류");
            return;
        }
        boolean success = user.deleteMemo(index);
        if (success) {
            out.println("/deletememo success");
        } else {
            out.println("/deletememo fail 유효하지 않은 인덱스");
        }
    }

    // 메모 목록 요청 처리
    private void handleGetMemos(String msg) {
        // 형식: /getmemos userId
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
        List<String> memos = user.getMemos();
        out.println("/memosstart");
        for (int i = 0; i < memos.size(); i++) {
            out.println("/memo " + i + " " + memos.get(i).replace(" ", "_"));
        }
        out.println("/memosend");
    }
}
