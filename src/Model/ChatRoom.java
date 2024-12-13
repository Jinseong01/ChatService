package Model;// Model.ChatRoom.java

import java.util.*;
import java.util.concurrent.*;

public class ChatRoom {
    private String id; // 채팅방 고유ID
    private String name; // 채팅방 이름
    private Set<User> members; // 채팅방 참여자 목록
    private List<String> chatHistory; // 채팅방 대화 내용

    public ChatRoom(String id, String name, Set<User> members) {
        this.id = id;
        this.name = name;
        this.members = ConcurrentHashMap.newKeySet();
        this.members.addAll(members);
        this.chatHistory = Collections.synchronizedList(new ArrayList<>());
    }

    // Getter&Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<User> getMembers() {
        return members;
    }

    public void setMembers(Set<User> members) {
        this.members = members;
    }

    public List<String> getChatHistory() {
        return chatHistory;
    }

    public void setChatHistory(List<String> chatHistory) {
        this.chatHistory = chatHistory;
    }

    // 채팅 내용 추가
    public void addMessage(String message) {
        chatHistory.add(message);
    }
}
