# Comandos usados para desplegar

## Azure Server Connection

### Connect via SSH to the Azure machine
ssh -i ~/Documentos/vm-tingeso_key.pem azureuser@68.211.177.142

### Enter the project folder
cd Tingeso_2026_1


## Update and Deployment

### Download the latest versions of your images
docker compose pull frontend backend

### Recreate and start a specific container
docker compose up -d --force-recreate frontend

### Start all services
docker compose ps

### Force recreation of all services
docker compose up -d --force-recreate


## Monitoring

### View the current status of all containers
docker compose ps

### View logs of a specific container
docker compose logs -f backend


## Shutdown

### Stop and destroy all containers
docker compose down

### Stop containers without destroying them
docker compose stop