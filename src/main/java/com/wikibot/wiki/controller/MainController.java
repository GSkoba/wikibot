package com.wikibot.wiki.controller;

import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.client.TamTamSerializer;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.exceptions.SerializationException;
import chat.tamtam.botapi.model.*;
import com.wikibot.wiki.dao.Storage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;;
import java.util.*;

import com.wikibot.wiki.wiki.*;

@RestController
@RequestMapping
public class MainController {
    private static final Logger log = LogManager.getLogger(MainController.class);

    @Autowired
    private TamTamBotAPI tamTamBotAPI;

    @Autowired
    private TamTamSerializer tamTamSerializer;

    @Autowired
    private Storage storage;

    @Autowired
    private RequestWiki requestToWiki;

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
                    List<CallbackButton> listOfResult = null;

                    if (search.startsWith("/set_l")) {
                        NewMessageBody messageBody = new NewMessageBody("Выберите язык (Choose language)",
                                Collections.singletonList(new InlineKeyboardAttachmentRequest(
                                        new InlineKeyboardAttachmentRequestPayload(
                                                Collections.singletonList(
                                                        Arrays.asList(
                                                                new CallbackButton("/ru", "Русский \uD83C\uDDF7\uD83C\uDDFA", Intent.DEFAULT),
                                                                new CallbackButton("/en", "English \uD83C\uDDEC\uD83C\uDDE7", Intent.DEFAULT)
                                                        )
                                                )
                                        )))
                        );

                        try {
                            respondOnCommand(userId, messageBody);
                        } catch (ClientException e) {
                            e.printStackTrace();
                        } catch (APIException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                    storage.setMsgUser(userId, model.getMessage().getMessage().getText(), model.getTimestamp());
                    try {
                        assert search != null;
                        listOfResult = requestToWiki.requestToWiki(userId, search, model.getTimestamp());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (listOfResult != null) {
                        for (CallbackButton elem : listOfResult
                        ) {
                            NewMessageBody messageBody = new NewMessageBody(elem.getText(),
                                    Collections.singletonList(
                                            new InlineKeyboardAttachmentRequest(
                                                    new InlineKeyboardAttachmentRequestPayload(
                                                            Collections.singletonList(
                                                                    Arrays.asList(elem)
                                                            )
                                                    )
                                            )
                                    )
                            );
                            try {
                                respondOnCommand(userId, messageBody);
                            } catch (ClientException e) {
                                e.printStackTrace();
                            } catch (APIException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
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
                    Callback callback = model.getCallback();
                    Long userId = callback.getUser().getUserId();
                    NewMessageBody messageBody = null;
                    if (!callback.getPayload().startsWith("/")) {
                        WikiNote wikiNote = storage.getWikiAnswer(Long.parseLong(callback.getPayload()));
                        messageBody = new NewMessageBody(
                                wikiNote.toString(),
                                null
                        );
                    } else {
                         messageBody = new NewMessageBody(
                                callback.getPayload().equals("/ru")
                                        ? "Выбран русский язык \uD83C\uDDF7\uD83C\uDDFA"
                                        : "English is selected \uD83C\uDDEC\uD83C\uDDE7",
                                null);
                        String len = callback.getPayload().substring(1);
                        storage.setLen(userId, len);
                    }
                    try {
                        tamTamBotAPI.answerOnCallback(new CallbackAnswer().userId(userId).message(messageBody), model.getCallback().getCallbackId()).execute();
                    } catch (APIException e) {
                        e.printStackTrace();
                    } catch (ClientException e) {
                        e.printStackTrace();
                    }
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
                    long userId = model.getUserId();
                    String len = "ru";
                    storage.createWikiUser(userId, len);
                    NewMessageBody messageBody = new NewMessageBody("Привет! Я бот, который поможет" +
                            " тебе что-нибудь быстро найти в wikipedia. Напиши пожалуйста свой запрос и " +
                            "я попробую что-нибудь найти (Hello! I am a bot that will help you find " +
                            "something quickly on wikipedia. Please write your request and " +
                            "I will try to find something.)", Collections.EMPTY_LIST);
                    try {
                        respondOnCommand(userId, messageBody);
                    } catch (ClientException e) {
                        e.printStackTrace();
                    } catch (APIException e) {
                        e.printStackTrace();
                    }

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


}
