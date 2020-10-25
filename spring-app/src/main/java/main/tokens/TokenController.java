package main.tokens;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import utils.JwtUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;


@RestController
public class TokenController {

    private JwtUtil jwtUtil = new JwtUtil();

    private AtomicLong chats = new AtomicLong(); // TODO: change to redis counter

    @GetMapping(value = "/api/chat/v1/authorize")
    void token(@RequestHeader(value = "Authorization", defaultValue = "") String token, HttpServletResponse response) throws IOException {
        if (!token.isEmpty()) {
            response.setStatus(400);
            response.getWriter().println("You already have valid token");
            return;
        }
String chat_id = String.valueOf(chats.getAndAdd(1));
      //  String new_token = jwtUtil.generateToken(chat_id);

//        Cookie cookie = new Cookie("OpenChat", new_token);
//
//        // expires in 1 days
//        cookie.setMaxAge(24 * 60 * 60);
//
//        // optional properties
//        //cookie.setSecure(true);
//        cookie.setHttpOnly(true);
//        cookie.setPath("/");
//
//        // add cookie to response
//        response.addCookie(cookie);
        response.getWriter().print("{ \"token\": \"" + chat_id + "\"}");
    }

}