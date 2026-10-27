import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';
import keycloak from './services/keycloak';
import { ReactKeycloakProvider } from '@react-keycloak/web';

import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-icons/font/bootstrap-icons.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';

ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <ReactKeycloakProvider
            authClient={keycloak}
            initOptions={{
                onLoad: 'check-sso',
                checkLoginIframe: false,
                pkceMethod: 'S256',
            }}
            LoadingComponent={<div>Iniciando sesión...</div>}
        >
            <App />
        </ReactKeycloakProvider>
    </React.StrictMode>
);