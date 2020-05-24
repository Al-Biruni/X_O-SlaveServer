package SlaveServer;

import xoLib.Message.Message;
import xoLib.User;


import java.net.Socket;

public class ClientsConnectionsManager {
    protected int clientsNum = 0;
    protected int registeredClientsNum = 0;
    protected Server server;
    protected ClientThread[] maxThreadPool;
    protected ClientThread[] activeClients;
    boolean dirty = true;

    public ClientsConnectionsManager(Server server) {
        this.server = server;
        maxThreadPool = new ClientThread[server.maxClients];
    }

    synchronized void addNewClient(Socket newClientSocket) {
        dirty = true;

        for (int i = 0; i < maxThreadPool.length; i++)
            if (maxThreadPool[i] == null) {
                maxThreadPool[i] = new ClientThread(this, server.masterConnectionManger, clientsNum, newClientSocket);
                maxThreadPool[i].start();
                clientsNum++;

                break;
            }

    }

    synchronized void sendPrivateMsg(Message msg) {
        for (ClientThread ct : getActiveClients()) {
            if (ct.cUser.userName.equals(msg.receiver.userName))
                ct.sendToClient(msg);
        }
    }

    synchronized void sendToAll(Message msg) {
        for (ClientThread ct : getActiveClients()) {
            if (ct != null)
                if (ct.cUser != null)//didnt finish registration yet
                    ct.sendToClient(msg);
        }


    }

    synchronized void register(User temp, User regUsr) {

        for (int i = 0; i < maxThreadPool.length; i++)
            if (maxThreadPool[i] != null)
                if (maxThreadPool[i].cUser != null)
                    if (maxThreadPool[i].cUser.userName.equals(temp.userName)) {
                        maxThreadPool[i].cUser = regUsr;
                        dirty = true;
                        registeredClientsNum++;
                        break;

                    }


    }

    synchronized ClientThread find(User receiver) {

        for (ClientThread ct : getActiveClients()) {
            if (ct.cUser.userName.equals(receiver.userName))
                return ct;
        }

        return null;
    }

//nneeds to be fixed
    protected synchronized ClientThread[] getActiveClients() {
        if (!dirty)
            return activeClients;

        ClientThread[] cts = new ClientThread[registeredClientsNum];
        int i = 0, c = 0;
        while (i < registeredClientsNum&&c<clientsNum) {
            if (maxThreadPool[c] != null)
                if (maxThreadPool[c].cUser != null)
                    if (maxThreadPool[c].cUser.publicKey != null) {
                        cts[i] = maxThreadPool[c];
                        i++;
                    }
            c++;
        }
        dirty = false;
        activeClients = cts;
        return cts;

    }

    public void close(ClientThread clientThread) {
        dirty = true;

        for (int i = 0; i < maxThreadPool.length; i++)
            if (maxThreadPool[i] == clientThread) {
                System.out.println("Client " + maxThreadPool[i].cUser.userName + " log out");
                maxThreadPool[i] = null;
                clientsNum--;
                break;
            }
    }

    public User getServerUser() {
        return  server.getUser();
    }
}
