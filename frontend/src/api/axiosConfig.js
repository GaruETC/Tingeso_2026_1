import axios from 'axios';

const api = axios.create({
    baseURL: 'https://ipt-tingeso.duckdns.org/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

export default api;