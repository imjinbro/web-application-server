package webserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
     [1단계 요구사항]
     - localhost/index.html 요청했을 때 webapp/index.html 파일로 응답해
 */
public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    //소켓, input/outputstream 관리를 따로해줘야할까?
    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

       
    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {            
            
            readRequest(new BufferedReader(new InputStreamReader(in)));
            sendResponse();
            
                                    
            /* 응답부분도 하나로 묶기 */
            DataOutputStream dos = new DataOutputStream(out);            
            byte[] body = "안녕하세요".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
                 
            /* 
            HTTP : 통신하고 닫아주고 자원 반납하고 (소켓 유지하지않고), 쓰레드는 작업 처리이후 소멸            
            요청할 때마다 새로운 클라이언트(http / 소켓통신이랑은 다름)           
            어떤 요청에도 안녕하세요만 응답 : 읽기는? 연결만되면 그냥 되게끔인가... 
             */
            disconnect(in, out);
            
        } catch (IOException e) {
            log.error(e.getMessage());
        } 
    }
    
    private void disconnect(InputStream in, OutputStream out) {
        try {
            in.close();
            out.close();
            connection.close();
        } catch(IOException e) {
            log.info(e.getMessage());
        }        
    }
    
    
    /**************** REQUEST_PROCESS ****************/
    private void readRequest(BufferedReader br) {
        try {
            String line = null;            
            while(isContinueHTTP(line = br.readLine())) {
                log.info(line);
            }                                    
        } catch(IOException e) {
            log.error(e.getMessage());
        }        
    }
    
    private boolean isContinueHTTP(String line) {
        //HTTP EOF : 빈 라인 출력됨
        return !line.equals("");
    }
    
    
    /***************** RESPONSE_PROCESS *****************/
    private void sendResponse() {
        
    }

    //200만 있는게 아니라서 상태에 따라 처리하도록 해야할 것 같음
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
