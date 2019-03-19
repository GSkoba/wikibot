package com.wikibot.wiki.config;

import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.client.TamTamSerializer;
import chat.tamtam.botapi.client.impl.JacksonSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {

    @Bean
    public TamTamBotAPI getApi(@Value("${tamtambot.controller.access_token}") String token){
        return TamTamBotAPI.create(token);
    }

    @Bean
    public TamTamSerializer getTamTamSerializer(){
        return new JacksonSerializer();
    }
}
