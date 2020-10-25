package main.query_analyzer;

import com.google.gson.Gson;
import main.tokens.TokenController;
import models.*;
import models.RequestBody;
import okhttp3.MediaType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utils.CosineSimilarity;
import utils.DatabaseController;
import utils.JwtUtil;
import utils.RedisController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

//@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge=3600)
@RestController
public class QAController {

    //private JwtUtil jwtUtil = new JwtUtil();
    private RedisController redis = new RedisController();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Gson gson = new Gson();

    private static Map<Integer, Double> sortByValue(Map<Integer, Double> unsortMap) {

        List<Map.Entry<Integer, Double>> list =
                new LinkedList<Map.Entry<Integer, Double>>(unsortMap.entrySet());


        list.sort(new Comparator<Map.Entry<Integer, Double>>() {
            public int compare(Map.Entry<Integer, Double> o1,
                               Map.Entry<Integer, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>();
        for (Map.Entry<Integer, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    double[][] make_doc_matrix() throws FileNotFoundException, JSONException {
        ArrayList<double[]> docList = new ArrayList<>();
        File file = new File("path/to/file");
        Scanner sc = new Scanner(file);
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            JSONObject object = new JSONObject(line);
            JSONArray innerArray = object.getJSONArray("result").getJSONArray(0);
            double[] tmp = new double[innerArray.length()];

            for (int i = 0; i < innerArray.length(); i++) {
                tmp[i] = innerArray.getDouble(i);
            }
            docList.add(tmp);

        }
        double[][] docMatrix = new double[docList.size()][768];
        return docList.toArray(docMatrix);
    }

    @GetMapping(value = "/api/chat/v1/bot", produces = "application/json")
    ResponseEntity<String> message(@RequestHeader(value = "Authorization", defaultValue = "0") String token, @RequestParam(value = "question", defaultValue = "") String question) throws IOException, JSONException, SQLException {
        if (question.isEmpty()) {
            return ResponseEntity.status(400)
                    .body("question is empty");
        }
        System.out.println(question);
        String chatId = token; // jwtUtil.parseToken(token); for testing system this code is commented
        if (chatId == null) {
            return ResponseEntity.status(401)
                    .body("I don't know you");
        }

        Context context = redis.getContextByChatId(chatId);
        if (context == null) {
            System.out.println("Context is " + context);
            return ResponseEntity.status(500).body("Context state failed");
        } else {
            System.out.println("Context state is " + context.getState().toString());
        }

        context.newRequest();
        ConversationState state = context.getState();
        if (state == ConversationState.Answered) {
            if (question.toLowerCase().equals("no")) {
                Map<Integer, ContextCandidate> entries = context.getCandidates();


                if (context.getCounter() == 6 || entries.isEmpty()) {
                    DatabaseController controller = new DatabaseController();
                    if (controller.establishConnection()) {
                        System.out.println("Database connected");
                    }
                    controller.push_unknown_question(context.getOriginalQuestion());
                    context = new Context();
                    redis.setContextByChatId(chatId, context);
                    return ResponseEntity.ok().body(gson.toJson(new MessageResponse("unknown")));
                }

                Map.Entry<Integer, ContextCandidate> entity = entries.entrySet().iterator().next();
                ContextCandidate answer = entity.getValue();
                entries.remove(entity.getKey());

                context.setCandidates(entries);
                redis.setContextByChatId(chatId, context);

                MessageResponse resp = new MessageResponse(answer.getDatabaseEntry().getAnswer());
                return ResponseEntity.ok().body(gson.toJson(resp));
            } else if (question.toLowerCase().equals("yes")) {
                context = new Context();
                redis.setContextByChatId(chatId, context);
                MessageResponse resp = new MessageResponse("Отлично, жду следующий вопрос!");
                return ResponseEntity.ok().body(gson.toJson(resp));
            }
            state = ConversationState.Init;
            context.resetCounter();
        }

        if (state == ConversationState.Init) {
            context.setOriginalQuestion(question);
            RequestBody req_body = new RequestBody();
            req_body.setId(chatId);
            ArrayList<String> messages = new ArrayList<>();
            messages.add(question);
            req_body.setTexts(messages);
            req_body.set_tokenized(false);

            okhttp3.RequestBody body = okhttp3.RequestBody.create(gson.toJson(req_body), JSON);
            JSONObject response = new JSONObject(RequestBody.make_post_request("http://indexer:8125/encode", body));
            JSONArray innerArray = response.getJSONArray("result").getJSONArray(0);

            DatabaseController controller = new DatabaseController();
            if (controller.establishConnection()) {
                System.out.println("Database connected");
            } else {
                System.out.println("Connection failed");
                return ResponseEntity.status(505)
                        .body("Db connection failed");
            }
            Double[][] docMatrix = controller.get_vectors();
            double[] query_vector = new double[768];
            for (int i = 0; i < innerArray.length(); i++) {
                query_vector[i] = innerArray.getDouble(i);
            }
            double[] similarity = CosineSimilarity.cosine_similarity(docMatrix, query_vector);
            HashMap<Integer, Double> map = new HashMap<Integer, Double>();
            for (int i = 0; i < similarity.length; i++) {
                map.put(i + 1, similarity[i]);
            }
            Map<Integer, Double> sorted_map = sortByValue(map);

            Iterator<Map.Entry<Integer, Double>> it = sorted_map.entrySet().iterator();

            Map.Entry<Integer, Double> best = it.next();
            Map.Entry<Integer, Double> second = it.next();

            if (best.getValue() == 1d || (best.getValue() > 0.95d && best.getValue() - second.getValue() > 0.01)) {
                context.setState(ConversationState.Answered);
                redis.setContextByChatId(chatId, context);
                DatabaseEntry entry = controller.get_question(best.getKey());
                MessageResponse resp = new MessageResponse(entry.getAnswer());
                return ResponseEntity.ok().body(gson.toJson(resp));
            }

            Set<String> categories = new HashSet<>();
            HashMap<Integer, ContextCandidate> entries = new LinkedHashMap<>();

            DatabaseEntry best_entry = controller.get_question(best.getKey());
            categories.add(best_entry.request);
            entries.put(best.getKey(), new ContextCandidate(best.getValue(), best_entry));
            DatabaseEntry second_entry = controller.get_question(second.getKey());
            categories.add(second_entry.request);
            entries.put(second.getKey(), new ContextCandidate(second.getValue(), second_entry));

            for (int i = 2; i < 10 && it.hasNext(); i++) {
                Map.Entry<Integer, Double> entity = it.next();
                DatabaseEntry entry = controller.get_question(entity.getKey());
                categories.add(entry.request);
                entries.put(entity.getKey(), new ContextCandidate(entity.getValue(), entry));
            }

            context.setCandidates(entries);
            context.setCategories(categories);
            // case: in one category, shoud find request_type
            if (categories.size() == 1) {
                Set<String> types = new HashSet<>();
                for (Map.Entry<Integer, ContextCandidate> entry : entries.entrySet()) {
                    types.add(entry.getValue().getDatabaseEntry().request_type);
                }

                context.setTypes(types);
                if (types.size() == 1) {
                    context.setState(ConversationState.Answered);
                    redis.setContextByChatId(chatId, context);
                    MessageResponse resp = new MessageResponse(best_entry.getAnswer());
                    return ResponseEntity.ok().body(gson.toJson(resp));
                }

                Iterator<String> cit = types.iterator();
                StringBuilder clarification_question = new StringBuilder("Уточняющий запрос: Уточните тип запроса: {" + cit.next() + "}");
                while (cit.hasNext()) {
                    clarification_question.append(", {").append(cit.next()).append("}");
                }

                context.setState(ConversationState.Clarification);
                redis.setContextByChatId(chatId, context);

                MessageResponse resp = new MessageResponse(clarification_question.toString());
                return ResponseEntity.ok().body(gson.toJson(resp));
            }

            Iterator<String> cit = categories.iterator();
            StringBuilder clarification_question = new StringBuilder("Уточняющий запрос: Уточните категорию: {" + cit.next() + "}");
            while (cit.hasNext()) {
                clarification_question.append(", {").append(cit.next()).append("}");
            }
            clarification_question.append(", {").append("Другое").append("}");

            context.setState(ConversationState.Clarification);
            redis.setContextByChatId(chatId, context);
            MessageResponse resp = new MessageResponse(clarification_question.toString());
            return ResponseEntity.ok().body(gson.toJson(resp));
        } else if (state == ConversationState.Clarification) {
            if (question.equals("Другое")) {
                DatabaseController controller = new DatabaseController();
                if (controller.establishConnection()) {
                    System.out.println("Database connected");
                }
                controller.push_unknown_question(context.getOriginalQuestion());
                context = new Context();
                redis.setContextByChatId(chatId, context);
                return ResponseEntity.ok().body(gson.toJson(new MessageResponse("unknown")));
            }

            Set<String> categories = context.getCategories();
            if (categories.size() == 1) {
                Set<String> topics = context.getTypes();
                if (!topics.contains(question)) {
                    return ResponseEntity.status(400)
                            .body("It is not topic");
                }
                context.setTypes(new HashSet<>(Collections.singletonList(question)));
                Map<Integer, ContextCandidate> entries = context.getCandidates();
                entries.entrySet().removeIf(integerContextCandidateEntry -> !integerContextCandidateEntry.getValue().getDatabaseEntry().request_type.equals(question));

                Map.Entry<Integer, ContextCandidate> entity = entries.entrySet().iterator().next();
                ContextCandidate answer = entity.getValue();
                entries.remove(entity.getKey());

                context.setState(ConversationState.Answered);
                context.setCandidates(entries);
                redis.setContextByChatId(chatId, context);

                MessageResponse resp = new MessageResponse(answer.getDatabaseEntry().getAnswer());
                return ResponseEntity.ok().body(gson.toJson(resp));
            } else {
                if (!categories.contains(question)) {
                    return ResponseEntity.status(400)
                            .body("It is not category");
                }
                context.setCategories(new HashSet<>(Collections.singletonList(question)));
                Map<Integer, ContextCandidate> entries = context.getCandidates();
                entries.entrySet().removeIf(integerContextCandidateEntry -> !integerContextCandidateEntry.getValue().getDatabaseEntry().request.equals(question));

                Set<String> types = new HashSet<>();
                for (Map.Entry<Integer, ContextCandidate> entry : entries.entrySet()) {
                    types.add(entry.getValue().getDatabaseEntry().request_type);
                }

                context.setTypes(types);
                if (types.size() == 1) {
                    context.setState(ConversationState.Answered);

                    Map.Entry<Integer, ContextCandidate> entity = entries.entrySet().iterator().next();
                    ContextCandidate answer = entity.getValue();
                    entries.remove(entity.getKey());

                    context.setCandidates(entries);
                    redis.setContextByChatId(chatId, context);

                    MessageResponse resp = new MessageResponse(answer.getDatabaseEntry().getAnswer());
                    return ResponseEntity.ok().body(gson.toJson(resp));
                }

                Iterator<String> cit = types.iterator();
                StringBuilder clarification_question = new StringBuilder("Уточняющий запрос: Уточните тип запроса: {" + cit.next() + "}");
                while (cit.hasNext()) {
                    clarification_question.append(", {").append(cit.next()).append("}");
                }

                clarification_question.append(", {").append("Другое").append("}");

                context.setState(ConversationState.Clarification);
                redis.setContextByChatId(chatId, context);

                MessageResponse resp = new MessageResponse(clarification_question.toString());
                return ResponseEntity.ok().body(gson.toJson(resp));
            }
        }

        return ResponseEntity.badRequest().build();
    }
}
