/**
 * Created by Gabriel on 16/06/2016.
 */
import java.net.*;
import java.io.*;
public class FileClient{
    public static void main (String [] args ) throws IOException {
        int filesize=6022386;
        long start = System.currentTimeMillis();
        int bytesRead;
        int current = 0;
        Socket sock = new Socket("127.0.0.1",13267);
        // recebendo o arquivo
        byte [] mybytearray  = new byte [filesize];
        InputStream is = sock.getInputStream();
        FileOutputStream fos = new FileOutputStream("c:/users/gabri/desktop/MAIS UM SO PRA BRINCAR.png");
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bytesRead = is.read(mybytearray,0,mybytearray.length);
        current = bytesRead;
        do {
            bytesRead =
                    is.read(mybytearray, current, (mybytearray.length-current));
            if(bytesRead >= 0) current += bytesRead;
        } while(bytesRead > -1);
        bos.write(mybytearray, 0 , current);
        long end = System.currentTimeMillis();
        System.out.println(end-start);
        bos.close();
        sock.close();
    }
}
