import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class Client extends Thread {
	
	InetAddress hostIP = null;
	static Random rand = new Random();
	int Port = ConstantsManager.Port;
	static List<String> atributes = new Vector<String>();
	
	public Client(int Port, String host) throws UnknownHostException {
		associate();
		hostIP = InetAddress.getByName(host);
		if(hostIP == null) {
			hostIP = InetAddress.getByName("192.168.56.1");
		}
		this.Port = Port;
	}
	public Client() {
		associate();
	}
	private void associate() {
		atributes.add("shop_id");
		atributes.add("item_id");
	}
	
	@SuppressWarnings("resource")
	public void run() {
		InetAddress address = null;
		
		address = hostIP;
		
		Socket host = null;
		BufferedReader InputSocket =null;
	    PrintWriter OutputSocket =null;
	    
		try {
			host = new Socket(address, Port);
			InputSocket = new BufferedReader(new InputStreamReader(host.getInputStream()));
			OutputSocket = new PrintWriter(host.getOutputStream(), true);
			
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		
		
		System.out.println("Endereco Cliente : " + address);
		
		boolean byIndex = (rand.nextInt(2) == 0);
		if(byIndex) {
			int index = rand.nextInt(ConstantsManager.InstanceCount);
			OutputSocket.println("true " + index);
			
		}
		else {
			int index = 0;
			int at = rand.nextInt(atributes.size());
			if(at == 0) {
				index = rand.nextInt(ConstantsManager.MaxShop);
			}
			else {
				index = rand.nextInt(ConstantsManager.MaxItem);
			}
			
			OutputSocket.println("false " + atributes.get(at) + " " + index);
		}
		

		
		try {
			String response = InputSocket.readLine();
			System.out.println("Server Response: " + response);
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		OutputSocket.close();
		try {
			InputSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Connection Closed");
	}
}
