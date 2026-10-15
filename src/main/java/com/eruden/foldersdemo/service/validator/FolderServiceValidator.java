package com.eruden.foldersdemo.service.validator;

import com.eruden.foldersdemo.controller.dto.request.FolderRequest;
import com.eruden.foldersdemo.exception.ValidationException;
import com.eruden.foldersdemo.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FolderServiceValidator {

    private final FolderRepository folderRepository;

    public void validateCreation(FolderRequest folderCreateRequest) {
        validateParentExists(folderCreateRequest.getParentId());
    }

    public void validateUpdate(FolderRequest folderCreateRequest, Long id) {
        validateParentExists(folderCreateRequest.getParentId());
        validateNotSelfParenting(folderCreateRequest.getParentId(), id);
    }

    private void validateParentExists(Long parentId) {
        if (parentId == null) return;
        folderRepository.findById(parentId).orElseThrow(
                () -> new ValidationException("Parent does not exist")
        );
    }

    private void validateNotSelfParenting(Long parentId, Long id) {
        if (id == null || parentId == null) return;
        if (parentId.equals(id)) {
            throw new ValidationException("A folder cannot be its own parent");
        }
    }
}
