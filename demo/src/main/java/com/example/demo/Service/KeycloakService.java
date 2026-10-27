package com.example.demo.Service;

import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class KeycloakService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm:sisgr-realm}")
    private String sisgrRealm;

    public KeycloakService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public String createUserWithRole(String username, String email, String fullName, String defaultPassword, String realmRole) {
        RealmResource realmResource = keycloak.realm(sisgrRealm);
        UsersResource usersResource = realmResource.users();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(fullName);
        user.setEnabled(true);
        user.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(defaultPassword);
        credential.setTemporary(true);
        user.setCredentials(Collections.singletonList(credential));

        Response response = usersResource.create(user);

        if (response.getStatus() == 409) {
            throw new IllegalArgumentException("Ya existe una cuenta en Keycloak con ese usuario o correo.");
        }

        if (response.getStatus() != 201) {
            throw new RuntimeException("Error al crear usuario en Keycloak. Código HTTP: " + response.getStatus());
        }

        String userId = CreatedResponseUtil.getCreatedId(response);

        RoleRepresentation role = realmResource.roles().get(realmRole).toRepresentation();
        realmResource.users().get(userId).roles().realmLevel().add(List.of(role));

        return userId;
    }


    public void deleteUser(String keycloakUserId) {
        try {
            keycloak.realm(sisgrRealm).users().get(keycloakUserId).remove();
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar usuario en keycloak");
        }
    }

    public void deleteUserByUsername(String username) {
        try {
            RealmResource realmResource = keycloak.realm(sisgrRealm);
            List<UserRepresentation> users = realmResource.users().searchByUsername(username, true);
            if (!users.isEmpty()) {
                String userId = users.getFirst().getId();
                realmResource.users().get(userId).remove();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar usuario en keycloak por su username: " + username);
        }
    }
}