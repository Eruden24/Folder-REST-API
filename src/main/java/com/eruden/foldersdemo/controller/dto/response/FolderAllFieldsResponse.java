package com.eruden.foldersdemo.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Schema(description = "All fields of a folder.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderAllFieldsResponse {

    @Schema(description = "Id of the folder.", example = "2")
    private Long id;

    @Schema(description = "Id of the parent folder. Null for a top-level folder.", example = "1", nullable = true)
    private Long parentId;

    @Schema(description = "Name of the folder.", example = "Projects")
    private String subject;

    @Schema(description = "When the folder was created.", example = "2026-10-15T09:30:00.000+00:00")
    private Timestamp created;

    @Schema(description = "When the folder was last changed.", example = "2026-10-15T10:45:00.000+00:00")
    private Timestamp updated;
}
