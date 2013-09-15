package Client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.net.Socket;

public class ChatClient extends JFrame implements ActionListener {
	private String name;
	private boolean encrypted;

    
    private JTextArea  enteredText = new JTextArea(10, 32);
    private JTextField typedText   = new JTextField(32);
  

    // socket for connection to chat server
    private Socket socket;

    // for writing to and reading from the server
    private Out out;
    private In in;

    public ChatClient(String hostName, String port) {

        // connect to server
        try {
            socket = new Socket(hostName, Integer.parseInt(port));
            out    = new Out(socket);
            in     = new In(socket); //IF SERVER SENDS A SINGLE MESSAGE W/ RIGHT CURLY BRACE, 
        }
        catch (Exception ex) { ex.printStackTrace(); }

        // close output stream  - this will cause listen() to stop and exit
        addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    out.close();
                    
                    
//                    in.close();
//                    try                   { socket.close();        }
//                    catch (Exception ioe) { ioe.printStackTrace(); }
                }
            }
        );


        // create GUI stuff
        enteredText.setEditable(false);
        enteredText.setBackground(Color.BLACK);
        typedText.addActionListener(this);
        
<<<<<<< HEAD
        
        
        /*   
=======
>>>>>>> 0b8847f97f6a8a9ca58bb148bd2aa22be92f5e1e
        JOptionPane getName = new JOptionPane();
        String nicky = getName.showInputDialog("Please input your nickname.");//gets name
        out.println("/nick");
        name = nicky;
        
        Container content = getContentPane();
        content.add(new JScrollPane(enteredText), BorderLayout.CENTER);
        content.add(typedText, BorderLayout.SOUTH);
        
        // to encrypt, use /encrypt & encryption class
        //put button 
        //every time message is sent while encryption is on, put {slash in front of it
        // display the window, with focus on typing box
        setTitle("Chat Client 9000!!!!: [" + hostName + ":" + port + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        typedText.requestFocusInWindow();
        setVisible(true);

    }

    // process TextField after user hits Enter
<<<<<<< HEAD
    public void actionPerformed(ActionEvent e) 
    {
        String outy = ryptor.encrypt(typedText.getText(),5);
        
        out.println(outy);
        //System.out.println("11111");
        //if ()
=======
    public void actionPerformed(ActionEvent e) {
        out.println(typedText.getText());
>>>>>>> 0b8847f97f6a8a9ca58bb148bd2aa22be92f5e1e
        typedText.setText("");
        typedText.requestFocusInWindow();
    }
    
    public void encryptStuff()
    {
    	
    }
    
    
    // listen to socket and print everything that server broadcasts
    public void listen() {
        String s;
        while ((s = in.readLine()) != null) {
            enteredText.insert(s + "\n", enteredText.getText().length());
            enteredText.setCaretPosition(enteredText.getText().length());
        }
        out.close();
        in.close();
        try                 { socket.close();      }
        catch (Exception e) { e.printStackTrace(); }
        System.err.println("Closed client socket");
    }

    public static void main(String[] args)  
    {
        ChatClient client = new ChatClient(args[0], args[1]);
        client.listen();
    } 
}
