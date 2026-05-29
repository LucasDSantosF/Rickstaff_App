package com.rickstaff.app.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Character {
    private int id;
    private String name;
    private String status;
    private String species;
    private String type;
    private String gender;
    private String image;
    private String url;
    private String created;

    @SerializedName("origin")
    private LocationInfo origin;

    @SerializedName("location")
    private LocationInfo location;

    @SerializedName("episode")
    private List<String> episodes;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getSpecies() { return species; }
    public String getType() { return type; }
    public String getGender() { return gender; }
    public String getImage() { return image; }
    public String getUrl() { return url; }
    public String getCreated() { return created; }
    public LocationInfo getOrigin() { return origin; }
    public LocationInfo getLocation() { return location; }
    public List<String> getEpisodes() { return episodes; }
    public int getEpisodeCount() { return episodes != null ? episodes.size() : 0; }

    public static class LocationInfo {
        private String name;
        public String getName() { return name; }
    }
}
