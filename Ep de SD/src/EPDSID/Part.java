/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EPDSID;

import java.io.Serializable;
import java.util.HashMap;
import java.util.function.BiConsumer;

/**
 *
 * @author theus
 */
public class Part implements Serializable{
    private static int count = 0;
    private int code;
    private String name, desc, location, subPartsList;
    PartRepository server;
    private HashMap<Part, Integer> subComp;
    
    public Part(String name, String desc){
        this.code = ++count;
        this.name = name;
        this.desc = desc;
        subComp = new HashMap();
    }
    
    public int getPartCode(){
        return code;
    }

    public String getPartName(){
        return name;
    }

    public String getPartDesc(){
        return desc;
    }

    public String getSubPartList(){
        BiConsumer<Part,Integer> writeList = (part, quantity) -> {
            this.subPartsList += "\n\tSubpartCode: " + Integer.toString(part.getPartCode()) + "\tSubPart Name: " + part.getPartName() +
                                 "\tQuantity: "+ quantity + "\tLocation: " + part.getPartLocation();        
        };
        subPartsList = "";
        subComp.forEach(writeList);
        return subPartsList;
    }
    
    public String getPartLocation(){
        return location;
    }

    public void addNewSubPart(Part part){
        if (subComp.containsKey(part)){
            subComp.replace(part, (int)(subComp.get(code))+1);
        } else {
            subComp.put(part, 1);
        }
    }
    
    public void addSubPartList(HashMap<Part, Integer> subPartList){
        this.subComp = subPartList;
    }
    
    public void setLocation(String location,PartRepository server){
        this.location = location;
        this.server = server;
    }
    
    public PartRepository getServer(){
        return server;
    }
    
    public String getPartInfo(){
            return "Part Code: " + Integer.toString(getPartCode()) +
                   "\nPart Name: " + getPartName() +
                   "\nPart Description: " + getPartDesc() +
                   "\nPart Location: " + getPartLocation()+
                   "\nSub Components: " + getSubPartList();
    }
    
    public int getSubPartCount(){
        int cont = 0;
        for (Part part : subComp.keySet()){
            cont += subComp.get(part);
        }
        return cont;
    }
}
