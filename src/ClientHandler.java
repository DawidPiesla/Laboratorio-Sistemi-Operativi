import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

/**
 * ClientHandler implementa il gestore della connessione con un client
 */
public class ClientHandler implements Runnable {


    private Socket s; //Gestisce interamente il proprio socket (i.e. la propria connessione)
    private static ArrayList<FileBox> files;
    private Scanner fromClient;
    private PrintWriter toClient;
    private static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private String absolutPathToFile = "/Users/eugenio/Università/Secondo anno/Secondo semestre/Sistemi operativi/progetto2022/data/";
    private String userName = "";
    private static ArrayList<String> UserConnectedList = new ArrayList<>();
    private int randomNumbers = 0;


    public String getUserName() {
        return userName;
    }

    public ClientHandler(Socket s, ArrayList<FileBox> files, String userName) {
        this.s = s;
        this.files = files;
        this.userName = userName;
        clientHandlers.add(this);
    }

    public static void broadcastQuit() throws IOException {
        for (ClientHandler ch : clientHandlers) {
            ch.toClient.println("quit");
        }

    }

    public boolean fileExist(String fileName) {
        //confronta solo il nome senza txt
        boolean exist = false;
        for (FileBox f : files) {
            if (f.getFileName().equals(fileName)) {
                exist = true;
            }
        }
        return exist;
    }

    public int getFilePos(String fileName) {
        int pos = 0;
        for (FileBox f : files) {
            if (f.getFileName().equals(fileName)) {
                pos = files.indexOf(f);
            }
        }
        return pos;
    }

    public boolean userNameExist(String usName) {
        boolean check = false;

        for (String us : UserConnectedList) {
            if (us.equals(usName)) {
                check = true;
            }
        }
        return check;
    }

    public void setUserName(String userName) {
        if (!userNameExist(userName)) {
            this.userName = userName;
        } else {
            randomNumbers = (int) (Math.random() * 1000);
            this.userName = userName + randomNumbers;
        }
    }


