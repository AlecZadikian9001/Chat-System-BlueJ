package Server;
import java.net.*;
import java.util.*;
import java.io.*;

public class ChatServerMain{
    //the database of all users, synced with a persistent store
    private TreeMap<String, UserAccount> users;

    //the database of all chat rooms, synced with a persistent store
    private TreeMap<String, ChatServerChatRoom> chatRooms;

    //the main lobby
    private ChatServerChatRoom mainRoom;

    private TreeMap<String, AudioThread> audioRooms; //key string is the initiator and guest separated by space character

    /* This is the server socket to accept connections */
    private ServerSocket serverSocket;
    private int port; //port number we are listening on

    private boolean isRunning; //is the server up?

    public static void main (String[] args){
        ChatServerMain main = new ChatServerMain(Integer.parseInt(args[0]));
    }

    public ChatServerMain(int portNumber) {
        isRunning = true;
        audioRooms = new TreeMap<String, AudioThread>();
        databaseSetup();

        /* Create the server socket */
        try {
            port = portNumber;
            serverSocket = new ServerSocket(portNumber);
            System.out.println(serverSocket); //debug
        } catch (IOException e) {
            System.out.println("IOException: " + e);
            System.exit(1);
        }
        /* In the main thread, continuously listen for new clients and spin off threads for them. */
        while (isRunning) {
            try {
                /* Get a new client */
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from "+clientSocket.getInetAddress().toString());
                /* Create a thread for it and start, giving it the right id. */
                ChatServerThread clientThread = new ChatServerThread(clientSocket, null, mainRoom, this); //null user because the thread has to take the username/password
                clientThread.start();
                chatRooms.get("main").addThread(clientThread); //0 index is the main lobby
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
        if (account.getIsBanned()) return null;
        return account;
    }

    private void databaseSetup(){ //import data from persistent stores
        //Initialize chat rooms
        chatRooms = new TreeMap<String, ChatServerChatRoom>();
        users = new TreeMap();
        load();
        double randomPassword = Math.random();
        users.put("admin", new UserAccount("Admin", ""+randomPassword, 0, true)); //the default admin user
        System.out.println("Admin login is the username \"Admin\" and the password \""+randomPassword+"\" without quotes.");
        mainRoom = new ChatServerChatRoom("Main", 0); //TEMPORARY
        chatRooms.put("main", mainRoom); //0 index is main lobby
    }

    public void quit(){ //called when closing server down so data is saved to persistent stores
        System.out.println("Shutting down server...");
        Object lock = new Object();
        //synchronized(lock){
            save();
        //}
        //synchronized(lock){
            System.exit(0);
        //}
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
        try{
            File file = new File("rooms.txt");
            file.createNewFile();
            FileOutputStream output = new FileOutputStream(file.getPath());
            System.out.println("FileOutputStream path set to "+file.getPath());
            out = new PrintWriter(
                new BufferedWriter(
                    new OutputStreamWriter(output, "UTF-8")));
        } catch (Exception e) { System.out.println(e); }
        for (String name : chatRooms.keySet()){ //saving each chat room info
            if (!name.equalsIgnoreCase("Main")) //don't want to save the default main room
            {
                System.out.println("Saving room to key "+name);
                out.println(name); //name key to open
                out.println("{");  //then open bracket for holding the attributes
                ChatServerChatRoom room = chatRooms.get(name);
                out.println(room.getName()); //name first
                out.println(""+room.getID()); //then id
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

        input = null;
        try{
            File file = new File("rooms.txt");
            if(!file.exists()){
                file.createNewFile();
                return false;
            }
            input = new FileInputStream(file.getPath());
        } catch (Exception e) { System.out.println(e); }
        scanner = new Scanner(input);
        //variables used for each user, pre-defined to prevent unneeded garbage collection
        //String nameKey, name; int id;
        while (scanner.hasNextLine()){
            nameKey = scanner.nextLine();
            scanner.nextLine(); //opening bracket is skipped
            name = scanner.nextLine();
            id = Integer.parseInt(scanner.nextLine());
            scanner.nextLine(); //closing bracket is skipped
            //now to make the user...
            chatRooms.put(nameKey, new ChatServerChatRoom(name, id));
            System.out.println("Loaded room for key "+nameKey);
        }
        return true;
    }

    public boolean addRoom(String name){
        if (chatRooms.get(name.toLowerCase())!=null) return false;
        ChatServerChatRoom room = new ChatServerChatRoom(name, chatRooms.size());
        chatRooms.put(name.toLowerCase(), room);
        System.out.println("New room added: "+name);
        return true;
    }

    public boolean removeRoom(String name){
        if (name.equalsIgnoreCase("main")) return false;
        ChatServerChatRoom room = chatRooms.remove(name.toLowerCase());
        if (room==null) return false;
        room.close();
        //room.dealloc //oh wait no, we can't do that
        System.out.println("Room killed: "+name);
        return true;
    }

    public boolean changeRoom(String user, String room){
        UserAccount account = users.get(user.toLowerCase());
        ChatServerChatRoom room2 = chatRooms.get(room);
        if (account==null || room2 ==null) return false;
        ChatServerThread thread = account.getThread();
        if (thread==null) return false;
        thread.getRoom().removeThread(thread);
        room2.addThread(thread);
        thread.setRoom(room2);
        System.out.println(""+user+" changed to room "+room);
        return true;
    }

    public String getRoomNames(){
        Set<String> keys = chatRooms.keySet();
        StringBuffer ret = new StringBuffer(keys.toString().length()+11);
        ret.append("Chat rooms: [");
        for (String key : keys){
            ret.append(""+chatRooms.get(key).getName()+", ");
        }
        ret.delete(ret.length()-2, ret.length());
        ret.append("]");
        return ret.toString();
    }

    /*  private boolean addUser(String name, String password, boolean admin){
    if (users.contains(name)) return false;
    users.put(name, new UserAccount(name, password, users.size(), admin));
    return true;
    } */

    public boolean promoteUser(String name){ //op a user by name
        UserAccount user = users.get(name.toLowerCase()); if (user==null) return false;
        user.setIsAdmin(true);
        if (user.getThread()!=null) user.getThread().send("You are now op!");
        return true;
    }

    public boolean demoteUser(String name){ //deop a user by name
        if (name.equalsIgnoreCase("admin")) return false; //can't demote the admin
        UserAccount user = users.get(name.toLowerCase()); if (user==null) return false;
        user.setIsAdmin(false);
        if (user.getThread()!=null) user.getThread().send("You are no longer op.");
        return true;
    }

    public boolean banUser(String name){ //ban a user by name
        if (name.equalsIgnoreCase("admin")) return false; //can't ban the admin
        UserAccount user = users.get(name.toLowerCase()); if (user==null) return false;
        user.setIsBanned(true);
        disconnectUser(user);
        return true;
    }

    public boolean unbanUser(String name){ //unban a user by name
        UserAccount user = users.get(name.toLowerCase()); if (user==null) return false;
        user.setIsBanned(false);
        return true;
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
        if (user==null) return false;
        ChatServerThread thread = user.getThread();
        if (thread==null || !thread.isAlive()) return false;
        thread.tell(sender, message);
        return true;
    }

    public boolean audioChat(String sender, String target){ //this needs to be stress-tested and bug-fixed TODO
        if (sender.equalsIgnoreCase(target)) return false; //can't do audio chat with yourself!
        AudioThread thread = audioRooms.get(""+sender+" "+target);
        if (thread!=null){ return true; } //if this is an acceptance of a previously sent audio chat
        UserAccount user = users.get(target.toLowerCase());
        if (user==null) { System.out.println("Audio chat failed: cause 2"); return false; }
        ChatServerThread userThread = user.getThread();
        if (userThread==null){ System.out.println("Audio chat failed: cause 3"); return false; }
        AudioThread audioThread = new AudioThread(port+1); //default port 9001 for audio chats
        audioRooms.put(""+sender+" "+target, audioThread);
        audioThread.start();
        boolean ret = userThread.audioChat(sender);
        if (!ret){ System.out.println("Audio chat failed: cause 4"); return false; }
        return ret;
    }

    public void endAudioChat(String chat){
        AudioThread thread = audioRooms.get(chat);
        if (thread==null) return;
        Scanner scanner = new Scanner(chat); String sender = scanner.next(); String target = scanner.next();
        ChatServerThread senderThread = users.get(sender).getThread(); if (senderThread!=null) senderThread.send("/decline"); //to tell the client to stop the chat
        ChatServerThread targetThread = users.get(sender).getThread(); if (targetThread!=null) targetThread.send("/decline"); //to tell the client to stop the chat
        thread.stopRunning();
    }
}
