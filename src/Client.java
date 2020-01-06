import java.io.*;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Client {

    private Socket socket;
    private BufferedInputStream input;
    private PrintWriter out;
    private String url = "";
    private int port;

    private static boolean autoflush = true;

    public boolean connect(String url, int port)
    {
        try {
            this.socket = new Socket(Inet4Address.getByName(url).getHostAddress(), port);
            this.url = url;
            this.port = port;
            input = new BufferedInputStream(this.socket.getInputStream());
            out = new PrintWriter(this.socket.getOutputStream(), autoflush);
        }
        catch (SocketException e) {
            return false;
        }
        catch (IOException e) {
            return false;
        }

        return true;
    }

    public int getPage(String directory, String pageName) {
        try {
            if(!this.socket.isConnected())
                if(!connect(this.url, this.port))
                    return 1; // connection with server error

            if(pageName.charAt(0) != '/') {
                pageName = "\\" + pageName;
            }

            out.println("GET " + pageName + " HTTP/1.1");
            out.println("Host: " + url);
            out.println();

            byte[] temp = new byte[100];
            byte[] resultByte = new byte[1];
            boolean stop = false;
            boolean tempStop = false;
            while (!stop) {
                input.readNBytes(temp, 0, 1);

                if (temp[0] == 10 && tempStop)
                    stop = true;

                else {
                    if (temp[0] == 10)
                        tempStop = true;
                    else if (temp[0] != 13)
                        tempStop = false;

                    byte[] tempQuery = new byte[resultByte.length + 1];
                    System.arraycopy(resultByte, 0, tempQuery, 0, resultByte.length);
                    tempQuery[resultByte.length] = temp[0];
                    resultByte = tempQuery;
                }
            }
            String result = new String(resultByte);

            // check code value
            int code = Integer.valueOf(result.substring("HTTP/1.1 ".length() + 1, "HTTP/1.1 ".length() + 4));
            if(code == 404) return 4;   // 404
            if(code != 200) return code;    // return code if not 200

            // check for head status code ok (200)
            //System.out.println(result);
            if(!result.toLowerCase().contains("content-length:"))
                return 2;   // not valid answer

            // check length
            String lenghtString = "";
            for (int i = result.toLowerCase().indexOf("content-length: ") + "Content-Length: ".length(); Character.isDigit(result.charAt(i)); i++)
            {
                lenghtString += result.charAt(i);
            }

            int contentLength = Integer.valueOf(lenghtString);

            System.out.println(contentLength);
            if(contentLength == 0)
                return 3;   // no data

            // check type
            if(!result.toLowerCase().contains("content-type:"))
                return 2;   // not valid answer

            // save in file
            String fileName = pageName.substring(pageName.lastIndexOf("/") + 1);
            byte[] data = new byte[contentLength];
            input.readNBytes(data, 0, contentLength);

            try {
                FileOutputStream fileOut = new FileOutputStream(directory + fileName);
                fileOut.write(data);
                fileOut.close();

                return 0;

            } catch (IOException e) {
                return 5;   // pb with file
            }

        } catch (IOException e) {
            // connection with server error
            return 1;
        }
    }

    public int putFile(String filePath, String filePathInServeur) {

        // read file
        try {
            FileInputStream fileIn = new FileInputStream(filePath);
            byte[] result = fileIn.readAllBytes();
            fileIn.close();

            // send the file to the server
            if(!socket.isConnected())
                if(!connect(this.url, this.port))
                    return 1; // connection with server error

            try {
                // query HEAD
                out.println("PUT " + filePathInServeur + " HTTP/1.1");
                out.println("Host: " + url);
                out.println("Content-Type: " + Files.probeContentType(Paths.get(filePath)));
                out.println("Content-Length: " + result.length);
                out.println();

                // content
                this.socket.getOutputStream().write(result);
                this.socket.getOutputStream().flush();

                // get answer
                byte[] temp = new byte[100];
                byte[] resultByte = new byte[1];
                boolean stop = false;
                boolean tempStop = false;
                while (!stop) {
                    input.read(temp, 0,1);

                    if (temp[0] == 10 && tempStop)
                        stop = true;

                    else {
                        if (temp[0] == 10)
                            tempStop = true;
                        else if (temp[0] != 13)
                            tempStop = false;

                        byte[] tempQuery = new byte[resultByte.length + 1];
                        System.arraycopy(resultByte, 0, tempQuery, 0, resultByte.length);
                        tempQuery[resultByte.length] = temp[0];
                        resultByte = tempQuery;
                    }
                }

                // check code value
                int code = Integer.valueOf(new String(resultByte).substring("HTTP/1.1 ".length() + 1, "HTTP/1.1 ".length() + 4));
                if (code == 201 || code == 204 || code == 200)
                    return 0;

                return code;

            } catch (IOException e) {
                return 1;   // connexion with the server
            }

        } catch (IOException e) {
            return 5;   // pb with file
        }
    }
}