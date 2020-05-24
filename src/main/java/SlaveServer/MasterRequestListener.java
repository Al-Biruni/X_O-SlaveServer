package SlaveServer;

<<<<<<< HEAD:X_OMaxV2.0/SlaveServer/src/main/java/SlaveServer/MasterRequestListener.java
import xoLib.Exceptions.NotUniqueUserNameException;
import xoLib.Message.Message;
=======
import Commons.Message.Message;
>>>>>>> 849689989595f5a55e0e97aa0b3d48b1c88050da:X_OMaxV2.0/Server/src/main/SlaveServer/MasterRequestListener.java

import java.io.IOException;
import java.io.ObjectInputStream;


public class MasterRequestListener extends Thread {
    ObjectInputStream masterInputStream;
    SlaveServerMasterMessageHandler  masterRequestHandler;

    public MasterRequestListener(Server server) {
        masterRequestHandler = server.getMasterRequestHandler();
        this.masterInputStream = server.masterConnectionManger.getInputStream();

    }

    public void run() {
        Message masterReq;
        while (true) {
            // check for master
            try {
                masterReq = (Message) masterInputStream.readObject();

                if (validMasterRequest(masterReq)) {
                    Message.handel(masterReq,masterRequestHandler);
                    System.out.println("MAster  request: " + masterReq.toString());

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NotUniqueUserNameException e) {
                e.printStackTrace();
            }

        }
    }


    private boolean validMasterRequest(Message masterReq) {
        if(masterReq == null || masterReq.TTL<=0)
            return false;
        return true;
    }


}
