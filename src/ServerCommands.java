import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ServerCommands implements Runnable {
    Scanner fromConsole;
    @Override
    public void run() {
        fromConsole = new Scanner(System.in);
        while (true) {

            String command = "init";
            try {
                command = fromConsole.nextLine();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            if (command.equals("info")) {
                //in showinfo manca numclientlett numclientscritt
                Server.showInfo();
            } else if (command.equals("quit")) {
                System.err.println("Server closed.");
                try {
                    Server.closeServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fromConsole.close();
                break;
            } else {
                System.out.println("Unknown command");
            }

        }
    }


}
