package com.eruden.foldersdemo.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "Id of a created or deleted folder.")
@Data
@Builder
public class FolderResponse {

    @Schema(description = "Id of the folder.", example = "1")
    private Long id;
}
