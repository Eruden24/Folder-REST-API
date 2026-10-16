package com.eruden.foldersdemo.service.validator;

import com.eruden.foldersdemo.controller.dto.request.FolderRequest;
import com.eruden.foldersdemo.entity.Folder;
import com.eruden.foldersdemo.exception.ValidationException;
import com.eruden.foldersdemo.repository.FolderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FolderServiceValidatorTest {

    @Mock
    private FolderRepository folderRepository;

    @InjectMocks
    private FolderServiceValidator validator;

    @Test
    void validateCreation_passes_whenParentIdIsNull() {
        // Arrange
        FolderRequest request = FolderRequest.builder().subject("Docs").parentId(null).build();

        // Act + Assert
        assertThatCode(() -> validator.validateCreation(request)).doesNotThrowAnyException();
        verifyNoInteractions(folderRepository);
    }

    @Test
    void validateCreation_passes_whenParentExists() {
        FolderRequest request = FolderRequest.builder().subject("Docs").parentId(1L).build();
        when(folderRepository.findById(1L)).thenReturn(Optional.of(new Folder()));

        assertThatCode(() -> validator.validateCreation(request)).doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_passes_whenParentIsDifferentFolder() {
        FolderRequest request = FolderRequest.builder().subject("Docs").parentId(3L).build();

        when(folderRepository.findById(3L)).thenReturn(Optional.of(new Folder()));
        assertThatCode(() -> validator.validateUpdate(request, 5L)).doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_passes_whenParentIdIsNull() {
        FolderRequest request = FolderRequest.builder().subject("Docs").parentId(null).build();

        assertThatCode(() -> validator.validateUpdate(request, 5L)).doesNotThrowAnyException();
        verifyNoInteractions(folderRepository);
    }
    @Test
    void validateCreation_throws_whenParentIdIsNotPresent() {
        FolderRequest request = FolderRequest.builder().subject("Docs").parentId(99L).build();

        when(folderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> validator.validateCreation(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Parent does not exist");
    }

    @Test
    void validateUpdate_throws_whenFolderIsItsOwnParent() {
        FolderRequest request = FolderRequest.builder().subject("Docs").parentId(5L).build();

        when(folderRepository.findById(5L)).thenReturn(Optional.of(new Folder()));

        assertThatThrownBy(() -> validator.validateUpdate(request, 5L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("A folder cannot be its own parent");
    }

    @Test
    void validateUpdate_throws_whenNewParentIsASubfolder() {
        // Tree: 5 → 6 → 7. Moving 5 into 7 would make 5 its own grandchild.
        Folder folder5 = folder(5L, null);
        Folder folder6 = folder(6L, folder5);
        Folder folder7 = folder(7L, folder6);
        FolderRequest request = FolderRequest.builder().subject("Docs").parentId(7L).build();
        when(folderRepository.findById(7L)).thenReturn(Optional.of(folder7));

        assertThatThrownBy(() -> validator.validateUpdate(request, 5L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("A folder cannot be moved into one of its own subfolders");
    }

    @Test
    void validateUpdate_passes_whenNewParentIsInAnotherBranch() {
        // Tree: 1 → 2 and 1 → 5. Moving 5 into 2 is fine: 2 is a sibling, not a subfolder.
        Folder folder1 = folder(1L, null);
        Folder folder2 = folder(2L, folder1);
        FolderRequest request = FolderRequest.builder().subject("Docs").parentId(2L).build();
        when(folderRepository.findById(2L)).thenReturn(Optional.of(folder2));

        assertThatCode(() -> validator.validateUpdate(request, 5L)).doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_stops_whenExistingDataAlreadyHasALoop() {
        // 8 and 9 point at each other (saved before the loop check existed). The walk must end.
        Folder folder8 = folder(8L, null);
        Folder folder9 = folder(9L, folder8);
        folder8.setParentFolder(folder9);
        FolderRequest request = FolderRequest.builder().subject("Docs").parentId(9L).build();
        when(folderRepository.findById(9L)).thenReturn(Optional.of(folder9));

        assertThatCode(() -> validator.validateUpdate(request, 5L)).doesNotThrowAnyException();
    }

    private static Folder folder(Long id, Folder parent) {
        Folder folder = new Folder();
        ReflectionTestUtils.setField(folder, "id", id);
        folder.setParentFolder(parent);
        return folder;
    }
}
