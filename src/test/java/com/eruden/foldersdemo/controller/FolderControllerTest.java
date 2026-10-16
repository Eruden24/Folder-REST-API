package com.eruden.foldersdemo.controller;

import com.eruden.foldersdemo.config.SecurityConfig;
import com.eruden.foldersdemo.controller.dto.request.FolderRequest;
import com.eruden.foldersdemo.controller.dto.response.FolderAllFieldsResponse;
import com.eruden.foldersdemo.controller.dto.response.FolderResponse;
import com.eruden.foldersdemo.exception.NotFoundException;
import com.eruden.foldersdemo.exception.ValidationException;
import com.eruden.foldersdemo.service.FolderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the HTTP layer only: URLs, JSON in and out, validation and status codes.
 * <p>
 * {@code @WebMvcTest} starts just the web part of Spring (controllers, {@code @RestControllerAdvice},
 * validation) without a database. The service is replaced by a mock with {@code @MockitoBean}.
 * {@code SecurityConfig} is imported so the real security rules apply.
 */
@WebMvcTest(FolderController.class)
@Import(SecurityConfig.class)
class FolderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FolderService folderService;

    // ---------- POST /api/folder ----------

    @Test
    void create_returns200WithNewId_whenBodyIsValid() throws Exception {
        when(folderService.create(any())).thenReturn(FolderResponse.builder().id(1L).build());

        mockMvc.perform(post("/api/folder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject": "Docs", "parentId": null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        // The JSON was turned into the right request object
        verify(folderService).create(FolderRequest.builder().subject("Docs").parentId(null).build());
    }

    @Test
    void create_returns400WithFieldError_whenSubjectIsBlank() throws Exception {
        mockMvc.perform(post("/api/folder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid body"))
                .andExpect(jsonPath("$.errors.subject").exists());

        // Invalid requests never reach the service
        verifyNoInteractions(folderService);
    }

    @Test
    void create_returns400_whenSubjectIsLongerThan50Characters() throws Exception {
        String body = """
                {"subject": "%s"}
                """.formatted("a".repeat(51));

        mockMvc.perform(post("/api/folder").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.subject").exists());

        verifyNoInteractions(folderService);
    }

    @Test
    void create_returns400WithMessage_whenParentDoesNotExist() throws Exception {
        when(folderService.create(any())).thenThrow(new ValidationException("Parent does not exist"));

        mockMvc.perform(post("/api/folder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject": "Docs", "parentId": 99}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Parent does not exist"));
    }

    // ---------- GET /api/folder/{id} ----------

    @Test
    void getById_returns200WithAllFields_whenFolderExists() throws Exception {
        when(folderService.getById(5L)).thenReturn(
                FolderAllFieldsResponse.builder().id(5L).parentId(1L).subject("Docs").build());

        mockMvc.perform(get("/api/folder/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.parentId").value(1))
                .andExpect(jsonPath("$.subject").value("Docs"));
    }

    @Test
    void getById_returns404_whenFolderDoesNotExist() throws Exception {
        when(folderService.getById(99L)).thenThrow(new NotFoundException("Folder not found"));

        mockMvc.perform(get("/api/folder/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Folder not found"));
    }

    @Test
    void getById_returns400_whenIdIsNotPositive() throws Exception {
        mockMvc.perform(get("/api/folder/0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(folderService);
    }

    // ---------- PUT /api/folder/{id} ----------

    @Test
    void update_returns200WithUpdatedFolder() throws Exception {
        when(folderService.update(any(), eq(5L))).thenReturn(
                FolderAllFieldsResponse.builder().id(5L).parentId(3L).subject("Renamed").build());

        mockMvc.perform(put("/api/folder/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject": "Renamed", "parentId": 3}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("Renamed"))
                .andExpect(jsonPath("$.parentId").value(3));

        verify(folderService).update(FolderRequest.builder().subject("Renamed").parentId(3L).build(), 5L);
    }

    @Test
    void update_returns400_whenFolderIsItsOwnParent() throws Exception {
        when(folderService.update(any(), eq(5L)))
                .thenThrow(new ValidationException("A folder cannot be its own parent"));

        mockMvc.perform(put("/api/folder/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject": "Docs", "parentId": 5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("A folder cannot be its own parent"));
    }

    @Test
    void update_returns400_whenSubjectIsBlank() throws Exception {
        mockMvc.perform(put("/api/folder/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject": ""}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(folderService);
    }

    // ---------- PATCH /api/folder/{id} ----------

    @Test
    void updateSubject_returns200WithRenamedFolder() throws Exception {
        when(folderService.updateSubject(any(), eq(5L))).thenReturn(
                FolderAllFieldsResponse.builder().id(5L).subject("Renamed").build());

        mockMvc.perform(patch("/api/folder/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject": "Renamed"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("Renamed"));
    }

    @Test
    void updateSubject_returns404_whenFolderDoesNotExist() throws Exception {
        when(folderService.updateSubject(any(), eq(99L))).thenThrow(new NotFoundException("Folder not found"));

        mockMvc.perform(patch("/api/folder/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject": "Renamed"}
                                """))
                .andExpect(status().isNotFound());
    }

    // ---------- DELETE /api/folder/{id} ----------

    @Test
    void delete_returns200WithDeletedId() throws Exception {
        when(folderService.delete(5L)).thenReturn(FolderResponse.builder().id(5L).build());

        mockMvc.perform(delete("/api/folder/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void delete_returns404_whenFolderDoesNotExist() throws Exception {
        when(folderService.delete(99L)).thenThrow(new NotFoundException("Folder not found"));

        mockMvc.perform(delete("/api/folder/99"))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /api/folder/all and /children ----------

    @Test
    void getAll_returns200WithList() throws Exception {
        when(folderService.getAll()).thenReturn(List.of(
                FolderAllFieldsResponse.builder().id(1L).subject("A").build(),
                FolderAllFieldsResponse.builder().id(2L).subject("B").build()));

        mockMvc.perform(get("/api/folder/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].subject").value("B"));
    }

    @Test
    void getChildren_passesNull_whenParentIdIsLeftOut() throws Exception {
        mockMvc.perform(get("/api/folder/children"))
                .andExpect(status().isOk());

        verify(folderService).getChildren(null);
    }

    @Test
    void getChildren_passesTheId_whenParentIdIsGiven() throws Exception {
        mockMvc.perform(get("/api/folder/children").param("parentId", "5"))
                .andExpect(status().isOk());

        verify(folderService).getChildren(5L);
    }

    @Test
    void getChildren_returns400_whenParentIdIsNotANumber() throws Exception {
        mockMvc.perform(get("/api/folder/children").param("parentId", "abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(folderService);
    }
}
