package com.rickstaff.app.data.model;

import java.util.List;

public class CharacterResponse {
    private Info info;
    private List<Character> results;

    public Info getInfo() { return info; }
    public List<Character> getResults() { return results; }

    public static class Info {
        private int count;
        private int pages;
        private String next;
        private String prev;

        public int getPages() { return pages; }
        public String getNext() { return next; }
    }
}