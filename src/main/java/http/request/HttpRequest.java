package http.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import http.HttpMethod;

/*
 * 나눌 수 있을 때까지 나누기
 */
public class HttpRequest {
    private BufferedReader reader;
    
    private RequestLine requestLine;  
    private RequestHeader requestHeader;
    

    //리팩토링 대상
    private RequestParameter requestParameter;
    private Map<String,String> params = new HashMap<>(); //GET : queryString, POST : body
    
    
    public HttpRequest(InputStream in) throws IOException {
        initRequest(in);
    }        
    
    /*********** reader, read ***********/
    private void initRequest(InputStream in) throws IOException {
        if(isInvalidStream(in)) {
            throw new IOException();
        }               
        reader = new BufferedReader(new InputStreamReader(in));
        requestLine = new RequestLine(reader.readLine());      
        requestHeader = new RequestHeader();
        readHeader(reader);
    }
    
    private boolean isInvalidStream(InputStream in) {
        return in == null;
    }

  
    /*********** request_line ***********/
    public HttpMethod getMethod() {
        return requestLine.getMethod();
    }
    
    public String getPath() {
        return requestLine.getPath();
    }
    
          
    /*********** request_header ***********/
    private void readHeader(BufferedReader br) throws IOException {
        String header = null;
        
        while(!isEndOfHeader((header = br.readLine()))) {      
            requestHeader.addHeader(header);            
        }                 
    }
    
    private boolean isEndOfHeader(String header) {
        return header.equals("");
    }
    
    public String getHeader(String headerKey) {
        return requestHeader.getHeader(headerKey);
    }
}
