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
import Common.Encryptor;

public class ChatClient extends JFrame implements ActionListener 
{
    private String name;
    private boolean encrypted;
    private Encryptor ryptor;
    
    
    private JTextArea  enteredText = new JTextArea(10, 32);
    private JTextField typedText   = new JTextField(32);

    // socket for connection to chat server
    private Socket socket;

    // for writing to and reading from the server
    private Out out;
    private In in;
    
    
    
    public ChatClient(String hostName, String port) 
    {
        
        encrypted = false;
        ryptor= new Encryptor();
        // connect to server
        try 
        {
            socket = new Socket(hostName, Integer.parseInt(port));
            out    = new Out(socket);
            in     = new In(socket); //IF SERVER SENDS A SINGLE MESSAGE W/ CLOSING CURLY BRACE
        }
        catch (Exception ex) { ex.printStackTrace(); }

        
        
        if (in.exists() && in.readLine().length()!=0)
        {
            
            
        }
            
            
        // close output stream  - this will cause listen() to stop and exit
        addWindowListener
        (
            new WindowAdapter()  
            {
                public void windowClosing(WindowEvent e) 
                {
                    out.close();
                    
//                    in.close();
//                    try                   { socket.close();        }
//                    catch (Exception ioe) { ioe.printStackTrace(); }
                }
            }
        );


        // create GUI stuff
        enteredText.setEditable(false);
        enteredText.setBackground(Color.LIGHT_GRAY);
        typedText.addActionListener(this);
        
        
        
        /*
        
        
        
        JOptionPane getName = new JOptionPane();
        String nicky = getName.showInputDialog("Please input your nickname.");//gets name
        //out.println("/nick");
        System.out.println("ball so hatd");
        name = nicky;
        */
       
        
        Container content = getContentPane();
        content.add(new JScrollPane(enteredText), BorderLayout.CENTER);
        content.add(typedText, BorderLayout.SOUTH);
        
        // to encrypt, use /encrypt & encryption class
        //put button 
        //every time message is sent while encryption is on, put {slash in front of it
        // display the window, with focus on typing box
        setTitle("Chat Client 1.0: [" + hostName + ":" + port + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        typedText.requestFocusInWindow();
        setVisible(true);

        
        
    }

    // process TextField after user hits Enter
    public void actionPerformed(ActionEvent e) 
    {
        
        out.println(typedText.getText() + "aa");
        System.out.println("11111");
        //if ()
        typedText.setText("");
        typedText.requestFocusInWindow();
    }
    
    public void encryptStuff()  //WHEN .IN GETS "}", CALL THIS METHOD
    { 
        encrypted = true;
        
        
    }
    
    
    // listen to socket and print everything that server broadcasts
    public void listen() {
        String s;
        while ((s = in.readLine()) != null) {
            String decryzzled = ryptor.decrypt(s, 5);
            enteredText.insert(decryzzled + "\n", enteredText.getText().length());
            enteredText.setCaretPosition(enteredText.getText().length());
            //System.out.println("2222");
        }
        
        
            
        out.close();
        in.close();
        try                 { socket.close();      }
        catch (Exception e) { e.printStackTrace(); }
        System.err.println("Closed client socket");
    }
    
    public void run()
    {
        
        
    }
    
    
    public static void main(String[] args)  
    {
        ChatClient client = new ChatClient(args[0], args[1]);
        client.listen();
    } 
}
