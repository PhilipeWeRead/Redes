import java.io.IOException;

public class main2 {

	public static void main(String[] args) throws IOException {
		for(int i =1;i<100;i++) {
			new Client(6700, "192.168.0.110").start();
		}

	}

}
