package Server;
import java.net.*;
import java.util.Scanner;
import java.io.*;
import Common.Encryptor;

public class ChatServerThread extends Thread {
    /* The client socket and IO we are going to handle in this thread */
    protected Socket         socket;
    protected PrintWriter    out;
    protected BufferedReader in;

    //the user this thread corresponds to
    private UserAccount user;

    //chat room this belongs to
    private ChatServerChatRoom chatRoom;
    //server this belongs to
    private ChatServerMain chatServer;

    //id specific to room
    private int id;
    
    private boolean pendingAudioChatRequest; //pending request?

    public static final boolean ENCRYPTED = true; //encryption enabled? false for server debug

    public boolean isLoggedIn(){ return (user!=null); }

    public int getID(){ return id; }

    public void setID(int a){ id = a; }

    public String getUserName(){ if (user!=null) return user.getName(); return null; }

    public void setUserName(String n){ user.setName(n); }

    public ChatServerChatRoom getChatRoom(){ return chatRoom; }

    public ChatServerThread(Socket socket, UserAccount account, ChatServerChatRoom  chatRoom, ChatServerMain chatServer){
        user = account;
        this.chatRoom = chatRoom;
        this.chatServer = chatServer;

        /* Create the I/O variables */
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            /* Debug */
            System.out.println("Client handler thread created.");
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
    }

    private void send(String a){
        if (ENCRYPTED) a = Encryptor.encrypt(a, 5); //encrypt it 5 times
        this.out.println(a);
    }

    private String receive(){
        try{
            if (ENCRYPTED) return Encryptor.decrypt(this.in.readLine(), 5); //decrypt it 5 times
            else return this.in.readLine();
        } catch (Exception e) {}
        return null;
    }

    @Override
    public void run() {
        //ask for login credentials
        send("Enter username (will be created if doesn't exist).");
        String name = receive();
        send("Enter your password (or desired password for new account).");
        String password = receive();
        System.out.println("Handling login for "+name+", password is "+password+".");
        user = chatServer.handleLogin(name, password);
        if (user==null){ send("Invalid password or username, disconnecting!"); System.out.println("Disconnecting user because of bad login."); forceDisconnect(); return; }
        user.setThread(this);

        this.tell("Server Message", "You've joined the chat room "+chatRoom.getName()+".");
        chatRoom.tellEveryone("Server Message", ""+user.getName()+" joined the room.");
        //else{ System.out.println("Unknown error run ChatServerThread run from handling login!!!"); this.out.println("Technical difficulties, disconnecting."); forceDisconnect(); }

        Scanner scanner; //for analyzing text
        while (true) {
            try {
                if (user==null) return; //check if thread should be killed off

                /* Get string from client */
                String fromClient = receive();
                if (user==null) return; //check if thread should be killed off

                /* If null, connection is closed, so just finish */
                if (fromClient == null) {
                    disconnect(null);
                }

                /* Handle the text. */
                if (fromClient.length()>0){ 
                    if (fromClient.charAt(0)=='/'){ //if it's a command
                        scanner = new Scanner(fromClient);
                        String firstWord = scanner.next();
                        if (firstWord.equalsIgnoreCase("/whisper")){ //to tell a user a private message
                            if (!scanner.hasNext()) send("You must specify the target's name.");
                            String target = scanner.next();
                            if (!scanner.hasNext()) send("You must specify a message.");
                            String message = scanner.next();
                            System.out.println("User "+user.getName()+" saying (privately) "+message+" to target "+target+".");
                            if (!chatServer.tellUser(user.getName()+" (privately)", target, message)) send("User not found online.");
                        }
                        else if (firstWord.equalsIgnoreCase("/nick")){ //to change a user's name
                            if (!scanner.hasNext()) send("You must specify a name.");
                            String oldName = user.getName();
                            if (!chatServer.changeUserName(user.getName(), scanner.next())){ send("Name already taken or invalid."); return; }
                            if (chatRoom!=null) chatRoom.tellEveryone("Server Message", "User "+oldName+" is now known as "+user.getName()+"."); //server message  
                        }
                        else if (firstWord.equalsIgnoreCase("/disconnect")){ //to disconnect gracefully
                            String message = null;
                            if (scanner.hasNext()) message = scanner.next();
                            disconnect(message);
                        }
                        else if (firstWord.equalsIgnoreCase("/stop")){ //to close the server, ADMIN ONLY
                            if (user.getIsAdmin()) chatServer.quit();
                            else send("You do not have permission to use this command.");
                        }
                        if (firstWord.equalsIgnoreCase("/audio")){ //to start an audio chat with someone
                            if (!scanner.hasNext()) send("You must specify the target's name.");
                            String target = scanner.next();
                            System.out.println("User "+user.getName()+" starting audio chat with "+target+".");
                            if (!chatServer.audioChat(user.getName(), target)) send("User not online.");
                            else{
                                send("Starting audio chat. You can end it at any time with /decline.");
                                send("/accept"); //client interprets this and makes a chat thread in this case
                            }
                        }
                        if (firstWord.equalsIgnoreCase("/accept")){
                            if (pendingAudioChatRequest) send("/accept");
                            else send("No pending audio chat request.");
                        }
                        if (firstWord.equalsIgnoreCase("/decline")){
                            if (pendingAudioChatRequest) pendingAudioChatRequest = false;
                            else send("No pending audio chat request.");
                        }
                        /*      else if (firstWord.equalsIgnoreCase("/changeroom")){
                        if (!scanner.hasNext()) send("You must specify a room name.");
                        else if (!chatServer.changeRoom(scanner.next(), user.getName())) send("Invalid room name specified. ");   TODO
                        } */
                        else send("Invalid command.");
                        scanner.close();
                    }

                    else chatRoom.tellEveryone(user.getName(), fromClient);
                }

            } catch (Exception e) {
                /* On exception, stop the thread */
                return;
            }
        }
    }

    public void disconnect(String message){ //called when disconnecting from server
        if (message == null) message = "No message given.";
        System.out.println("Client "+user.getName()+ " disconnected");
        if (message!=null && chatRoom!=null) chatRoom.tellEveryone(user.getName()+" (disconnecting)", message);
        if (chatRoom!=null) chatRoom.tellEveryone("Server Message", ""+user.getName()+" disconnected"); //server message
        tell("Server Message", "You have been disconnected: "+message);
        this.user = null; //causes the thread to stop
        try{
            this.socket.close();
            this.in.close();
            this.out.close();
        } catch (Exception e) { System.out.println("Caught non-problematic exception: "+e); }
    }

    public void forceDisconnect(){ //just disconnect, sending no messages
        System.out.println("Client disconnected forcibly");
        tell("Server Message", "You have been disconnected forcibly.");
        this.user = null; //causes the thread to stop
        //this.user = null;
    }

    public void tell(String user, String message){
        if (message==null || message.length()<=0) return;
        send(user+": "+message);
    }
    
    public void audioChat(String user){
        tell(user, "I've invited you to an audio chat. Type /accept to accept or /decline to decline. If you accept, you can still exit at any time with /decline.");
        pendingAudioChatRequest = true;
    }
}
