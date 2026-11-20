package com.caldova.production.service;

import com.caldova.production.repository.ProductionRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProductionServiceTest {
    private final ProductionRepository repository = mock(ProductionRepository.class);
    private final ProductionService service = new ProductionService(repository);

    @Test
    void filtersAreValidatedBeforeAnyRead() {
        assertThrows(IllegalArgumentException.class, () -> service.lines("XX", ""));
        assertThrows(IllegalArgumentException.class, () -> service.lines("NB", "running"));
        assertThrows(IllegalArgumentException.class, () -> service.lines("NB' OR 1=1", ""));
        verifyNoInteractions(repository);
    }

    @Test
    void allAndCombinedFiltersUseTheSameRepositoryPath() {
        service.lines("", "");
        service.lines("NB", "STOPPED");
        verify(repository).lines("", "");
        verify(repository).lines("NB", "STOPPED");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void nonpositiveAndMissingLinesDoNotBecomeSuccessfulEmptyDetails() {
        assertThrows(IllegalArgumentException.class, () -> service.detail(0));
        when(repository.line(9999)).thenReturn(java.util.Optional.empty());
        assertThrows(LineNotFoundException.class, () -> service.detail(9999));
        verify(repository).line(9999);
        verifyNoMoreInteractions(repository);
    }
}
