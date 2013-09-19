package Server;
import java.net.*;
import java.util.*;
import java.io.*;

public class AudioThread{

    /* This is the server socket to accept connections */
        private ServerSocket serverSocket;
    
    private boolean isRunning; //is the server up?

    public static void main (String[] args){
        AudioThread main = new AudioThread(9001); //9001 is audio port
        main.start();
    }

    public ChatServerMain(Socket socket) {
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
    }
    
    public void run(){
        while (isRunning) {
            try {
                System.out.println("New audio connection from "+clientSocket.getInetAddress().toString());
            } catch (IOException e) {
                System.out.println("Accept failed: " + e);
                System.out.println("Server has stopped running.");
                System.exit(1);
            }
        }
    }
}
