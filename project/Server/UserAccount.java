package Server;

public class UserAccount
{
    private String name; //default nickname
    private int id; //universal ID
    private String password; //if password protected
    private boolean isAdmin; //if the user has elevated privileges
    private boolean isBanned; //if the user is banned from joining
    private ChatServerThread thread; //the thread this user is using

    public UserAccount(String n, String pw, int i, boolean admin) //called when new account created
    {
        name = n; password = pw; id = i; isAdmin = admin; isBanned = false;
    }
    
    public UserAccount(String n, String pw, int i, boolean admin, boolean banned) //called when account loaded from file
    {
        name = n; password = pw; id = i; isAdmin = admin; isBanned = banned;
    }
    
    public String getName(){ return name; }
    public int getID(){ return id; }
    public boolean getIsAdmin(){ return isAdmin; }
    public String getPassword(){ return password; }
    public boolean getIsBanned(){ return isBanned; }
    public ChatServerThread getThread(){ return thread; }
    
    public void setName(String n){ name = n; }
    public void setID(int i){ id = i; }
    public void setIsAdmin(boolean admin){ isAdmin = admin; }
    public void setPassword(String pw){ password = pw; }
    public void setIsBanned(boolean b){ isBanned = b; }
    public void setThread(ChatServerThread t){ thread = t; }
    
    public int hashCode(){ return id; }
}
