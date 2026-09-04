package com.booking.service;

import com.booking.dto.ResourceRequest;
import com.booking.dto.ResourceResponse;
import com.booking.exception.ResourceNotFoundException;
import com.booking.model.Resource;
import com.booking.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ResourceService resourceService;

    private Resource resource;

    @BeforeEach
    void setUp() {
        resource = new Resource("Conference Room 1", "Main hall", "ROOM", "Building A", 10, true);
        resource.setId(1L);
    }

    @Test
    void testGetAllResources() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Resource> page = new PageImpl<>(List.of(resource));
        when(resourceRepository.findAll(pageable)).thenReturn(page);

        Page<ResourceResponse> result = resourceService.getAllResources(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Conference Room 1", result.getContent().get(0).getName());
    }

    @Test
    void testGetAllResourcesList() {
        when(resourceRepository.findAll()).thenReturn(List.of(resource));

        List<ResourceResponse> list = resourceService.getAllResourcesList();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Conference Room 1", list.get(0).getName());
    }

    @Test
    void testGetResourceById_Success() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        ResourceResponse response = resourceService.getResourceById(1L);

        assertNotNull(response);
        assertEquals("Conference Room 1", response.getName());
    }

    @Test
    void testGetResourceById_NotFound() {
        when(resourceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> resourceService.getResourceById(99L));
    }

    @Test
    void testCreateResource() {
        ResourceRequest request = new ResourceRequest("New Room", "Desc", "ROOM", "Loc", 5, true);
        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> {
            Resource saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        ResourceResponse response = resourceService.createResource(request);

        assertNotNull(response);
        assertEquals("New Room", response.getName());
    }

    @Test
    void testUpdateResource_Success() {
        ResourceRequest request = new ResourceRequest("Updated Room", "New Desc", "ROOM", "New Loc", 15, false);
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));
        when(resourceRepository.save(any(Resource.class))).thenReturn(resource);

        ResourceResponse response = resourceService.updateResource(1L, request);

        assertNotNull(response);
        assertEquals("Updated Room", response.getName());
    }

    @Test
    void testUpdateResource_NotFound() {
        ResourceRequest request = new ResourceRequest("Updated Room", "Desc", "ROOM", "Loc", 5, true);
        when(resourceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> resourceService.updateResource(99L, request));
    }

    @Test
    void testDeleteResource_Success() {
        when(resourceRepository.existsById(1L)).thenReturn(true);
        doNothing().when(resourceRepository).deleteById(1L);

        assertDoesNotThrow(() -> resourceService.deleteResource(1L));
        verify(resourceRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteResource_NotFound() {
        when(resourceRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> resourceService.deleteResource(99L));
    }
}
