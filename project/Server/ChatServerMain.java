package Server;
import java.net.*;
import java.util.*;
import java.io.*;

public class ChatServerMain{
    //the database of all users, synced with a persistent store
    private TreeMap<String, UserAccount> users;
    
    //the database of all chat rooms, synced with a persistent store
    private ArrayList<ChatServerChatRoom> chatRooms;
    
    //the main lobby
    ChatServerChatRoom mainRoom;

    public static void main (String[] args){
        ChatServerMain main = new ChatServerMain(args);
    }

    public ChatServerMain(String [] args) {
        databaseSetup();

        /* Check port exists */
        if (args.length < 1) {
            System.out.println("Usage: ChatServerMain <port>");
            System.exit(1);
        }

        /* This is the server socket to accept connections */
        ServerSocket serverSocket = null;

        /* Create the server socket */
        try {
            serverSocket = new ServerSocket(Integer.parseInt(args[0]));
            System.out.println(serverSocket); //debug
        } catch (IOException e) {
            System.out.println("IOException: " + e);
            System.exit(1);
        }
        System.out.println("1");
        /* In the main thread, continuously listen for new clients and spin off threads for them. */
        while (true) {
            try {
                 System.out.println("2");
                /* Get a new client */
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from "+clientSocket.getInetAddress().toString());
                /* Create a thread for it and start, giving it the right id. */
                ChatServerThread clientThread = new ChatServerThread(clientSocket, null, mainRoom, this); //null user because the thread has to take the username/password
                clientThread.start();
                chatRooms.get(0).addThread(clientThread); //0 index is the main lobby
                //clientThread.start();
            } catch (IOException e) {
                System.out.println("Accept failed: " + e);
                System.exit(1);
            }
        }
    }
    
    public UserAccount handleLogin(String name, String password){ //returns null if error
        if (name==null || password==null){ System.out.println("ERROR IN handleLogin"); return null; }
        UserAccount account = users.get(name.toLowerCase());
        if (account==null){ //if a new user accout must be created
            if (!name.matches("[A-Za-z][A-Za-z0-9_]+")) return null; //invalid username since it can only contain letters, numbers, and underscores
            account = new UserAccount(name, password, users.size(), false);
            users.put(name.toLowerCase(), account); //to make name mappings non-case-sensitive
            return account;
        }
        if (!account.getPassword().equals(password)) return null; //if user exists, but login fails
        if (account.getThread()!=null && account.getThread().isAlive()){ //if user is already logged in, and login succeeds
            System.out.println("Multiple login.");
            account.getThread().disconnect("I am now signing in from a different location.");
          // return null; //TEMPORARY!!! TODO
        }
        return account;
    }
    
    public void databaseSetup(){ //import data from persistent stores
        //Initialize chat rooms
        chatRooms = new ArrayList<ChatServerChatRoom>();
        users = new TreeMap();
        
        //TODO read in files
        //this is temporary
        mainRoom = new ChatServerChatRoom("Main", 0);
        chatRooms.add(mainRoom); //0 index is main lobby
    }
    
    public void quit(){ //called when closing server down so data is saved to persistent stores
        
    }

    public boolean addChatRoom(String name, String greeting){
        for (ChatServerChatRoom room : chatRooms) if (room.getName().equalsIgnoreCase(name)) return false;
        ChatServerChatRoom room = new ChatServerChatRoom(name, chatRooms.size());
        chatRooms.add(room);
        return true;
    }
    
  /*  private boolean addUser(String name, String password, boolean admin){
        if (users.contains(name)) return false;
        users.put(name, new UserAccount(name, password, users.size(), admin));
        return true;
    } */
    
    public boolean banUser(String name){ //ban a user by name
        UserAccount user = users.get(name.toLowerCase()); if (user==null) return false;
        user.setIsBanned(true);
        disconnectUser(user);
        return true;
    }
    
    public boolean unbanUser(String name){ //unban a user by name
        UserAccount user = users.get(name.toLowerCase()); if (user==null) return false;
        user.setIsBanned(false);
        return false;
    }
    
    public boolean disconnectUser(UserAccount account){
        ChatServerThread thread = account.getThread();
        if (thread==null) return false;
        thread.forceDisconnect(); return true;
    }
    
    public boolean changeUserName(String oldName, String newName){
        if (users.get(newName.toLowerCase())!=null) return false;
        UserAccount account = users.remove(oldName.toLowerCase());
        account.setName(newName);
        users.put(newName.toLowerCase(), account);
        return true;
    }
    
    public boolean tellUser(String sender, String target, String message){
        UserAccount user = users.get(target.toLowerCase());
        ChatServerThread thread = user.getThread();
        if (thread==null) return false;
        thread.tell(sender, message);
        return true;
       }
}
