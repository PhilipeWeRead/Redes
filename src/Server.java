import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Server extends Thread {
	static List<Socket> ClientsList;
	static ServerSocket server;
	static Socket client;
	private List<List<String>> database = null;
	private BufferedReader fileBUF = null;
	private String file = "";
	private int Port = ConstantsManager.Port;
	
	public Server(String fileRef, int Port) {
		ClientsList = new ArrayList<Socket>();
		client = null;
		server = null;
		file = fileRef;
		this.Port = Port;
		
		
	}
	public Server(String fileRef) {
		ClientsList = new ArrayList<Socket>();
		client = null;
		server = null;
		file = fileRef;
	}

	public void loadFile(String fileRef) {
		file = fileRef;
		loadFile();
	}
	public void loadFile() {
		
		try {
			fileBUF = new BufferedReader(new FileReader(file));
		} catch(IOException e) {
			e.printStackTrace();
			System.out.println("Arquivo nao encontrado");
			return;
		}
		
		database = new ArrayList<>();
		try {
			String line;
			while((line = fileBUF.readLine()) != null) {
				String[] values = line.split(",");
				database.add(Arrays.asList(values));
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void run() {
		if(database == null) {
			System.out.println("ERROR - File not loaded");
			return;
		}
		
		System.out.println("Server start at Port " + Port + "\n");
		
		try {
			server = new ServerSocket(Port);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		
		while(true) {
			try {
				client = server.accept(); //Aceita qualquer conex�o
				System.out.println("Conexao estabelecida");
				ClientsList.add(client);
				
				if(ClientsList.size() > 0) {
					DataSearch ds = new DataSearch(client, database);
					ds.start();
					ClientsList.remove(client);
				}
 				
			}
			catch(Exception e) {
				e.printStackTrace();
				System.out.println("Erro de conexao");
			}
			
		}
		
	}
}

//FORMATO DA MENSAGEM DO CLIENTE DEVE SER "true int" OU "false string string" ONDE O PRIMEIRO PARAMETRO INDICA SE A BUSCA É POR INDEX
//CASO true, O PRÓXIMO VALOR DEVE SER UM int PARA INDICAR ESSE INDEX
//CASO false, OS PRÓXIMOS DEVEM SER DUAS Strings, INDICANDO O ATRIBUTO E O VALOR DO ATRIBUTO RESPECTIVAMENTE
class DataSearch extends Thread { //Classe que busca o dado na database para o cliente
	
	BufferedReader file = null; //Referência do arquivo da database
	Socket client = null;
	boolean searchByIndex = false; //Se a busca será feita dada um index de instância ou dada um certo dado de um atributo
	int index = 0; //Index da busca
	List<List<String>> database=null;
	
	int atribute = -1; //Atributo a ser procurado

	
	public DataSearch(Socket client, List<List<String>> datab) {
		database = datab;
		this.client = client;
	}
	
	public void run() {
		
		
		BufferedReader inSocket = null;
		PrintWriter outSocket = null;
		try {
			inSocket = new BufferedReader(new InputStreamReader(client.getInputStream()));
			outSocket = new PrintWriter(client.getOutputStream(), true);
		} catch(IOException e) {
			e.printStackTrace();
			System.out.println("IO error in server thread");
		}
		
		try {
			String message = inSocket.readLine();
			String[] messages = message.split(" ");
			
			if(messages[0].equals("true")) {
				searchByIndex = true;
			}
			else if(messages[0].equals("false")) {
				searchByIndex = false;
			}
			else {
				System.out.println("To " + client.getInetAddress() + ": " +"Requisicao Invalida no primeiro parametro");
				reportError(client, outSocket, "Requisicao Invalida no primeiro parametro");
				return;
			}
			
			
			
			if(messages.length != 2 && searchByIndex) {
				System.out.println("To " + client.getInetAddress() + ": " +"Requisicao Invalida, foram dados " + messages.length + " parametros, numero requistado = 2");
				reportError(client, outSocket, "Requisicao Invalida, foram dados " + messages.length + " parametros, numero requistado = 2");
				return;
			}
			else if(messages.length != 3 && !searchByIndex) {
				System.out.println("To " + client.getInetAddress() + ": " +"Requisicao Invalida, foram dados " + messages.length + " parametros, numero requistado = 3");
				reportError(client, outSocket, "Requisicao Invalida, foram dados " + messages.length + " parametros, numero requistado = 3");
				return;
			}
			
			if(searchByIndex) {
				int indexRL = Integer.parseInt(messages[1]);
				if(indexRL < database.size()-1 && indexRL >= 0) {
					String result = "";
					for(int i=0; i<database.get(indexRL+1).size(); i++) {
						if(i <database.get(indexRL+1).size()-1) {
							result+=database.get(indexRL+1).get(i) + ", ";
						}
						else {
							result+=database.get(indexRL+1).get(i);
						}
					}
					
					System.out.println("To " + client.getInetAddress() + ": " + result);
					outSocket.println(result);
					
				}
				else {
					System.out.println("To " + client.getInetAddress() + ": " +"Requisicao Invalida no segundo parametro. Max " + (database.size()-2) + ", Min 1");
					reportError(client, outSocket, "Requisicao Invalida no segundo parametro. Max " + (database.size()-2) + ", Min 1");
					return;
				}
			}
			else {
				for(int i = 0; i < database.get(0).size(); i++) {
					if(messages[1].equals(database.get(0).get(i))) {
						atribute = i;
						break;
					}	
				}
				
				if(atribute == -1) {
					System.out.println("To " + client.getInetAddress() + ": " +"Requisicao Invalida no segundo parametro. Atributo " + messages[1] + " nao existe.");
					reportError(client, outSocket, "Requisicao Invalida no segundo parametro. Atributo " + messages[1] + " nao existe.");
					return;
				}
				
				
				boolean found = false;
				String result = "";
				for(int i=1; i<database.size();i++) {
					if(database.get(i).get(atribute).toString().equals(messages[2])) {
						
						for(int j=0; j<database.get(atribute).size(); j++) {
							if(j < database.get(atribute).size()-1) {
								result+=database.get(atribute).get(j) + ", ";
							}
							else {
								result+=database.get(atribute).get(j);
							}
						}
						found = true;
						break;
					}
				}
				
				if(!found) {
					System.out.println("To " + client.getInetAddress() + ": " +"Requisicao Invalida no terceiro parametro. Instancia " + messages[2] + " do atributo " + messages[1] + " nao existe.");
					reportError(client, outSocket, "Requisicao Invalida no terceiro parametro. Instancia " + messages[2] + " do atributo " + messages[1] + " nao existe.");
					return;
				}
				else {
					System.out.println("To " + client.getInetAddress() + ": " + result);
					outSocket.println(result);
					client.close();
				}
			}
			
		} catch(IOException e) {
			e.printStackTrace();
			System.out.println("IO error in server thread");
		}
		
	}
	
	void reportError(Socket c, PrintWriter client, String error) throws IOException {
		client.println("ERRO - " + error);
		c.close();
	}
	
}