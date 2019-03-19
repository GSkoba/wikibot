package com.wikibot.wiki.controller;

import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.client.TamTamSerializer;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.exceptions.SerializationException;
import chat.tamtam.botapi.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.wikibot.wiki.wiki.*;

@RestController
@RequestMapping
public class MainController {
    private static final Logger log = LogManager.getLogger(MainController.class);

    @Autowired
    private TamTamBotAPI tamTamBotAPI;

    @Autowired
    private TamTamSerializer tamTamSerializer;

    @Value("${tamtambot.controller.host}")
    private String host;

    protected static final String ENDPOINT = "/getUpdate";

    @RequestMapping(ENDPOINT)
    @ResponseStatus(value = HttpStatus.OK)
    public void getUpdate(@RequestBody String body) {
        Update update = parseUpdate(body);

        if (update != null) {

            update.visit(new Update.Visitor() {
                @Override
                public void visit(MessageCreatedUpdate model) {
                    long userId = model.getMessage().getSender().getUserId();
                    String search = model.getMessage().getMessage().getText();
                    List<WikiNote> listOfResult = null;
                    try {
                        assert search != null;
                        listOfResult = requestToWiki(search.replace(' ', '+'));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (listOfResult != null) {
                        for (WikiNote elem : listOfResult
                        ) {
                            NewMessageBody messageBody = new NewMessageBody(elem.toString(), null);
                            try {
                                respondOnCommand(userId, messageBody);
                            } catch (ClientException e) {
                                e.printStackTrace();
                            } catch (APIException e) {
                                e.printStackTrace();
                            }
                        }
                    }  else {
                        NewMessageBody messageBodyError = new NewMessageBody("Что то пошло не так(", null);
                        try {
                            respondOnCommand(userId, messageBodyError);
                        } catch (ClientException e) {
                            e.printStackTrace();
                        } catch (APIException e) {
                            e.printStackTrace();
                        }
                    }

                }

                @Override
                public void visit(MessageCallbackUpdate model) {

                }

                @Override
                public void visit(MessageEditedUpdate model) {

                }

                @Override
                public void visit(MessageRemovedUpdate model) {

                }

                @Override
                public void visit(MessageRestoredUpdate model) {

                }

                @Override
                public void visit(BotAddedToChatUpdate model) {

                }

                @Override
                public void visit(BotRemovedFromChatUpdate model) {

                }

                @Override
                public void visit(UserAddedToChatUpdate model) {

                }

                @Override
                public void visit(UserRemovedFromChatUpdate model) {

                }

                @Override
                public void visit(BotStartedUpdate model) {

                }

                @Override
                public void visit(ChatTitleChangedUpdate model) {

                }

                @Override
                public void visitDefault(Update model) {

                }
            });

        }
    }

    @Nullable
    protected Update parseUpdate(String body) {
        try {
            return tamTamSerializer.deserialize(fixModel(body), Update.class);
        } catch (SerializationException e) {
            log.error("Error while parsing web hook body " + body);
            return null;
        }
    }

    @NotNull
    private String fixModel(String requestBody) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(requestBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (jsonObject.has("payload")) {
            try {
                jsonObject.put("message", jsonObject.get("payload"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            requestBody = jsonObject.toString();
        }

        return requestBody;
    }

    @PostConstruct
    private void init() throws ClientException, APIException {
        final SubscriptionRequestBody body = new SubscriptionRequestBody(host + ENDPOINT)
                .updateTypes(new HashSet<>(Arrays.asList("message_created", "message_callback", "bot_started")));

        tamTamBotAPI.subscribe(body).execute();
    }

    protected void respondOnCommand(Long userId, NewMessageBody messageBody) throws ClientException, APIException {
        tamTamBotAPI.sendMessage(messageBody).userId(userId).execute();
    }

    @ExceptionHandler(Exception.class)
    public void handleError(HttpServletRequest req, HttpServletResponse response) {
        response.setStatus(200);
    }

    public List<WikiNote> requestToWiki(String search) throws IOException, JSONException {
        String url = "https://ru.wikipedia.org/w/api.php?action=opensearch&search=" + search + "&limit=3&origin=*&format=json";
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        List<WikiNote> list = new ArrayList<WikiNote>();

        JSONArray jsonArray = new JSONArray(response.toString());
        JSONArray wikiName = jsonArray.getJSONArray(1);
        JSONArray wikiDisc = jsonArray.getJSONArray(2);
        JSONArray wikiLink = jsonArray.getJSONArray(3);

        if (wikiName.length() > 0)
            list.add(new WikiNote(wikiName.getString(0), wikiDisc.getString(0), wikiLink.getString(0)));
        if (wikiName.length() > 1)
            list.add(new WikiNote(wikiName.getString(1), wikiDisc.getString(1), wikiLink.getString(1)));
        if (wikiName.length() > 2)
            list.add(new WikiNote(wikiName.getString(2), wikiDisc.getString(2), wikiLink.getString(2)));

        return list;
    }
}
