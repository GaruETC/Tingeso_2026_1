import React from 'react';
import { useKeycloak } from '@react-keycloak/web';
import { Navigate } from 'react-router-dom';

export const PrivateRoute = ({ children, requiredRole }) => {
    const { keycloak, initialized } = useKeycloak();

    if (!initialized) {
        return <div>Verificando credenciales...</div>;
    }

    if (!keycloak.authenticated) {
        keycloak.login();
        return null;
    }

    if (requiredRole && !keycloak.hasRealmRole(requiredRole)) {
        return <Navigate to="/unauthorized" replace />;
    }

    return children;
};