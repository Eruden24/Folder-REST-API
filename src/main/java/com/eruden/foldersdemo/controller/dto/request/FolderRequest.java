package com.eruden.foldersdemo.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Data for creating a folder or replacing all its fields.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderRequest {

    @Schema(description = "Name of the folder.", example = "Projects", maxLength = 50,
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max=50)
    private String subject;

    @Schema(description = "Id of the parent folder. Null or left out for a top-level folder.",
            example = "1", nullable = true)
    private Long parentId;
}
