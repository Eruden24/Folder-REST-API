package com.eruden.foldersdemo.service;

import com.eruden.foldersdemo.controller.dto.request.FolderRequest;
import com.eruden.foldersdemo.controller.dto.request.FolderSubjectRequest;
import com.eruden.foldersdemo.controller.dto.response.FolderAllFieldsResponse;
import com.eruden.foldersdemo.controller.dto.response.FolderResponse;
import com.eruden.foldersdemo.entity.Folder;
import com.eruden.foldersdemo.exception.NotFoundException;
import com.eruden.foldersdemo.repository.FolderRepository;
import com.eruden.foldersdemo.service.mapper.FolderServiceMapper;
import com.eruden.foldersdemo.service.validator.FolderServiceValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderServiceValidator folderServiceValidator;
    private final FolderRepository folderRepository;
    private final FolderServiceMapper folderServiceMapper;

    @Transactional
    public FolderResponse create(FolderRequest request) {
        folderServiceValidator.validateCreation(request);

        Folder saved = folderRepository.save(folderServiceMapper.toFolder(request));
        log.info("Created folder {} with parent {}", saved.getId(), request.getParentId());

        return folderServiceMapper.toFolderResponse(saved);
    }

    @Transactional
    public FolderResponse delete(Long id) {
        Folder folder = findFolder(id);

        folderRepository.delete(folder);
        log.info("Deleting folder {}", id);

        return folderServiceMapper.toFolderResponse(folder);
    }

    @Transactional(readOnly = true)
    public FolderAllFieldsResponse getById(Long id) {
        return folderServiceMapper.toFolderAllFieldsResponse(findFolder(id));
    }

    @Transactional
    public FolderAllFieldsResponse update(FolderRequest request, Long id) {
        folderServiceValidator.validateUpdate(request, id);

        Folder folder = findFolder(id);
        folderServiceMapper.updateFolder(request, folder);
        log.info("Updated folder {}: parent is now {}", id, request.getParentId());

        return folderServiceMapper.toFolderAllFieldsResponse(folderRepository.saveAndFlush(folder));
    }

    @Transactional
    public FolderAllFieldsResponse updateSubject(FolderSubjectRequest request, Long id) {
        Folder folder = findFolder(id);

        folderServiceMapper.updateSubject(request, folder);
        log.info("Renamed folder {}", id);

        return folderServiceMapper.toFolderAllFieldsResponse(folderRepository.saveAndFlush(folder));
    }

    @Transactional(readOnly = true)
    public Page<FolderAllFieldsResponse> getAll(Pageable pageable) {
        return folderRepository.findAll(pageable).map(folderServiceMapper::toFolderAllFieldsResponse);
    }

    @Transactional(readOnly = true)
    public List<FolderAllFieldsResponse> getTopLevel() {
        return folderServiceMapper.toFolderAllFieldsResponses(folderRepository.findAllByParentFolderIsNull());
    }

    @Transactional(readOnly = true)
    public List<FolderAllFieldsResponse> getChildren(Long parentId) {
        if (!folderRepository.existsById(parentId)) {
            throw new NotFoundException("Folder not found");
        }

        return folderServiceMapper.toFolderAllFieldsResponses(folderRepository.findAllByParentFolderId(parentId));
    }

    private Folder findFolder(Long id) {
        return folderRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Folder not found")
        );
    }
}
