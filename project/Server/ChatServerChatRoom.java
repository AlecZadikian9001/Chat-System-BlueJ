package Server;

import java.util.ArrayList;

public class ChatServerChatRoom {
	//the name of this chat room (to be used later)
	private String name;
	
	//Is this chat room in tinfoil hat mode?
	private boolean encrypted;
	
	//ChatServerThread threads stored here:
	private ArrayList<ChatServerThread> threads;
	
	public ChatServerChatRoom(String n, boolean e){
		name = n;
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
		tellEveryone(""+thread.getUserName()+" joined the room.", -1, "Server Message"); //id -1 reserved for server messages
	}
	
	public String getName(){ return name; }
	
	public void tellEveryone(String a, int id, String name){ //general chat
		if (a==null || a.length()==0) return;
		int count = threads.size();
		for (int i = 0; i<count; i++){
			if (i!=id){
				threads.get(i).tell(a, name);
			}
		}
	}
	
	public boolean tell(String receiver, String message, String sender){ //only tell one person
		for (ChatServerThread thread : threads){
			if (thread.getUserName().equalsIgnoreCase(receiver)){
				thread.tell(message,  "(privately) "+sender);
				return true;
			}
		}
		return false;
	}
	
	public boolean changeName(String name, int id){
		for (ChatServerThread thread : threads){
			if (thread.getUserName().equalsIgnoreCase(name)) return false;
		}
		threads.get(id).setUserName(name);
		return true;
	}
}
