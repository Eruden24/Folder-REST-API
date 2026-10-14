package com.eruden.foldersdemo.service;

import com.eruden.foldersdemo.controller.dto.FolderCreateRequest;
import com.eruden.foldersdemo.controller.dto.FolderCreateResponse;
import com.eruden.foldersdemo.entity.Folder;
import com.eruden.foldersdemo.exception.FolderNotFoundException;
import com.eruden.foldersdemo.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;

    public FolderCreateResponse create(FolderCreateRequest request) {

        Long parentId = request.getParentId();
        if (parentId != null && !folderRepository.existsById(parentId)) {
            throw new FolderNotFoundException(parentId);
        }

        Folder folder = new Folder();
        folder.setParentId(parentId);
        folder.setSubject(request.getSubject());
        folder = folderRepository.save(folder);

        return  FolderCreateResponse.builder()
                .id(folder.getId())
                .build();
    }
}
