package pl.multitalk.android.datatypes;

/**
 * Informacje o użytkowniku
 * @author Michał Kołodziejski
 */
public class UserInfo {

    private String uid;
    private String username;
    private String macAddress;
    private String ipAddress;

    public UserInfo(){
        this.uid = null;
        this.username = null;
        this.macAddress = null;
        this.ipAddress = null;
    }

    public UserInfo(String uid, String username, String ipAddress) {
        this.uid = uid;
        this.username = username;
        this.ipAddress = ipAddress;
        this.macAddress = null;
    }

    public UserInfo(String uid, String username, String macAddress, String ipAddress) {
        this.uid = uid;
        this.username = username;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
    }
    
    public UserInfo(UserInfo userInfo){
        this.uid = (userInfo.uid != null)? new String(userInfo.uid) : null;
        this.username = (userInfo.username != null)? new String(userInfo.username) : null;
        this.macAddress = (userInfo.macAddress != null)? new String(userInfo.macAddress) : null;
        this.ipAddress = (userInfo.ipAddress != null)? new String(userInfo.ipAddress) : null;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    
    @Override
    public boolean equals(Object o) {
        if(o == null)
            return false;
        
        if(this == o){
            return true;
        }
        
        if(!(o instanceof UserInfo))
            return false;
        
        UserInfo oUI = (UserInfo) o;
        if(this.getUid() == null || oUI.getUid() == null)
            return false;
        
        return this.getUid().equals(oUI.getUid());
    }
    
    
    @Override
    public int hashCode() {
        if(getIpAddress() == null){
            // whatever
            return 123;
        }
        return getIpAddress().hashCode();
    }
}
