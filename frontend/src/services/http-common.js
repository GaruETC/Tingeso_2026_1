import axios from 'axios';
import keycloak from './keycloak';

const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

apiClient.interceptors.request.use(
    async (config) => {
        if (keycloak.authenticated) {
            try {
                // Refresca el token si va a expirar en los próximos 30 segundos
                await keycloak.updateToken(30);
                config.headers.Authorization = `Bearer ${keycloak.token}`;
            } catch (error) {
                console.error('Error al refrescar el token:', error);
                keycloak.login();
            }
        }
        return config;
    },
    (error) => Promise.reject(error)
);

export default apiClient;