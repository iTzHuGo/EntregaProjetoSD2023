package com.example.meta1;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class SearchModule extends UnicastRemoteObject implements SearchModuleInterface, Serializable {
    public static Socket s;
    public SearchModule() throws RemoteException {
        super();
    }

    public static void main(String[] args) {
        try {
            SearchModule searchModule = new SearchModule();
            LocateRegistry.createRegistry(5000).rebind("SearchModule", searchModule);
            try {
				s = new Socket("0.0.0.0", 6500);
			} catch (IOException e) {
				e.printStackTrace();
			}
            System.out.println("SearchModule Started");

        } catch (RemoteException re) {
            System.out.println("Remote Exception in SearchModule - Main: " + re);
        }
    }

    public boolean verifyLogin(String username, String password) {
        boolean isValid = false;
        try {
            Scanner scanner = new Scanner(System.in);

            if (username.equals("user") && password.equals("pass")) {
                isValid = true;
                System.out.println("Successfully logged on as " + username);
            }
            scanner.close();

        } catch (Exception e) {
            System.out.println("Exception in SearchModule - Login: " + e);
        }
        return isValid;

    }

    public void addURL(String url) throws RemoteException {
        try {
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            out.writeUTF(url);
            // s.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String search(String term) throws RemoteException {
        // invoke barrel method somewhere here somehow
        try {
            BarrelInterface barrel = (BarrelInterface) LocateRegistry.getRegistry(5100).lookup("Barrel");
            System.out.println("Barrel connection established");
            System.out.println("Searching for " + term);
            return barrel.searchWord(term);
        } catch (RemoteException | NotBoundException re) {
            System.out.println("Exception in SearchModule search: " + re);
            return null;
        }
    }

    public String search2(String url) throws RemoteException {
        try {
            BarrelInterface barrel = (BarrelInterface) LocateRegistry.getRegistry(5100).lookup("Barrel");
            System.out.println("Barrel connection established");
            System.out.println("Searching for URL: " + url);
            return barrel.searchUrl(url);
        } catch (RemoteException | NotBoundException re) {
            System.out.println("Exception in SearchModule search2: " + re);
            return null;
        }
    }
}
