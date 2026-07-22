package com.perinfinity.volunteering.opportunity.service;

import com.perinfinity.volunteering.opportunity.dto.StoredImage;
import com.perinfinity.volunteering.opportunity.exception.InvalidImageException;
import com.perinfinity.volunteering.opportunity.exception.ResourceNotFoundException;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ImageStorageService implements IImageStorageService {

    static final long MAX_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp",
            MediaType.IMAGE_GIF_VALUE
    );

    private final GridFsTemplate gridFsTemplate;

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageException("Le fichier est vide");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new InvalidImageException("L'image dépasse la taille maximale de 5 Mo");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidImageException("Format d'image non supporté (JPEG, PNG, WebP ou GIF attendu)");
        }
        try (InputStream in = file.getInputStream()) {
            ObjectId id = gridFsTemplate.store(in, file.getOriginalFilename(), contentType);
            return id.toHexString();
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de lire le fichier envoyé", e);
        }
    }

    @Override
    public StoredImage load(String id) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Image non trouvée : " + id);
        }

        GridFSFile file = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(objectId)));
        if (file == null) {
            throw new ResourceNotFoundException("Image non trouvée : " + id);
        }

        GridFsResource resource = gridFsTemplate.getResource(file);
        String contentType;
        try {
            contentType = resource.getContentType();
        } catch (RuntimeException e) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return new StoredImage(resource, contentType);
    }
}
