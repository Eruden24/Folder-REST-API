package com.eruden.foldersdemo.service.mapper;

import com.eruden.foldersdemo.controller.dto.request.FolderRequest;
import com.eruden.foldersdemo.controller.dto.request.FolderSubjectRequest;
import com.eruden.foldersdemo.controller.dto.response.FolderAllFieldsResponse;
import com.eruden.foldersdemo.controller.dto.response.FolderResponse;
import com.eruden.foldersdemo.entity.Folder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel="spring")
public interface FolderServiceMapper {

    // Request -> Entity
    @Mapping(target="id", ignore = true)
    @Mapping(target="parentFolder", ignore = true)
    @Mapping(target="createdAt", ignore=true)
    @Mapping(target="updatedAt", ignore=true)
    @Mapping(target="subject", source="subject")
    Folder toFolder(FolderRequest request);

    @Mapping(target="id",  ignore = true)
    @Mapping(target="parentFolder",  ignore = true)
    @Mapping(target="createdAt",  ignore = true)
    @Mapping(target="updatedAt",  ignore = true)
    @Mapping(target="subject", source="subject")
    void updateFolder(FolderRequest request, @MappingTarget Folder folder);

    @Mapping(target="id",  ignore = true)
    @Mapping(target="parentFolder",  ignore = true)
    @Mapping(target="createdAt",  ignore = true)
    @Mapping(target="updatedAt",  ignore = true)
    @Mapping(target="subject", source="subject")
    void updateSubject(FolderSubjectRequest request, @MappingTarget Folder folder);

    // Entity -> only id
    @Mapping(target="id", source="id")
    FolderResponse toFolderResponse(Folder folder);

    // Entity -> Full response
    @Mapping(target="id", source="id")
    @Mapping(target="parentId", source="parentFolder.id")
    @Mapping(target="created", source="createdAt")
    @Mapping(target="updated", source="updatedAt")
    @Mapping(target="subject", source="subject")
    FolderAllFieldsResponse toFolderAllFieldsResponse(Folder folder);

    List<FolderAllFieldsResponse> toFolderAllFieldsResponses(List<Folder> folders);
}
