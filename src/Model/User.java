package Model;// Model.User.java

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class User {
    private String loginID; // 로그인ID
    private String loginPW; // 로그인PW
    private String userName; // 사용자 이름
    private String birthday; // 생일
    private String nickname; // 닉네임
    private String information; // 상태 메시지(?)
    private Set<String> friends; // 친구 목록
    private List<String> memos; // 메모 목록

    public User(String loginID, String loginPW, String userName, String birthday, String nickname, String information) {
        this.loginID = loginID;
        this.loginPW = loginPW;
        this.userName = userName;
        this.birthday = birthday;
        this.nickname = nickname;
        this.information = information;
        this.memos = new ArrayList<>();
    }

    // Getter & Setter
    public String getLoginID() {
        return loginID;
    }

    public void setLoginID(String loginID) {
        this.loginID = loginID;
    }

    public String getLoginPW() {
        return loginPW;
    }

    public void setLoginPW(String loginPW) {
        this.loginPW = loginPW;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public Set<String> getFriends() {
        return friends;
    }

    public void setFriends(Set<String> friends) {
        this.friends = friends;
    }

    public List<String> getMemos() {
        return memos;
    }

    public void setMemos(List<String> memos) {
        this.memos = memos;
    }

    // 친구 목록 관련


    // 메모 목록 관련
    public void addMemo(String memo) {
        this.memos.add(memo);
    }

    public void editMemo(int index, String newMemo) {
        if (index >= 0 && index < memos.size()) {
            this.memos.set(index, newMemo);
        }
    }
}
