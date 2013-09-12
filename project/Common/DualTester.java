package Common;
import Server.ChatServerMain;
import Client.ChatClient;


public class DualTester {
	public static void main(String [] args){
		if (args.length!=2){
			args = new String[2];
			args[0] = "localhost";
			args[1] = "9000";
		}
		String arg1 = args[1];
		final String[] args2 = new String[1];
		args2[0] = arg1;
		final String[] args2final = args2;
		final String[] argsfinal = args;
		new Thread(){
			public void run(){
				ChatServerMain.main(args2final);
			}
		}.start();
		new Thread(){
			public void run(){
				ChatClient.main(argsfinal);
			}
		}.start();
		new Thread(){
			public void run(){
				ChatClient.main(argsfinal);
			}
		}.start();
		new Thread(){
			public void run(){
				ChatClient.main(argsfinal);
			}
		}.start();
	}
}
