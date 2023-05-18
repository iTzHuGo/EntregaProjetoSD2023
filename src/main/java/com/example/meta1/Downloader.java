package com.example.meta1;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Downloader {
    private static int serverPort = 6500;
    public static Set<String> stopWords;
    public int NUMBER_THREADS = 3;
    public static Vector<String> urlList;

    public Downloader() {
        urlList = new Vector<String>();
        stopWords = new HashSet<>();
        stopWords.add("a");
        stopWords.add("as");
        stopWords.add("o");
        stopWords.add("os");
        stopWords.add("um");
        stopWords.add("uns");
        stopWords.add("uma");
        stopWords.add("umas");
        stopWords.add("e");
        stopWords.add("mas");
        stopWords.add("ou");
        stopWords.add("porque");
        stopWords.add("se");
        stopWords.add("que");
        stopWords.add("como");
        stopWords.add("com");
        stopWords.add("de");
        stopWords.add("em");
        stopWords.add("para");
        stopWords.add("por");
        stopWords.add("sobre");
        stopWords.add("este");
        stopWords.add("essa");
        stopWords.add("aquele");
        stopWords.add("aquela");
        stopWords.add("isto");
        stopWords.add("isso");
        stopWords.add("aquilo");
        stopWords.add("seu");
        stopWords.add("sua");
        stopWords.add("seus");
        stopWords.add("suas");
        stopWords.add("|");
    }

    public static void main(String args[]) {
        Downloader d = new Downloader();
        Vector<String> urlList = new Vector<String>();

        try (ServerSocket listenSocket = new ServerSocket(serverPort)) {
            System.out.println("A escuta no porto " + serverPort);
            System.out.println("LISTEN SOCKET=" + listenSocket);
            while (true) {
                Socket clientSocket = listenSocket.accept();
                System.out.println("CLIENT_SOCKET (created at accept())=" + clientSocket);
                new TCPConnection(clientSocket, urlList).start();
                ExecutorService executorDownloaders = Executors.newFixedThreadPool(d.NUMBER_THREADS);
                for (int i = 0; i < d.NUMBER_THREADS; i++) {
                    executorDownloaders.execute(new DownloaderThread(i, urlList));
                    System.out.println("Downloader " + i + " initialized");
                    Thread.sleep(50);
                }
                executorDownloaders.shutdown();
                try {
                    executorDownloaders.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Listen:" + e.getMessage());
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

    }

    private static class TCPConnection extends Thread {
        DataInputStream in;
        Vector<String> urlList;
        // DataOutputStream out;

        public TCPConnection(Socket aClientSocket, Vector<String> urlList) throws IOException {
            this.in = new DataInputStream(aClientSocket.getInputStream());
            this.urlList = urlList;
        }

        @Override
        public void run() {
            try {
                while (true) {

                    String data = "";
                    data = in.readUTF();
                    if (in.available() > 0) {
                        System.out.println("Received: " + data);
                    }
                    if (!data.isEmpty()) {
                        System.out.println("Recebeu: " + data);
                        urlList.add(data);

                        System.out.println("URL ADDED: " + data);
                        System.out.println("URL LIST: " + urlList);
                    }

                    /*
                     * String msg_aux = "";
                     * Set<Thread> threads = Thread.getAllStackTraces().keySet();
                     * for (Thread t : threads) {
                     * if (t.getName().contains("pool-1-thread-")) {
                     * msg_aux += t.getName() + " " + t.getState() + " " + t.getPriority() + " "
                     * + t.isDaemon() + "\n";
                     * }
                     * 
                     * }
                     * out.writeUTF(msg_aux);
                     */
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class DownloaderThread implements Runnable {
        public String MULTICAST_ADDRESS = "224.3.2.1";
        public int PORT = 5250;
        private String mens;
        private long SLEEP_TIME = 1000;
        private int id;
        private Vector<String> urlList;

        public DownloaderThread(int id, Vector<String> urlList) {
            this.id = id;
            this.urlList = urlList;
        }

        @Override
        public void run() {
            try {
                while (urlList.isEmpty()) {
                    // System.out.println("THREAD " + id + " SLEEPING");
                    Thread.sleep(SLEEP_TIME/2);
                }
                while (!urlList.isEmpty()) {
                    System.out.println(urlList);

                    for (var e : urlList) {
                        System.out.println(e);
                    }
                    // System.out.println("URL LIST: " + urlList);
                    crawl(id);
                    System.out.println("THREAD " + id + " SLEEPING");
                    Thread.sleep(SLEEP_TIME);
                }
            } catch (InterruptedException e) {
                System.out.println("HERE");
                e.printStackTrace();
            }
        }

        public void crawl(int id) {
            try {
                if (urlList.size() < id + 1)
                    return;
                mens = "";
                // System.out.println("MENSAGEM1: " + mens.length() + "\n");

                String url = urlList.get(id);

                if (!(url.startsWith("http://") || url.startsWith("https://"))) {
                    url = "http://".concat(url);
                }

                Document doc = Jsoup.connect(url).get();

                StringTokenizer tokens = new StringTokenizer(doc.text());

                int countTokens = 0;
                String text = "";
                String quote = "";
                while (tokens.hasMoreElements()) {
                    String token = tokens.nextToken().toLowerCase();
                    if (!(stopWords.contains(token))) {
                        text += token + " ";
                    }
                    if (countTokens < 10) {
                        quote += token + " ";
                        countTokens++;
                    }
                }

                Elements links = doc.select("a[href]");
                String l = "";
                for (Element link : links) {
                    l += link.attr("abs:href") + " ";
                    urlList.add(link.attr("abs:href"));
                }

                mens += url + "|||" + doc.title() + "|||" + quote + "|||" + text + "|||" + l;
                System.out.println(urlList.get(id) + " successfully downloaded");
                urlList.remove(id);
                // System.out.println("MENSAGEM2: " + mens.length() + "\n");

                sendMulticast(mens);
            } catch (HttpStatusException e) {
                urlList.remove(id);
            } catch (SocketTimeoutException e) {
                urlList.remove(id);
            } catch (UnknownHostException e) {
                urlList.remove(id);
            } catch (IOException e) {
                urlList.remove(id);
            } catch (IllegalArgumentException e) {
                urlList.remove(id);
            } catch (NullPointerException e) {
                urlList.remove(id);
            } catch (IndexOutOfBoundsException e) {
                urlList.remove(id);
            } catch (Exception e) {
                e.printStackTrace();
                urlList.remove(id);
            }
        }

        public void sendMulticast(String message) {
            MulticastSocket socket = null;
            try {
                socket = new MulticastSocket();
                byte[] buffer = message.getBytes();

                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                socket.close();
            }
        }
    }
}
