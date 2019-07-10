package com.haothink.common.web.session;


public class SessionBean extends AbstractSession {

    private Integer sid; //用户id
    private Integer orgId; //所在机构id
    private String username; //登陆名 唯一
    private String userNick; //登陆昵称 唯一
    private String phone; //手机号码
    private int status; //状态: 0 冻结;1 可用 ;2 删除

    public SessionBean(Integer sid, Integer orgId, String username, String userNick, String phone, int status) {
        this.sid = sid;
        this.orgId = orgId;
        this.username = username;
        this.userNick = userNick;
        this.phone = phone;
        this.status = status;
    }

    public Integer getSid() {
        return sid;
    }

    public void setSid(Integer sid) {
        this.sid = sid;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserNick() {
        return userNick;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SessionBean{" +
                "sid=" + sid +
                ", orgId=" + orgId +
                ", username='" + username + '\'' +
                ", userNick='" + userNick + '\'' +
                ", phone='" + phone + '\'' +
                ", status=" + status +
                '}';
    }
}
