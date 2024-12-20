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
    public static Map<String, User> userCredentials = new ConcurrentHashMap<String, User>() {{
        put("test", new User("test", "1234", "짱구", "2001-01-01",  "허허이"));
        put("tmp1", new User("tmp1", "1234", "철수", "2001-02-21",  "학원..."));
        put("tmp2", new User("tmp2", "1234", "유리", "2001-01-51",  "인형"));
    }};

    // 현재 접속자 목록 : { loginID : ClientHandler }
    public static Map<String, UserHandler> onlineUsers = new ConcurrentHashMap<>();

    // 채팅방 : { chatRoomId : ChatRoom }
    public static Map<String, ChatRoom> chatRooms = new ConcurrentHashMap<>();

    // 친구 요청 : { loginID : ["친구A", "친구B", "친구C"] }
    public static Map<String, Set<String>> friendRequests = new ConcurrentHashMap<>();

    static {
        User testUser = userCredentials.get("test");
        friendRequests.put("test", ConcurrentHashMap.newKeySet());

        User testUser2 = userCredentials.get("tmp1");
        friendRequests.put("tmp1", ConcurrentHashMap.newKeySet());

        User testUser3 = userCredentials.get("tmp2");
        friendRequests.put("tmp2", ConcurrentHashMap.newKeySet());

        // test 계정에 기본 메모 추가
        testUser.getMemos().add("내일 유치원에서 술래잡기");
        testUser.getMemos().add("친구들과 약속");
    }

    public static void main(String[] args) {
        System.out.println("[서버 시작]");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[클라이언트 연결] : " + socket.getInetAddress());
                new UserHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
