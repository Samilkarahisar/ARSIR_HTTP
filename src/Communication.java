import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Communication implements Runnable {

    private Socket connexion;

    public Communication(Socket connexion) {
        this.connexion = connexion;
    }

    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();
            InputStream in = connexion.getInputStream();
            PrintWriter out = new PrintWriter(connexion.getOutputStream());

            while (connexion.isConnected()) {

                byte[] query = new byte[1];
                Thread.sleep(30);

                // we got something!
                byte[] temp = new byte[1];
                in.readNBytes(temp, 0,1);
                try {
                    if (temp[0] != 0) {
                        boolean stop = false;
                        boolean tempStop = false;
                        while (!stop) {

                            if (temp[0] == 10 && tempStop)
                                stop = true;

                            else {
                                if (temp[0] == 10)
                                    tempStop = true;
                                else if (temp[0] != 13)
                                    tempStop = false;

                                byte[] tempQuery = new byte[query.length + 1];
                                System.arraycopy(query, 0, tempQuery, 0, query.length);
                                tempQuery[query.length] = temp[0];
                                query = tempQuery;

                                in.readNBytes(temp, 0, 1);
                            }
                        }

                        String queryString = new String(query);
                        if (queryString.contains("GET") || queryString.contains("PUT")) {

                            // if it's a get
                            if (queryString.contains("GET")) {
                                System.out.print("GET reçu : ");

                                String fileName = queryString.substring("GET ".length() + 1, queryString.indexOf("HTTP/1.1") - 1);
                                if(fileName.equals("/")) fileName = "index.html";

                                // read file
                                try {
                                    FileInputStream inFile = new FileInputStream(Server.workspace + "/" + fileName);

                                    // send data
                                    byte[] data = inFile.readAllBytes();
                                    inFile.close();

                                    out.println("HTTP/1.1 200 OK");
                                    out.println("Content-Length: " + data.length);
                                    out.println("Content-Type: " + Files.probeContentType(Paths.get(Server.workspace + "/" + fileName)));
                                    out.println();
                                    out.flush();

                                    connexion.getOutputStream().write(data);
                                    connexion.getOutputStream().flush();

                                    System.out.println("200");
                                }

                                // not found
                                catch (FileNotFoundException e) {

                                    // send 404
                                    out.println("HTTP/1.1 404 Not Found");
                                    out.println("Content-Length: 0");
                                    out.println();
                                    out.flush();

                                    System.out.println("404");
                                }
                            }

                            // if it's a PUT
                            else {
                                System.out.print("PUT reçu : ");

                                String fileName = queryString.substring("GET ".length() + 1, queryString.indexOf("HTTP/1.1") - 1);
                                String code;
                                File f = new File(Server.workspace + "/" + fileName);
                                if(f.exists() && !f.isDirectory()) code = "HTTP/1.1 204 No Content";
                                else code = "HTTP/1.1 201 Created";

                                try {
                                    FileOutputStream fileOut = new FileOutputStream(Server.workspace + "/" + fileName);

                                    // check length
                                    String lenghtString = "";
                                    for (int i = queryString.toLowerCase().indexOf("content-length: ") + "Content-Length: ".length(); Character.isDigit(queryString.charAt(i)); i++)
                                        lenghtString += queryString.charAt(i);
                                    int contentLength = Integer.valueOf(lenghtString);

                                    // copy data into file
                                    byte[] content = new byte[1];
                                    content = in.readAllBytes();
                                    fileOut.write(content);

                                    fileOut.close();

                                    out.println(code);
                                    out.println("Content-Location: " + fileName);
                                    out.println();
                                    out.flush();

                                    System.out.println(contentLength + " octets ok");
                                }

                                catch (FileNotFoundException e) {
                                    // send 500
                                    out.println("HTTP/1.1 500 Internal Server Error");
                                    out.println("Content-Length: 0");
                                    out.println();
                                    out.flush();

                                    System.out.println("500");
                                }
                            }

                            if(queryString.toLowerCase().contains("connection: close"))
                                this.connexion.close();
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) { }

                if(System.currentTimeMillis() - start > (long) 600000) // 10 minutes
                    this.connexion.close();
            }

        }
        catch (SocketException e) { }
        catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
