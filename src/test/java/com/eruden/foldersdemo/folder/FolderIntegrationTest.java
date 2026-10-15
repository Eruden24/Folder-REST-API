package com.eruden.foldersdemo.folder;

import com.eruden.foldersdemo.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack tests: real controller, service, repository, Flyway migrations and a real MySQL (Testcontainers).
 * Slower than the other tests, so they cover whole flows rather than every edge case.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class FolderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void folderLifecycle() throws Exception {
        long root = createFolder("Root", null);
        long child = createFolder("Child", root);

        mockMvc.perform(get("/api/folders/{id}", child))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("Child"))
                .andExpect(jsonPath("$.parentId").value(root))
                .andExpect(jsonPath("$.createdAt").exists());

        mockMvc.perform(get("/api/folders/{id}/children", root))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(child));

        mockMvc.perform(put("/api/folders/{id}", child)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject": "Renamed", "parentId": null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("Renamed"))
                .andExpect(jsonPath("$.parentId").doesNotExist());

        mockMvc.perform(delete("/api/folders/{id}", child))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/folders/{id}", child))
                .andExpect(status().isNotFound());
    }

    @Test
    void cannotMoveFolderIntoItsOwnSubfolder() throws Exception {
        long parent = createFolder("Parent", null);
        long child = createFolder("Child", parent);

        mockMvc.perform(put("/api/folders/{id}", parent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject": "Parent", "parentId": %d}
                                """.formatted(child)))
                .andExpect(status().isConflict());
    }

    @Test
    void cannotDeleteFolderWithSubfolders() throws Exception {
        long parent = createFolder("Parent", null);
        createFolder("Child", parent);

        mockMvc.perform(delete("/api/folders/{id}", parent))
                .andExpect(status().isConflict());
    }

    @Test
    void cannotCreateFolderWithUnknownParent() throws Exception {
        mockMvc.perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject": "Orphan", "parentId": 999999}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void sortingByUnknownProperty_returns400() throws Exception {
        mockMvc.perform(get("/api/folders").param("sort", "doesNotExist"))
                .andExpect(status().isBadRequest());
    }

    private long createFolder(String subject, Long parentId) throws Exception {
        String body = mockMvc.perform(post("/api/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject": "%s", "parentId": %s}
                                """.formatted(subject, parentId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.<Number>read(body, "$.id").longValue();
    }
}
