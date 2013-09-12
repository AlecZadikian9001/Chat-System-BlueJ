package Server;
import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class ChatServerMain{
	//for putting the threads into
		ChatServerChatRoom chatRoom;

	public static void main (String[] args){
		ChatServerMain main = new ChatServerMain(args);
	}

	public ChatServerMain(String [] args) {

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

		//Initialize messenger thread.
		chatRoom = new ChatServerChatRoom("Main", false);

		/* In the main thread, continuously listen for new clients and spin off threads for them. */
		while (true) {
			try {
				/* Get a new client */
				Socket clientSocket = serverSocket.accept();
				System.out.println("New connection from "+clientSocket.getInetAddress().toString());
				/* Create a thread for it and start, giving it the right id. */
				ChatServerThread clientThread = new ChatServerThread(clientSocket, -2, "INVALID NAME", chatRoom); //id = -2 for uninitialized
				chatRoom.addThread(clientThread);
				clientThread.start();
			} catch (IOException e) {
				System.out.println("Accept failed: " + e);
				System.exit(1);
			}
		}
	}


}
