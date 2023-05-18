package com.example.servingwebcontent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // This indicates that any properties not bound in this type should be
                                            // ignored.
public record HackerNewsUserRecord(
        String id, // The user's unique username. Case-sensitive. Required.
        Long created, // Creation date of the user, in Unix Time.
        Integer karma, // The user's karma.
        String about, // The user's optional self-description. HTML.
        List submitted // List of the user's stories, polls and comments.
) {
}
