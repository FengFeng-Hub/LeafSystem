package leaf.system.pojo;

public class SysCurrentUser {
    /**
     * 前台用户代码
     */
    private String frontendUserId;
    /**
     * 后台用户代码
     */
    private String backendUserId;

    public SysCurrentUser() {
    }

    public SysCurrentUser(String frontendUserId, String backendUserId) {
        this.frontendUserId = frontendUserId;
        this.backendUserId = backendUserId;
    }

    public String getFrontendUserId() {
        return frontendUserId;
    }

    public void setFrontendUserId(String frontendUserId) {
        this.frontendUserId = frontendUserId;
    }

    public String getBackendUserId() {
        return backendUserId;
    }

    public void setBackendUserId(String backendUserId) {
        this.backendUserId = backendUserId;
    }

    @Override
    public String toString() {
        return "SysCurrentUser{" +
                "frontendUserId='" + frontendUserId + '\'' +
                ", backendUserId='" + backendUserId + '\'' +
                '}';
    }
}
