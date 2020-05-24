package SlaveServer;//-------------------------------SlaveServer.ClientThread-----------------------------------------------------------

import xoLib.*;
import xoLib.Exceptions.NotUniqueUserNameException;
import xoLib.Message.Message;
import xoLib.Message.MessageHandler;
import xoLib.Message.MessageType;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class ClientThread extends Thread implements MessageHandler {

    protected int clientNumber;
    protected Socket mySocket;

    protected ObjectOutputStream output;
    protected ObjectInputStream input;
    protected Boolean active = true;

    protected User cUser;
    ClientsConnectionsManager clientsConnectionsManager;
    MasterConnectionManger masterConnectionManger;
// -------------------------------constructor------------------------------------------------

    public ClientThread(ClientsConnectionsManager clientsConnectionsManager, MasterConnectionManger masterConnectionManger, int cNum, Socket myCS) {
        this.clientNumber = cNum;
        this.clientsConnectionsManager = clientsConnectionsManager;
        this.masterConnectionManger = masterConnectionManger;
        try {
            mySocket = myCS;
            output = new ObjectOutputStream(myCS.getOutputStream());
            input = new ObjectInputStream(myCS.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();

            this.close();
        }
    }

    Thread clientHandler;

    public void run() {

        // recive("Welcome to Client.View.X_O Secure chat room");

        Thread parent = this;
        clientHandler = new Thread(parent) {
            Message receivedMsg;

            public void run() {
                while (active) {

                    try {
                        receivedMsg = (Message) input.readObject();

                        if (isValidMsg(receivedMsg)) {
                            ((ClientThread) parent).handel(receivedMsg);
                        }
                    } catch (EOFException e) {

                        // continue;
                        System.out.println("end of connection");
                        close();
                        break;

                    } catch (IOException | ClassNotFoundException e) {

                        e.printStackTrace();

                        close();

                    } catch (NotUniqueUserNameException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        clientHandler.start();
    }

    private void handel(Message receivedMsg) throws NotUniqueUserNameException {
        try {
            Message.handel(receivedMsg, this);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private boolean isValidMsg(Message receivedMsg) {
        if (receivedMsg != null)
            if (receivedMsg.TTL > 0)
                return true;
        return false;
    }

    //---------------------------------------helper ----------------------------------------------------------
    void sendToClient(Message msg) {
        System.out.println("Sending to cliend \n " + msg.toString());
        Message m = new Message(msg);
        m.decTTL();

        try {
            output.writeObject(m);
            output.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void close() {
        Message m = new Message(clientsConnectionsManager.getServerUser(), null, cUser.userName, MessageType.LOGOUT);


        try {

            this.input.close();
            this.output.close();
            this.mySocket.close();


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            clientsConnectionsManager.close(this);
            masterConnectionManger.sendToMaster(m);
            clientsConnectionsManager.sendToAll(m);
            active = false;

        }


    }

    @Override
    public void register(Message registerMessage) {


        String cn = "";
        cn += clientNumber;
        cUser = new User(cn, null);
        //cUser.userName=cn;
        registerMessage.users = new User[1];
        registerMessage.users[0] = cUser;
        registerMessage.TTL = 4;
        masterConnectionManger.sendToMaster(registerMessage);

    }

    @Override
    public void publicMessage(Message msg) {
        clientsConnectionsManager.sendToAll(msg);
        masterConnectionManger.sendToMaster(msg);

    }

    @Override
    public void privateMessage(Message msg) {
        ClientThread pu = clientsConnectionsManager.find(msg.receiver);
        if (pu == null) {
            masterConnectionManger.sendToMaster(msg);

        } else {
            pu.sendToClient(msg);
        }

    }

    @Override
    public void onlineUsersRequest(Message message) {

    }

    @Override
    public void sendToAll(Message message) {
        clientsConnectionsManager.sendToAll(message);
    }

    @Override
    public void logout(Message msg) {
        close();
    }

    @Override
    public void getAllUsers(Message msg) {

        Message m = new Message(clientsConnectionsManager.getServerUser(),
                msg.sender, "",
                MessageType.GETALLUSERS);
        masterConnectionManger.sendToMaster(m);
    }

    @Override
    public void newUser(Message msg) {

    }


    public void register2(Message message) throws NotUniqueUserNameException {

    }
}