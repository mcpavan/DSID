/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EPDSID;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 *
 * @author theus
 */
public class Client {
    private Scanner sc;
    private String serverName, subPartsList;
    private PartRepository repository;
    private Part currentPart;
    private HashMap<Part, Integer> currentSubPartList;
    
    private Client(String serverName){
        this.serverName = serverName;
        this.sc = new Scanner(System.in);
        currentPart = null;
        currentSubPartList = new HashMap<>();
    }
    
    public static void main (String[] args){
        if (args.length<1){
            System.err.println("No args found.");
            return;
        }
        
        String serverName = args[0];
        try{
            Client client = new Client(serverName);
            boolean connected = client.connect();
            while (connected){
                connected = client.run();
            }
        } catch (Exception e){
            System.err.println("Client Exception: " + e);
            e.printStackTrace();
        } 
    }
    
    private boolean connect() throws Exception{
        try{
            Registry registry = LocateRegistry.getRegistry();
            this.repository = (PartRepository) registry.lookup(serverName);
            return true;
        } catch (Exception e){
            System.err.println("Client connection error: " + e);
        }
        return false;
    }
    
    private boolean run() throws Exception{
        BiConsumer<Part,Integer> writeList = (part, quantity) -> {
            this.subPartsList += "\n\tSubpartCode: " + Integer.toString(part.getPartCode()) + "\tSubPart Name: " + part.getPartName() +
                                 "\tQuantity: "+ quantity + "\tLocation: " + part.getPartLocation();        
        };
        
        String currentPartCode = (currentPart==null) ? "":Integer.toString(currentPart.getPartCode());
        String currentPartName = (currentPart==null) ? "":currentPart.getPartName();
        subPartsList = "";
        currentSubPartList.forEach(writeList);
        System.out.print("Current Repository: " + serverName +
                         "\nCurrent Part: " + currentPartCode + " - " + currentPartName + 
                         "\nCurrent SubPart List: " + subPartsList +
                         "\n>>");
        String line = sc.nextLine();
        String[] splittedLine = line.split(" ");
        switch(splittedLine[0].toLowerCase()){
            case "quit":
                System.out.println("Bye!");
                return false;
            case "bind":
                String backup = serverName;
                serverName = splittedLine[1];
                if (connect()){
                    System.out.println("Connected with server " + serverName);
                    break;
                }
                System.out.println("Failed Connection with server " + splittedLine[1] + ". Reconnected to server "+ serverName);
                serverName = backup;
                connect();
                break;
            case "showrep":
                System.out.println(repository.getRepositoryInfo());
                break;
            case "listp":
                System.out.print(repository.getPartList());
                break;
            case "getp":
                currentPart = repository.getPart(Integer.parseInt(splittedLine[1]));
                
                if(currentPart==null) System.out.println("No parts found with code " + splittedLine[1]);
                else System.out.println("Part " + splittedLine[1] + " selected!");
                
                break;
            case "addp":
                if(currentPart==null) System.out.println("There is no part to add.");
                else{
                    /*BiConsumer<Part, Integer> addSubParts = (part, quantity) -> {
                        try {
                            if(part.getPartLocation() == null){
                                part.setLocation(serverName);
                                repository.addNewPart(part, quantity);
                            }
                        } catch (RemoteException e) {
                            System.err.println("Client Exception: " + e);
                            e.printStackTrace();
                        }
                    };*/
                    
                    currentPart.setLocation(serverName);
                    currentPart.addSubPartList((HashMap<Part, Integer>)currentSubPartList.clone());
                    //currentSubPartList.forEach(addSubParts);
                    repository.addNewPart(currentPart,1);
                    System.out.println("The new part has been added to the current repository!");
                }
                break;
            case "createp":
                if(splittedLine[2].equals(line) || splittedLine[2] == null) System.out.println("Please insert the arguments for this command: addp <name> <desc>");
                else{
                    currentPart = new Part(splittedLine[1], splittedLine[2]);
                    System.out.println("The new part has been created!");
                }
                break;
            case "showp":
                if (currentPart == null) System.out.println("There is no part selected to show."); 
                else System.out.println(currentPart.getPartInfo());
                break;
            case "locationp":
                if (currentPart == null) System.out.println("There is no part selected to show."); 
                else System.out.println("The part " + Integer.toString(currentPart.getPartCode()) + " is located on server " + currentPart.getPartLocation());
                break;
            case "isprimitive":
                if (currentPart == null) System.out.println("There is no part selected to show."); 
                else System.out.println(currentPart.getSubPartCount()==0);
                break;
            case "nsubpart":
                if (currentPart == null) System.out.println("There is no part selected to show."); 
                else System.out.println(currentPart.getSubPartCount());
                break;
            case "listsubpart":
                if (currentPart == null) System.out.println("There is no part selected to show."); 
                else{
                    subPartsList = "";
                    currentSubPartList.forEach(writeList);
                    System.out.println(subPartsList);
                }
                break;
            case "addsubpart":
                int quantity = 1;
                if(splittedLine.length > 1) quantity = Integer.parseInt(splittedLine[1]);
                
                if(currentPart == null) System.out.println("There is no part selected to add.");
                else if(currentPart.getPartLocation() != null && !currentPart.getPartLocation().equals(serverName)){
                    currentSubPartList.put(currentPart, quantity);
                    System.out.println("The subpart has been added to the SubPart List!");
                } else if(currentSubPartList.containsKey(currentPart)) {
                    /*Set subPartSet = currentSubPartList.keySet();
                    for(Iterator it = subPartSet.iterator(); it.hasNext();){
                        Part p = (Part) it.next();
                        if(p.getPartCode()==currentPart.getPartCode()){
                            currentSubPartList.replace(p, (int)(currentSubPartList.get(p))+quantity);
                            return true;
                        }
                    }*/
                    currentSubPartList.replace(currentPart, (int)(currentSubPartList.get(currentPart))+quantity);
                    repository.addNewPart(currentPart, quantity);
                    System.out.println("The SubPart quantity has been updated on the SubPart List!");
                } else {
                    currentPart.setLocation(serverName);
                    currentSubPartList.put(currentPart, quantity);
                    repository.addNewPart(currentPart, quantity);
                    System.out.println("The subpart has been added to the SubPart List!");
                }
                break;
            case "clearlist":
                currentSubPartList.clear();
                System.out.println("The SubPart list has been cleared!");
                break;
            default:
                System.out.println("Please type a valid command:\n"+
                                    "quit\t\t\tdisconnects the client\n" +
                                    "bind <servername>\tconnects to a new server\n" +
                                    "showrep\t\t\tshows repository information\n" +
                                    "listp\t\t\tlists the repository parts\n" +
                                    "getp <partCode>\t\tgets the part to current part\n" +
                                    "addp\t\t\t\tadds the current part and subPart list to the repository\n" +
                                    "createp <name> <desc>\tcreates a new part on current Part \n" +
                                    "showp\t\t\tshows current part information\n" +
                                    "locationp\t\tshows current part location\n" +
                                    "isprimitive\t\ttells if the current part is primitive\n" +
                                    "nsubpart\t\tshows the number of current part subComponents\n" +
                                    "listsubpart\t\tlists the current part subComponents\n" +
                                    "addsubpart <n>\t\tadds the current part to the current subpart list\n" +
                                    "clearlist\t\tclears the current subpart list\n" +
                                    "help\t\t\tshows help message");
        }
        System.out.println("\n\n");
        return true;
    }
}
