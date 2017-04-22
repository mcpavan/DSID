/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EPDSID;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.BiConsumer;

/**
 *
 * @author theus
 */
public class Client {
    private Scanner sc;
    private String serverName, subPartsList, rmiIp;
    private PartRepository repository;
    private Part currentPart;
    private HashMap<Part, Integer> currentSubPartList;
    
    private Client(){
        this.sc = new Scanner(System.in);
        currentPart = null;
        currentSubPartList = new HashMap<>();
    }
    
    public static void main (String[] args){
        try{
            Client client = new Client();
            System.out.println("Type the RMI Registry IP: ");
            client.rmiIp = client.sc.nextLine();
            if(client.rmiIp.equals("")) client.rmiIp = null;
            
            boolean connected = client.showServers();
            while (connected){
                connected = client.run();
            }
        } catch (Exception e){
            System.err.println("Client Exception: " + e);
            e.printStackTrace();
        } 
    }
    
    private boolean showServers() throws Exception{
        Registry registry = LocateRegistry.getRegistry(rmiIp);
        for(int i = 0; i < 3; i++){
            System.out.println("These are the connected servers at this moment:\n" + Arrays.toString(registry.list()) + "\n");
            System.out.println("\nPlease use bind <server name> to connect to a server.");
            
            String line = sc.nextLine();
            String[] splittedLine = line.split(" ");
            if(splittedLine[0].equals("bind")){
                serverName = splittedLine[1];
                if(connect()) return true;
            }
        }
        System.out.println("Three attempts failed. Closing the Client.");
        return false;
    }
    
    private boolean connect() throws Exception{
        try{
            Registry registry = LocateRegistry.getRegistry(rmiIp);
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
                if(splittedLine[2] == null) System.out.println("Please insert the arguments for this command: addp <name> <desc>");
                else{
                    currentPart = repository.addNewPart(splittedLine[1], splittedLine[2], currentSubPartList, 1);
                    System.out.println("The new part has been added to the current repository!");
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
                else System.out.println(currentPart.getSubPartList());
                break;
            case "addsubpart":
                int quantity = 1;
                if(splittedLine.length > 1) quantity = Integer.parseInt(splittedLine[1]);
                
                if(currentPart == null) System.out.println("There is no part selected to add.");
                else if(currentSubPartList.containsKey(currentPart)) {
                    currentSubPartList.replace(currentPart, (int)(currentSubPartList.get(currentPart))+quantity);
                    System.out.println("The subpart quantity has been updated on the SubPart List!");
                } else {
                    currentSubPartList.put(currentPart, quantity);
                    System.out.println("The subpart has been added to the SubPart List!");
                }
                break;
            case "clearlist":
                currentSubPartList.clear();
                System.out.println("The SubPart list has been cleared!");
                break;
            case "showreplist":
                Registry registry = LocateRegistry.getRegistry(rmiIp);
                System.out.println("These are the connected servers at this moment:\n" + Arrays.toString(registry.list()) + "\n\nPlease use bind <server name> to connect to a different server.");
                break;
            default:
                System.out.println("Please type a valid command:\n"+
                                    "quit\t\t\tdisconnects the client\n" +
                                    "bind <servername>\tconnects to a new server\n" +
                                    "showrep\t\t\tshows repository information\n" +
                                    "listp\t\t\tlists the repository parts\n" +
                                    "getp <partCode>\t\tgets the part to current part\n" +
                                    "addp <name> <desc>\tadds a new part and with current subPart list to the repository\n" +
                                    "showp\t\t\tshows current part information\n" +
                                    "locationp\t\tshows current part location\n" +
                                    "isprimitive\t\ttells if the current part is primitive\n" +
                                    "nsubpart\t\tshows the number of current part subComponents\n" +
                                    "listsubpart\t\tlists the current part subComponents\n" +
                                    "addsubpart <n>\t\tadds the current part to the current subpart list\n" +
                                    "clearlist\t\tclears the current subpart list\n" +
                                    "showreplist\t\tshows the active repositories\n" +
                                    "help\t\t\tshows help message");
        }
        System.out.println("\n\n");
        return true;
    }
}
