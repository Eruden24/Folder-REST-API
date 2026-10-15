package com.eruden.foldersdemo.service.helper;

import com.eruden.foldersdemo.entity.Folder;
import com.eruden.foldersdemo.exception.ValidationException;
import com.eruden.foldersdemo.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FolderServiceMapperHelper {

    private final FolderRepository folderRepository;

    @Named("parentIdToParentFolder")
    public Folder parentIdToParentFolder(Long parentId) {
        if (parentId == null) {
            return null;
        }

        return folderRepository.findById(parentId).orElseThrow(
                () -> new ValidationException("Parent id not found!")
        );
    }
}
