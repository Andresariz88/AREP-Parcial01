package edu.eci.arep;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.io.*;
import java.util.List;

public class HttpServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(36000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        boolean running = true;
        while (running) {

            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine;

            boolean firstLine = true;
            String request = "/";
            while ((inputLine = in.readLine()) != null) {
                if (firstLine) {
                    request = inputLine.split(" ")[1];
                    firstLine = false;
                }
                System.out.println("Recibí: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }

            outputLine = "";

            if (request.startsWith("/consulta?comando=")) {
                String command = request.replace("/consulta?comando=", "").replace("%20", "");
                try {
                    String[] params = command.split("\\(")[1].replace(")", "").split(",");
                    String body = "";
                    Class<?> clazz = Class.forName(params[0]);

                    for (String st : params) {
                        System.out.println(st);
                    }

                    if (command.toLowerCase().startsWith("class")) {
                        //System.out.println(clazz);
                        body += "Nombre de la clase: </br>" + clazz.getName() + "</br>";
                        body += "</br>Campos: </br>";
                        for (Field fi : clazz.getFields()) {
                            //System.out.println(fi);
                            body += fi + "</br>";
                        }
                        body += "</br>Métodos:</br>";
                        for (Method me : clazz.getMethods()) {
                            //System.out.println(me);
                            body += me + "</br>";
                        }
                        outputLine = new HttpResponse(body).getResponse();
                        System.out.println(outputLine);
                    }

                    else if (command.toLowerCase().startsWith("invoke")) {
                        Method method = clazz.getMethod(params[1]);
                        body = "Retorno del método: </br>" + method.invoke(null);
                        outputLine = new HttpResponse(body).getResponse();

                    }

                    else if (command.toLowerCase().startsWith("unaryinvoke")) {
                        Method method = clazz.getMethod(params[1]);
                        Class<?> paramType = Class.forName(params[2]);
                        String value = params[3];
                        System.out.println(method.invoke(value));

                        //Object<paramType> paramValue = params[3];
                    }

                    }  catch (ClassNotFoundException e) {
                    outputLine = new HttpResponse("No encontré la clase :(").getResponse();
                } catch (NoSuchMethodException e) {
                    outputLine = new HttpResponse("No encontré ese método :(").getResponse();
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else {
                outputLine = index();
            }


            out.println(outputLine);

            out.close();

            in.close();

            clientSocket.close();
        }
        serverSocket.close();
    }

    private static String index() {
        return  "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n" +
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <title>Form Example</title>\n" +
                "        <meta charset=\"UTF-8\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <h1>Form with GET</h1>\n" +
                "        <form action=\"/hello\">\n" +
                "            <label for=\"name\">Name:</label><br>\n" +
                "            <input type=\"text\" id=\"name\" name=\"name\" value=\"John\"><br><br>\n" +
                "            <input type=\"button\" value=\"Submit\" onclick=\"loadGetMsg()\">\n" +
                "        </form> \n" +
                "        <div id=\"getrespmsg\"></div>\n" +
                "\n" +
                "        <script>\n" +
                "            function loadGetMsg() {\n" +
                "                let nameVar = document.getElementById(\"name\").value;\n" +
                "                const xhttp = new XMLHttpRequest();\n" +
                "                xhttp.onload = function() {\n" +
                "                    document.getElementById(\"getrespmsg\").innerHTML =\n" +
                "                    this.responseText;\n" +
                "                }\n" +
                "                xhttp.open(\"GET\", \"/consulta?comando=\"+nameVar);\n" +
                "                xhttp.send();\n" +
                "            }\n" +
                "        </script>\n" +
                "\n" +
                "        \n" +
                "    </body>\n" +
                "</html>";
    }

}