package com.example.meta1;
import java.rmi.*;

public interface BarrelInterface extends Remote {
    public String searchWord(String word) throws RemoteException;
    public String searchUrl(String url) throws RemoteException;
}
