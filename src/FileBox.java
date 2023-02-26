import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileBox {


    private File file; //file da tenere su disco
    private String absolutPathToFile = "/Users/eugenio/Università/Secondo anno/Secondo semestre/Sistemi operativi/progetto2022/data";
    private String fileName;

    public FileBox(String fileName) throws IOException {
        this.fileName = fileName;
        this.file = new File(absolutPathToFile + "/" + fileName + ".txt");
    }

    //altro costruttore per il metodo loadFiles
    public FileBox(String fileName, boolean loader) throws IOException {
        if (loader) {
            this.fileName = fileName;
            this.file = new File(absolutPathToFile + "/" + fileName + ".txt");
        }
    }

    public File getFile() {
        return this.file;
    }

    public String getLastModified() {
        String fileName1 = absolutPathToFile + "/" + this.fileName + ".txt";
        String filesAttr = "";
        try {
            Path file = Paths.get(fileName1);
            BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
            FileTime creationTime = attr.creationTime();
            FileTime lastAccessTime = attr.lastAccessTime();
            FileTime lastModifiedTime = attr.lastModifiedTime();
            DateFormat dfCT = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
            String dfCreationTime = dfCT.format(creationTime.toMillis());
            String dfLastAccessTime = dfCT.format(lastAccessTime.toMillis());
            String dfLastModifiedTime = dfCT.format(lastModifiedTime.toMillis());

            filesAttr = " creationTime: " + dfCreationTime + " " +
                    " lastAccessTime: " + dfLastAccessTime + " " + " lastModifiedTime: " + dfLastModifiedTime;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return filesAttr;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public void appendLine(String editRequest) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(this.getFile(), "rw");
        long fileLength = this.getFile().length();
        raf.seek(fileLength);
        raf.writeBytes(editRequest + "\n");
        raf.close();
    }

    public String removeLastLine() throws IOException {
        String filePath = absolutPathToFile + "/" + this.fileName + ".txt";
        RandomAccessFile f = new RandomAccessFile(filePath, "rw");
        long length = f.length() - 1;
        byte b;
        if (!filePath.isEmpty()) {
            do {
                length -= 1;
                f.seek(length);
                b = f.readByte();
            } while (b != 10 && length > 0);
            if (length == 0) {
                f.setLength(length);
            } else {
                f.setLength(length + 1);
            }
            return "Line successfully removed";
        } else {
            return "Cannot remove line from empty file";
        }
    }

    public String getRemovedLine() throws IOException {
        String fileName1 = absolutPathToFile + "/" + this.fileName + ".txt";
        String lineRemoved = "";
        if (this.getFile().length() == 0) {
            return lineRemoved;
        } else {
            File f = new File(fileName1);
            String nextLine = "";
            BufferedReader reader = new BufferedReader(new FileReader(f));
            while ((nextLine = reader.readLine()) != null) {
                lineRemoved = nextLine;
            }
        }
        return lineRemoved;
    }

    public void setFile(File file) {
        this.file = file;
    }

}

