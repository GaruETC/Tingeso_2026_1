package com.example.demo.Service;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakServiceTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @Mock
    private RolesResource rolesResource;

    @Mock
    private RoleResource roleResource;

    @Mock
    private RoleMappingResource roleMappingResource;

    @Mock
    private RoleScopeResource roleScopeResource;

    @Mock
    private Response response;

    @InjectMocks
    private KeycloakService keycloakService;

    @BeforeEach
    void setUp() throws Exception {
        Field realmField = KeycloakService.class.getDeclaredField("sisgrRealm");
        realmField.setAccessible(true);
        realmField.set(keycloakService, "sisgr-realm");
    }

    @Test
    void whenCreateUserWithRole_thenReturnUserId() {
        //Given
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);
        when(response.getLocation()).thenReturn(URI.create("http://localhost/auth/admin/realms/sisgr-realm/users/new-uuid-123"));

        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("STUDENT")).thenReturn(roleResource);

        RoleRepresentation roleRepresentation = new RoleRepresentation();
        when(roleResource.toRepresentation()).thenReturn(roleRepresentation);

        when(usersResource.get("new-uuid-123")).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

        //When
        String result = keycloakService.createUserWithRole(
                "mgomez", "maria.gomez@usach.cl", "Maria Gomez", "pass123", "STUDENT"
        );

        //Then
        assertThat(result).isEqualTo("new-uuid-123");
        verify(roleScopeResource).add(anyList());
    }

    @Test
    void whenCreateUserWithRoleConflict_thenThrowException() {
        //Given
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(409);

        //When / Then
        assertThatThrownBy(() -> keycloakService.createUserWithRole(
                "mgomez", "maria.gomez@usach.cl", "Maria Gomez", "pass123", "STUDENT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe una cuenta en Keycloak");
    }

    @Test
    void whenDeleteUser_thenVerifyRemoval() {
        //Given
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get("uuid-123")).thenReturn(userResource);

        //When
        keycloakService.deleteUser("uuid-123");

        //Then
        verify(userResource).remove();
    }

    @Test
    void whenDeleteUserByUsernameFound_thenVerifyRemoval() {
        //Given
        UserRepresentation user = new UserRepresentation();
        user.setId("uuid-456");

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.searchByUsername("mgomez", true)).thenReturn(List.of(user));
        when(usersResource.get("uuid-456")).thenReturn(userResource);

        //When
        keycloakService.deleteUserByUsername("mgomez");

        //Then
        verify(userResource).remove();
    }
}