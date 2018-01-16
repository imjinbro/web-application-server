package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import util.HttpRequestUtils;
import util.HttpRequestUtils.Pair;
import util.IOUtils;

public class HttpRequest {
    private BufferedReader reader;
    
    private String[] requestLine;    
    private Map<String,String> requestHeaderMap;
    
    private String requestBody;
    
    public HttpRequest(InputStream in) {
        if(isInvalidStream(in)) {
            //+ 예외처리
        }        
        reader = new BufferedReader(new InputStreamReader(in));        
        requestHeaderMap = new HashMap<>();        
    }    
    
    /*********** reader(-), read(+) ***********/    
    private BufferedReader getReader() {
        return reader;
    }
    
    private boolean isInvalidStream(InputStream in) {
        return in == null;
    }
    
        
    public void readRequest() throws IOException {
        readRequestLine(getReader());
        readRequestHeader(getReader());        
        
        if(getRequestMethod().equals("POST")) {
            readRequestBody();
        }
    }
        
    
    

    /*********** request_line ***********/      
    private void readRequestLine(BufferedReader br) throws IOException {
        String requestLine = br.readLine();          
        if(isInvalidRequestLine(requestLine)) {
            //+ 예외처리
        }        
        this.requestLine = HttpRequestUtils.splitRequestLine(requestLine, " ");
    }
    
    private boolean isInvalidRequestLine(String requestLine) {
        return requestLine.isEmpty() || HttpRequestUtils.splitRequestLine(requestLine, " ").length != 3;
    }
    
    public String getRequestMethod() {
        return requestLine[0];
    }       
    
    public String getRequestPath() {
        return requestLine[1];
    }
    
    
    
    
    /*********** request_header ***********/
    private void readRequestHeader(BufferedReader br) throws IOException {
        String requestHeader = null;
        while(!isEndOfHeader(requestHeader = br.readLine())) {
            setRequestHeader(requestHeader);
        }
    }
    
    private boolean isEndOfHeader(String requestHeader) {
        return requestHeader.equals("");
    }
    
    private void setRequestHeader(String requestHeader) {
        Pair headerPair = HttpRequestUtils.parseHeader(requestHeader);
        requestHeaderMap.put(headerPair.getKey(), headerPair.getValue());
    }
    
    private Map<String, String> getRequestHeaderMap(){
        return requestHeaderMap;
    }
    
    public String getRequestHeader(String headerKey) throws NoSuchElementException{        
        if(isInvalidRequestHeaderKey(headerKey) || !isExistHeader(getRequestHeaderMap(), headerKey)) {
            throw new NoSuchElementException();
        }

        return requestHeaderMap.get(headerKey);
    }
    
    private boolean isInvalidRequestHeaderKey(String headerKey) {
        return headerKey.isEmpty() || headerKey == null;
    }
    
    private boolean isExistHeader(Map<String,String> requestHeaderMap, String headerKey) {
        return requestHeaderMap.containsKey(headerKey);
    }

    
    
    
    /*********** request_body ***********/   
    private void readRequestBody() throws IOException {
        requestBody = IOUtils.readData(getReader(), getContentLength());
    }
    
    public String getRequestBody() {
        if(isInvalidRequestBody(requestBody)) {
            //+ 예외처리
        }
        return requestBody; 
    }
    
    private boolean isInvalidRequestBody(String requestBody) {
        return requestBody == null;
    }
    
    private int getContentLength() {
        String contentLengthVal = getRequestHeader("Content-Length");
        return HttpRequestUtils.strToInt(contentLengthVal);
    }
    
}
