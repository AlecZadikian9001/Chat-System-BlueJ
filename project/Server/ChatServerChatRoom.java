package Server;

import java.util.ArrayList;

public class ChatServerChatRoom {
    //the name of this chat room
    private String name;
    //the greeting message displayed when a user enters
    private String greeting;
    //Is this chat room in tinfoil hat mode?
    private boolean encrypted;
    //unique ID
    private int id;

    //ChatServerThread threads stored here:
    private ArrayList<ChatServerThread> threads;

    public ChatServerChatRoom(String n, String g, int i, boolean e){
        name = n;
        greeting = g;
        id = i;
        encrypted = e;
        threads = new ArrayList<ChatServerThread>();
    }

    public void addThread(ChatServerThread thread){ //replaces the first dead thread with this one, taking its id
        int count = threads.size(); boolean found = false;
        for (int i = 0; i<count; i++){
            if (!threads.get(i).isAlive()){
                thread.setID(i);
                thread.setUserName(""+i);
                threads.remove(i);
                threads.add(i, thread);
                System.out.println("New thread named "+thread.getUserName()+" added and ID set to "+i+".");
                found = true;
                break;
            }
        }
        if (!found){
            threads.add(thread);
            thread.setID(count);
            thread.setUserName(""+count);
            System.out.println("New thread named "+thread.getUserName()+" added and ID set to "+count+".");
        }
        thread.tell("You've joined the chat room "+name+".", "Server Message");
        thread.tell(greeting);
        tellEveryone(""+thread.getUserName()+" joined the room.", -1, "Server Message"); //id -1 reserved for server messages
    }

    public String getName(){ return name; }

    public int getID(){ return id; }

    public void tellEveryone(String name, int userID, String message){ //general chat
        if (a==null || a.length()==0) return;
        for (ChatServerThread thread : threads){
            threads.get(i).tell(name, message);
        }
    }

    /*  public boolean changeUserName(String name, int id){
    for (ChatServerThread thread : threads){
    if (thread.getUserName().equalsIgnoreCase(name)) return false;
    }
    threads.get(id).setUserName(name);
    return true;
    } */
}
