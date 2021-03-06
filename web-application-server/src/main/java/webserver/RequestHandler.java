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

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (
        		InputStream in = connection.getInputStream();
        		InputStreamReader isr = new InputStreamReader(in);
        		BufferedReader br = new BufferedReader(isr);
        		OutputStream out = connection.getOutputStream();
        	) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
        	String otherInfoLine;
        	String firstLine = br.readLine();
        	String[] firstLines = firstLine.split(" ");
        	
        	log.debug(firstLine);
        	
        	while((otherInfoLine = br.readLine()) != null && !"".equals(otherInfoLine)) {
        		log.debug(otherInfoLine);
        	}
        	
        	String method = firstLines[0];
        	String reqUrl = firstLines[1];
        	
        	if (reqUrl.startsWith("/user/create")) {
        		int index = reqUrl.indexOf("?");
            	String reqPath = reqUrl.substring(0, index);
            	String params = reqUrl.substring(index + 1);

            	Map<String, String> paramMap = util.HttpRequestUtils.parseQueryString(params);
            	
            	String id = paramMap.get("userId");
            	String password = paramMap.get("password");
            	String name = paramMap.get("name");
            	String email = paramMap.get("email");
            	
            	User user = new User(id, password, name, email);
            	
            	DataBase.addUser(user);
        	}
        	
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = Files.readAllBytes(new File("./webapp" + reqUrl).toPath());
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

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
