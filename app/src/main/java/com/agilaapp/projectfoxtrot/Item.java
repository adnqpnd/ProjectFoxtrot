package com.agilaapp.projectfoxtrot;


import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Item extends RealmObject {
    @PrimaryKey
    private long id;

    @Required
    private String label;

    private boolean placeSearch;

    private String placeName;

    private boolean done;

    private RealmList<Place> places;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isDone() {
        return done;
    }

    public void setStatus(boolean done) {
        this.done = done;
    }

    public boolean isPlaceSearch() {
        return placeSearch;
    }

    public void setPlaceSearch(boolean placeSearch) {
        this.placeSearch = placeSearch;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public RealmList<Place> getPlaces() {
        return places;
    }

    public void setPlaces(RealmList<Place> places) {
        this.places = places;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", placeSearch=" + placeSearch +
                ", placeName='" + placeName + '\'' +
                ", done=" + done +
                '}';
    }
}
