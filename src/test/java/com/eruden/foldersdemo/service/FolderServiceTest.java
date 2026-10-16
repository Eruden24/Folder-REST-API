package com.eruden.foldersdemo.service;

import com.eruden.foldersdemo.controller.dto.request.FolderRequest;
import com.eruden.foldersdemo.controller.dto.request.FolderSubjectRequest;
import com.eruden.foldersdemo.controller.dto.response.FolderAllFieldsResponse;
import com.eruden.foldersdemo.controller.dto.response.FolderResponse;
import com.eruden.foldersdemo.entity.Folder;
import com.eruden.foldersdemo.exception.NotFoundException;
import com.eruden.foldersdemo.exception.ValidationException;
import com.eruden.foldersdemo.repository.FolderRepository;
import com.eruden.foldersdemo.service.mapper.FolderServiceMapper;
import com.eruden.foldersdemo.service.validator.FolderServiceValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link FolderService}. All three dependencies are mocks, so these tests check only
 * the service's own decisions: what it calls, in which order, and what it does when something is missing.
 */
@ExtendWith(MockitoExtension.class)
class FolderServiceTest {

    @Mock
    private FolderServiceValidator folderServiceValidator;

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private FolderServiceMapper folderServiceMapper;

    @InjectMocks
    private FolderService folderService;

    // ---------- create ----------

    @Test
    void create_validatesThenSaves_andReturnsNewId() {
        FolderRequest request = request("Docs", null);
        Folder mapped = new Folder();
        Folder saved = folder(1L, "Docs");
        when(folderServiceMapper.toFolder(request)).thenReturn(mapped);
        when(folderRepository.save(mapped)).thenReturn(saved);
        when(folderServiceMapper.toFolderResponse(saved)).thenReturn(FolderResponse.builder().id(1L).build());

        FolderResponse result = folderService.create(request);

        assertThat(result.getId()).isEqualTo(1L);

        // Validation must happen before anything is saved
        InOrder inOrder = inOrder(folderServiceValidator, folderRepository);
        inOrder.verify(folderServiceValidator).validateCreation(request);
        inOrder.verify(folderRepository).save(mapped);
    }

