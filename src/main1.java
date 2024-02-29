
public class main1 {
	public static void main(String[] args) {
		Server server = new Server("defaultPath\\sales_train_vs.csv", 6700); //colocar o path do arquivo "sales_train_v2.csv" no primeiro parametro
		System.out.println("Loading file...");
		server.loadFile();
		System.out.println("File loaded!");
		server.start();
	}
}
