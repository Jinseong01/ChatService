// ChatRoom.java
import java.util.*;
import java.util.concurrent.*;

public class ChatRoom {
    private String id;
    private String name;
    private Set<User> members;
    private List<String> chatHistory;

    public ChatRoom(String id, String name, Set<User> members) {
        this.id = id;
        this.name = name;
        this.members = ConcurrentHashMap.newKeySet();
        this.members.addAll(members);
        this.chatHistory = Collections.synchronizedList(new ArrayList<>());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<User> getMembers() {
        return members;
    }

    public List<String> getChatHistory() {
        return chatHistory;
    }

    public void addMessage(String message) {
        chatHistory.add(message);
    }
}
