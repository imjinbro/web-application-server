# 실습을 위한 개발 환경 세팅
* https://github.com/slipp/web-application-server 프로젝트를 자신의 계정으로 Fork한다. Github 우측 상단의 Fork 버튼을 클릭하면 자신의 계정으로 Fork된다.
* Fork한 프로젝트를 eclipse 또는 터미널에서 clone 한다.
* Fork한 프로젝트를 eclipse로 import한 후에 Maven 빌드 도구를 활용해 eclipse 프로젝트로 변환한다.(mvn eclipse:clean eclipse:eclipse)
* 빌드가 성공하면 반드시 refresh(fn + f5)를 실행해야 한다.

# 웹 서버 시작 및 테스트
* webserver.WebServer 는 사용자의 요청을 받아 RequestHandler에 작업을 위임하는 클래스이다.
* 사용자 요청에 대한 모든 처리는 RequestHandler 클래스의 run() 메서드가 담당한다.
* WebServer를 실행한 후 브라우저에서 http://localhost:8080으로 접속해 "Hello World" 메시지가 출력되는지 확인한다.

# 각 요구사항별 학습 내용 정리
* 구현 단계에서는 각 요구사항을 구현하는데 집중한다. 
* 구현을 완료한 후 구현 과정에서 새롭게 알게된 내용, 궁금한 내용을 기록한다.
* 각 요구사항을 구현하는 것이 중요한 것이 아니라 구현 과정을 통해 학습한 내용을 인식하는 것이 배움에 중요하다. 

### 요구사항 1 - http://localhost:8080/index.html로 접속시 응답
* 웹서버 : 요청을 읽고 처리할건지 정하고 요청에 따라 결과를 줌(응답 - byte단위)
* 요청을 동시(concurrency)에 처리
* HTTP 요청 프로토콜 : 요청라인(URI, HTTP 버젼, HTTP 메서드) - 요청헤더 - 요청본문(헤더와 본문 사이에는 공백 라인이 있음)
* HTTP 응답 프로토콜 : 응답라인(HTTP 버젼, 응답코드) - 응답헤더 - 응답본문
* 1개씩 요청 - 응답 처리 : 필요할 때마다 1번씩 요청 - 응답(.html 응답 -> 브라우저 html 분석 필요한 것 재요청 -> 응답 루프(쓰레드)
* 특정 포트 사용 프로세스 정보 : sudo lsof -i:$PORT

### 요구사항 2 - get 방식으로 회원가입
* HTTP 메서드 GET : 민감한 정보를 줄 때 주소에 다 드러남(요청라인), 요청라인 길이 제한 있음, 있는 자원 요청할 때만 사용(상태조회) 

### 요구사항 3 - post 방식으로 회원가입
* HTTP 메서드 POST : 요청 시 추가적인 정보를 줄 때 사용(상태변경), POST 요청라인일 때 Content-Length(요청바디) 

### 요구사항 4 - redirect 방식으로 이동
* HTTP 3XX 응답코드 : redirect 완료 상태코드
* 리다이렉트 : 클라이언트에 새로운 url 제공, 새로운 url로 재요청 -> 재응답(요청 상태 유지X) 

### 요구사항 5 - cookie
* 쿠키 : HTTP는 연결마다 독립됨(하고 끊고 하고 끊고) 각 연결마다 상태를 공유할 수 있는 수단(클라이언트에 정보 저장 - 보안 위험)
* 요청 헤더(Cookie) : 쿠키 있는지에 따라서 처리 다르게하기
* 요청 처리 - 리다이렉트 담당 메서드 따로 두기 

### 요구사항 6 - stylesheet 적용
* 요청 헤더(Accept) - 응답 헤더(Content-Type) : 요청한 콘텐츠타입에 맞는 응답 콘텐츠타입

### heroku 서버에 배포 후
* 