    @Test
    void create_savesNothing_whenValidationFails() {
        FolderRequest request = request("Docs", 99L);
        doThrow(new ValidationException("Parent does not exist"))
                .when(folderServiceValidator).validateCreation(request);

        assertThatThrownBy(() -> folderService.create(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Parent does not exist");

        verifyNoInteractions(folderRepository, folderServiceMapper);
    }

    // ---------- getById ----------

    @Test
    void getById_returnsMappedFolder_whenFolderExists() {
        Folder existing = folder(5L, "Docs");
        FolderAllFieldsResponse response = FolderAllFieldsResponse.builder().id(5L).subject("Docs").build();
        when(folderRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(folderServiceMapper.toFolderAllFieldsResponse(existing)).thenReturn(response);

        assertThat(folderService.getById(5L)).isSameAs(response);
    }

    @Test
    void getById_throwsNotFound_whenFolderDoesNotExist() {
        when(folderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> folderService.getById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Folder not found");
    }

    // ---------- update ----------

    @Test
    void update_changesTheExistingFolder_insteadOfCreatingANewOne() {
        FolderRequest request = request("Renamed", 3L);
        Folder existing = folder(5L, "Old name");
        when(folderRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(folderRepository.saveAndFlush(existing)).thenReturn(existing);
        when(folderServiceMapper.toFolderAllFieldsResponse(existing))
                .thenReturn(FolderAllFieldsResponse.builder().id(5L).build());

        FolderAllFieldsResponse result = folderService.update(request, 5L);

        assertThat(result.getId()).isEqualTo(5L);
        verify(folderServiceValidator).validateUpdate(request, 5L);
        // The loaded folder is changed in place and saved...
        verify(folderServiceMapper).updateFolder(request, existing);
        verify(folderRepository).saveAndFlush(existing);
        // ...and no new Folder is ever built (that was the duplicate-row bug)
        verify(folderServiceMapper, never()).toFolder(any());
    }

    @Test
    void update_throwsNotFound_andSavesNothing_whenFolderDoesNotExist() {
        FolderRequest request = request("Renamed", null);
        when(folderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> folderService.update(request, 99L))
                .isInstanceOf(NotFoundException.class);

        verify(folderRepository, never()).saveAndFlush(any());
    }

    @Test
    void update_savesNothing_whenValidationFails() {
        FolderRequest request = request("Docs", 5L);
        doThrow(new ValidationException("A folder cannot be its own parent"))
                .when(folderServiceValidator).validateUpdate(request, 5L);

        assertThatThrownBy(() -> folderService.update(request, 5L))
                .isInstanceOf(ValidationException.class);

        verifyNoInteractions(folderRepository, folderServiceMapper);
    }

    // ---------- updateSubject ----------

    @Test
    void updateSubject_changesTheExistingFolder() {
        FolderSubjectRequest request = FolderSubjectRequest.builder().subject("Renamed").build();
        Folder existing = folder(5L, "Old name");
        when(folderRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(folderRepository.saveAndFlush(existing)).thenReturn(existing);
        when(folderServiceMapper.toFolderAllFieldsResponse(existing))
                .thenReturn(FolderAllFieldsResponse.builder().id(5L).build());

        folderService.updateSubject(request, 5L);

        verify(folderServiceMapper).updateSubject(request, existing);
        verify(folderRepository).saveAndFlush(existing);
    }

    @Test
    void updateSubject_throwsNotFound_whenFolderDoesNotExist() {
        FolderSubjectRequest request = FolderSubjectRequest.builder().subject("Renamed").build();
        when(folderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> folderService.updateSubject(request, 99L))
                .isInstanceOf(NotFoundException.class);

        verify(folderRepository, never()).saveAndFlush(any());
    }

    // ---------- delete ----------

    @Test
    void delete_deletesTheFolder_andReturnsItsId() {
        Folder existing = folder(5L, "Docs");
        when(folderRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(folderServiceMapper.toFolderResponse(existing)).thenReturn(FolderResponse.builder().id(5L).build());

        FolderResponse result = folderService.delete(5L);

        assertThat(result.getId()).isEqualTo(5L);
        verify(folderRepository).delete(existing);
    }

    @Test
    void delete_throwsNotFound_andDeletesNothing_whenFolderDoesNotExist() {
        when(folderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> folderService.delete(99L))
                .isInstanceOf(NotFoundException.class);

        verify(folderRepository, never()).delete(any());
    }

    // ---------- getAll / getChildren ----------

    @Test
    void getAll_returnsEveryFolderMapped() {
        List<Folder> folders = List.of(folder(1L, "A"), folder(2L, "B"));
        List<FolderAllFieldsResponse> responses = List.of(
                FolderAllFieldsResponse.builder().id(1L).build(),
                FolderAllFieldsResponse.builder().id(2L).build());
        when(folderRepository.findAll()).thenReturn(folders);
        when(folderServiceMapper.toFolderAllFieldsResponses(folders)).thenReturn(responses);

        assertThat(folderService.getAll()).isSameAs(responses);
    }

    @Test
    void getChildren_returnsTopLevelFolders_whenParentIdIsNull() {
        folderService.getChildren(null);

        verify(folderRepository).findAllByParentFolderIsNull();
        verify(folderRepository, never()).findAllByParentFolderId(any());
    }

    @Test
    void getChildren_returnsChildrenOfTheGivenFolder_whenParentIdIsGiven() {
        folderService.getChildren(5L);

        verify(folderRepository).findAllByParentFolderId(5L);
        verify(folderRepository, never()).findAllByParentFolderIsNull();
    }

    // ---------- helpers ----------

    private static FolderRequest request(String subject, Long parentId) {
        return FolderRequest.builder().subject(subject).parentId(parentId).build();
    }

    private static Folder folder(Long id, String subject) {
        Folder folder = new Folder();
        folder.setId(id);
        folder.setSubject(subject);
        return folder;
    }
}
