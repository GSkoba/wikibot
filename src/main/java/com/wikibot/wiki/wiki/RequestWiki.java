package com.wikibot.wiki.wiki;

import chat.tamtam.botapi.model.CallbackButton;
import chat.tamtam.botapi.model.Intent;
import com.wikibot.wiki.dao.Storage;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RequestWiki {

    @Autowired
    private Storage storage;

    public List<CallbackButton> requestToWiki(long userId, String search, long time) throws IOException, JSONException {
        String len = storage.getUserLen(userId);
        String url = "https://" + len + ".wikipedia.org/w/api.php?action=opensearch&search=" + search.replace(' ', '+') + "&limit=3&origin=*&format=json";
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
        List<CallbackButton> callbackButtons = new ArrayList<>();

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

        long msg_id = storage.getMsgId(search, userId, time);
        long wiki_answer = 0;
        for (WikiNote wikiNote: list
             ) {
                storage.setWikiAnswer(wikiNote.getName(), wikiNote.getDisc(), wikiNote.getLink(), msg_id);
                wiki_answer = storage.getWikiAnswerId(wikiNote.getName(), wikiNote.getDisc(), wikiNote.getLink(), msg_id);
                callbackButtons.add(new CallbackButton(
                        Long.toString(wiki_answer),
                        wikiNote.getName(),
                        Intent.DEFAULT
                ));
        }


        return callbackButtons;
    }

}
