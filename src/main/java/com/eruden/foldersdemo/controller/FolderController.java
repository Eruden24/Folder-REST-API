package com.eruden.foldersdemo.controller;

import com.eruden.foldersdemo.controller.dto.FolderCreateRequest;
import com.eruden.foldersdemo.controller.dto.FolderCreateResponse;
import com.eruden.foldersdemo.service.FolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/folder")
@RequiredArgsConstructor
public class FolderController {
    
    private final FolderService folderService;
    
    @PostMapping
    public FolderCreateResponse create(@RequestBody @Valid FolderCreateRequest request){
        return folderService.create(request);
    }
}
