package com.eruden.foldersdemo.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "New subject for renaming a folder.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderSubjectRequest {

    @Schema(description = "New name of the folder.", example = "Archive", maxLength = 50,
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 50)
    private String subject;
}
