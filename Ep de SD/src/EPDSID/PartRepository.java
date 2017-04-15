/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EPDSID;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author theus
 */
public interface PartRepository extends Remote {
    public String getRepositoryInfo() throws RemoteException;
    public int containsPart(int partCode) throws RemoteException;
    public String getPartList() throws RemoteException;
    
    public void addNewPart(Part part, int quantity) throws RemoteException;
    public Part getPart(int partCode) throws RemoteException;
}
