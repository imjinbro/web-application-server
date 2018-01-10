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
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

/*
     [로그인 과정으로 알아보는 HTTP..................... 요구사항] : HTTP 요청 - 응답 프로토콜 구성, HTTP 메서드, redirect, 포맷, 쿠키 
     (1) url에 따라 처리
     (2) 쿼리스트링 처리
     (3) GET, POST 각각 처리 
     (4) 파일만 보내줘서 상태(요청 정보) 유지되니깐 새로고침으로 : 리다이렉트
     (5) 로그인 - 성공(인덱스 페이지 이동, 로그인 쿠키 true), 실패(실패 페이지로 이동, 로그인 쿠키 false)
     (6) 로그인 되어있을 때(Cookie: logined=true) 유저 리스트 보여주기, 로그인 되지않았으면 로그인 페이지로 이동
     (7) 요청에 맞는 콘텐츠 타입 응답 : 현재 고정된 text/html 타입으로 응답헤더 구성
     
     
     [문제점] 
     - 처리할 페이지 혹은 액션이 늘어날 때마다 if ~ else if 혹은 switch문을 추가해야함 : 변경사항이 생길 때마다 요동치는 코드
     - 그냥 리팩토링 해야할게 천지다....... 
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
                                         
            /*********** Request - Response Process ***********/
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String requestLine = br.readLine();
           
            if(isInvaildRequestLine(requestLine)) {                     
                disconnect(connection);
            }
                      
            String[] splitedRequestLine = splitString(requestLine, " "); 
            String url = splitedRequestLine[1];
                
                                 
            String requestHeader = null;
            
            String contentType = null;
            int contentLength = 0;
            boolean isLogined = false;
            
            while(!isEndOfHeader(requestHeader = br.readLine())) {      
                if(isCookie(requestHeader)) {
                    isLogined = getCookie(requestHeader, "logined");
                }
                
                if(isContentType(requestHeader)) {
                    contentType = getContentType(requestHeader);
                }
                
                if(isContentLength(requestHeader)) {
                    contentLength = getContentLength(requestHeader);
                }
            }
            
            
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
    

    /**** HTTP ****/
    private boolean isInvaildRequestLine(String requestLine) {        
        return requestLine.isEmpty() || requestLine == null;
    }
    
    private boolean isEndOfHeader(String requestHeader) {
        return requestHeader.equals("");
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
    
    
    
    /**** GET, POST ****/
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