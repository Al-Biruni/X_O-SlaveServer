package SlaveServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientConnectionListener extends Thread {

    protected Socket newClientS;
    protected ServerSocket mySocket;

    protected ClientsConnectionsManager clientsConnectionsManager;

    public ClientConnectionListener(int clientPort, ClientsConnectionsManager clientsConnectionsManager) throws IOException {
        this.clientsConnectionsManager = clientsConnectionsManager;
        mySocket = new ServerSocket(clientPort);
        newClientS = new Socket();

    }


    public void run() {
        while (true) {
            try {
                newClientS = mySocket.accept();
            } catch (IOException e) {
                System.err.println("Client Socket Error while accepting");
                e.printStackTrace();
            }
            if (newClientS != null) {

                System.out.println("COnnecting creating new THread");
                clientsConnectionsManager.addNewClient(newClientS);

                newClientS = new Socket();

            }

        }

    }

}
