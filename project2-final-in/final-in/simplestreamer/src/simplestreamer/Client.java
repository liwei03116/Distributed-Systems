/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simplestreamer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.json.simple.JSONValue;

import javax.swing.JFrame;

import org.apache.commons.codec.binary.Base64;
import org.bridj.Pointer;

import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.DeviceList;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;
import java.util.LinkedHashMap;
import java.util.Map;
import simplestream.Compressor;
import java.awt.event.*;


/**
 *
 * @author weili
 */
public class Client implements Runnable{
    
    Map obj;
    String stopstream;
    String strs;
    BufferedReader in;
    boolean loop;
    
    int width,height,rate, rport;
    String remote;
    
    
    public Client(String re, int rp, int w, int h, int r){
        remote = re;
        rport= rp ;
        width = w;
        height =h;
        rate = r;
        
        
    }
    public void run(){
        
         Socket socket = null;
         try {
		socket = new Socket(remote, rport);

		in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		final PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
			
                obj =new LinkedHashMap();
                obj.put("type", "startstream");
                obj.put("format", "raw");
                obj.put("width", width);
                obj.put("height", height);
                String msgsstart = JSONValue.toJSONString(obj);
		System.err.println("Sending: "+ msgsstart);
                out.println(msgsstart);
                
                Viewer myViewer = new Viewer(width, height);
                JFrame frame = new JFrame("Server Stream Viewer");
                frame.setVisible(true);
                frame.setSize(width, height);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                
                frame.add(myViewer);
                
                /**
                 * This example show how to use native OpenIMAJ API to capture raw bytes
                 * data as byte[] array. It also calculates current FPS.
                 */
			
                OpenIMAJGrabber grabber = new OpenIMAJGrabber();
                        
                Device device = null;
                Pointer<DeviceList> devices = grabber.getVideoDevices();
                        
                for (Device d : devices.get().asArrayList()) {
                       device = d;
                       break;
                 }

                 boolean started = grabber.startSession(width, height, 30, Pointer.pointerTo(device));
                 /*
                 if (!started) {
                       throw new RuntimeException("Not able to start native grabber!");
                 }
                 * 
                 */
             
                 loop = true;
                 /*
                  * listen the keybord press
                  */
                 frame.addKeyListener(new KeyListener() {
                    @Override
                    public void keyPressed(KeyEvent e){
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            System.out.println("Enter key pressed");
                            obj =new LinkedHashMap();
                            obj.put("type", "stopstream");
                            stopstream = JSONValue.toJSONString(obj);
                            System.err.println("Sending: "+ stopstream);
                            out.println(stopstream);
                            
                            try{
                                  String str = in.readLine();
                                  System.out.println("Server:" + str);
                                }catch (IOException ei) {
                                  ei.printStackTrace();
                                }
                            loop = false;
                        }
                        

                    }
                    public void keyTyped(KeyEvent e){
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            //System.out.println("Enter key typed");
                        }
                    }
                    public void keyReleased(KeyEvent e){
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            //System.out.println("Enter key Released");
                        }
                    }
                    
                });
                 

		do {
			
                                /* Get a frame from the webcam. */
                                grabber.nextFrame();
                                /* Get the raw bytes of the frame. */
                                byte[] raw_image=grabber.getImage().getBytes(width * height * 3);
                                /* Apply a crude kind of image compression. */
                                byte[] compressed_image = Compressor.compress(raw_image);
                                /* Prepare the date to be sent in a text friendly format. */
                                byte[] base64_image = Base64.encodeBase64(compressed_image);
                    
                                try {
                                    Thread.sleep(rate);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    System.exit(-1);
                                }

                                obj=new LinkedHashMap();
                                obj.put("type", "image");
                                obj.put("data", new String(base64_image));
                                String stream = JSONValue.toJSONString(obj);
                                
                                //String printmsg = stream.replace("\\", "");
                                System.err.println("Sending: "+ stream);
                                out.println(stream);
                                
                                
                                /*
                                * The image data can be sent to connected server.
                                */

                                /*
                                * Assume we received some image data.
                                * Remove the text friendly encoding.
                                */
                                
                            //grabber.stopSession();
                            String str = in.readLine();
                            System.out.println(str);
                            String [] st =str.split(":");
                            //System.out.println("--------------"+str);
                            String tempst = st[2];
                            String[] sttemp =tempst.split("}");
                            String tempst1 = sttemp[0];
                            String tempst12 = tempst1.replace("\"", "");
                            byte[] buf = tempst12.getBytes();
                            

                            /*
                             * Assume we received some image data.
                             * Remove the text friendly encoding.
                             */
                            byte[] nobase64_image1 = Base64.decodeBase64(buf);
                            /* Decompress the image */
                            byte[] decompressed_image1 = Compressor.decompress(nobase64_image1);
                            /* Give the raw image bytes to the viewer. */
                            myViewer.ViewerInput(decompressed_image1, width, height);
                            frame.repaint();
                            
                            
			} while (loop);
                        frame.setVisible(false);
                        frame.dispose();
			//input.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (socket != null) {
				try {
					socket.close();
					System.out.println("Client disconneted.");
                                        System.out.println("Server disconneted.");
                                        System.exit(0);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }
    
}
