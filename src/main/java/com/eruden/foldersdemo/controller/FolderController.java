package com.eruden.foldersdemo.controller;

import com.eruden.foldersdemo.controller.dto.request.FolderRequest;
import com.eruden.foldersdemo.controller.dto.request.FolderSubjectRequest;
import com.eruden.foldersdemo.controller.dto.response.FolderAllFieldsResponse;
import com.eruden.foldersdemo.controller.dto.response.FolderResponse;
import com.eruden.foldersdemo.controller.swagger.FolderControllerSwagger;
import com.eruden.foldersdemo.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for managing folders, under {@code /api/folder}.
 * <p>
 * Folders form a tree: a folder with {@code parentId = null} is a top-level folder,
 * any other folder sits inside its parent.
 * <p>
 * The Swagger documentation and the validation rules ({@code @Valid}, {@code @Positive}) for every endpoint
 * are declared in {@link FolderControllerSwagger}. Spring and Bean Validation read them from there,
 * so they still apply to the methods below.
 * <p>
 * Errors are turned into responses by
 * {@link com.eruden.foldersdemo.exception.GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/api/folder")
@RequiredArgsConstructor
public class FolderController implements FolderControllerSwagger {

    private final FolderService folderService;

    /**
     * Creates a new folder.
     * <p>
     * {@code POST /api/folder}
     * <p>
     * Leave {@code parentId} out or send {@code null} to create a top-level folder.
     * Send the id of an existing folder to create a subfolder inside it.
     *
     * @param folderRequest the new folder's data:
     *                      {@code subject} (required, not blank, at most 50 characters) and
     *                      {@code parentId} (optional, id of an existing folder)
     * @return the id of the created folder
     * @throws com.eruden.foldersdemo.exception.ValidationException if {@code parentId} points to a folder
     *                                                             that does not exist (400)
     * @throws org.springframework.web.bind.MethodArgumentNotValidException if the body breaks a validation
     *                                                                     rule (400)
     */
    @Override
    @PostMapping
    public FolderResponse create(@RequestBody FolderRequest folderRequest) {
        return folderService.create(folderRequest);
    }

    /**
     * Deletes a folder.
     * <p>
     * {@code DELETE /api/folder/{id}}
     * <p>
     * A folder that still has subfolders cannot be deleted: the database blocks it
     * ({@code ON DELETE RESTRICT}). Delete or move its subfolders first.
     *
     * @param id the id of the folder to delete; must be positive
     * @return the id of the deleted folder
     * @throws com.eruden.foldersdemo.exception.NotFoundException if no folder has this id (404)
     * @throws org.springframework.dao.DataIntegrityViolationException if the folder still has subfolders
     *                                                                 (currently a 500, because no handler
     *                                                                 maps it yet)
     */
    @Override
    @DeleteMapping("/{id}")
    public FolderResponse delete(@PathVariable Long id) {
        return folderService.delete(id);
    }

    /**
     * Returns one folder with all its fields.
     * <p>
     * {@code GET /api/folder/{id}}
     *
     * @param id the id of the folder; must be positive
     * @return the folder's id, subject, parent id ({@code null} for a top-level folder),
     *         creation time and last update time
     * @throws com.eruden.foldersdemo.exception.NotFoundException if no folder has this id (404)
     */
    @Override
    @GetMapping("/{id}")
    public FolderAllFieldsResponse getById(@PathVariable Long id) {
        return folderService.getById(id);
    }

    /**
     * Replaces a folder's subject and parent.
     * <p>
     * {@code PUT /api/folder/{id}}
     * <p>
     * Both fields are always written. Sending {@code parentId: null} moves the folder to the top level,
     * so send the current parent id if you only want to change the subject, or use
     * {@link #updateSubject(Long, FolderSubjectRequest)} instead.
     *
     * @param id            the id of the folder to update; must be positive
     * @param folderRequest the new values: {@code subject} (required, not blank, at most 50 characters) and
     *                      {@code parentId} (optional, id of an existing folder other than this one)
     * @return the folder after the update, with the new update time
     * @throws com.eruden.foldersdemo.exception.NotFoundException   if no folder has this id (404)
     * @throws com.eruden.foldersdemo.exception.ValidationException if the parent folder does not exist,
     *                                                             or {@code parentId} equals {@code id} (400)
     * @throws org.springframework.web.bind.MethodArgumentNotValidException if the body breaks a validation
     *                                                                     rule (400)
     */
    @Override
    @PutMapping("/{id}")
    public FolderAllFieldsResponse update(@PathVariable Long id,
                                          @RequestBody FolderRequest folderRequest) {
        return folderService.update(folderRequest, id);
    }

    /**
     * Renames a folder without moving it.
     * <p>
     * {@code PATCH /api/folder/{id}}
     * <p>
     * Only the subject changes; the parent stays the same.
     *
     * @param id                   the id of the folder to rename; must be positive
     * @param folderSubjectRequest the new {@code subject} (required, not blank, at most 50 characters)
     * @return the folder after the update, with the new update time
     * @throws com.eruden.foldersdemo.exception.NotFoundException if no folder has this id (404)
     * @throws org.springframework.web.bind.MethodArgumentNotValidException if the body breaks a validation
     *                                                                     rule (400)
     */
    @Override
    @PatchMapping("/{id}")
    public FolderAllFieldsResponse updateSubject(@PathVariable Long id,
                                                 @RequestBody FolderSubjectRequest folderSubjectRequest) {
        return folderService.updateSubject(folderSubjectRequest, id);
    }

    /**
     * Returns every folder in one flat list.
     * <p>
     * {@code GET /api/folder/all}
     * <p>
     * The list contains top-level folders and subfolders together; use each item's {@code parentId}
     * to rebuild the tree. To browse one level at a time, use {@link #getChildren(Long)}.
     *
     * @return all folders, or an empty list if there are none
     */
    @Override
    @GetMapping("/all")
    public List<FolderAllFieldsResponse> getAll() {
        return folderService.getAll();
    }

    /**
     * Returns the folders directly inside a folder.
     * <p>
     * {@code GET /api/folder/children?parentId={parentId}}
     * <p>
     * Only one level is returned: the children, not the children's subfolders.
     * Leave {@code parentId} out to get the top-level folders.
     *
     * @param parentId the id of the parent folder, or {@code null} (parameter left out) for the top level
     * @return the direct children; an empty list if the folder has none or does not exist
     */
    @Override
    @GetMapping("/children")
    public List<FolderAllFieldsResponse> getChildren(@RequestParam(required = false) Long parentId) {
        return folderService.getChildren(parentId);
    }
}
