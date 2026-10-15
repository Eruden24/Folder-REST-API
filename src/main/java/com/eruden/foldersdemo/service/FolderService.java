package com.eruden.foldersdemo.service;

import com.eruden.foldersdemo.controller.dto.request.FolderRequest;
import com.eruden.foldersdemo.controller.dto.request.FolderSubjectRequest;
import com.eruden.foldersdemo.controller.dto.response.FolderAllFieldsResponse;
import com.eruden.foldersdemo.controller.dto.response.FolderResponse;
import com.eruden.foldersdemo.entity.Folder;
import com.eruden.foldersdemo.exception.ValidationException;
import com.eruden.foldersdemo.repository.FolderRepository;
import com.eruden.foldersdemo.service.mapper.FolderServiceMapper;
import com.eruden.foldersdemo.service.validator.FolderServiceValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderServiceValidator folderServiceValidator;
    private final FolderRepository folderRepository;
    private final FolderServiceMapper folderServiceMapper;

    @Transactional
    public FolderResponse create(FolderRequest request) {
        folderServiceValidator.validateCreation(request);

        Folder folder = folderServiceMapper.toFolder(request);

        folder.setParentFolder(findParent(request.getParentId()));
        Folder saved = folderRepository.save(folder);

        return folderServiceMapper.toFolderResponse(saved);
    }

    @Transactional
    public FolderResponse delete(Long id) {
        Folder folder = findFolder(id);

        folderRepository.delete(folder);

        return folderServiceMapper.toFolderResponse(folder);
    }

    public FolderAllFieldsResponse getById(Long id) {

        Folder folder = findFolder(id);

        return folderServiceMapper.toFolderAllFieldsResponse(folder);
    }

    @Transactional
    public FolderAllFieldsResponse update(FolderRequest request, Long id) {
        folderServiceValidator.validateUpdate(request, id);

        Folder folder = findFolder(id);
        folderServiceMapper.updateFolder(request, folder);
        folder.setParentFolder(findParent(request.getParentId()));

        return folderServiceMapper.toFolderAllFieldsResponse(folderRepository.saveAndFlush(folder));
    }

    @Transactional
    public FolderAllFieldsResponse updateSubject(FolderSubjectRequest request, Long id) {
        Folder folder = findFolder(id);

        folderServiceMapper.updateSubject(request, folder);

        return folderServiceMapper.toFolderAllFieldsResponse(folderRepository.saveAndFlush(folder));
    }

    public List<FolderAllFieldsResponse> getAll() {
        List<Folder> folders = folderRepository.findAll();

        return folderServiceMapper.toFolderAllFieldsResponses(folders);
    }

    private Folder findFolder(Long id) {
        return folderRepository.findById(id).orElseThrow(
                () -> new ValidationException("Folder not found")
        );
    }

    private Folder findParent(Long id) {
        if (id == null) {
            return null;
        }

        return folderRepository.findById(id).orElseThrow(
                () -> new ValidationException("Parent folder id not found!")
        );
    }
}
