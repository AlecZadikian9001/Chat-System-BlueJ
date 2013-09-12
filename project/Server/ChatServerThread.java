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

	//unique id
	private int id;

	//name
	private String name;

	//chat room this belongs to
	private ChatServerChatRoom chatRoom;

	public int getID(){ return id; }
	public void setID(int a){ id = a; }

	public String getUserName(){ return name; }
	public void setUserName(String n){ name = n; }

	public ChatServerChatRoom getChatRoom(){ return chatRoom; }

	public ChatServerThread(Socket socket, int i, String n, ChatServerChatRoom chatRoom){
		name = n;
		id = i;

		//set room
		this.chatRoom = chatRoom;

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

	@Override
	public void run() {
		Scanner scanner; //for analyzing text
		while (true) {
			try {
				/* Get string from client */
				String fromClient = this.in.readLine();

				/* If null, connection is closed, so just finish */
				if (fromClient == null) {
					
				}

				/* Handle the text. */
				if (fromClient.length()>0){ 
					if (fromClient.charAt(0)=='/'){ //if it's a command
						scanner = new Scanner(fromClient);
						String firstWord = scanner.next();
						if (firstWord.equalsIgnoreCase("/whisper")){ //to tell a user a private message
							if (!chatRoom.tell(scanner.next(), scanner.next(), name)) this.out.println("User not found online.");
						}
						else if (firstWord.equalsIgnoreCase("/nick")){ //to change a user's name
							if (!chatRoom.changeName(scanner.next(), id)) this.out.println("Name already taken.");
						}
						else if (firstWord.equalsIgnoreCase("/disconnect")){ //to disconnect gracefully
							disconnect(scanner.next());
						}
						else if (firstWord.equalsIgnoreCase("/chatroom")){
							if (!chatRoom.changeName(scanner.next(), id)) this.out.println("Name already taken.");
						}
						else this.out.println("Invalid command.");
						scanner.close();
					}

					else chatRoom.tellEveryone(fromClient, id, name);
				}

			} catch (IOException e) {
				/* On exception, stop the thread */
				return;
			}
		}
	}
	
	public void disconnect(String message){ //called when disconnecting from server
		System.out.println("Client "+name+ " disconnected");
		if (message!=null) chatRoom.tellEveryone(message, id, name);
		chatRoom.tellEveryone(""+name+" disconnected.", -1, "Server Message"); //server message
		try{
		this.in.close();
		this.out.close();
		this.socket.close();
		} catch (Exception e) { /*#nobodycares*/ }
	}

	public void tell(String message, String user){
		if (message==null || message.length()<=0) return;
		this.out.println(user+": "+message);
	}

	public int hashCode(){
		return id;
	}
}
