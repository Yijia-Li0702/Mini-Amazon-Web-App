package server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.*;

import server.U2A.UToA;
import server.WorldAmazonProtoTxt.ACommands;
import server.WorldAmazonProtoTxt.AResponses;


public class App 
{    
    static long worldId = 0;
    //static CommWorld commWorld = new CommWorld("vcm-18233.vm.duke.edu");
    static String worldIP = "127.0.0.1";
    static CommWorld commWorld = new CommWorld(worldIP);
    static CommClient commClient = new CommClient();
    static CommUPS commUPS = new CommUPS();
    static Map<Long, String> packagesStatusMap = new ConcurrentHashMap<>();
    static Map<Long, Integer> truckArrivedMap = new ConcurrentHashMap<>();
    static AtomicLong seqNum = new AtomicLong((long)9);
    

    public static void main(String[] args)
    {        
        App main = new App(); 
        DbManager db = new DbManager();
        //db.createSeqNumTable();
        //db.insertSeqNum();  

        // DbManager db = new DbManager();
        // db.dropAllTables();
        // db.createAllTables();
        // db.insertEntries();
    
        //if(!commWorld.createWorld())return;
    
       
        App.worldId = commUPS.recvWorldId();
        boolean connResult = commWorld.connectToWorld(worldId, true);
        commUPS.sendWorldStatus(worldId, connResult);
        if(!connResult)return;

        new Thread(){
            public void run(){
                commWorld.resendCommand();
            }
        }.start();

        commWorld.purchaseAllProducts(1000000);
        System.out.println("Purchased All Products");

        new Thread(){
            public void run(){
                commWorld.runCommWithWorld();
            }
        }.start();
        System.out.println("communicating with world is running");

        new Thread(){
            public void run(){
                commClient.runCommWithClient();
            }
        }.start();
        System.out.println("communicating with world is running");

        new Thread(){
            public void run(){
                commUPS.runCommWithUPS();
            }
        }.start();
        System.out.println("communicating with UPS is running");
        
    }
    
}
