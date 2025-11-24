package com.markovic.javazadanie.fx.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ActionLogDto {

    // logId -> id
    @JsonProperty("logId")
    private Long id;

    // action -> actionType
    @JsonProperty("action")
    private String actionType;

    // details -> description
    @JsonProperty("details")
    private String description;

    // timestamp -> createdAt (nechajme radšej String, nech to nerieši JavaTime)
    @JsonProperty("timestamp")
    private String createdAt;

    // user ostáva, JSON má "user"
    private SimpleUserDto user;

    public ActionLogDto() {
    }

    // --- getters & setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public SimpleUserDto getUser() {
        return user;
    }

    public void setUser(SimpleUserDto user) {
        this.user = user;
    }
}
