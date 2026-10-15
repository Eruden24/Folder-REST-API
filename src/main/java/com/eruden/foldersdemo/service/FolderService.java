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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

        return folderServiceMapper.toFolderResponse(folderRepository.save(folder));
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
                () -> new NotFoundException("Folder not found")
        );
    }

    public List<FolderAllFieldsResponse> getChildren(Long parentId) {

        List<Folder> children = (parentId == null)
                ? folderRepository.findAllByParentFolderIsNull()
                : folderRepository.findAllByParentFolderId(parentId);

        return folderServiceMapper.toFolderAllFieldsResponses(children);
    }
}
