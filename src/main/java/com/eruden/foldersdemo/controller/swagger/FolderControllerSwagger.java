package com.eruden.foldersdemo.controller.swagger;

import com.eruden.foldersdemo.controller.dto.request.FolderRequest;
import com.eruden.foldersdemo.controller.dto.request.FolderSubjectRequest;
import com.eruden.foldersdemo.controller.dto.response.FolderAllFieldsResponse;
import com.eruden.foldersdemo.controller.dto.response.FolderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * OpenAPI (Swagger) contract for {@code /api/folder}.
 * <p>
 * Validation annotations ({@code @Valid}, {@code @Positive}) live here and not on the controller:
 * Bean Validation forbids adding parameter constraints on a method that implements an interface method.
 */
@Tag(name = "Folders", description = "Create, read, update and delete folders and browse the folder tree.")
public interface FolderControllerSwagger {

    @Operation(
            summary = "Create a folder",
            description = "Creates a new folder. Send `parentId: null` (or leave it out) to create a top-level folder, "
                    + "or the id of an existing folder to create a subfolder. Returns the id of the new folder."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Folder created.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FolderResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid body (blank subject or longer than 50 characters), "
                            + "or the given parent folder does not exist.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.", content = @Content)
    })
    FolderResponse create(@RequestBody @Valid FolderRequest folderRequest);

    @Operation(
            summary = "Delete a folder",
            description = "Deletes the folder with the given id and returns its id. "
                    + "A folder that still has subfolders cannot be deleted."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Folder deleted.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FolderResponse.class))),
            @ApiResponse(responseCode = "400", description = "The id is not a positive number.", content = @Content),
            @ApiResponse(responseCode = "404", description = "No folder with this id.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500",
                    description = "The folder still has subfolders (blocked by the database), or an unexpected server error.",
                    content = @Content)
    })
    FolderResponse delete(
            @Parameter(description = "Id of the folder to delete.", example = "1")
            @PathVariable @Positive Long id);

    @Operation(
            summary = "Get a folder",
            description = "Returns every field of the folder with the given id."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Folder found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FolderAllFieldsResponse.class))),
            @ApiResponse(responseCode = "400", description = "The id is not a positive number.", content = @Content),
            @ApiResponse(responseCode = "404", description = "No folder with this id.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.", content = @Content)
    })
    FolderAllFieldsResponse getById(
            @Parameter(description = "Id of the folder.", example = "1")
            @PathVariable @Positive Long id);

    @Operation(
            summary = "Replace a folder",
            description = "Replaces the subject and the parent of the folder. Both fields are always written: "
                    + "sending `parentId: null` moves the folder to the top level. "
                    + "A folder cannot be its own parent."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Folder updated. Returns the folder after the update.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FolderAllFieldsResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid body or id, the parent folder does not exist, "
                            + "or the folder was set as its own parent.",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No folder with this id.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.", content = @Content)
    })
    FolderAllFieldsResponse update(
            @Parameter(description = "Id of the folder to update.", example = "1")
            @PathVariable @Positive Long id,
            @RequestBody @Valid FolderRequest folderRequest);

    @Operation(
            summary = "Rename a folder",
            description = "Changes only the subject of the folder. The parent stays the same."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subject updated. Returns the folder after the update.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FolderAllFieldsResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid body (blank subject or longer than 50 characters) or invalid id.",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No folder with this id.",
                    content = @Content(mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.", content = @Content)
    })
    FolderAllFieldsResponse updateSubject(
            @Parameter(description = "Id of the folder to rename.", example = "1")
            @PathVariable @Positive Long id,
            @RequestBody @Valid FolderSubjectRequest folderSubjectRequest);

    @Operation(
            summary = "List all folders",
            description = "Returns every folder, top-level folders and subfolders, in one flat list. "
                    + "Use `parentId` in each item to rebuild the tree."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All folders (empty list if there are none).",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = FolderAllFieldsResponse.class)))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.", content = @Content)
    })
    List<FolderAllFieldsResponse> getAll();

    @Operation(
            summary = "List the direct children of a folder",
            description = "Returns the folders directly inside the given folder (not their subfolders). "
                    + "Leave `parentId` out to get the top-level folders. "
                    + "An id that does not exist returns an empty list."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Child folders (empty list if there are none).",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = FolderAllFieldsResponse.class)))),
            @ApiResponse(responseCode = "400", description = "`parentId` is not a number.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.", content = @Content)
    })
    List<FolderAllFieldsResponse> getChildren(
            @Parameter(description = "Id of the parent folder. Leave out to get the top-level folders.", example = "1")
            @RequestParam(required = false) Long parentId);
}