    @Override
    public void run() {
        try {
            fromClient = new Scanner(s.getInputStream()); //Canale di ricezione dal client (in)
            toClient = new PrintWriter(s.getOutputStream(), true); //Canale di invio verso il client (out)
            //Ciclo di vita del ClientHandler
            boolean userNameEmpty = true;
            while (true) {
                if (userNameEmpty) {
                    toClient.println("SETUSERNAME");
                    userName = fromClient.nextLine();
                    boolean check = false;
                    if (userNameExist(userName)) {
                        check = true;
                    }
                    setUserName(userName);
                    if (!check) {
                        toClient.println("UserName: [" + userName + "]");
                    } else {
                        toClient.println("UserName already exist. Your userName will be: [" + userName + "]");

                    }
                    System.out.println("New client connected with username:  [" + getUserName()+"]");
                    UserConnectedList.add(getUserName());
                    userNameEmpty = false;
                    toClient.println("USERNAMEOK");
                } else {
                    String request = fromClient.nextLine(); //Leggi la richiesta del client,...
                    String[] splitRequest = request.split(" ", 3);
                    String requestType = splitRequest[0];
                    String requestArgument1 = splitRequest.length > 1 ? splitRequest[1] : "";
                    String requestArgument2 = splitRequest.length > 2 ? splitRequest[2] : "";
                    if (requestArgument2.equals("")) {
                        if (requestArgument1.equals("")) {
                            System.out.println("CLIENT: [" + this.getUserName() + "] REQUEST: [" + request + "]");
                        } else {
                            System.out.println("CLIENT: [" + this.getUserName() + "] REQUEST: [" + request + "] ARGUMENT1: [" + requestArgument1+"]");
                        }

                    } else {
                        System.out.println("CLIENT: [" + this.getUserName() + "] REQUEST: [" + request + "] ARGUMENT1: [" + requestArgument1 + "] ARGUMENT2: [" + requestArgument2+"]");
                    }

                    if (requestType.equals("quit")) {
                        UserConnectedList.remove(userName);
                        //Se il client ha terminato, interrompi il ciclo
                        break;
                    } else if (requestType.equals("list")) {
                        int index = 0;

                        for (int i = 0; i < files.size(); i++) {
                            index++;
                            toClient.println("- " + index + " FILE NAME: " + files.get(i).getFileName() + " || FILE INFO: " + files.get(i).getLastModified());
                        }
                        toClient.println("ENDLIST");
                    } else if (requestType.equals("create")) {
                        if (fileExist(requestArgument1)) {
                            toClient.println("Cannot create file. Name [" + requestArgument1 + "] is already used");
                        } else {
                            if (!requestArgument1.equals("")) {
                                FileBox fileToCreate = new FileBox(requestArgument1);
                                files.add(fileToCreate);
                                toClient.println("File named: [" + requestArgument1 + "] created successfully.");
                            } else {
                                toClient.println("Cannot create file. Name not insered.");

                            }
                        }
                    } else if (requestType.equals("delete")) {
                        if (fileExist(requestArgument1)) {
                            FileBox fileToDelete = files.get(getFilePos(requestArgument1));
                            files.remove(fileToDelete);
                            fileToDelete.getFile().delete();
                            toClient.println("File named: [" + requestArgument1 + "] deleted.");
                        } else {
                            toClient.println("Cannot delete not existing file");
                        }
                    } else if (requestType.equals("read")) {
                        if (fileExist(requestArgument1)) {
                            BufferedReader br = new BufferedReader(new FileReader(absolutPathToFile + requestArgument1 + ".txt"));
                            String line;
                            toClient.println("READING: [" + requestArgument1 + "]");
                            while ((line = br.readLine()) != null) {
                                toClient.println(line);
                            }
                            toClient.println("WAITFORCLOSECOMMAND");
                            boolean readModeEnable = true;
                            while (readModeEnable) {
                                String closeRequest = fromClient.nextLine();
                                if (closeRequest.equals(":close")) {
                                    toClient.println("ENDREAD");
                                    readModeEnable = false;
                                } else {
                                    toClient.println("Unknown command");
                                    toClient.println("WAITFORCLOSECOMMAND");
                                }
                            }

                        } else {
                            toClient.println("Cannot find the file [" + requestArgument1 + "] to read. Try using another filename.");
                            toClient.println("ENDREAD");
                        }

                    } else if (requestType.equals("rename")) {
                        if (fileExist(requestArgument1)) {
                            if (!requestArgument2.equals("") && !fileExist(requestArgument2)) {
                                Path source = Paths.get(String.valueOf(files.get(getFilePos(requestArgument1)).getFile()));
                                files.get(getFilePos(requestArgument1)).setFileName(requestArgument2);
                                String renamePath = requestArgument2 + ".txt";
                                files.get(getFilePos(requestArgument1)).setFile(source.resolveSibling(renamePath).toFile());
                                Files.move(source, source.resolveSibling(renamePath));
                                toClient.println("File named: [" + requestArgument1 + "] successfully renamed to: [" + requestArgument2 + "]");
                                toClient.println("ENDRENAME");
                            } else {
                                toClient.println("Cannot rename file. Name already used or not compatible.");
                                toClient.println("ENDRENAME");
                            }
                        } else {
                            toClient.println("Cannot rename non existing file.");
                            toClient.println("ENDRENAME");
                        }
                    } else if (requestType.equals("edit")) {
                        if (fileExist(requestArgument1)) {
                            File fileToEdit = new File(String.valueOf(files.get(getFilePos(requestArgument1)).getFile()));
                            FileBox fileToEditFB = files.get(getFilePos(requestArgument1));
                            toClient.println("EDITING: [" + requestArgument1 + "]");

                            toClient.println("GOEDITMODE");
                            boolean editModeEnable = true;
                            while (editModeEnable) {
                                String editRequest = fromClient.nextLine();
                                if (!editRequest.equals("")) {
                                    if (editRequest.charAt(0) == ':') {
                                        if (editRequest.equals(":backspace")) {
                                            if (fileToEdit.length() == 0) {
                                                System.out.println("CLIENT: [" + this.getUserName() + "] USED: [" + editRequest + "] BUT NO LINE WAS REMOVED FROM FILE: [" +
                                                        requestArgument1+"]");
                                                toClient.println("Cannot remove line from empty file.");
                                                toClient.println("GOEDITMODE");
                                            } else {
                                                String lineRemoved = fileToEditFB.getRemovedLine();
                                                fileToEditFB.removeLastLine();
                                                System.out.println("CLIENT: [" + this.getUserName() + "] USED: [" + editRequest + "] AND REMOVED LINE: ["+lineRemoved+"] FROM FILE: [" +
                                                        requestArgument1+"]");
                                                toClient.println("Line: [" + lineRemoved + "] removed with success.");
                                                toClient.println("GOEDITMODE");
                                            }
                                        } else if (editRequest.equals(":close")) {
                                            System.out.println("CLIENT: [" + this.getUserName() + "] USED: [" + editRequest + "] TO CLOSE EDITING SESSION ON FILE:"+ requestArgument1);
                                            toClient.println("ENDEDIT");
                                            editModeEnable = false;
                                        } else {
                                            toClient.println("Unknown command");
                                            toClient.println("GOEDITMODE");
                                        }
                                    } else {
                                        fileToEditFB.appendLine(editRequest);
                                        System.out.println("CLIENT: [" + this.getUserName() + "] ADDED LINE: [" + editRequest + "] IN FILE: [" +
                                                requestArgument1+"]");
                                        toClient.println("LINE: [" + editRequest + "] added.");
                                        toClient.println("GOEDITMODE");
                                    }
                                } else {
                                    toClient.println("GOEDITMODE");
                                }
                            }
                        } else {
                            toClient.println("Cannot edit non existing file.");
                            toClient.println("ENDEDIT");
                        }
                    } else {
                        //Se il comando è sconosciuto, informa il client
                        toClient.println("Unknown command");
                    }
                }
            }
            //Chiudi la connessione e arresta il ClientHandler
            s.close();
            Client.closeClient();
            UserConnectedList.remove(userName);
            System.out.println("Client [" + this.getUserName() + "] terminated");

        } catch (IOException e) {
            UserConnectedList.remove(userName);
            System.err.println("Error during I/O operation in CH:");
            e.printStackTrace();
        } catch (NoSuchElementException ex) {
            System.err.println("Client: " + userName + " suddenly disconnected its input.");
            UserConnectedList.remove(userName);
            ex.printStackTrace();
        }
    }

}

