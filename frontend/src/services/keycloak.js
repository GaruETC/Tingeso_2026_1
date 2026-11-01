import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
    url: 'https://ipt-tingeso.duckdns.org',
    realm: 'sisgr-realm',
    clientId: 'sisgr-frontend',
});

export default keycloak;