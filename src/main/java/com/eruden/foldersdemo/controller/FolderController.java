package com.eruden.foldersdemo.controller;

import com.eruden.foldersdemo.controller.dto.request.FolderRequest;
import com.eruden.foldersdemo.controller.dto.request.FolderSubjectRequest;
import com.eruden.foldersdemo.controller.dto.response.FolderAllFieldsResponse;
import com.eruden.foldersdemo.controller.dto.response.FolderResponse;
import com.eruden.foldersdemo.controller.swagger.FolderControllerSwagger;
import com.eruden.foldersdemo.service.FolderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folder")
@RequiredArgsConstructor
public class FolderController implements FolderControllerSwagger {

    private final FolderService folderService;

    /**
     * Create folder endpoint.
     * @param folderCreateRequest parentId, subject
     * @return id of the created folder
     */
    @PostMapping
    @Override
    public FolderResponse create(@RequestBody @Valid FolderRequest folderCreateRequest) {
        return folderService.create(folderCreateRequest);
    }

    @DeleteMapping("/{id}")
    public FolderResponse delete(@PathVariable @Positive Long id) {
        return folderService.delete(id);
    }

    @GetMapping("/{id}")
    public FolderAllFieldsResponse getById(@PathVariable @Positive Long id){
        return folderService.getById(id);
    }

    @PutMapping("/{id}")
    public FolderAllFieldsResponse update(@PathVariable @Positive Long id,
                                          @RequestBody @Valid FolderRequest folderUpdateRequest) {
        return folderService.update(folderUpdateRequest, id);
    }

    @PatchMapping("/{id}")
    public FolderAllFieldsResponse updateSubject(@PathVariable @Positive Long id,
                                          @RequestBody @Valid FolderSubjectRequest folderUpdateSubjectRequest) {
        return folderService.updateSubject(folderUpdateSubjectRequest, id);
    }

    @GetMapping("/all")
    public List<FolderAllFieldsResponse> getAll() {
        return folderService.getAll();
    }

}
