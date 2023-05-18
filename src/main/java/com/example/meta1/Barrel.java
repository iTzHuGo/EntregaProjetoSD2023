package com.example.meta1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Barrel implements BarrelInterface, Serializable {
    public Barrel() throws RemoteException {
        super();
    }

    static HashMap<String, HashMap<String, String>> info = new HashMap<>();
    static HashMap<String, HashSet<String>> wordUrls = new HashMap<>();
    static HashMap<String, HashSet<String>> urlUrls = new HashMap<>();

    public String MULTICAST_ADDRESS = "224.3.2.1";
    public int PORT = 5250;

    public static void main(String[] args) {
        try {
            Barrel barrel = new Barrel();
            LocateRegistry.createRegistry(5100).rebind("Barrel", barrel);

            System.out.println("Barrel Started");

            File fich = new File("barrel.txt");
            fich.createNewFile();

            System.out.println("Barrel Loaded");

            ExecutorService executorBarrels = Executors.newFixedThreadPool(1);
            executorBarrels.execute(new BarrelThread());
            executorBarrels.shutdown();
            try {
                executorBarrels.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            System.out.println("Exception in Barrel (Main): " + e);
        }
    }

    public String searchWord(String term) throws RemoteException {
        String msg = "";
        try {
            term += " ";
            String[] word = term.split(" ");
            HashMap<String, HashSet<String>> words = new HashMap<>();
            words = (HashMap<String, HashSet<String>>) readObject("word.obj");

            HashMap<String, HashMap<String, String>> info = new HashMap<>();
            info = (HashMap<String, HashMap<String, String>>) readObject("info.obj");

            HashMap<String, HashSet<String>> u = new HashMap<>();
            u = (HashMap<String, HashSet<String>>) readObject("urls.obj");

            List<ArrayList<Object>> notOrder = new ArrayList<>();

            for (int i = 0; i < word.length; i++) {
                if (words.containsKey(word[i])) {
                    HashSet<String> urls = words.get(word[i]);
                    for (String url : urls) {
                        HashMap<String, String> infoUrl = info.get(url);
                        HashSet<String> urlsPoint = u.get(url);
                        int count = 0;
                        if (urlsPoint != null)
                            count += urlsPoint.size();
                        String title = infoUrl.get("title");
                        String quote = infoUrl.get("quote");
                        String aux = title + "\n" + url + "\n" + quote + "\n\n";
                        notOrder.add(new ArrayList<Object>(List.of(aux, count)));
                    }

                    List<ArrayList<Object>> inOrder = notOrder.stream().sorted((lista1, lista2) -> ((Integer) lista2.get(1)).compareTo((Integer) lista1.get(1))).collect(Collectors.toList());
                    
                    for (ArrayList<Object> list : inOrder) {
                        msg += list.get(0);
                    }
                } else {
                    msg = "Nenhum resultado encontrado";
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in Barrel (searchWord): " + e);
        }
        return msg;
    }

    public String searchUrl(String url) throws RemoteException {
        String msg = "";
        try {
            HashMap<String, HashSet<String>> urls = new HashMap<>();
            urls = (HashMap<String, HashSet<String>>) readObject("urls.obj");

            HashSet<String> point = urls.get(url);
            if (point != null) {
                for (String u : point) {
                    msg += u + "\n";
                }
                msg += "\n";
            } else {
                msg += "Sem URLS associados.\n\n";
            }
        }  catch (Exception e) {
            System.out.println("Exception in Barrel (searchWord): " + e);
        }
        return msg;
    }

    public Object readObject(String filename) {
        Object obj = null;
        try {
            File fich = new File(filename);
            try (FileInputStream fis = new FileInputStream(fich);
                    ObjectInputStream ois = new ObjectInputStream(fis)) {
                obj = ois.readObject();
            } catch (FileNotFoundException e) {
                System.out.println("Erro de abertura do ficheiro.");
            } catch (IOException e) {
                System.out.println("Erro a ler ficheiro.");
            } catch (ClassNotFoundException e) {
                System.out.println("Erro a converter para objeto.");
            }
        } catch (Exception e) {
            System.out.println("Exception in Barrel (readObject): " + e);
        }
        return obj;
    }

    private static class BarrelThread implements Runnable {
        public String MULTICAST_ADDRESS = "224.3.2.1";
        public int PORT = 5250;
        File fich;
        FileWriter fw;

        public BarrelThread() {
        }

        public void run() {
            while (true) {
                System.out.println("Waiting for multicast message...");
                receiveMulticast();
            }
        }

        public void receiveMulticast() {
            MulticastSocket socket = null;
            String msg = "";
            try {
                SearchModuleInterface searchModuleInterface = (SearchModuleInterface) LocateRegistry.getRegistry(5000).lookup("SearchModule");
                socket = new MulticastSocket(5250);
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                socket.joinGroup(group);

                byte[] buffer = new byte[999999999];

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":"
                        + packet.getPort() + " with message:");
                String received = new String(packet.getData(), 0, packet.getLength());
                // System.out.println("received: " + received);
                String[] data = received.split("\\|\\|\\|");

                info.put(data[0], new HashMap<String, String>());
                info.get(data[0]).put("title", data[1]);
                info.get(data[0]).put("quote", data[2]);

                // searchModuleInterface.status("mensagem " + data[1]);
                // msg += "Título: " + data[1] + " e citaçãoo: "  + data[2] + "adicionados ao URL: " + data[0] + "\n";

                String[] words = data[3].split("[ ,:.?!\"<>«»(){}/#_'|]");

                for (int i = 0; i < words.length; i++) {
                    if (words[i].equals(""))
                        continue;
                    if (wordUrls.containsKey(words[i])) {
                        wordUrls.get(words[i]).add(data[0]);
                    } else {
                        HashSet<String> urls = new HashSet<>();
                        urls.add(data[0]);
                        wordUrls.put(words[i], urls);
                    }
                    // msg += "URL: " + data[0] + " adicionado a palavra: " + words[i] + "\n";
                }
                if (data.length == 5) {
                    if (!data[4].isEmpty()) {
                        String[] links = data[4].split(" ");

                        for (int i = 0; i < links.length; i++) {
                            if (urlUrls.containsKey(links[i])) {
                                urlUrls.get(links[i]).add(data[0]);
                            } else {
                                HashSet<String> urls = new HashSet<>();
                                urls.add(data[0]);
                                urlUrls.put(links[i], urls);
                            }
                            msg += "URL: " + data[0] + " adicionado ao URL: " + links[i] + "\n";
                        }
                    }
                }

                File fichInfo = new File("info.obj");
                File fichWord = new File("word.obj");
                File fichUrls = new File("urls.obj");
                try (FileOutputStream fos = new FileOutputStream(fichInfo);
                        ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(info);
                } catch (FileNotFoundException e) {
                    System.out.println("Erro a criar ficheiro info");
                } catch (IOException e) {
                    System.out.println("Erro a escrever no ficheiro info.");
                }

                try (FileOutputStream fos = new FileOutputStream(fichWord);
                        ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(wordUrls);
                } catch (FileNotFoundException e) {
                    System.out.println("Erro a criar ficheiro word");
                } catch (IOException e) {
                    System.out.println("Erro a escrever no ficheiro word.");
                }

                try (FileOutputStream fos = new FileOutputStream(fichUrls);
                        ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(urlUrls);
                } catch (FileNotFoundException e) {
                    System.out.println("Erro a criar ficheiro urls");
                } catch (IOException e) {
                    System.out.println("Erro a escrever no ficheiro urls.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Exception in Barrel (receiveMulticast) : " + e);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NotBoundException e1) {
                e1.printStackTrace();
            } finally {
                socket.close();
            }
        }
    }
}
