import axios from 'axios';
import keycloak from './keycloak';

const apiClient = axios.create({
    baseURL: 'https://ipt-tingeso.duckdns.org/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

apiClient.interceptors.request.use(
    async (config) => {
        if (keycloak.authenticated) {
            try {
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