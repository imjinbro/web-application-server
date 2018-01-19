package http;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import http.request.HttpRequest;
import http.request.RequestLine;

public class HttpRequestTest {
    private String srcDir = "./src/test/java/http/";
    
    @Test
    public void test_HttpMethod() {
        RequestLine requestLine = new RequestLine("POST /index.html HTTP/1.1");        
        assertEquals(true, requestLine.getMethod().isPost());
    }
    
    @Test
    public void test_RequestLine() {
        RequestLine requestLine = new RequestLine("GET /index.html HTTP/1.1");
        assertEquals(requestLine.getMethod(), HttpMethod.GET);
        assertEquals(requestLine.getPath(), "/index.html");
    }
    
    
    @Test
    public void test_GET() {
        try {
            File test = new File(srcDir + "HTTP_GET.txt");                        
            InputStream in = new FileInputStream(test);
            
            HttpRequest request = new HttpRequest(in);
            assertEquals(request.getMethod(), HttpMethod.GET);
            assertEquals(request.getPath(), "/index.html");            
            assertEquals(request.getHeader("Host"), "localhost:8080");            
            
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }    
    }
    
    
    
    @Test
    public void test_POST() {
        try {
            File test = new File(srcDir + "HTTP_POST.txt");                        
            InputStream in = new FileInputStream(test);
            
            HttpRequest request = new HttpRequest(in);
            assertEquals(request.getMethod(), HttpMethod.POST);
            assertEquals(request.getPath(), "/user/create");
            assertEquals(request.getHeader("Content-Length"), "40");
            
            
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }    
    }

}
