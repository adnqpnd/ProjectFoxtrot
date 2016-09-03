package com.agilaapp.projectfoxtrot;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Checklist extends RealmObject {
    @PrimaryKey
    private long id;

    @Required
    private String name;

    private boolean enableSearchPlace;

    private RealmList<Item> items;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<Item> getItems() {
        return items;
    }

    public void setItems(RealmList<Item> items) {
        this.items = items;
    }

    public boolean isEnableSearchPlace() {
        return enableSearchPlace;
    }

    public void setEnableSearchPlace(boolean enableSearchPlace) {
        this.enableSearchPlace = enableSearchPlace;
    }

    @Override
    public String toString() {
        return "Checklist id "+id+",name " + name+",items " + items.get(1);
    }
}
