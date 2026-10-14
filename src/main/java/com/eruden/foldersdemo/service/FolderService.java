package com.eruden.foldersdemo.service;

import com.eruden.foldersdemo.controller.dto.FolderCreateRequest;
import com.eruden.foldersdemo.controller.dto.FolderCreateResponse;
import com.eruden.foldersdemo.entity.Folder;
import com.eruden.foldersdemo.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;

    public FolderCreateResponse create(FolderCreateRequest request) {
        Folder folder = new Folder();
        folder.setParentId(request.getParentId());
        folder.setSubject(request.getSubject());
        folder = folderRepository.save(folder);

        return  FolderCreateResponse.builder()
                .id(folder.getId())
                .build();
    }
}
