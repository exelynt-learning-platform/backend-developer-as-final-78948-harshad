package com.booking.dto;

import com.booking.model.Resource;

public class ResourceResponse {

    private Long id;
    private String name;
    private String description;
    private String type;
    private String location;
    private Integer capacity;
    private Boolean available;

    public ResourceResponse() {}

    public ResourceResponse(Resource resource) {
        this.id = resource.getId();
        this.name = resource.getName();
        this.description = resource.getDescription();
        this.type = resource.getType();
        this.location = resource.getLocation();
        this.capacity = resource.getCapacity();
        this.available = resource.getAvailable();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
