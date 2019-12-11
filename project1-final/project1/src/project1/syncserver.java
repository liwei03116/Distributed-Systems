/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project1;

import filesync.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 *
 * @author weili
 */
public class syncserver {
    
    static String hostname;
    
    public syncserver(){
        
    }
    public void sethostname(String ht){
        this.hostname = ht;
    }
    
    public static void main(String[] args) {
        
                String filename = "";
                int serverPort = 4144;//default server port
                
                for (int i = 0; i < args.length; i++) {
                    String arg = args[i].toLowerCase();
                    //if port is specified from command line
                    if(arg.startsWith("-file")){
                        filename = args[i + 1];
                    }
                    if (arg.startsWith("-p"))
                        serverPort = Integer.parseInt(args[i + 1]);
                }
                
		DatagramSocket socket = null;
                InstructionFactory instFact=new InstructionFactory();
                boolean checkwithblock = true;

		try {
                        int port = serverPort + 1;
			socket = new DatagramSocket(port);
			System.out.println("Server is running...");
                        
                        byte[] buf11 = new byte[2048];
                        DatagramPacket req1 = new DatagramPacket(buf11, buf11.length);
                        socket.receive(req1);
                        String str1 = new String(req1.getData());
                        str1= str1.trim();
                        System.out.println(str1);
                        JSONParser parserinst1 = new JSONParser();
                        JSONObject objinst1 = null;
                        try {
                                 objinst1 = (JSONObject) parserinst1.parse(str1);
                            } catch (ParseException e) {
                                // alert the user
                                 e.printStackTrace();

                            }
                        if(objinst1!=null){
                            str1 = objinst1.get("direction").toString();
                            //System.out.println(str1);   
                         }
                        
                        //receive negotiation of direction is "push"
                        if(str1.compareTo("push") == 0){
                            Server se = new Server();
                            se.Server(filename, serverPort);
                        }
                        
                        //receive negotiation of direction is "pull"
                        if(str1.compareTo("pull") == 0){
                            
                            System.out.println("Reverse synchronised from the server to the client");
                            
                            try {
                                    Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    System.exit(-1);
                            }
                            Client cl = new Client();
                            cl.Client(filename, hostname, serverPort);
                            
                            
                        }//
                        
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}
	}
}
