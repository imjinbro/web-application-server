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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import http.HttpConnector;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

/*
     [구현된 것] 
     (1) url에 따라 처리
     (2) 쿼리스트링 처리
     (3) GET, POST 각각 처리 
     (4) 파일만 보내줘서 상태(요청 정보) 유지되니깐 새로고침으로 : 리다이렉트
     (5) 로그인 - 성공(인덱스 페이지 이동, 로그인 쿠키 true), 실패(실패 페이지로 이동, 로그인 쿠키 false)
     (6) 로그인 되어있을 때(Cookie: logined=true) 유저 리스트 보여주기, 로그인 되지않았으면 로그인 페이지로 이동
     (7) 요청에 맞는 콘텐츠 타입 응답 : 현재 고정된 text/html 타입으로 응답헤더 구성
     
     
     [문제점 찾기] 
     (1) RequestHandler 역할
     - 쓰레드
     - 소켓 관리
     - 요청, 응답처리
     - 유저 생성
     
     (2) 요청이라는 큰 틀은 같지만 세부적인 내용이 다르다고해서 같은 인터페이스로 처리하지않고 if ~ else 로 분기
     - 요청의 형태가 달라지거나 요청 종류가 많아지면 if ~ else 혹은 switch 혹은 코드 자체가 달라질 수 있음 
     
     (3) 응답도 마찬가지로 응답코드 대응에 따라 메서드가 증가 
     - 지금은 200, 302만 하지만 404, 500과 같은 응답코드에 대한 처리가 없으니 지금 코드라면 메서드가 더 늘어날 예정
     
         
     [잊지말기]
     - 브랜치 체크아웃 전에 커밋하거나 stash 해야 브랜치 코드 혼선 안일으킴 
     - 모든 예외 한 곳에서 처리하기 : 메세지만 따로 던지고 핸들러에서 한꺼번에
 */

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    
    /*** 없어질 코드 ***/
    private Socket connection;
    
    private HttpConnector connector;
    
    private HttpRequest request;
    private HttpResponse response;
    

    public RequestHandler(Socket connectionSocket) { 
        if(isInvalidConnection(connectionSocket)) {
            return;
        }        
        connector = new HttpConnector(connectionSocket);
    }
    
    private boolean isInvalidConnection(Socket connection) {
        return connection == null;
    }
    
    private void setHttp(HttpConnector connector) {
        request = new HttpRequest(connector.getInput());
        response = new HttpResponse(connector.getOutput());
    }
    
    private HttpConnector getHttpConnector() {
        return connector;
    }

    
    
    /************* 쓰레드 영역 *************/
    public void run() {
        setHttp(getHttpConnector());
        
        /* 여기 부분도 만들고 난 뒤 메서드로 처리
         * 연결정보를 가져온다 -> 읽는다(요청라인,요청헤더,구분줄,요청바디) -> 응답한다 
         * 필요한 요청라인, 헤더, 바디를 가지고 올 수 있음
         * 응답하기 : 리턴으로 모드 가져오기 할까
         */ 
        try {
            request.readRequest();
            
            
            
            
        } catch (IOException e) {
            log.error(e.getMessage()); 
            connector.disconnect();                       
        }
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        /********************* 이 밑으로는 여기서 없어질 코드 *********************/
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
                                         
            /*********** Request - Response Process ***********/
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String requestLine = br.readLine();
           
            
                      
            String[] splitedRequestLine = splitString(requestLine, " "); 
            String url = splitedRequestLine[1];
                
                                 
            String requestHeader = null;
            
            String contentType = null;
            int contentLength = 0;
            boolean isLogined = false;
                       
            
            
            
            
            DataOutputStream dos = new DataOutputStream(out);
                        
            if(url.startsWith("/user/create")) {       
                String requestBody = getRequestBody(br, contentLength);                                
                Map<String, String> requestBodyMap = HttpRequestUtils.parseQueryString(requestBody);                                   
                DataBase.addUser(createUser(requestBodyMap));         
                url = "/index.html";
                response302Header(dos, url);
            } else if(url.equals("/user/login")) {
                String requestBody = getRequestBody(br, contentLength);
                Map<String, String> requestBodyMap = HttpRequestUtils.parseQueryString(requestBody);                
                User user = DataBase.findUserById(requestBodyMap.get("userId"));
                
                if(!isExistUser(user) || !isRightPwd(user, requestBodyMap.get("password"))) {    
                    url = "/user/login_failed.html";
                    responseResource(dos, url, contentType);
                    return;
                }                                                
                response302LoginSuccessHeader(dos);                
            } else if(url.startsWith("/user/list")){
                if(!isLogined) {
                    url = "/user/login.html";
                    response302Header(dos, url);
                    return;
                } 
                /* list 응답 메서드 호출 : 지금은 그냥 list.html 그대로(어떤 응답처리 후에 거기서 리다이렉트 시키려면 메서드 */
                responseResource(dos, url, contentType);                
            } else {
                responseResource(dos, url, contentType);
            }
              
            
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }   
   
    
    
    /**** RESPONSE ****/
    private void responseResource(DataOutputStream dos, String url, String contentType) throws IOException{
        byte[] body = Files.readAllBytes(new File("./webapp"+ url).toPath());
        response200Header(dos, body.length, contentType);
        responseBody(dos, body);
    }
        
    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType +";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location : " + url + "\r\n");            
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response302LoginSuccessHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            dos.writeBytes("Location: /index.html \r\n");            
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
    
    
    
    /**** Request : GET, POST ****/
    private boolean isCookie(String requestHeader) {
        return requestHeader.contains("Cookie");
    }
    
    private boolean isExistCookie(String cookie) {
        return cookie != null;
    }
    

    private boolean getCookie(String requestHeader, String cookieName) {
        String[] header = splitString(requestHeader, ":");
        Map<String,String> cookieMap = HttpRequestUtils.parseCookies(header[1]);
        String cookie = cookieMap.get(cookieName);              
        
        if(!isExistCookie(cookie)) {
            return false;
        }
        return true;
    }
        
    private boolean isContentType(String requestHeader) {        
        return requestHeader.startsWith("Accept:");
    }
    
    private String getContentType(String requestHeader) {
        String[] header = splitString(requestHeader, " |,");
        return header[1];
        
    }
    
    private boolean isContentLength(String requestHeader) {
        return requestHeader.contains("Content-Length");
    }
    
    private int getContentLength(String requestHeader) {
        String[] header = splitString(requestHeader, " ");
        return strToInt(header[1]);
    }
        
    private String getRequestBody(BufferedReader br, int contentLength) {
        String requestBody = "";        
        try {
            requestBody = IOUtils.readData(br, contentLength);
        }catch(IOException e) {
            log.error(e.getMessage());
        }        
        return requestBody;
    }
    
    private String getQueryString(String url) {
        int queryStringIdx = url.indexOf("?") + 1;
        return url.substring(queryStringIdx);        
    }
    
    private User createUser(Map<String,String> queryMap) {
        //String userId, String password, String name, String email, 나중에 중복 체크까지        
        return new User(queryMap.get("userId"), queryMap.get("password"), queryMap.get("name"), queryMap.get("email"));
    }
    
    private boolean isExistUser(User user) {
        return user != null;
    }
    
    private boolean isRightPwd(User user, String inputPwd) {
        return user.getPassword().equals(inputPwd);
    }
    
    
    
    
    /**** UTILITY ****/    
    private String[] splitString(String str, String delimiter) {
        return str.split(delimiter);
    }
    
    private int strToInt(String str) {
        return Integer.parseInt(str);
    }
  
   
    
    private void disconnect(Socket connection) {
        try {
            connection.getInputStream().close();
            connection.getOutputStream().close();
        } catch(IOException e) {
            log.error(e.getMessage());
        }        
    }
    
}