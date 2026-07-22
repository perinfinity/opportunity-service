package com.perinfinity.volunteering.opportunity.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.perinfinity.volunteering.opportunity.dto.StoredImage;
import com.perinfinity.volunteering.opportunity.exception.InvalidImageException;
import com.perinfinity.volunteering.opportunity.exception.ResourceNotFoundException;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageStorageServiceTest {

    @Mock
    private GridFsTemplate gridFsTemplate;

    @InjectMocks
    private ImageStorageService imageStorageService;

    private MockMultipartFile validFile() {
        return new MockMultipartFile("file", "photo.png", "image/png", new byte[]{1, 2, 3, 4});
    }

    @Test
    void store_shouldReturnHexId_whenFileIsValid() {
        ObjectId objectId = new ObjectId();
        when(gridFsTemplate.store(any(InputStream.class), anyString(), anyString())).thenReturn(objectId);

        String id = imageStorageService.store(validFile());

        assertThat(id).isEqualTo(objectId.toHexString());
    }

    @Test
    void store_shouldThrow_whenFileIsEmpty() {
        MockMultipartFile empty = new MockMultipartFile("file", "photo.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> imageStorageService.store(empty))
                .isInstanceOf(InvalidImageException.class);
        verify(gridFsTemplate, never()).store(any(InputStream.class), anyString(), anyString());
    }

    @Test
    void store_shouldThrow_whenFileIsNull() {
        assertThatThrownBy(() -> imageStorageService.store(null))
                .isInstanceOf(InvalidImageException.class);
    }

    @Test
    void store_shouldThrow_whenFileIsTooLarge() {
        byte[] tooBig = new byte[(int) ImageStorageService.MAX_SIZE_BYTES + 1];
        MockMultipartFile file = new MockMultipartFile("file", "big.png", "image/png", tooBig);

        assertThatThrownBy(() -> imageStorageService.store(file))
                .isInstanceOf(InvalidImageException.class)
                .hasMessageContaining("5 Mo");
    }

    @Test
    void store_shouldThrow_whenContentTypeIsNotAllowed() {
        MockMultipartFile pdf = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[]{1});

        assertThatThrownBy(() -> imageStorageService.store(pdf))
                .isInstanceOf(InvalidImageException.class)
                .hasMessageContaining("Format");
    }

    @Test
    void load_shouldReturnStoredImage_whenFileExists() {
        ObjectId objectId = new ObjectId();
        GridFSFile gridFsFile = new GridFSFile(new BsonObjectId(objectId), "photo.png", 4L, 255,
                new Date(), new Document("_contentType", "image/png"));
        GridFsResource resource = mock(GridFsResource.class);
        when(gridFsTemplate.findOne(any(Query.class))).thenReturn(gridFsFile);
        when(gridFsTemplate.getResource(gridFsFile)).thenReturn(resource);
        when(resource.getContentType()).thenReturn("image/png");

        StoredImage image = imageStorageService.load(objectId.toHexString());

        assertThat(image.resource()).isSameAs(resource);
        assertThat(image.contentType()).isEqualTo("image/png");
    }

    @Test
    void load_shouldFallbackToOctetStream_whenContentTypeIsMissing() {
        ObjectId objectId = new ObjectId();
        GridFSFile gridFsFile = new GridFSFile(new BsonObjectId(objectId), "photo", 4L, 255,
                new Date(), new Document());
        GridFsResource resource = mock(GridFsResource.class);
        when(gridFsTemplate.findOne(any(Query.class))).thenReturn(gridFsFile);
        when(gridFsTemplate.getResource(gridFsFile)).thenReturn(resource);
        when(resource.getContentType()).thenThrow(new IllegalStateException("no content type"));

        StoredImage image = imageStorageService.load(objectId.toHexString());

        assertThat(image.contentType()).isEqualTo("application/octet-stream");
    }

    @Test
    void load_shouldThrowNotFound_whenFileDoesNotExist() {
        when(gridFsTemplate.findOne(any(Query.class))).thenReturn(null);

        assertThatThrownBy(() -> imageStorageService.load(new ObjectId().toHexString()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void load_shouldThrowNotFound_whenIdIsNotAValidObjectId() {
        assertThatThrownBy(() -> imageStorageService.load("not-an-object-id"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
