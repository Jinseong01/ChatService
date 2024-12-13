// User.java
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String loginID;
    private String loginPW;
    private String userName;
    private String birthday;
    private String nickname;
    private String information;
    private List<String> memos;

    public User(String loginID, String loginPW, String userName, String birthday, String nickname, String information) {
        this.loginID = loginID;
        this.loginPW = loginPW;
        this.userName = userName;
        this.birthday = birthday;
        this.nickname = nickname;
        this.information = information;
        this.memos = new ArrayList<>();
    }

    // Getter and Setter methods
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

    public List<String> getMemos() {
        return memos;
    }

    public void addMemo(String memo) {
        this.memos.add(memo);
    }

    public void editMemo(int index, String newMemo) {
        if (index >= 0 && index < memos.size()) {
            this.memos.set(index, newMemo);
        }
    }
}
