package http.request;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import util.HttpRequestUtils;
import util.HttpRequestUtils.Pair;

public class RequestHeader {
    private Map<String,String> headerMap = new HashMap<>();   
        
    public void addHeader(String header) {
        if(isInvalidHeader(header)) {
            throw new IllegalArgumentException();
        }
        
        Pair headers = HttpRequestUtils.parseHeader(header);
        headerMap.put(headers.getKey(), headers.getValue());
    }
    
    private boolean isInvalidHeader(String header) {
        return header.isEmpty() || header == null;
    }
    
    public String getHeader(String headerKey) {
        if(isExistHeader(headerKey)) {
            throw new NoSuchElementException();
        }     
        return headerMap.get(headerKey);
    }
    
    private boolean isExistHeader(String headerKey) {
        return headerMap.containsKey(headerKey);
    }
}
