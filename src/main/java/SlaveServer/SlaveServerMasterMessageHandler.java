package SlaveServer;

import xoLib.*;
import xoLib.Exceptions.NotUniqueUserNameException;
import xoLib.Message.Message;
import xoLib.Message.MessageHandler;
import xoLib.Message.MessageType;


public class SlaveServerMasterMessageHandler implements MessageHandler {

    ClientsConnectionsManager clientsConnectionsManager;
    User serverUser;

    public SlaveServerMasterMessageHandler(User serverUser, ClientsConnectionsManager clientsConnectionsManager, MasterConnectionManger masterConnectionManger){
        this.clientsConnectionsManager = clientsConnectionsManager;
        this.serverUser = serverUser;


    }


    @Override
    public void sendToAll(Message message) {
        clientsConnectionsManager.sendToAll(message);
    }

    @Override
    public void logout(Message msg) {
        sendToAll(msg);
    }

    @Override
    public void getAllUsers(Message msg) {
        sendToAll(msg);
    }

    @Override
    public void newUser(Message msg) {
sendToAll(msg);
    }


    @Override
    public void register(Message masterReq) throws  NotUniqueUserNameException{
        if (masterReq.msgBody.equals("true")) {
            uniqueName(masterReq);

        } else {
            notUniqueName(masterReq);
            throw new NotUniqueUserNameException(new Exception());

        }
    }

    @Override
    public void publicMessage(Message masterReq) {
        clientsConnectionsManager.sendToAll(masterReq);

    }

    //send requests to master
    @Override
    public void privateMessage(Message masterReq) {
        clientsConnectionsManager.sendPrivateMsg(masterReq);
    }


    private void uniqueName(Message masterReq) {
        clientsConnectionsManager.register(masterReq.users[0], masterReq.receiver);

        for (ClientThread ct : clientsConnectionsManager.maxThreadPool) {
            if(ct!=null)
                if(ct.cUser!=null)
            if (ct.cUser.userName.equals(masterReq.receiver.userName)) {
                ct.sendToClient(masterReq);

            } else {
                ct.sendToClient(new Message(serverUser, masterReq.receiver, "", MessageType.NEWUSER));
            }

        }
    }

    private void notUniqueName(Message masterReq) {
        //the registration process is made with a temp user created with the client number
        //so we can communicate with the registering user without having a duplicated name
        // we save the temp user in User[0] we manipulated this for less variables this isnt the intended use
        for (ClientThread ct : clientsConnectionsManager.maxThreadPool) {
            if(ct!=null)
                if(ct.cUser!=null)
            if (ct.cUser.userName.equals(masterReq.users[0].userName)) {
                ct.sendToClient(masterReq);

            }
        }
    }


    public void onlineUsersRequest(Message masterReq) {
        for (ClientThread ct : clientsConnectionsManager.getActiveClients())
            if (ct.cUser != null)// didn't finish registration yet
                if (masterReq.receiver.userName.equals(ct.cUser.userName))
                    ct.sendToClient(masterReq);
        System.out.println("Se3nding members LIst to client " + masterReq.sender.userName);
    }



}
