package com.perinfinity.volunteering.opportunity.utils;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

public class URIUtils {
    public static URI entityWithLocation(String childPath) {
        return ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{childPath}")
                .buildAndExpand(childPath).toUri();
    }
}
