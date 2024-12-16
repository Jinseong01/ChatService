package Model;

public class Friend {
    private String loginID;       // 로그인 ID
    private String userName;      // 사용자 이름
    private String information;   // 상태 메시지
    private String profileImage;  // 프로필 이미지

    public Friend(String loginID, String userName, String information, String profileImage) {
        this.loginID = loginID;
        this.userName = userName;
        this.information = information;
        this.profileImage = profileImage;
    }

    public String getLoginID() {
        return loginID;
    }

    public void setLoginID(String loginID) {
        this.loginID = loginID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
