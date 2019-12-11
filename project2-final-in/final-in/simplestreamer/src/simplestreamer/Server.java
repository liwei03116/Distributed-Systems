/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simplestreamer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author weili
 */
public class Server implements Runnable{
    
    int port;

    
    public Server(int p){
        port = p;
    }
    
    public void run(){
        ServerSocket serversocket = null;
	Socket socket = null;
	try {
		serversocket = new ServerSocket(port);
		System.out.println("Server's listening...");
                
		while (true) {
			socket = serversocket.accept();
			System.out.println("Connected.");
			Thread con = new Connection(socket);
			con.start();         
		}
			// out.flush();
	} catch (IOException e) {
		e.printStackTrace();
	} finally {
		if (socket != null)
			try {
				socket.close();
				serversocket.close();
                                
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
    }
    
}
