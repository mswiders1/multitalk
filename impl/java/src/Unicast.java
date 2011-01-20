import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import net.sf.json.JSONObject;
import net.sf.*;
import net.sf.json.*;






public class Unicast {

	private Socket socket;
	private PrintWriter writer;
	private InputStreamReader reader;
	private NetManagement net_management;
	private Connection connection;
	private BlockingQueue<Message> sendbox;
	private BlockingQueue<Message> receivebox;
	private Writer W;
	private Reader R;
	private ReceiveBoxListener L;
	
	Unicast(Socket s, NetManagement nm, Connection connection)
	{
		this.socket = s;
		this.net_management = nm;
		this.connection = connection;
		sendbox = new ArrayBlockingQueue<Message>(100);
		receivebox = new ArrayBlockingQueue<Message>(100);
		W = new Writer();
		R = new Reader();
		L = new ReceiveBoxListener();
		Thread WThread = new Thread(W);
		WThread.start();
		Thread RThread = new Thread(R);
		RThread.start();
		Thread LThread = new Thread(L);
		LThread.start();
	}
		
	/**
	 * puts new Message to be sent do queue
	 * @param m
	 */
	public void putMessage(Message m)
	{
		try
		{
			sendbox.put(m);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	class Writer extends Thread
	{
		/**
		 * sends messages from the queue (sendbox) to through the socket stream
		 */
		
		public void run()
		{
			try{
				Unicast.this.writer = new PrintWriter(Unicast.this.socket.getOutputStream());
				
			}			
			catch(Exception e)
			{
				e.printStackTrace();
			}
			TCPMessage tcp_send_msg;
			Message send_msg;
			while(true)
			{
				try
				{
					send_msg = Unicast.this.sendbox.take();
					tcp_send_msg = new TCPMessage(send_msg);
					Unicast.this.writer.append(tcp_send_msg.create());
					Unicast.this.writer.flush();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	class Reader extends Thread
	{
		private static final String BEGIN_MESSAGE_FLAG = "BEGIN_MESSAGE";
		
		/**
		 * Reads from the stream and puts into the receivebox.
		 */
		public void run()
		{
			try
			{
				Unicast.this.reader = new InputStreamReader(Unicast.this.socket.getInputStream());
								
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return;
			}
	        char[] buf = new char[1024];
	        int readChars = 0;
	        
	        boolean atNewMessage = false;
	        StringBuffer sb = new StringBuffer();
	        String packet;
	        int messageLength = -1;
	        int messageReadBytes = 0;
	        
	        boolean continueRead = false;
	        try {
	            while(true){
	            
	                while(continueRead == true || ((readChars = reader.read(buf)) != -1)){
	                    if(continueRead){
	                        //Log.d(Constants.DEBUG_TAG, "Kontynuuję odczytywanie poprzedniego pakietu");
	                        continueRead = false;
	                        packet = "";
	                    }
	                    else { // (readChars != -1)
	                        packet = new String(buf, 0, readChars);
//	                        Log.d(Constants.DEBUG_TAG, "Read packet: "+packet);
	                        sb.append(packet);
	                    }
	                    
	                    if(!atNewMessage){
	                        // pierwszy pakiet nowego komunikatu
	                        atNewMessage = true;
	                        messageLength = -1;
	                        messageReadBytes = 0;
	                    }

	                    
	                    if(messageLength == -1){
	                        // nie mamy jeszcze długości wiadomości
	                        String msgBuf = sb.toString();

	                        int beginMessageFlagIdx = msgBuf.indexOf(BEGIN_MESSAGE_FLAG);
	                        if(beginMessageFlagIdx == -1){
	                            // za mało odczytał... jazda dalej
	    //                        Log.d(Constants.DEBUG_TAG, "przed 'BEGIN_MESSAGE' w nagłówku");
	                            continue;
	                            
	                        }
	                        sb.delete(0, beginMessageFlagIdx);
	                        msgBuf = sb.toString();
	                        
	                        int newLineIdx = msgBuf.indexOf("\n");
	                        if(newLineIdx == -1){
	                            // za mało odczytał... jazda dalej
	      //                      Log.d(Constants.DEBUG_TAG, "przed znakiem nowej linii w nagłówku");
	                            continue;
	                            
	                        }
	                        
	                        String header = msgBuf.substring(0, newLineIdx);
//	                        Log.d(Constants.DEBUG_TAG, "Nagłówek "+header);
	                        
	                        // 14 == header.indexOf(":")
	                        messageLength = Integer.valueOf(header.substring(14)).intValue();
	                        messageReadBytes = msgBuf.length() - newLineIdx;
	                        
	                    } else {
	                        messageReadBytes += packet.length();
	                        
	                    }
	                    
	                    if(messageReadBytes < messageLength){
	                        continue;
	                    }
	                    
	                    // odczytaliśmy całą wiadomość - przetwarzamy
	                    String bufContent = sb.toString();

	                    // wycięcie nagłówka
	                    int newLineIdx = bufContent.indexOf("\n");
	                    bufContent = bufContent.substring(newLineIdx + 1);
	                    
	                    // wycięcie i utworzenie wiadomości
	                    String messageString = bufContent.substring(0, messageLength);
	        //            Log.d(Constants.DEBUG_TAG, "Odczytano wiadomość:\n" + messageString);
	                    
	                    Message message = null;
	                    try {
	                    	Serializer serial = new Serializer();
	                    	message = serial.unpack(messageString);
	                        //message = MessageFactory.fromJSON(messageString);
	                        if(message != null){
	                            // znana wiadomość - przekazujemy
	                          //  passMessage(message);
	                        	Unicast.this.receivebox.put(message);
	                        }
	                        
	                    } catch (JSONException e) {
	            //            Log.d(Constants.ERROR_TAG, "Błąd parse-owania JSON-a: \n" 
	              //                  + e.getMessage());
	                        // trudno, jedziemy dalej...
	                    }
	                    
	                    // wyczyszczenie sb
	                    sb.delete(0, newLineIdx + messageLength + 1);
	                    atNewMessage = false;
	                    if(sb.toString().length() > 0){
	                        // odczytaliśmy całą wiadomość, ale coś zostało i trzeba przetworzyć
	                        // resztę
	          //              Log.d(Constants.DEBUG_TAG, "W buforze pozostało:\n" + sb.toString());
	                        continueRead = true;
	                    }
	                }
	                
	                // brak danych od klienta - poczekaj...
	                Thread.sleep(500);
	            }
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
			
		}
	}
	class ReceiveBoxListener extends Thread
	{
		public void run()
		{
			try{
				while(true)
				{
					Message msg = receivebox.take();
					System.out.println("wiadomosc w ReceiveBoxListener" + msg);
					Unicast.this.net_management.add_received(Unicast.this.connection.getContact(), msg);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
