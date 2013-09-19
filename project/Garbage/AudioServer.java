package Garbage;
import java.net.*;
import java.util.*;
import java.io.*;
import javax.sound.sampled.*;

public class AudioServer{
    /* This is the server socket to accept connections */
    private ServerSocket serverSocket;
    private InputStream audioInputStream;
	private SourceDataLine  line;
	Socket clientSocket;
    
    private boolean isPlaying, isRunning; //is the server up?

    public static void main (String[] args){
        AudioServer main = new AudioServer(args);
    }

    public AudioServer(String [] args) {
        isRunning = true; isPlaying = true;

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
                clientSocket = serverSocket.accept();
                System.out.println("New connection from "+clientSocket.getInetAddress().toString());
                /* Create a thread for it and start, giving it the right id. */
                //clientThread.start();
                
                playSound();

            } catch (IOException e) {
                System.out.println("Accept failed: " + e);
                System.out.println("Server has stopped running.");
                System.exit(1);
            }
        }
        System.out.println("Server has stopped running.");
    }
    
    public void playSound(){
        audioInputStream = null;
		try
		{
			audioInputStream = clientSocket.getInputStream();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		} 
		int sampleRate = 44100;
		int sampleSizeInBits = 16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = false;
		AudioFormat audioFormat = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);

		DataLine.Info   info = new DataLine.Info(SourceDataLine.class,audioFormat);
		try
		{
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(audioFormat);
		}
		catch (LineUnavailableException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		line.start();
		int     nBytesRead = 0;
		byte[]  abData = new byte[128000];
		while (nBytesRead != -1 && isPlaying)
		{
			try
			{
				nBytesRead = audioInputStream.read(abData, 0, abData.length);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			if (nBytesRead >= 0)
			{
				int     nBytesWritten = line.write(abData, 0, nBytesRead);
			}
		}
		line.drain();
		line.close();

    }
}
