package http.request;

import http.HttpMethod;
import util.HttpRequestUtils;

public class RequestLine {     
    private HttpMethod method;
    private String path;

    public RequestLine(String requestLine) {
        readRequestLine(requestLine);
    }

    private void readRequestLine(String requestLine){
        String[] splitedRequestLine = HttpRequestUtils.splitRequestLine(requestLine, " ");                       
        setRequestLineData(splitedRequestLine);
    }
    
    private void setRequestLineData(String[] splitedRequestLine){
        if(isInvalidRequestLine(splitedRequestLine)) {
            throw new IllegalArgumentException();
        }
        
        method = getHttpMethod(splitedRequestLine[0]);        
        path = splitedRequestLine[1];
    }
    
    private boolean isInvalidRequestLine(String[] splitedRequestLine) {
        return splitedRequestLine == null || splitedRequestLine.length != 3;
    }

    private HttpMethod getHttpMethod(String method) {
        return HttpMethod.valueOf(method);
    }
    
    public HttpMethod getMethod() {        
        return method;
    }       
    
    public String getPath() {
        return path;
    }      
}
