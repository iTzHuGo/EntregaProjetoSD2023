package com.example.meta1;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SearchModuleInterface extends Remote {
    public boolean verifyLogin(String username, String password) throws RemoteException;
    // public void runIndexInfo(String url) throws RemoteException;
    public void addURL(String url) throws RemoteException;
    public String search(String term) throws RemoteException;
    public String search2(String url) throws RemoteException;
}
