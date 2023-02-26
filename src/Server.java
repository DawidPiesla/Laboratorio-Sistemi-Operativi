import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


public class Server {

    private static ArrayList<FileBox> files;
    private static ServerSocket listener;
    private static Socket s;

    public static void main(String args[]) {
        if (args.length < 1) {
            System.err.println("Usage: java Server <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        //carico i file su arraylist files contenente tutti i file.txt
        loadFiles();
        try {
            //creo listener per connessione con client
            listener = new ServerSocket(port);
            //Avvio thread per i comandi del server
            Thread serverCommandThread = new Thread(new ServerCommands());
            serverCommandThread.start();

            //Ciclo di vita del thread principale del server
            while (true) {
                System.out.println("Listening...");
                s = listener.accept(); //Connettiti a un client
                System.out.println("Connected");
                //Delega la gestione della nuova connessione a un thread ClientHandler dedicato
                ClientHandler ch = new ClientHandler(s, files, "userName");
                Thread clientHandlerThread = new Thread(ch);
                clientHandlerThread.start();

                // E rimettiti in ascolto
            }
        } catch (IOException e) {
            System.err.println("Error in I/O.");

        }
    }

    public static ArrayList<FileBox> loadFiles() {
        File folder = new File("/Users/eugenio/Università/Secondo anno/Secondo semestre/Sistemi operativi/progetto2022/data");
        files = new ArrayList<>();
        for (File f : folder.listFiles()) {
            if (!f.isHidden()) {
                try {
                    // System.out.println("filename: " + f.getName());
                    String fileNameToLoad = (f.getName()).replace(".txt", "");
                    FileBox fileToLoad = new FileBox(fileNameToLoad, true);
                    files.add(fileToLoad);
                } catch (IOException e) {
                    System.err.println("Unable to fetch file name");
                    e.printStackTrace();
                }
            }
        }
        return files;
    }

    public static void showInfo() {
        System.out.println("Number of files on the server: " + files.size());
    }


    public static void closeServer() throws IOException {
        s.shutdownInput();

        Client.closeClient();
        ClientHandler.broadcastQuit();
        if (listener != null) {
            try {
               // s.shutdownOutput();
                listener.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}

