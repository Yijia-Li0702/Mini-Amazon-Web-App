package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.GeneratedMessageV3;

import server.A2U.*;
import server.A2U.world_info;
import server.U2A.*;

public class CommUPS {
    ServerSocket ss;
    InputStream in;
    OutputStream out;
    
    public CommUPS(){
        try {
            ss = new ServerSocket(22222);
            System.out.println("--------waiting for ups to connect--------");
            Socket client = ss.accept();
            System.out.println("--------ups has connected to amazon--------");
            in = client.getInputStream();
            out = client.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long recvWorldId(){
        UToA uToA = recv();
        return uToA.getWorldInfo().getWorldid();
    }

    public void sendWorldStatus(long worldId, boolean result){
        world_info worldInfo = world_info.newBuilder().setWorldid(worldId).setResult(result).build();
        AToU aToU = AToU.newBuilder().setWorldInfo(worldInfo).build();
        send(aToU);
    }

    public void sendRequestTruckToUPS(long packageId, int whNum, String UPSAccount, int x, int y, String description){
        request_truck requeTruck = request_truck.newBuilder().setShipid(packageId).setWhnum(whNum).setUpsAccountId(UPSAccount).setLocationX(x).setLocationY(y).setItemDesc(description).build();
        AToU aToU = AToU.newBuilder().setRequest(requeTruck).build();
        send(aToU);
    }

    public void sendChangeDestToUPS(long packageId, int newX, int newY){
        change_destination changeDest = change_destination.newBuilder().setShipid(packageId).setLocationX(newX).setLocationY(newY).build();
        AToU aToU = AToU.newBuilder().setChangeDest(changeDest).build();
        send(aToU);
    }

    public void sendReadyForDeliveryToUPS(long packageId){
        ready_for_delivery readyForDelivery = ready_for_delivery.newBuilder().setShipid(packageId).build();
        AToU aToU = AToU.newBuilder().setReadyForDelivery(readyForDelivery).build();
        send(aToU);
    }
    
    public void send(AToU aToU){
        try{
            aToU.writeDelimitedTo(out);
            out.flush();
            /*
            byte[] data = aToU.toByteArray();
            CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(out);
            codedOutputStream.writeUInt32NoTag(data.length);
            codedOutputStream.writeRawBytes(data);
            codedOutputStream.flush();
            */
            System.out.println("Amazon sends to UPS:");
            System.out.println(aToU.toString());
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UToA recv(){
        UToA uToA = null;
        try {
            uToA = UToA.parseDelimitedFrom(in);
            /*
            UToA.Builder uToABuilder = UToA.newBuilder();
            CodedInputStream codedInputStream = CodedInputStream.newInstance(in);
            int len = codedInputStream.readRawVarint32();
            int oldLimit = codedInputStream.pushLimit(len);
            uToABuilder.mergeFrom(codedInputStream);
            codedInputStream.popLimit(oldLimit);
            uToA = uToABuilder.build();
            */
            if(uToA != null){
                System.out.println("Amazon receives from UPS:");
                System.out.println(uToA.toString());
            }
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        return uToA;
    }

    public void parseTruckArrivedResponse(truck_arrived uToA){
        long packageId = uToA.getShipid();
        int truckId = uToA.getTruckid();
        App.truckArrivedMap.put(packageId, truckId);
        String status = App.packagesStatusMap.get(packageId);
        int whNum = 1;
        if(status.equals("packed")){
            App.commWorld.putOnTruck(packageId, truckId, whNum);
        }
    }

    public void parseDeliverStartResponse(deliver_started uToA){
        long packageId = uToA.getShipid();
        App.commWorld.updateStatus(packageId, "delivering");
    }

    public void parseDeliveredResponse(delivered uToA){
        long packageId = uToA.getShipid();
        App.commWorld.updateStatus(packageId, "delivered");
    }

    public void solveAResponse(UToA uToA){
        if(uToA.hasTruckArrived()){
            parseTruckArrivedResponse(uToA.getTruckArrived());
        }
        if(uToA.hasDeliverStarted()){
            parseDeliverStartResponse(uToA.getDeliverStarted());
        }
        if(uToA.hasDelivered()){
            parseDeliveredResponse(uToA.getDelivered());
        }
    }

    public void runCommWithUPS(){
        while(true){
            UToA uToA = recv();
            if(uToA != null){
                solveAResponse(uToA);
            }
        }
    }
/*
    public void runCommWithUPS(){
        try {
            while(true){
                Socket client = ss.accept();
                System.out.println("--------ups has connected to amazon--------");
                in = client.getInputStream();
                out = client.getOutputStream();
                UToA uToA = recv();
                if(uToA != null){
                    solveAResponse(uToA);
                }
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
 */
    
}
