package com.eruden.foldersdemo.controller.swagger;

import com.eruden.foldersdemo.controller.dto.request.FolderRequest;
import com.eruden.foldersdemo.controller.dto.response.FolderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name="Folders", description = "Api for managing folders")
public interface FolderControllerSwagger {

    @Operation(
            summary = "Create folder",
            description = "Creates a new folder and returns the new folder's id."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                        description = "Folder created successfully.",
                        content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = FolderResponse.class)) ),
            @ApiResponse(responseCode = "400",
                        description = "Validation error - invalid fields."),
            @ApiResponse(responseCode = "404",
                        description = "Referenced entity not found."),
            @ApiResponse(responseCode = "500",
                        description = "Internal server error.",
                        content = @Content)
    })
    FolderResponse create(@RequestBody @Valid FolderRequest folderRequest) ;
}
