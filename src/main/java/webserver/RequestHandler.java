package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
    [1단계 요구사항]
    - localhost/index.html 요청했을 때 webapp/index.html 파일로 응답해    
    - 읽는다 -> 읽은 요청을 찬찬히 살펴본다(결과를 받아와야해 결과는 어떤 형식이지? 응답하는걸 보자) -> 그에 맞게 응답해준다.    
 */

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private Socket connection;
       
    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());
        

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
                                         
            /*********** Request Process ***********/
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String requestLine = br.readLine(); // 요청라인(URI) : GET /index.html HTTP/1.1
            String[] requestLineTokens = requestLine.split(" ");                 
            
            //requestLine 확인해보기
            log.debug("==============================");
            log.debug("request line : {} ", requestLine);
            log.debug("==============================");
            
            
            if(isInvaildRequestLine(requestLine)) {
                log.debug("연결종료 : 잘못된 요청");
                disconnect(connection);
            }
            
            String requestMessage = null;
            while(!isEndOfRequest(requestMessage = br.readLine())) {
                log.debug("header : {} ", requestMessage);
            }

            
            /*********** Response Process ***********/    
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = Files.readAllBytes(new File("./webapp"+ requestLineTokens[1]).toPath());
            response200Header(dos, body.length);
            responseBody(dos, body);  
            
            
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void disconnect(Socket connection) {
        try {
            connection.getInputStream().close();
            connection.getOutputStream().close();
        } catch(IOException e) {
            log.error(e.getMessage());
        }        
    }
    
    private boolean isInvaildRequestLine(String requestLine) {
        return requestLine.isEmpty() || requestLine == null;
    }
    
    private boolean isEndOfRequest(String requestMessage) {
        return requestMessage.isEmpty();
    }
           

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
}

enum ResponeseState{
    
}