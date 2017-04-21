/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EPDSID;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 *
 * @author theus
 */
public class Server implements PartRepository{
    private String serverName;
    private int partsCount;
    private HashMap<Part, Integer> repository;
    private String partsList;
    
    //constructor for Server objects
    private Server (String name){
       this.serverName = name;
       this.partsCount = 0;
       this.repository = new HashMap();
    }
    
    public static void main(String[] args){
        try{
            Server server = new Server(args[0]);        
            PartRepository stub = (PartRepository) UnicastRemoteObject.exportObject(server, 0);
            //bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(server.serverName, stub);
            
            System.err.println("The server " + server.serverName + " is running.");
            System.out.println();
            Scanner sc = new Scanner(System.in);
            if(sc.nextLine().equals("quit")){
                registry.unbind(server.serverName);
            }
        } catch (Exception e){
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public String getRepositoryInfo() throws RemoteException {
        return "There are " + this.repository.size() + " parts at the repository located on the server " + this.serverName + ".";
    }

    @Override
    public int containsPart(int partCode) throws RemoteException {
        Set partSet = repository.keySet();
        for(Iterator it = partSet.iterator(); it.hasNext();){
            Part part = (Part) it.next();
            if(part.getPartCode() == partCode){
                return repository.get(part);
            }
        }
        return 0;
    }

    @Override
    public String getPartList() throws RemoteException {
        this.partsList =   "\t\t\t\tRepository " + serverName + 
                   "\n\n\t\t----------------------------------------------\n\n";
        
        BiConsumer<Part,Integer> writeList;
        writeList = (part, quantity) -> {
            this.partsList += part.getPartInfo() + "\nQuantity: "+ quantity + "\n\n";        
        };
        
        
        this.repository.forEach(writeList);
        
        return this.partsList;
    }
    
    @Override
    public Part addNewPart(String name, String desc, HashMap<Part, Integer> subPartList, int quantity) throws RemoteException {
        Set partSet = repository.keySet();
        for(Iterator it = partSet.iterator(); it.hasNext();){
            Part p = (Part) it.next();
            if(p.getPartName().equals(name) &&
               p.getPartDesc().equals(desc) &&
               p.getSubPartList().equals(subPartList)){
                repository.replace(p, (int)(repository.get(p))+quantity);
                return p;
            }
        }
        /*if(repository.containsKey(part)) repository.replace(part, (int)(repository.get(part))+quantity);
        */
        Part part = new Part(name, desc);
        part.setLocation(serverName, this);
        part.addSubPartList((HashMap <Part, Integer>) subPartList.clone());
        repository.put(part, quantity);
        return part;
    }
    
    @Override
    public Part getPart(int partCode) {
        Set partSet = repository.keySet();
        for(Iterator it = partSet.iterator(); it.hasNext();){
            Part part = (Part) it.next();
            if(part.getPartCode() == partCode){
                return part;
            }
        }
        return null;
    }
}
