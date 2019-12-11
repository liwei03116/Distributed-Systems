/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project1;


import filesync.BlockUnavailableException;
import filesync.Instruction;
import filesync.InstructionFactory;
import filesync.SynchronisedFile;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
public class Server {
    
    int port;
    
    public Server(){
        port = 4144; //default server port
    }
    public void Server(String destinfilename, int port){
        
                DatagramSocket socket = null;
                InstructionFactory instFact=new InstructionFactory();
                SynchronisedFile toFile = null;
                String tempst12 = "";
                boolean checkwithblock = true;

		try {
                        toFile = new SynchronisedFile(destinfilename);
                        //int port = 4145;
			socket = new DatagramSocket(port);
			//System.out.println("Server is running...");
                        
                        while(true){
                            
                            int ackcounter = 1;
                            boolean statement = true;

                            while (statement) {
                                    byte[] buf = new byte[2048];
                                    DatagramPacket req = new DatagramPacket(buf, buf.length);
                                    socket.receive(req);

                                    String str = new String(req.getData());
                                    str= str.trim();
                                    //System.out.println(str);
                                    String strcounter = "";
                                    JSONParser parserinst = new JSONParser();
                                    JSONObject objinst = null;
                                    try {
                                            objinst = (JSONObject) parserinst.parse(str);
                                        } catch (ParseException e) {
                                            // alert the user
                                                e.printStackTrace();

                                        }
                                    if(objinst!=null){
                                        str = objinst.get("inst").toString();
                                        String [] st =str.split(":");
                                        String tempst = st[1];
                                        String[] sttemp =tempst.split("}");
                                        String tempst1 = sttemp[0];
                                        tempst12 = tempst1.replace("\"", ""); 
                                        //System.out.println(tempst12);
                                        //System.out.println(str);
                                        strcounter = objinst.get("counter").toString();
                                        //System.out.println("1: " + strcounter);
                                    }

                                    /*
                                    * The Server receives the instruction here.
                                    */
                                    Instruction receivedInst = instFact.FromJSON(str);

                                    try {
                                            // The Server processes the instruction
                                            toFile.ProcessInstruction(receivedInst);

                                    } catch (IOException e) {
                                            e.printStackTrace();
                                            System.exit(-1); // just die at the first sign of trouble
                                    } catch (BlockUnavailableException e) {
                                            // The server does not have the bytes referred to by the block hash.
                                                try{
                                                     /*
                                                    * At this point the Server needs to send a request back to the Client
                                                    * to obtain the actual bytes of the block.
                                                    */
                                                    String msg1 = "NOblock";
                                                    byte[] buf1 = new byte[2048];
                                                    buf1 = msg1.getBytes();
                                                    DatagramPacket reply = new DatagramPacket(buf1, buf1.length,
                                                    req.getAddress(), req.getPort());
                                                    socket.send(reply);

                                                    Map objack=new LinkedHashMap();
                                                    objack.put("type", "expection");
                                                    objack.put("counter", ackcounter);
                                                    String msgack = JSONValue.toJSONString(objack);
                                                    System.err.println("Sending: "+ msgack);
                                                    //System.out.println("2: " + ackcounter);
                                                    checkwithblock = false;
                                                    //ackcounter++;

                                                    // network delay

                                                    /*
                                                    * Server receives the NewBlock instruction.
                                                    */
                                                    Instruction receivedInst2 = instFact.FromJSON(str);
                                                    toFile.ProcessInstruction(receivedInst2);
                                                    
                                                }catch (IOException e1){
                                                    e1.printStackTrace();
                                                    System.exit(-1);
                                                }catch (BlockUnavailableException e1){
                                                    assert(false);
                                                }

                                    }
                                    if((ackcounter == Integer.parseInt(strcounter))&&checkwithblock){

                                        String msg = "OK";
                                        byte[] rbuf = msg.getBytes();
                                        DatagramPacket reply = new DatagramPacket(rbuf, rbuf.length,
                                                        req.getAddress(), req.getPort());
                                        socket.send(reply);
                                        Map objack=new LinkedHashMap();
                                        objack.put("type", "ack");
                                        objack.put("counter", ackcounter);
                                        String msgack = JSONValue.toJSONString(objack);
                                        System.err.println("Sending: "+ msgack);
                                        //System.out.println("3: "+ackcounter);
                                        ackcounter++;
                                    }
                                    else if ((ackcounter != Integer.parseInt(strcounter))&&checkwithblock){
                                        String msg1 = "NO";
                                        byte[] buf1 = new byte[2048];
                                        buf1 = msg1.getBytes();
                                        DatagramPacket reply = new DatagramPacket(buf1, buf1.length,
                                        req.getAddress(), req.getPort());
                                        socket.send(reply);
                                        Map objack=new LinkedHashMap();
                                        objack.put("type", "expecting");
                                        objack.put("counter", ackcounter);
                                        String msgack = JSONValue.toJSONString(objack);
                                        System.err.println("Sending: "+ msgack);
                                        //System.out.println("4: " +ackcounter);
                                        //ackcounter++;

                                    }
                                    if(tempst12.compareTo("EndUpdate") == 0){
                                        statement = false;
                                    }
                                    
                                    checkwithblock = true;
                                // System.out.println(tempst12.compareTo("EndUpdate"));

                            }
                        }
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
