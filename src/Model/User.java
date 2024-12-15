package Model;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class User {
    private String loginID;       // 로그인 ID
    private String loginPW;       // 로그인 PW
    private String userName;      // 사용자 이름
    private String birthday;      // 생일
    private String nickname;      // 닉네임
    private String information;   // 상태 메시지
    private Set<String> friends;  // 친구 목록
    private List<String> memos;   // 메모 목록

    public User(String loginID, String loginPW, String userName, String birthday, String nickname, String information) {
        this.loginID = loginID;
        this.loginPW = loginPW;
        this.userName = userName;
        this.birthday = birthday;
        this.nickname = nickname;
        this.information = information;
        this.friends = ConcurrentHashMap.newKeySet();
        this.memos = new CopyOnWriteArrayList<>();
    }

    // Getters and Setters
    public String getLoginID() {
        return loginID;
    }

    public String getLoginPW() {
        return loginPW;
    }

    public String getUserName() {
        return userName;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getNickname() {
        return nickname;
    }

    public String getInformation() {
        return information;
    }

    public Set<String> getFriends() {
        return friends;
    }

    public List<String> getMemos() {
        return memos;
    }


    // 친구 추가
    public boolean addFriend(String friendLoginID) {
        return friends.add(friendLoginID);
    }

    // 친구 제거
    public boolean removeFriend(String friendLoginID) {
        return friends.remove(friendLoginID);
    }

    // 메모 추가
    public void addMemo(String memoContent) {
        memos.add(memoContent);
    }

    // 메모 수정
    public boolean editMemo(int index, String newContent) {
        if (index < 0 || index >= memos.size()) {
            return false;
        }
        memos.set(index, newContent);
        return true;
    }

    // 메모 삭제
    public boolean deleteMemo(int index) {
        if (index < 0 || index >= memos.size()) {
            return false;
        }
        memos.remove(index);
        return true;
    }

    @Override
    public String toString() {
        return "User {\n" +
                "  loginID='" + loginID + "',\n" +
                "  loginPW='" + loginPW + "',\n" +
                "  userName='" + userName + "',\n" +
                "  birthday='" + birthday + "',\n" +
                "  nickname='" + nickname + "',\n" +
                "  information='" + information + "',\n" +
                "  friends=" + friends + ",\n" +
                "  memos=" + memos + "\n" +
                "}";
    }
}
