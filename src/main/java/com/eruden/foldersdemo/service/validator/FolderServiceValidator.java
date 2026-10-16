package com.eruden.foldersdemo.service.validator;

import com.eruden.foldersdemo.controller.dto.request.FolderRequest;
import com.eruden.foldersdemo.entity.Folder;
import com.eruden.foldersdemo.exception.ValidationException;
import com.eruden.foldersdemo.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class FolderServiceValidator {

    private final FolderRepository folderRepository;

    public void validateCreation(FolderRequest request) {
        findParent(request.getParentId());
    }

    public void validateUpdate(FolderRequest request, Long id) {
        Folder parent = findParent(request.getParentId());
        if (parent == null) return;

        validateNotSelfParenting(request.getParentId(), id);
        validateNotMovedIntoOwnSubfolder(parent, id);
    }

    /** Returns the parent folder, or {@code null} for a top-level folder. */
    private Folder findParent(Long parentId) {
        if (parentId == null) return null;
        return folderRepository.findById(parentId).orElseThrow(
                () -> new ValidationException("Parent does not exist")
        );
    }

    private void validateNotSelfParenting(Long parentId, Long id) {
        if (parentId.equals(id)) {
            throw new ValidationException("A folder cannot be its own parent");
        }
    }

    /**
     * Walks up from the new parent to the top. If folder {@code id} is on that path, the new parent is
     * one of its own subfolders, and the move would create a loop (A inside B inside A).
     */
    private void validateNotMovedIntoOwnSubfolder(Folder parent, Long id) {
        Set<Long> visited = new HashSet<>();
        for (Folder current = parent.getParentFolder(); current != null; current = current.getParentFolder()) {
            if (id.equals(current.getId())) {
                throw new ValidationException("A folder cannot be moved into one of its own subfolders");
            }
            // Stops on loops that were saved before this check existed, instead of walking forever
            if (!visited.add(current.getId())) return;
        }
    }
}
