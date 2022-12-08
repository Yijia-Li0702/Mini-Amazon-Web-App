package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import server.WorldAmazonProtoTxt.*;

public class CommWorld {
    volatile Socket worldSocket;
    volatile OutputStream out;
    volatile InputStream in;
    Map<Long, ACommands> notAckedCommandsMap;
    ConcurrentLinkedQueue<Long> notAckedQueue;
    Map<Long, ACommands> sentAcksMap;
    //key is sequence number and value is package_id, this is for purchasemore to know which package's purchase has arrived 
    Map<Long, Long> purchaseMap;
    DbManager dbManager;
    volatile boolean finished = false;

    
    public CommWorld(String ip){
        buildAmazonWorldSocket(ip);
        dbManager = new DbManager();
        notAckedCommandsMap = new ConcurrentHashMap<>();
        sentAcksMap = new ConcurrentHashMap<>();
        purchaseMap = new ConcurrentHashMap<>();
        notAckedQueue = new ConcurrentLinkedQueue<>();
    }

    public void buildAmazonWorldSocket(String ip){
        try{
            System.out.println("trying to connect to world....");
            worldSocket = new Socket(ip, 23456);
            out = worldSocket.getOutputStream();
            in = worldSocket.getInputStream();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }    

    public boolean createWorld(){
        AInitWarehouse aiwh = AInitWarehouse.newBuilder().setId(1).setX(1).setY(1).build();
        //not denote id means create a world
        AConnect aconn = AConnect.newBuilder().setIsAmazon(true).addInitwh(aiwh).build();
        AConnected connectedResponse = null;
        try {
            aconn.writeDelimitedTo(out);
            out.flush();
            connectedResponse = AConnected.parseDelimitedFrom(in);  
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        if(connectedResponse.getResult().contains("error")){
            System.out.println(connectedResponse.toString());
            return false;
        }
        else{
            System.out.println("--------Successfully connect to the world---------");
            return true;
        }
    }

    public boolean connectToWorld(long worldId, boolean initWarehouse){
        AConnect aconn = null;
        if(initWarehouse){
            AInitWarehouse aiwh = AInitWarehouse.newBuilder().setId(1).setX(1).setY(1).build();
            aconn = AConnect.newBuilder().setIsAmazon(true).setWorldid(worldId).addInitwh(aiwh).build();
        }
        else{
            aconn = AConnect.newBuilder().setIsAmazon(true).setWorldid(worldId).build();
        }
        //AConnect aconn = AConnect.newBuilder().setIsAmazon(true).setWorldid(worldId).build();
        AConnected connectedResponse = null;
        //while(true){
            try {
                aconn.writeDelimitedTo(out);
                out.flush();
                System.out.println("amazon sends to world:");
                System.out.println(aconn.toString());
                connectedResponse = AConnected.parseDelimitedFrom(in); 
                if(connectedResponse == null){
                    return false;
                }
            } 
            catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("amazon receives from world:");
            System.out.println(connectedResponse.toString());
            if(connectedResponse.getResult().contains("error")){
                return false;
            }
            else{
                return true;
            }

        //}
    }

    public void reconnectToWorld(long worldId){
        AConnect aconn = AConnect.newBuilder().setIsAmazon(true).setWorldid(worldId).build();
        AConnected connectedResponse = null;
        boolean connected = false;
        try {
            System.out.println("trying to build socket communication with world again...");
            this.worldSocket = new Socket(App.worldIP, 23456);
            in = worldSocket.getInputStream();
            out = worldSocket.getOutputStream();
            connectToWorld(worldId, false);
            System.out.println("---------connected to world again---------");
/*
            while(!connected){  
                aconn.writeDelimitedTo(out);
                out.flush();
                System.out.println("amazon sends to world:");
                System.out.println(aconn.toString());
                connectedResponse = AConnected.parseDelimitedFrom(in); 
                if(connectedResponse == null){
                    connected = false;
                }
                else{
                    System.out.println("amazon receives from world:");
                    System.out.println(connectedResponse.toString());
                    if(connectedResponse.getResult().contains("error")){
                        connected = false;
                    }
                    else{
                        connected = true;
                    }
                }
            }
            */
        } 
        catch (IOException e) {
            e.printStackTrace();
            
        }
    }

    public ACommands buildPurchaseMore(long seqNum, int whNum, long productId, String description, int count){
        AProduct ap = AProduct.newBuilder().setId(productId).setDescription(description).setCount(count).build();
        APurchaseMore apm = APurchaseMore.newBuilder().setSeqnum(seqNum).setWhnum(whNum).addThings(ap).build();
        ACommands ac = ACommands.newBuilder().addBuy(apm).build();
        return ac;
    }


    public ACommands buildToPack(long seqNum, int whNum, long packageId, long productId, String description, int count){
        AProduct ap = AProduct.newBuilder().setId(productId).setDescription(description).setCount(count).build();
        APack apack = APack.newBuilder().setWhnum(whNum).setShipid(packageId).setSeqnum(seqNum).addThings(ap).build();
        ACommands ac = ACommands.newBuilder().addTopack(apack).build();
        //ACommands ac = ACommands.newBuilder().addTopack(apack).setDisconnect(true).build();
        return ac;
    }

    public ACommands buildPutOnTruck(long packageId, int truckId, int whnum, long seqNum){
        APutOnTruck apot = APutOnTruck.newBuilder().setWhnum(whnum).setTruckid(truckId).setShipid(packageId).setSeqnum(seqNum).build();
        //ACommands ac = ACommands.newBuilder().addLoad(apot).setDisconnect(true).build();
        ACommands ac = ACommands.newBuilder().addLoad(apot).build();
        return ac;
    }

    public void send(ACommands command){
        try {
            command.writeDelimitedTo(out);
            out.flush();
            System.out.println("Amazon sends to world:");
            System.out.println(command.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AResponses recv(){
        AResponses response = null;
        try {
            response = AResponses.parseDelimitedFrom(in);
            if(response != null){
                System.out.println("Amazon receives from world:");
                System.out.println(response.toString());
            }
        } 
        catch(IOException e){
            e.printStackTrace();
        }

        return response;
    }

    //send command other than ack command
    public void sendANormalCommand(long seqNum, ACommands command){
        notAckedCommandsMap.put(seqNum, command);
        notAckedQueue.add(seqNum);
        send(command);
    }

    //can be called when send the first ack
    public void sendAckCommand(long ackNum){
        ACommands ackCommand = ACommands.newBuilder().addAcks(ackNum).build();
        send(ackCommand);
        sentAcksMap.put(ackNum, ackCommand);
    }

    public void updateStatus(long packageId, String status){
        dbManager.updateOrderStatus(packageId, status);
        App.packagesStatusMap.put(packageId, status);
    }
    
    public void purchaseAllProducts(int amount){
        List<Product> productsList = dbManager.getAllProducts();
        int count = productsList.size();

         //break loop when recving all purchaseResponse
        Thread t1 = new Thread(){
            public void run(){
                int i = 0;
                while(i != count){
                    try{
                        AResponses response = recv();
                        parseFinishedResponse(response);
                        parseAcksResponse(response);
                        i += parsePurchaseMoreResponse(response);
                        System.out.println("i:"+i+" "+"count:"+count);
                    }
                    catch(Exception e){
                        e.getMessage();
                    }
                }
            }
        };
        t1.start();
        
        Thread t2 = new Thread(){
            public void run(){
                int whNum = 1;
                ACommands.Builder commandBuilder = ACommands.newBuilder();
                for(int i = 0; i < count; i++){
                    Product product = productsList.get(i);
                    long seqNum = dbManager.getSeqNumAndAddOne();
                    AProduct ap = AProduct.newBuilder().setId(product.productId).setDescription(product.description).setCount(amount).build();
                    APurchaseMore apm = APurchaseMore.newBuilder().setSeqnum(seqNum).setWhnum(whNum).addThings(ap).build();
                    ACommands command = ACommands.newBuilder().addBuy(apm).build();
                    commandBuilder.addBuy(apm);                    
                    notAckedCommandsMap.put(seqNum, command);
                    notAckedQueue.add(seqNum);
                }
                send(commandBuilder.build());
            }
        };
        t2.start();

        /*
        Thread t2 = new Thread(){
            public void run(){
                int whNum = 1;
                ACommands command = null;
                for(int i = 0; i < count; i++){
                    Product product = productsList.get(i);
                    long seqNum = dbManager.getSeqNumAndAddOne();
                    // if(i == 0){//add finished = true
                    //     AProduct ap = AProduct.newBuilder().setId(product.productId).setDescription(product.description).setCount(amount).build();
                    //     APurchaseMore apm = APurchaseMore.newBuilder().setSeqnum(seqNum).setWhnum(whNum).addThings(ap).build();
                    //     command = ACommands.newBuilder().addBuy(apm).setDisconnect(true).build();
                    // }
                    //else{
                        command = buildPurchaseMore(seqNum, whNum, product.productId, product.description, amount);
                    //}
                    sendANormalCommand(seqNum, command);
                    notAckedCommandsMap.put(seqNum, command);
                    notAckedQueue.add(seqNum);
                }
            }
        };
        t2.start();
*/
        try{
            t2.join();
            t1.join();
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    public void parseAcksResponse(AResponses response){
        for(long seqNum: response.getAcksList()){
            //if ack is recvd, delete the command from map
            // System.out.println("before this Ack:");
            // System.out.println(notAckedCommandsMap);
            if(notAckedCommandsMap.containsKey(seqNum)){
                notAckedCommandsMap.remove(seqNum);
            }
            else{//denote curr seq num has alreday recvd ack
                continue;
            }
            // System.out.println("after this Ack:");
            // System.out.println(notAckedCommandsMap);
        }
    }

    public int parsePurchaseMoreResponse(AResponses response){
        int count = response.getArrivedCount();
        for(APurchaseMore apm: response.getArrivedList()){
            long ackNum = apm.getSeqnum();
            AProduct ap = apm.getThings(0);
            //if ack is not sent for current response
            if(!sentAcksMap.containsKey(ackNum)){
                //send ack and put ack in the map
                sendAckCommand(ackNum);
                //add stock to warehouse
                dbManager.updateProductAmount(true, ap.getCount(), ap.getId());
            }
            else{//send ack again if world request for it again
                sendRepeatedAcks(sentAcksMap.get(ackNum));
            }
        }
        return count;
    }

    public void parsePackedResponse(AResponses response){
        for(APacked ap: response.getReadyList()){
            //if this is the first time i recv this response
            long ackNum = ap.getSeqnum();     
            long packageId = ap.getShipid();
            if(!sentAcksMap.containsKey(ackNum)){
                sendAckCommand(ackNum);
                updateStatus(packageId, "packed");
                //if truck has arrived and package is packed, send load to world
                if(App.truckArrivedMap.containsKey(packageId) && App.packagesStatusMap.get(packageId).equals("packed")){
                    int truckId = App.truckArrivedMap.get(packageId);
                    int whNum = 1;
                    putOnTruck(packageId, truckId, whNum);
                }
            }
            else{
                sendRepeatedAcks(sentAcksMap.get(ackNum));
            }
        }
    }

    public void putOnTruck(long packageId, int truckId, int whNum){
        //long currSeqNum = App.seqNum.getAndIncrement();
        long currSeqNum = dbManager.getSeqNumAndAddOne();
        ACommands loadCommand = buildPutOnTruck(packageId, truckId, whNum, currSeqNum);
        sendANormalCommand(currSeqNum, loadCommand);
        updateStatus(packageId, "loading");
    }

    public void parseLoadedResponse(AResponses response){ 
        for(ALoaded al: response.getLoadedList()){
            long ackNum = al.getSeqnum();
            long packageId = al.getShipid();
            if(!sentAcksMap.containsKey(ackNum)){
                sendAckCommand(ackNum);
                updateStatus(packageId, "loaded");
                //to ups
                App.commUPS.sendReadyForDeliveryToUPS(packageId);
            }
            else{
                sendRepeatedAcks(sentAcksMap.get(ackNum));
            }
        }
    }

    public void parseErrorResponse(AResponses response){ 
        for(AErr ae: response.getErrorList()){
            long ackNum = ae.getSeqnum();
            if(!sentAcksMap.containsKey(ackNum)){
                sendAckCommand(ackNum);
            }
            else{
                sendRepeatedAcks(sentAcksMap.get(ackNum));
            }
        }
    }

    public void parseFinishedResponse(AResponses response){
        boolean isfinished = response.getFinished();
        if(isfinished){
            this.finished = true;
            reconnectToWorld(App.worldId);
            //System.out.println("reconnect Success is: "+reconnectSuccess);
            //notify resend thread to resend
            this.finished = false;
        }
    }

    public void parseResponse(AResponses response){  
        //if disconnect, need to build the socket again
        parseFinishedResponse(response);     
        parseAcksResponse(response);
        parsePurchaseMoreResponse(response);
        parsePackedResponse(response);
        parseLoadedResponse(response);
        parseErrorResponse(response);      
    }

    public void sendRepeatedAcks(ACommands command){
        System.out.println("Resend Acks:");
        send(command);
    }
    
    public void resendCommand(){
        while(true){
            while(!notAckedQueue.isEmpty()){
                long notAckedSeqNum = notAckedQueue.poll();
                //in the map means this seq num is still not acked
                if(notAckedCommandsMap.containsKey(notAckedSeqNum)){
                    ACommands command = notAckedCommandsMap.get(notAckedSeqNum);
                    if(command != null){
                        while(finished){
                            System.out.println("waiting for connecting to world again");
                            try{
                                Thread.sleep(2000);
                            }
                            catch(InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                        send(command);
                        //resend command every 1 second
                        try{
                            Thread.sleep(300);
                        }
                        catch(InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
                else{
                    continue;
                }
                notAckedQueue.add(notAckedSeqNum);
            }
        }
        /*
        while(true){
            for(long currSeqNum: notAckedCommandsMap.keySet()){
                System.out.println("finished:"+finished);
                while(finished){
                    System.out.println("waiting for connecting to world again");
                }
                System.out.println("Resend Message:");
                ACommands command = notAckedCommandsMap.get(currSeqNum);
                if(command != null){
                    send(command);
                }
                try{
                    Thread.sleep(1000);
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
        */
    }

    public void runCommWithWorld(){       
        while(true){
            if(!this.finished){
                AResponses response = recv();
                if(response != null){  
                    new Thread(){
                        public void run(){
                            parseResponse(response);
                        }
                    }.start();
                }
            }
        }
    }

}
