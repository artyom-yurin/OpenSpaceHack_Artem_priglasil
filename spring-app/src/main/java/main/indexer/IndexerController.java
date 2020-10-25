package main.indexer;

import com.google.gson.Gson;
import okhttp3.*;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.DatabaseController;

import javax.annotation.PostConstruct;
import java.io.*;
import java.sql.SQLException;



@RestController
@CrossOrigin
public class IndexerController {
    MediaType JSON;
    Gson gson;
    DatabaseController controller;

    public IndexerController() {
        this.JSON = MediaType.get("application/json; charset=utf-8");
        this.gson = new Gson();
        this.controller = new DatabaseController();
        if (this.controller.establishConnection()) {
            System.out.println("Database connected");
        }
    }



    @GetMapping("/index")
    public ResponseEntity<String> index() throws IOException, JSONException, SQLException {
        this.controller.prepareDb();
        this.controller.initDb();
        this.controller.index();
/*
        controller.closeConnection();
*/
        return new ResponseEntity<>("Indexing done", HttpStatus.OK);
    }

    private ResponseEntity<String> preindex() {
        ResponseEntity<String> resp = null;
        try {
            resp = index();
        } catch (Exception e) {
            return null;
        }
        return resp;
    }

    @PostConstruct
    public void do_index() {
        this.controller.prepareDb();
        this.controller.initDb();
        ResponseEntity<String> resp = null;
        do {
            resp = preindex();
        } while (resp == null);
        System.out.println("Indexing DONE");
    }
}
