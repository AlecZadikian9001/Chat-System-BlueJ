package Server;
import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class ChatServerMain{
    //the database of all users, synced with a persistent store
    private TreeSet<UserAccount> users;
    
    //the database of all chat rooms, synced with a persistent store
    private ArrayList<ChatServerChatRoom> chatRooms;

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

        /* In the main thread, continuously listen for new clients and spin off threads for them. */
        while (true) {
            try {
                /* Get a new client */
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from "+clientSocket.getInetAddress().toString());
                /* Create a thread for it and start, giving it the right id. */
                ChatServerThread clientThread = new ChatServerThread(clientSocket, null, chatRoom, this); //null user because the thread has to take the username/password
                chatRooms.get(0).addThread(clientThread); //0 index is the main lobby
                clientThread.start();
            } catch (IOException e) {
                System.out.println("Accept failed: " + e);
                System.exit(1);
            }
        }
    }
    
    public void databaseSetup(){
        //Initialize chat rooms
        chatRooms = new ArrayList<ChatServerChatRoom>();
        
        //TODO read in files
        //this is temporary:
        ChatServerChatRoom mainRoom = new ChatServerChatRoom("Main", "This is the main lobby.", false);
        chatRooms.add(mainRoom); //0 index is main lobby
    }

    public boolean addChatRoom(String name, String greeting, boolean encrypted){
        ChatServerChatRoom room = new ChatServerChatRoom(name, greeting, encrypted);
        chatRooms.add(room);
    }
    
    public boolean addUser(String name, String password, boolean admin){
        for (UserAccount user : users){
            if (user.getName().equalsIgnoreCase(name)) return false;
        }
        users.add(new UserAccount(name, password, users.size(), admin));
        return true;
    }
    
    public boolean banUser(String name){ //ban a user by name
        UserAccount target;
        for (UserAccount user : users){
            if (user.getName().equalsIgnoreCase(name)){ target = user; break; }
        }
        user.setBanned(true);
        disconnectUser(user);
        return true;
    }
    
    public boolean unbanUser(String name){ //unban a user by name
        for (UserAccount user : users){
            if (user.getName().equalsIgnoreCase(name)){ user.setBanned(false); return true; }
        }
        return false;
    }
    
    public boolean disconnectUser(UserAccount account){
        account.getThread().disconnect(null);
    }
    
    public boolean changeUserName(String newName, int id){
        for (UserAccount user : users){
            if (user.getName().equalsIgnoreCase(newName)) return false;
        }
		users.get(id).setUserName(newName);
		return true;
	}
}
