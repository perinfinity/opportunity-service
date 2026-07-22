package com.perinfinity.volunteering.opportunity.service;

import com.perinfinity.volunteering.opportunity.dto.StoredImage;
import org.springframework.web.multipart.MultipartFile;

public interface IImageStorageService {

    String store(MultipartFile file);

    StoredImage load(String id);
}
