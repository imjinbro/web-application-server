package http.response;

import http.request.HttpRequest;

/*
 *  HTTP 메서드에 따른 처리 + 상태 처리(처리 결과?)
 *  라인(HTTP버젼 상태코드 코드문자열)/헤더(Content-Type, Content-Length)/빈라인/바디(내용 - byte[])
 */
public class HttpResponse {    
    
    private HttpRequest request;
    private final String resourcePath = "/webapp/";
    
    public HttpResponse(HttpRequest request) {
        this.request = request;
    }
    
 
    
}