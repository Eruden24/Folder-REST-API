package com.eruden.foldersdemo.folder;

import com.eruden.foldersdemo.common.exception.ConflictException;
import com.eruden.foldersdemo.config.SecurityConfig;
import com.eruden.foldersdemo.folder.dto.CreateFolderRequest;
import com.eruden.foldersdemo.folder.dto.FolderResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests: only the controller, JSON (de)serialization, validation and the exception handler are loaded.
 * The service is mocked, so these tests check the HTTP contract, not the business logic.
 */
@WebMvcTest(FolderController.class)
@Import(SecurityConfig.class)
class FolderControllerTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FolderService folderService;

    @Test
    void create_returns201WithLocationHeader() throws Exception {
        when(folderService.create(new CreateFolderRequest("Documents", null)))
                .thenReturn(new FolderResponse(7L, "Documents", null, NOW, NOW));

        mockMvc.perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject": "Documents"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/folders/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.subject").value("Documents"));
    }

    @Test
    void create_withBlankSubject_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.subject").exists());

        verifyNoInteractions(folderService);
    }

    @Test
    void create_withMalformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{oops"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_whenMissing_returns404ProblemDetail() throws Exception {
        when(folderService.getById(42L)).thenThrow(new FolderNotFoundException(42L));

        mockMvc.perform(get("/api/folders/42"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.detail").value("Folder not found with id 42"));
    }

    @Test
    void getById_withNonNumericId_returns400() throws Exception {
        mockMvc.perform(get("/api/folders/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_returnsPage() throws Exception {
        var page = new PageImpl<>(List.of(new FolderResponse(1L, "A", null, NOW, NOW)), PageRequest.of(0, 20), 1);
        when(folderService.getAll(any())).thenReturn(page);

        mockMvc.perform(get("/api/folders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].subject").value("A"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/folders/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenNotEmpty_returns409() throws Exception {
        doThrow(new ConflictException("Folder 1 is not empty")).when(folderService).delete(1L);

        mockMvc.perform(delete("/api/folders/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Folder 1 is not empty"));
    }
}
