package Garbage;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class AudioClient //sending data to server socket
{
	final static float MAX_8_BITS_SIGNED = Byte.MAX_VALUE;
	final static float MAX_8_BITS_UNSIGNED = 0xff;
	final static float MAX_16_BITS_SIGNED = Short.MAX_VALUE;
	final static float MAX_16_BITS_UNSIGNED = 0xffff;
	private float level, sampleRate;
	private TargetDataLine targetDataLine;
	private AudioFormat format;
	byte[] buffer, volBuff;
	private int bufferSize;
	
	private boolean isRunning;
	
	public static void main(String[] args){
	    String address = args[0];
	    int port = Integer.parseInt(args[1]);
	   }
	
    public AudioClient (Socket socket){ //put in socket with server address
       try {
        sampleRate = 44100;
		int sampleSizeInBits = 16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = false;
		format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
		DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
		bufferSize = (int) format.getSampleRate() * format.getFrameSize();
		
		targetDataLine = (TargetDataLine)AudioSystem.getLine(dataLineInfo);
			targetDataLine.open(format, (int)sampleRate);
			targetDataLine.start();
		
            OutputStream out = socket.getOutputStream();
                while (isRunning) {
                    targetDataLine.read(buffer, 0, buffer.length);
                    out.write(buffer);
                }
            
            socket.close();
        } catch (Exception e) {
            System.out.println("AudioOut exception: "+e);
        }
    }
    
    public void stop(){ isRunning = false; }
}
