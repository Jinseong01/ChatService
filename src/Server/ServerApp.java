package Server;

import Model.ChatRoom;
import Model.User;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class ServerApp {
    private static final int PORT = 12345;

    // 이용자 목록 : { loginID : User }
//    public static Map<String, User> userCredentials = new ConcurrentHashMap<>();
    public static Map<String, User> userCredentials = new ConcurrentHashMap<String, User>() {{
        put("test", new User("test", "1234", "홍길동", "2001-01-01", "닉네임", "정보"));
        put("tmp1", new User("tmp1", "1234", "황기태", "2001-02-21", "닉네임", "정보"));
        put("tmp2", new User("tmp2", "1234", "허준영", "2001-01-51", "닉네임", "정보"));
    }};

    // 현재 접속자 목록 : { loginID : ClientHandler }
    public static Map<String, UserHandler> onlineUsers = new ConcurrentHashMap<>();

    // 채팅방 : { chatRoomId : ChatRoom }
    public static Map<String, ChatRoom> chatRooms = new ConcurrentHashMap<>();

    // 친구 요청 : { loginID : ["친구A", "친구B", "친구C"] }
    public static Map<String, Set<String>> friendRequests = new ConcurrentHashMap<>();

    static {
        // test 계정에 기본 친구 추가 (tmp1, tmp2)
        User testUser = userCredentials.get("test");
        testUser.getFriends().add("tmp1");
        testUser.getFriends().add("tmp2");
        friendRequests.put("test", ConcurrentHashMap.newKeySet());

        User testUser2 = userCredentials.get("tmp1");
        testUser2.getFriends().add("test");

        User testUser3 = userCredentials.get("tmp2");
        testUser3.getFriends().add("test");

        // test 계정에 기본 메모 추가
        testUser.getMemos().add("기본 메모 1");
        testUser.getMemos().add("기본 메모 2");
    }

    public static void main(String[] args) {
        System.out.println("서버 시작 중...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("클라이언트 연결: " + socket.getInetAddress());
                new UserHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
