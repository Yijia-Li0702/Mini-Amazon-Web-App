package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import server.FrontEnd.FECommands;
import server.FrontEnd.Fchange_dest;
import server.WorldAmazonProtoTxt.ACommands;

public class CommClient {
    ServerSocket ss;

    DbManager dbManager;
    public CommClient(){
        dbManager = new DbManager();
        try {
            ss = new ServerSocket(11111);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void solveAClient(Socket clientSocket){
        InputStream in = null;
        FrontEnd.FECommands request = null;
        try {
            in = clientSocket.getInputStream();
            request = FrontEnd.FECommands.parseDelimitedFrom(in);
            //clientSocket.close();
            System.out.println("Recv From Client:");
            System.out.println(request.toString());
            if(request.hasPurchase()){
                long packageId = request.getPurchase().getPackegeId();
                int whNum = 1;       
                //to World
                App.commWorld.updateStatus(packageId, "packing");
                Product product = dbManager.getProduct(packageId);
                //update database
                int remainedAmount = dbManager.updateProductAmount(false, product.count, product.productId);
                //long currSeqNum = App.seqNum.getAndIncrement();
                long currSeqNum = dbManager.getSeqNumAndAddOne();
                WorldAmazonProtoTxt.ACommands toPackCommand = App.commWorld.buildToPack(currSeqNum, whNum, packageId, product.productId, product.description, product.count);
                App.commWorld.sendANormalCommand(currSeqNum, toPackCommand); 
                //to UPS    
                Warehouse wh = dbManager.getWarehouse(whNum);  
                App.commUPS.sendRequestTruckToUPS(packageId, wh.whId, product.UPSAccount, product.x, product.y, product.description);
                //purchase more stock
                if(remainedAmount < 10000){
                    //currSeqNum = App.seqNum.getAndIncrement();
                    currSeqNum = dbManager.getSeqNumAndAddOne();
                    WorldAmazonProtoTxt.ACommands purchaseMoreCommand = App.commWorld.buildPurchaseMore(currSeqNum, whNum, product.productId, product.description, 100000000);
                    App.commWorld.sendANormalCommand(currSeqNum, purchaseMoreCommand); 
                }
            }
            if(request.hasChange()){
                FrontEnd.Fchange_dest fcd = request.getChange();
                App.commUPS.sendChangeDestToUPS(fcd.getPackegeId(), fcd.getNewX(), fcd.getNewY());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally{
            try {
                in.close();
                clientSocket.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }
    public void runCommWithClient(){
        try {
            while(true){
                System.out.println("listening to client:");
                Socket clientSocket = ss.accept();
                System.out.println("Client has connected");
                new Thread(){
                    public void run(){
                        solveAClient(clientSocket);
                    }
                }.start();
            } 
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally{
            try {
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } 
}
