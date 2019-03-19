package com.wikibot.wiki.wiki;

import java.io.Serializable;

public class WikiNote implements Serializable {

    private String name;
    private String disc;
    private String link;

    public WikiNote(String name, String disc, String link) {
        this.name = name;
        this.disc = disc;
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisc() {
        return disc;
    }

    public void setDisc(String disc) {
        this.disc = disc;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return name + '\n' + disc + '\n' + link;
    }
}
