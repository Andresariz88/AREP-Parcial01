package edu.eci.arep;

public class HttpResponse {
    String header;
    String body;

    HttpResponse(String body) {
        header = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n";
        setBody(body);
    }

    public void setBody(String body) {
        this.body = "<html>\n"
                + "<head>\n"
                + "<meta charset=\"UTF-8\">\n"
                + "<title>Title of the document</title>\n"
                + "</head>\n"
                + "<body>\n"
                + "<p>\n"
                + body
                + "</p>\n"
                + "</body>\n"
                + "</html>\n";

    }

    public String getResponse() {
        return header + body;
    }
}
