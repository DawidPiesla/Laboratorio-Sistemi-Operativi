import java.io.*;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client {


    private static Socket s;
    private static Scanner fromServer;
    private static PrintWriter toServer;

    public static BufferedReader getUserInput() {
        return userInput;
    }

    private static BufferedReader userInput;


    public static void main(String args[]) {
        //Prendi in input l'indirizzo e la porta a cui connettersi
        if (args.length < 2) {
            System.err.println("Usage: java Client <host> <port>");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        try {
            //Connettiti a host:port
            s = new Socket(host, port);
            System.out.println("Connected");
            fromServer = new Scanner(s.getInputStream());//Canale di ricezione dal server (in verita comunica con CH) (ricezione da ch)
            toServer = new PrintWriter(s.getOutputStream(), true); //Canale di invio verso il server (invio da ch)
            userInput = new BufferedReader(new InputStreamReader(System.in)); //Lettura dell'input da terminale
            boolean userSetup = true;

            //Ciclo di vita del client
            while (true) {
                String response = "init";

                if (userSetup) {
                    while (response != null) {
                        response = fromServer.nextLine();
                        if (response.equals("SETUSERNAME")) {
                            System.out.println("Enter username");
                            String userNameInput = userInput.readLine();
                            toServer.println(userNameInput);
                        } else if (response.equals("USERNAMEOK")) {
                            userSetup = false;
                            break;
                        } else if (response.equals("quit")) {
                            break;
                        } else {
                            System.out.println(response);
                        }
                    }
                } else {
                    System.out.println("WHILE TRUE OK");
                    String request="init";
                    request = userInput.readLine(); //Leggi la richiesta dell'utente...
                    System.out.println("USERINPUT OK: " + request);
                    toServer.println(request); //... e inoltrala al server
                    System.out.println("FORWARD TOSERVER OK: " + request);
                    String[] splitRequest = request.split(" ", 2);
                    String requestType = splitRequest[0];
                    System.out.println("COMMAND INPUT: " + requestType);
                    System.out.println("------------------------------------");

                    if (requestType.equals("list")) {
                        while (response != null) {
                            response = fromServer.nextLine();
                            if (response.equals("ENDLIST")) {
                                break;
                            } else {
                                System.out.println(response);
                            }
                        }
                    } else if (request.equals("quit")) {
                        //Se l'utente chiede di uscire, termina il ciclo
                        break;
                    } else if (requestType.equals("create")) {
                        response = fromServer.nextLine();
                        System.out.println(response);
                    } else if (requestType.equals("delete")) {
                        response = fromServer.nextLine();
                        System.out.println(response);
                    } else if (requestType.equals("read")) {
                        boolean readMode = true;
                        while (response != null) {
                            response = fromServer.nextLine();
                            if (response.equals("ENDREAD")) {
                                break;
                            } else {
                                if (response.equals("WAITFORCLOSECOMMAND")) {
                                    if (readMode) {
                                        System.out.println("':close' to close the reading op.  ");
                                        String closeRequest = userInput.readLine();
                                        toServer.println(closeRequest);
                                    }

                                } else {
                                    System.out.println(response);
                                }
                            }
                        }

                    } else if (requestType.equals("rename")) {
                        while (response != null) {
                            response = fromServer.nextLine();
                            if (response.equals("ENDRENAME")) {
                                break;
                            } else {
                                System.out.println(response);
                            }
                        }
                    } else if (requestType.equals("edit")) {
                        boolean editMode = true;
                        while (response != null) {
                            response = fromServer.nextLine();
                            if (response.equals("ENDEDIT")) {
                                break;
                            }
                            if (response.equals("GOEDITMODE")) {
                                if (editMode) {
                                    System.out.println("COMMANDS: ");
                                    System.out.println("':backspace' to remove last line.  ");
                                    System.out.println("':close' to close the editing op.  ");
                                    System.out.println("or add a line by writing smth ");
                                    String editRequest = userInput.readLine();
                                    toServer.println(editRequest);
                                }
                            } else {
                                System.out.println(response);
                            }
                        }
                    } else {
                        response = fromServer.nextLine();
                        if(response.equals("quit")){
                            break;
                        } else {
                            System.out.println(response); //... e stampala sul terminale
                        }
                    }
                }
            }

            //Prima di arrestare il client, chiudi la connessione e lo scanner
            s.close();
            userInput.close();
            fromServer.close();
            System.out.println("Client Closed");

        } catch (IOException e) {
            System.err.println("Error during an I/O operation:");
            e.printStackTrace();
            System.err.println("Make sure Server is started.");
        } catch (NoSuchElementException e) {
            System.err.println("Make sure Server is started.");
            e.printStackTrace();
        }
    }

    public static void closeClient() {
        try {
            //s.shutdownInput();
             //s.shutdownOutput();
            if (fromServer != null) {
                fromServer.close();
            }
            if (toServer != null) {
                toServer.close();
            }
            if (userInput != null) {
                userInput.close();
            }
            if (s != null) {
               // s.shutdownInput();
             //  s.shutdownOutput();
                System.out.println("SOCKET CLOSED!");
                s.close();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}