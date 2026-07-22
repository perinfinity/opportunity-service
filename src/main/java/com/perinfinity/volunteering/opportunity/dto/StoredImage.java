package com.perinfinity.volunteering.opportunity.dto;

import org.springframework.core.io.Resource;

public record StoredImage(Resource resource, String contentType) {
}
