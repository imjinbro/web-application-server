package http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webserver.RequestHandler;

public class HttpConnector {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private Socket connection;
    
    private InputStream input;
    private OutputStream output;
    
    public HttpConnector(Socket connection) {
        setConnection(connection);        
    }
    
    private void setConnection(Socket connection){       
        this.connection = connection;
        
        try {
            setInputStream();
            setOutputStream();
        } catch(IOException e) {
            log.error(e.getMessage());
        }
    }
    
    public void disconnect(){
        try {
            getInput().close();
            getOutput().close();
            getConnection().close();
        }catch(IOException e) {
            log.error(e.getMessage());
        }        
    }
       
    private void setInputStream() throws IOException {
        input = getConnection().getInputStream();
    }
    
    private void setOutputStream() throws IOException {
        output = getConnection().getOutputStream();
    }
    
    private Socket getConnection() throws IOException {
        return connection;
    }
    
    
    public String getConnInfo(){
        return "New Client Connect! Connected IP : {}, Port : {}" + connection.getInetAddress() + ", " + connection.getPort();
    }
    
    public InputStream getInput() {
        return input;
    }
    
    public OutputStream getOutput() {
        return output;
    }
   
}
