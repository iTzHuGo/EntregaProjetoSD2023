/*
* ======================================
* Ana Rita Martins Oliveira - 2020213684
* Hugo Sobral de Barros - 2020234332
* ======================================
*/

/*
* ====  PORTS  ====
* SearchModule  5000
* Barrels       5100
* Downloaders   5200
* URLQueue      5300
*/
package com.example.meta1;

import static java.lang.System.exit;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

public class Client implements Serializable, Remote{
    public static String option = "";
    public Client() throws RemoteException {
    }

    public static void main(String[] args) {
        try {
            Client client = new Client();
            LocateRegistry.createRegistry(4000).rebind("Client", client);


            Scanner scanner = new Scanner(System.in);
            SearchModuleInterface searchModuleInterface = (SearchModuleInterface) LocateRegistry.getRegistry(5000)
                    .lookup("SearchModule");

                    
            // Menu
            menuLogIn();
            option = scanner.nextLine();
            boolean login = false;
            
            switch (option) {
                case "1" -> {
                    // Log in
                    String username = "", password = "";
        
                    while (!searchModuleInterface.verifyLogin(username,password)) {
                        System.out.print("Insira o seu nome de utilizador: ");
                        username = scanner.nextLine();
                        System.out.print("Insira a sua palavra chave: ");
                        password = scanner.nextLine();
                    }
                    System.out.println("Sessão iniciada");
                    login = true;
                }
                case "2" -> {
                    System.out.println("Sem sessão iniciada");
                }
                default -> System.out.println("Opção inválida. Tente novamente.");
            }
            boolean exit = false;
            while (!exit) {
                menu();
                option = scanner.nextLine();

                switch (option) {
                    case "1" -> {
                        System.out.print("URL: ");
                        String url = scanner.nextLine();
                        System.out.println();
                        searchModuleInterface.addURL(url);
                    }
                    case "2" -> {
                        System.out.print("Termos: ");
                        String terms = scanner.nextLine();
                        String res = searchModuleInterface.search(terms);
                        int count = 0;
                        for (String s: res.split("\n\n")) {
                            if (count % 10 == 0 && count != 0) {
                                System.out.println("Prima ENTER para continuar...");
                                if (scanner.nextLine().equals("\n")) {
                                    count = 0;
                                    continue;
                                }
                            }
                            System.out.println(s + "\n");
                            count++;
                        }
                        System.out.println(res);
                    }
                    case "3" -> {
                        if (login) {
                            System.out.print("URL: ");
                            String url = scanner.nextLine();
                            String res = searchModuleInterface.search2(url);
                            System.out.println(res);
                        } else {
                            System.out.println("É necessário efetuar LogIn para aceder a esta funcionalidade.");
                        }
                    }
                    case "4" -> {
                        System.out.println("A abrir página de administração...");
                    }
                    case "5" -> {
                        System.out.println("Programa terminado\n");
                        exit = true;
                    }
                    default -> System.out.println("Opção inválida. Tente novamente.");
                }
            }
            scanner.close();
            exit(0);
        } catch (NotBoundException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private static void menu() {
        System.out.print("\nEscolha uma operação:\n" +
                "\t1. Indexar novo URL;\n" +
                "\t2. Pesquisar páginas que contenham um conjunto de termos ordenados por importância;\n" +
                "\t3. Consultar lista de páginas com ligação para uma página específica;\n" +
                "\t4. Página de administração atualizada em tempo real;\n" +
                "\t5. Sair.\n" +
                "\t>>> ");
    }

    private static void menuLogIn() {
        System.out.print("\nBem vindo ao Googol\nEscolha uma operação:\n" +
                "\t1. LogIn;\n" +
                "\t2. Continuar como anónimo;\n" +
                "\t>>> ");
    }
}