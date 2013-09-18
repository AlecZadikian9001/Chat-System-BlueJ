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
    private ChatServerChatRoom mainRoom;
    
    /* This is the server socket to accept connections */
        private ServerSocket serverSocket;
    
    private boolean isRunning; //is the server up?

    public static void main (String[] args){
        ChatServerMain main = new ChatServerMain(args);
    }

    public ChatServerMain(String [] args) {
        isRunning = true;
        databaseSetup();

        /* Check port exists */
        if (args.length < 1) {
            System.out.println("Usage: ChatServerMain <port>");
            System.exit(1);
        }

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
        while (isRunning) {
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
                System.out.println("Server has stopped running.");
                System.exit(1);
            }
        }
        System.out.println("Server has stopped running.");
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
            account.getThread().disconnect("I am now signing in from a different location.");
        }
        return account;
    }

    private void databaseSetup(){ //import data from persistent stores
        //Initialize chat rooms
        chatRooms = new ArrayList<ChatServerChatRoom>();
        users = new TreeMap();
        load();
        double randomPassword = Math.random();
        users.put("admin", new UserAccount("Admin", ""+randomPassword, 0, true)); //the default admin user
        System.out.println("Admin login is the username \"Admin\" and the password \""+randomPassword+"\" without quotes.");
        mainRoom = new ChatServerChatRoom("Main", 0); //TEMPORARY
        chatRooms.add(mainRoom); //0 index is main lobby
    }

    public void quit(){ //called when closing server down so data is saved to persistent stores
        System.out.println("Shutting down server...");
        save();
        System.exit(0);
   //     for (ChatServerChatRoom chatRoom : chatRooms){ //close all the chat rooms
   //         chatRoom.shutDown();
   //     }
   //     save();
   //     try{
   //     serverSocket.close(); //closes the socket
   // } catch (Exception e){ System.out.println(e); isRunning = false; /*the final blow, if needed*/ }
    }

    private void save(){
        System.out.println("Saving files...");
        PrintWriter out = null;
        try{
            File file = new File("users.txt");
            file.createNewFile();
            FileOutputStream output = new FileOutputStream(file.getPath());
            System.out.println("FileOutputStream path set to "+file.getPath());
            out = new PrintWriter(
                new BufferedWriter(
                    new OutputStreamWriter(output, "UTF-8")));
        } catch (Exception e) { System.out.println(e); }
        for (String name : users.keySet()){ //saving each user account into
            if (!name.equalsIgnoreCase("Admin")){ //don't want to save the default admin account
            System.out.println("Saving user to key "+name);
            out.println(name); //name key to open
            out.println("{");  //then open bracket for holding the attributes
            UserAccount user = users.get(name); //getting the account...
            out.println(user.getName()); //name first
            out.println(""+user.getID()); //then id
            out.println(user.getPassword()); //then password
            if (user.getIsAdmin()) out.println("1"); else out.println("0"); //admin status: 1 for true, 0 for false
            if (user.getIsBanned()) out.println("1"); else out.println("0"); //ban status: 1 for true, 0 for false
            out.println("}");  //close the user entry
        }
        }
        out.flush();
        out.close();
    }

    private boolean load(){ //returns true if loaded, false if new files created
        System.out.println("Loading files...");
        FileInputStream input = null;
        try{
            File file = new File("users.txt");
            if(!file.exists()){
                file.createNewFile();
                return false;
            }
            input = new FileInputStream(file.getPath());
        } catch (Exception e) { System.out.println(e); }
        Scanner scanner = new Scanner(input);
        //variables used for each user, pre-defined to prevent unneeded garbage collection
        String nameKey, name, password, boolCheck; int id; boolean isAdmin, isBanned;
        while (scanner.hasNextLine()){
            nameKey = scanner.nextLine();
            scanner.nextLine(); //opening bracket is skipped
            name = scanner.nextLine();
            id = Integer.parseInt(scanner.nextLine());
            password = scanner.nextLine();
            boolCheck = scanner.nextLine();
            isAdmin = (boolCheck.equals("1"));
            boolCheck = scanner.nextLine();
            isBanned = (boolCheck.equals("1"));
            scanner.nextLine(); //closing bracket is skipped
            //now to make the user...
            users.put(nameKey, new UserAccount(name, password, id, isAdmin, isBanned));
            System.out.println("Loaded user for key "+nameKey);
        }
        return true;
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
