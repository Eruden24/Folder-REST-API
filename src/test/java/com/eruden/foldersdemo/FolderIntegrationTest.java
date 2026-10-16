package com.eruden.foldersdemo;

import com.eruden.foldersdemo.repository.FolderRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLIntegrityConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs the whole application — controller, service, mapper, Flyway, Hibernate — against a real MySQL
 * started in Docker by {@link TestcontainersConfiguration}. Nothing is mocked.
 * <p>
 * These are the only tests that would catch a wrong migration, a wrong mapping to the table,
 * or the database rejecting a change.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class FolderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Every test starts with an empty table. The parent links are cleared first, because
     * {@code ON DELETE RESTRICT} would block deleting a parent before its children.
     */
    @BeforeEach
    void emptyTable() {
        jdbcTemplate.update("UPDATE folder SET parent_id = NULL");
        jdbcTemplate.update("DELETE FROM folder");
    }

    @Test
    void createdFolder_canBeReadBack_withTimestampsFilledIn() throws Exception {
        long id = createFolder("Docs", null);

        mockMvc.perform(get("/api/folder/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("Docs"))
                .andExpect(jsonPath("$.parentId").value(nullValue()))
                .andExpect(jsonPath("$.created").isNotEmpty())
                .andExpect(jsonPath("$.updated").isNotEmpty());
    }

    @Test
    void childFolder_isListedUnderItsParent_andNotAtTopLevel() throws Exception {
        long rootId = createFolder("Root", null);
        long childId = createFolder("Child", rootId);

        mockMvc.perform(get("/api/folder/{id}", childId))
                .andExpect(jsonPath("$.parentId").value(rootId));

        mockMvc.perform(get("/api/folder/children").param("parentId", String.valueOf(rootId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].subject").value("Child"));

        mockMvc.perform(get("/api/folder/children"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].subject").value("Root"));
    }

    @Test
    void create_returns400_whenParentDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/folder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject": "Orphan", "parentId": 999999}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Parent does not exist"));

        assertThat(folderRepository.count()).isZero();
    }

    @Test
    void update_changesTheSameRow_andCreatesNoDuplicate() throws Exception {
        long rootId = createFolder("Root", null);
        long folderId = createFolder("Old name", null);

        mockMvc.perform(put("/api/folder/{id}", folderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject": "New name", "parentId": %d}
                                """.formatted(rootId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(folderId))
                .andExpect(jsonPath("$.subject").value("New name"))
                .andExpect(jsonPath("$.parentId").value(rootId));

        assertThat(folderRepository.count()).isEqualTo(2);
    }

    @Test
    void update_withNullParent_movesFolderToTopLevel() throws Exception {
        long rootId = createFolder("Root", null);
        long childId = createFolder("Child", rootId);

        mockMvc.perform(put("/api/folder/{id}", childId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject": "Child", "parentId": null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parentId").value(nullValue()));
    }

    @Test
    void update_returns400_whenFolderIsItsOwnParent() throws Exception {
        long id = createFolder("Docs", null);

        mockMvc.perform(put("/api/folder/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject": "Docs", "parentId": %d}
                                """.formatted(id)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("A folder cannot be its own parent"));
    }

    @Test
    void updateSubject_renames_andKeepsTheParent() throws Exception {
        long rootId = createFolder("Root", null);
        long childId = createFolder("Child", rootId);

        mockMvc.perform(patch("/api/folder/{id}", childId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject": "Renamed"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("Renamed"))
                .andExpect(jsonPath("$.parentId").value(rootId));
    }

    @Test
    void delete_removesTheFolder() throws Exception {
        long id = createFolder("Docs", null);

        mockMvc.perform(delete("/api/folder/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/folder/{id}", id))
                .andExpect(status().isNotFound());
    }

    /**
     * Documents current behaviour: the database refuses ({@code ON DELETE RESTRICT}), and because
     * no handler maps that error yet, it escapes the controller (a 500 in the running app).
     * Once a {@code DataIntegrityViolationException} handler is added, change this test to expect 409.
     */
    @Test
    void delete_isRejectedByTheDatabase_whenFolderHasChildren() throws Exception {
        long rootId = createFolder("Root", null);
        createFolder("Child", rootId);

        assertThatThrownBy(() -> mockMvc.perform(delete("/api/folder/{id}", rootId)))
                .hasRootCauseInstanceOf(SQLIntegrityConstraintViolationException.class);

        assertThat(folderRepository.count()).isEqualTo(2);
    }

    /** Creates a folder through the API and returns its id. */
    private long createFolder(String subject, Long parentId) throws Exception {
        String body = """
                {"subject": "%s", "parentId": %s}
                """.formatted(subject, parentId);

        String response = mockMvc.perform(post("/api/folder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }
}
