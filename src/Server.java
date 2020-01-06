import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    static public boolean stop = false;
    static public String workspace;

    public Server(String workspace) {
        Server.workspace = workspace;
    }

    public void start(int port) {

        try {
            ServerSocket socket = new ServerSocket(port);
            System.out.println("Serveur démarré");

            while(!stop) {
                try {
                    Socket client = socket.accept();

                    System.out.println("Nouvelle connexion");
                    Thread t = new Thread(new Communication(client));
                    t.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            socket.close();

        } catch (IOException e) {
            System.out.println("Port déjà utilisé");
        }
    }

    public static void main(String[] main) {
        Server serveur = new Server("C:\\Users\\Epulapp\\Desktop\\arsir");
        serveur.start(8080);
    }
}
