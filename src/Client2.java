package com.polytech;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Client {


    public static void main(String[] args) {

        Scanner myObj = new Scanner(System.in);
        System.out.println("Veuillez entrer une adresse de connexion:");
        String serverAddr = myObj.nextLine();
        System.out.println("Veuillez entrer le port cible:");
        int port = myObj.nextInt();

        try (Socket socket = new Socket(serverAddr, port)) {

            InputStream input = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            System.out.println("Taper 1 pour envoyer 2 pour recevoir");
            int methodCode = myObj.nextInt();
            myObj.nextLine();

            switch (methodCode) {
                case 1:
                    System.out.println("Indiquez le fichier a envoyer");
                    String putFileName = myObj.nextLine();

                    out.write(("PUT " + "/" + putFileName + " HTTP/1.1\r\n").getBytes());
                    out.write("\r\n".getBytes());
                    byte[] data = readFile("./client_files/" + putFileName);
                    out.write(data);
                    out.flush();
                    break;
                case 2:
                    System.out.println("Indiquez le fichier a télécharger");
                    String getFileName = myObj.nextLine();

                    out.write(("GET " + "/" + getFileName + " HTTP/1.1\r\n").getBytes());
                    out.write("\r\n".getBytes());
                    out.flush();

                    int tempByte;
                    StringBuilder strBuilder = new StringBuilder();
                    while ((tempByte = input.read()) != 10) {
                        strBuilder.append((char) tempByte);
                    }

                    String header = strBuilder.toString().trim();
                    String[] tabHeader = header.split(" ");
                    String http = tabHeader[0];
                    String httpCode = tabHeader[1];
                    String httpMessage;
                    if (tabHeader.length > 3) {
                        httpMessage = tabHeader[2] + " " + tabHeader[3];
                    } else {
                        httpMessage = tabHeader[2];
                    }

                    if (httpCode.equals("200")) {
                        // On lit la ligne vide
                        input.read();
                        input.read();

                        FileOutputStream fos = new FileOutputStream("./client_files/" + getFileName);
                        writeFile(fos, input);
                        System.out.println("Fichier " + getFileName + " bien recu");
                    } else {
                        System.out.println("Erreur " + httpCode + " : " + httpMessage);
                    }
                    break;
                default:
                    System.out.println("Commande inconnue");
                    break;
            }

            out.close();
            input.close();
        } catch (UnknownHostException ex) {
            System.out.println("Le serveur est introuvable");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("La connexion est interrompue ");
        }
        
    }

    private static void writeFile(FileOutputStream file, InputStream input) {
        try {
            int tempChar;
            while ((tempChar = input.read()) != -1) {
                file.write(tempChar);
            }
        } catch (IOException e) {
            System.out.println("Erreur lors de l'écriture du fichier");
        }
    }

    private static byte[] readFile(String fileName) {
        String filePath = "./" + fileName;
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            return fileInputStream.readAllBytes();
        } catch (IOException e) {
            System.out.println("Fichier non trouvé");
            return new byte[]{};
        }
    }
}
