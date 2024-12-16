package Server;

import Model.ChatRoom;
import Model.Friend;
import Model.User;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                msg = msg.trim(); // 공백 제거
                if (msg.isEmpty()) { // 빈 메시지 무시
                    System.out.println("[빈 메시지 수신] : 처리하지 않음");
                    continue;
                }
                System.out.println("[수신] : " + msg);

                if (msg.startsWith("/signup")) {
                    handleSignup(msg);
                } else if (msg.startsWith("/login")) {
                    handleLogin(msg);
                } else if (msg.startsWith("/addfriend")) {
                    handleAddFriend(msg);
                } else if (msg.startsWith("/checkonline")) {
                    handleCheckOnline(msg);
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
                } else if (msg.startsWith("/sendemoji")) {
                    handleSendEmoji(msg);
                } else if (msg.startsWith("/sendimage")) {
                    handleSendImage(msg);
                } else if (msg.startsWith("/updateprofileimage")) {
                    handleUpdateProfileImage(msg);
                } else if (msg.startsWith("/updatestatus ")) {
                    handleUpdateStatus(msg);
                } else {
                    System.out.println("정의되지 않은 명령어: " + msg);
                    out.println("/error 알 수 없는 명령어입니다.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (loginID != null) {
                ServerApp.onlineUsers.remove(loginID);
                System.out.println("[개발용] : " + loginID + " 로그아웃");
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 헬퍼 메서드: 사용자의 친구 목록에서 loginID를 가진 Friend가 있는지 확인
    private boolean hasFriend(User u, String friendLoginID) {
        for (Friend f : u.getFriends()) {
            if (f.getLoginID().equals(friendLoginID)) return true;
        }
        return false;
    }

    private Friend friendFromUser(User u) {
        return new Friend(u.getLoginID(), u.getUserName(), u.getInformation(), u.getProfileImage());
    }

    private void handleUpdateStatus(String msg) {
        String[] tokens = msg.split(" ", 3);
        if (tokens.length != 3) {
            out.println("/error 잘못된 형식입니다. 사용법: /updatestatus loginID newStatus");
            return;
        }

        String targetLoginID = tokens[1];
        String newStatus = tokens[2];

        if (!targetLoginID.equals(loginID)) {
            out.println("/error 다른 사용자의 상태메시지는 수정할 수 없습니다.");
            return;
        }

        // 상태 메시지 업데이트
        user.setInformation(newStatus);

        // 변경된 상태 메시지를 현재 사용자에게 전송
        out.println("/updatestatus success");
        System.out.println("[개발용] : " + loginID + "의 상태메시지 변경 성공: " + newStatus);
        out.println("/statusupdate " + loginID + " " + newStatus);

        // 친구들에게 상태 업데이트 전송
        notifyFriendsAboutStatusChange(newStatus);
    }

    private void notifyFriendsAboutStatusChange(String newStatus) {
        // 1. 친구들에게 변경 사항 알림
        for (Friend friend : user.getFriends()) {
            if (ServerApp.onlineUsers.containsKey(friend.getLoginID())) {
                ServerApp.onlineUsers.get(friend.getLoginID()).sendMessage("/statusupdate " + loginID + " " + newStatus);
            }
        }

        // 2. 자신을 친구 요청 목록에 보유한 사용자들에게도 변경 사항 알림
        for (Map.Entry<String, Set<String>> entry : ServerApp.friendRequests.entrySet()) {
            String targetUserLoginID = entry.getKey();
            Set<String> requesters = entry.getValue();
            if (requesters.contains(loginID) && ServerApp.onlineUsers.containsKey(targetUserLoginID)) {
                ServerApp.onlineUsers.get(targetUserLoginID).sendMessage("/statusupdate " + loginID + " " + newStatus);
            }
        }
    }

    private void handleUpdateProfileImage(String msg) {
        String[] tokens = msg.split(" ", 3);
        if (tokens.length != 3) {
            out.println("/error 잘못된 형식입니다. 사용법: /updateprofileimage loginID base64Image");
            return;
        }

        String targetLoginID = tokens[1];
        String base64Image = tokens[2];

        if (!targetLoginID.equals(loginID)) {
            out.println("/error 다른 사용자의 프로필은 수정할 수 없습니다.");
            return;
        }

        // 프로필 이미지 업데이트
        user.setProfileImage(base64Image);
        System.out.println("[개발용] : " + loginID + "의 프로필 이미지 변경 성공");

        // 변경된 프로필 이미지를 현재 사용자에게 전송
        out.println("/profileimageupdate " + loginID + " " + base64Image);

        // 친구들에게 프로필 이미지 업데이트 전송
        notifyFriendsAboutProfileImageChange(base64Image);
    }

    private void notifyFriendsAboutProfileImageChange(String base64Image) {
        // 1. 친구들에게 변경 사항 알림
        for (Friend friend : user.getFriends()) {
            if (ServerApp.onlineUsers.containsKey(friend.getLoginID())) {
                ServerApp.onlineUsers.get(friend.getLoginID()).sendMessage("/profileimageupdate " + loginID + " " + base64Image);
            }
        }

        // 2. 자신을 친구 요청 목록에 보유한 사용자들에게도 변경 사항 알림
        for (Map.Entry<String, Set<String>> entry : ServerApp.friendRequests.entrySet()) {
            String targetUserLoginID = entry.getKey();
            Set<String> requesters = entry.getValue();
            if (requesters.contains(loginID) && ServerApp.onlineUsers.containsKey(targetUserLoginID)) {
                ServerApp.onlineUsers.get(targetUserLoginID).sendMessage("/profileimageupdate " + loginID + " " + base64Image);
            }
        }
    }

    private void handleSignup(String msg) {
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

        // 생일 형식 확인 (정규식: YYYY-MM-DD)
        if (!birthday.matches("\\d{4}-\\d{2}-\\d{2}")) {
            out.println("/signup fail 생일 형식 오류: YYYY-MM-DD 형식이어야 합니다.");
            return;
        }

        // 사용자 id 중복 확인
        if (ServerApp.userCredentials.containsKey(userLoginID)) {
            out.println("/signup fail 이미 존재하는 사용자입니다.");
        } else {
            User newUser = new User(userLoginID, loginPW, userName, birthday, nickname, information);
            ServerApp.userCredentials.put(userLoginID, newUser);
            ServerApp.friendRequests.put(userLoginID, ConcurrentHashMap.newKeySet());
            out.println("/signup success");
            System.out.println("[개발용] : 회원가입 완료: " + userLoginID);
        }
    }

    private void handleLogin(String msg) throws IOException {
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

            if(user.getProfileImage()==null) {
                Path imagePath = Paths.get("src", "Resources", "images", "BasicProfile.jpg");
                byte[] imageBytes = Files.readAllBytes(imagePath);
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                user.setProfileImage(base64Image);
            }

            String successMsg = String.format("/login success %s %s %s %s %s %s %s",
                    user.getLoginID(),
                    user.getLoginPW(),
                    user.getUserName(),
                    user.getBirthday(),
                    user.getNickname(),
                    user.getInformation(),
                    user.getProfileImage()
            );
            out.println(successMsg);

            System.out.println("[개발용] : " + userLoginID + " 로그인 성공");

            handleGetFriends();
            handleGetMemos("/getmemos " + loginID);
        }
    }

    private void handleCheckOnline(String msg) {
        String[] tokens = msg.split(" ");
        if (tokens.length < 3) { // chatRoomName + 최소 1명의 친구 필요
            out.println("/checkonline_fail 잘못된 형식입니다.");
            return;
        }

        String chatRoomName = tokens[1];
        List<String> friendsToCheck = Arrays.asList(tokens).subList(2, tokens.length);
        List<String> onlineFriends = new ArrayList<>();

        for (String friend : friendsToCheck) {
            if (ServerApp.onlineUsers.containsKey(friend)) {
                onlineFriends.add(friend);
            }
        }

        StringBuilder response = new StringBuilder("/checkonline_success " + chatRoomName);
        if (!onlineFriends.isEmpty()) {
            for (String onlineFriend : onlineFriends) {
                response.append(" ").append(onlineFriend);
            }
        }
        out.println(response.toString()); // 항상 chatRoomName 포함
    }

    // 친구 목록 전송 처리
    private void handleGetFriends() {
        Set<Friend> friends = user.getFriends();
        // 직렬화하여 전송
        out.println("/friends " + friendsListToString(friends));
        System.out.println("[개발용] : 서버 " + user.getLoginID() + " 친구 목록 : " + friends);
    }

    private void handleAddFriend(String msg) {
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
        if (hasFriend(user, friendLoginID)) {
            out.println("/addfriend fail 이미 친구입니다.");
            return;
        }

        Set<String> requests = ServerApp.friendRequests.get(friendLoginID);

        if (requests.contains(loginID)) {
            out.println("/addfriend fail 이미 친구 요청을 보냈습니다.");
            return;
        }

        requests.add(loginID);
        // 요청 보낸 사용자 정보를 Friend 객체로 생성
        Friend requesterFriend = friendFromUser(user);

        if (ServerApp.onlineUsers.containsKey(friendLoginID)) {
            ServerApp.onlineUsers.get(friendLoginID).sendMessage("/friendrequest " + friendToString(requesterFriend));
        }
        out.println("/addfriend success 친구 요청을 보냈습니다.");
        System.out.println("[개발용] : " + loginID + "님이 " + friendLoginID + "님에게 친구 요청을 보냈습니다.");
    }

    private String friendToString(Friend f) {
        return f.getLoginID() + "|" + f.getUserName().replace(" ", "_") + "|"
                + f.getInformation().replace(" ", "_") + "|"
                + f.getProfileImage();
    }

    private String friendsListToString(Set<Friend> friendsList) {
        List<String> serializedFriends = new ArrayList<>();
        for (Friend friend : friendsList) {
            serializedFriends.add(friendToString(friend));
        }
        return String.join(" ", serializedFriends);
    }

    private void handleAcceptFriend(String msg) {
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
        requests.remove(requester);
        User requesterUser = ServerApp.userCredentials.get(requester);

        Friend requesterFriend = friendFromUser(requesterUser);
        user.addFriend(requesterFriend);

        if (requesterUser != null) {
            requesterUser.addFriend(friendFromUser(user));
        }

        out.println("/acceptfriend success 친구가 되었습니다.");
        System.out.println("[개발용] : " + loginID + "님과 " + requester + "님이 친구가 되었습니다.");

        if (ServerApp.onlineUsers.containsKey(requester)) {
            ServerApp.onlineUsers.get(requester).sendMessage("/friendaccepted " + friendsListToString(user.getFriends()));
            // 친구의 최신 친구 목록을 동기화
            ServerApp.onlineUsers.get(requester).sendMessage("/friends " + friendsListToString(requesterUser.getFriends()));
        }

        // 현재 사용자의 최신 친구 목록을 동기화
        out.println("/friends " + friendsListToString(user.getFriends()));
        System.out.println("[개발용] : 서버 "+ user.getLoginID() + "의 친구 목록 : " + user.getFriends());
        System.out.println("[개발용] : 서버 "+ requesterUser.getLoginID() + "의 친구 목록 : " + requesterUser.getFriends());
    }

    private void handleRejectFriend(String msg) {
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
        requests.remove(requester);
        out.println("/rejectfriend success 친구 요청을 거절했습니다.");
        System.out.println("[개발용] : " + loginID + "님이 " + requester + "님의 친구 요청을 거절했습니다.");
        if (ServerApp.onlineUsers.containsKey(requester)) {
            ServerApp.onlineUsers.get(requester).sendMessage("/friendrejected " + loginID);
        }
    }

    private void handleCreateChat(String msg) {
        String[] tokens = msg.split(" ", 3);
        if (tokens.length < 3) {
            out.println("/createchat fail 잘못된 형식입니다.");
            return;
        }
        String chatRoomName = tokens[1];
        Set<User> members = ConcurrentHashMap.newKeySet();
        members.add(user);

        String[] friends = tokens[2].split(" ");
        for (String friendLoginID : friends) {
            // 친구인지 확인
            if (!hasFriend(user, friendLoginID)) {
                out.println("/createchat fail " + friendLoginID + "은(는) 친구가 아닙니다.");
                return;
            }
            User f = ServerApp.userCredentials.get(friendLoginID);
            if (f != null) {
                members.add(f);
            } else {
                out.println("/createchat fail " + friendLoginID + "은(는) 존재하지 않는 사용자입니다.");
                return;
            }
        }

        String chatRoomId = UUID.randomUUID().toString();
        ChatRoom chatRoom = new ChatRoom(chatRoomId, chatRoomName, members);
        ServerApp.chatRooms.put(chatRoomId, chatRoom);
        for (User member : members) {
            if (ServerApp.onlineUsers.containsKey(member.getLoginID())) {
                ServerApp.onlineUsers.get(member.getLoginID()).sendMessage("/createchat " + chatRoomId + " " + chatRoomName);
            }
        }
        out.println("/createchat_success 채팅방이 생성되었습니다.");
    }

    private void handleGetChatHistory(String msg) {
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

    private void handleChat(String msg) {
        String[] tokens = msg.split(" ", 5);
        if (tokens.length != 5) {
            out.println("/chat fail 잘못된 형식입니다.");
            return;
        }
        String chatRoomId = tokens[1];
        String sender = tokens[2];
        String time = tokens[3];
        String message = tokens[4];

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

        String formattedMessage = "/chat " + chatRoomId + " " + sender + " " + time + " " + message;
        chatRoom.addMessage(formattedMessage);
        for (User member : chatRoom.getMembers()) {
            if (ServerApp.onlineUsers.containsKey(member.getLoginID())) {
                ServerApp.onlineUsers.get(member.getLoginID()).sendMessage(formattedMessage);
            }
        }
    }

    private void handleSendImage(String msg) {
        System.out.println("수신한 sendimage 명령어: [" + msg + "]");
        String[] tokens = msg.split(" ", 5);
        if (tokens.length != 5) {
            System.out.println("sendimage 명령어 파싱 실패: 잘못된 형식");
            out.println("/sendimage fail 잘못된 형식입니다.");
            return;
        }

        String chatRoomId = tokens[1];
        String sender = tokens[2];
        String time = tokens[3];
        String imagePath = tokens[4];

        if (!ServerApp.chatRooms.containsKey(chatRoomId)) {
            out.println("/sendimage fail 존재하지 않는 채팅방입니다.");
            return;
        }

        ChatRoom chatRoom = ServerApp.chatRooms.get(chatRoomId);
        boolean isMember = chatRoom.getMembers().stream().anyMatch(u -> u.getLoginID().equals(sender));
        if (!isMember) {
            out.println("/sendimage fail 채팅방 멤버가 아닙니다.");
            return;
        }

        String formattedMessage = "/sendimage " + chatRoomId + " " + sender + " " + time + " " + imagePath;
        chatRoom.addMessage(formattedMessage);

        for (User member : chatRoom.getMembers()) {
            if (ServerApp.onlineUsers.containsKey(member.getLoginID())) {
                ServerApp.onlineUsers.get(member.getLoginID()).sendMessage(formattedMessage);
            }
        }

        System.out.println("sendimage 성공: " + formattedMessage);
    }

    private void handleSendEmoji(String msg) {
        System.out.println("[개발용] : 수신한 sendemoji 명령어: [" + msg + "]");
        String[] tokens = msg.split(" ", 5);
        if (tokens.length != 5) {
            out.println("/sendemoji fail 잘못된 형식입니다.");
            return;
        }

        String chatRoomId = tokens[1];
        String sender = tokens[2];
        String time = tokens[3];
        String emojiFileName = tokens[4];

        if (!ServerApp.chatRooms.containsKey(chatRoomId)) {
            out.println("/sendemoji fail 존재하지 않는 채팅방입니다.");
            return;
        }

        ChatRoom chatRoom = ServerApp.chatRooms.get(chatRoomId);
        boolean isMember = chatRoom.getMembers().stream().anyMatch(u -> u.getLoginID().equals(sender));
        if (!isMember) {
            out.println("/sendemoji fail 채팅방 멤버가 아닙니다.");
            return;
        }

        String emojiFilePath = "src/Resources/emojis/" + emojiFileName;
        File emojiFile = new File(emojiFilePath);
        if (!emojiFile.exists()) {
            out.println("/sendemoji fail 이모티콘 파일이 존재하지 않습니다.");
            return;
        }

        String formattedMessage = "/sendemoji " + chatRoomId + " " + sender + " " + time + " " + emojiFilePath;
        chatRoom.addMessage(formattedMessage);

        for (User member : chatRoom.getMembers()) {
            if (ServerApp.onlineUsers.containsKey(member.getLoginID())) {
                ServerApp.onlineUsers.get(member.getLoginID()).sendMessage(formattedMessage);
            }
        }

        System.out.println("[개발용] : sendemoji 성공: " + formattedMessage);
    }

    private void handleLogout() {
        ServerApp.onlineUsers.remove(loginID);
        out.println("/logout success 로그아웃 되었습니다.");
    }

    private void handleAddMemo(String msg) {
        String[] tokens = msg.split(" ", 2);
        if (tokens.length != 2) {
            out.println("/addmemo fail 잘못된 형식");
            return;
        }
        String memoContent = tokens[1].replace("_", " ");
        user.addMemo(memoContent);
        out.println("/addmemo success");
    }

    private void handleEditMemo(String msg) {
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

    private void handleDeleteMemo(String msg) {
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

    private void handleGetMemos(String msg) {
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
        System.out.println("[개발용] : 서버 "+ loginID +" 메모 목록 : " + memos);
        out.println("/memosstart");
        for (int i = 0; i < memos.size(); i++) {
            out.println("/memo " + i + " " + memos.get(i).replace(" ", "_"));
        }
        out.println("/memosend");
    }
}
