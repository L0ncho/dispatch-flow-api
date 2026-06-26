# dispatch-flow-api

API para gestión de guías de despacho con arquitectura Cloud Native. Incluye generación automática de PDF, persistencia en Oracle ATP, almacenamiento definitivo en AWS S3, y protección perimetral mediante AWS API Gateway y Azure AD B2C.

## 🛡️ Seguridad e Identidad (IDaaS)

El sistema delega la identidad y la exposición a servicios administrados en la nube:
- **API Gateway (AWS):** Todos los endpoints están ocultos detrás de una integración Proxy (`ANY /{proxy+}`). Ninguna petición anónima llega al servidor EC2.
- **Azure AD B2C:** Actúa como proveedor de identidad. El API Gateway valida la firma del token JWT antes de permitir el enrutamiento.
- **Roles (RBAC):** El backend valida el claim `extension_Rol` inyectado por Azure:
  - `ROLE_DESCARGA`: Permite únicamente el endpoint de descarga de PDFs.
  - `ROLE_ADMIN`: Acceso total al resto de operaciones (CRUD y búsqueda).

## Requisitos

- Java 21
- Maven 3.9+ (incluido vía `./mvnw`)
- Docker (para desarrollo local con LocalStack)
- Wallet Oracle Autonomous DB (solo para `./run-prod` o Docker prod)
- Tenant de Azure AD B2C configurado con el atributo `Rol` (para despliegue en la nube)

## Ejecutar en local (H2 + LocalStack)

```bash
chmod +x run-local run-prod run-docker scripts/init-localstack.sh scripts/setup-oracle-wallet.sh scripts/setup-efs-mount.sh scripts/prepare-wallet-for-docker.sh
./run-local
```

Este script levanta LocalStack, crea el bucket `dispatch-flow-local` y arranca la API con perfil `local` usando **H2 in-memory** (consola H2 disponible).

## Oracle en producción (`./run-prod`)

En local contra Oracle real (perfil `prod`):

```bash
# 1. Copiar wallet zip a la raíz del proyecto
cp /ruta/a/Wallet_DISPATCHFLOWDB.zip .

# 2. Configurar credenciales
cp .env.example .env
# Editar .env: SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD, AWS_*

# 3. Arrancar
./run-prod
```

El script descomprime el wallet en `Wallet_DISPATCHFLOWDB/`, carga `.env`, configura `TNS_ADMIN` y conecta a Oracle ATP vía alias `dispatchflowdb_high`.

| Variable | Descripción |
|----------|-------------|
| `SPRING_DATASOURCE_URL` | Default: `jdbc:oracle:thin:@dispatchflowdb_high` |
| `SPRING_DATASOURCE_USERNAME` | Usuario Oracle |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña Oracle |
| `TNS_ADMIN` | Default: `./Wallet_DISPATCHFLOWDB` |
| `AWS_REGION` | Región S3 |
| `S3_BUCKET_NAME` | Bucket prod (`dispatch-flow-prod`) |

Archivos sensibles del wallet están en `.gitignore` (`ewallet.*`, `cwallet.sso`, `*.jks`). No versionar `.env`.

Despliegue automatizado en EC2: [docs/guia-despliegue-ec2.md](docs/guia-despliegue-ec2.md).

## Ejecutar con Docker (prod local)

Simula producción en contenedor (`dispatch-flow-api:local`) con Oracle ATP, S3 real y EFS simulado en `./tmp/efs-docker`:

```bash
# 1. Copiar wallet zip a la raíz del proyecto
cp /ruta/a/Wallet_DISPATCHFLOWDB.zip .

# 2. Configurar credenciales
cp .env.example .env
# Editar .env: SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD, AWS_*

# 3. Build + run
./run-docker
```

El script prepara `wallet/` para la imagen, construye `dispatch-flow-api:local`, levanta el contenedor `dispatch-flow-api` en el puerto 8080 y monta `./tmp/efs-docker` en `/app/efs`.

| Artefacto | Nombre |
|-----------|--------|
| Imagen local | `dispatch-flow-api:local` |
| Imagen Docker Hub | `{DOCKERHUB_USERNAME}/dispatch-flow-api:latest` |
| Contenedor | `dispatch-flow-api` |

Despliegue completo vía Docker Hub: [docs/guia-despliegue-ec2.md](docs/guia-despliegue-ec2.md).

## Ejecutar tests

```bash
./mvnw test
```

Los tests E2E usan almacenamiento S3 en memoria (`dispatch.storage.s3.enabled=false`) para CI estable.

## Almacenamiento EFS (local / producción)

| Variable | Local (default) | EC2 Linux (prod) |
|----------|-----------------|------------------|
| `EFS_BASE_PATH` | `./tmp/efs` | `/app/efs` (dentro del contenedor) |
| Mount en host | No aplica | `/mnt/dispatch-flow-efs` (AWS EFS) |

Al **crear** o **actualizar** una guía, el sistema:

1. Genera un PDF con Apache PDFBox
2. Lo guarda temporalmente en `{EFS_BASE_PATH}/guides/{fecha}/{transportista-slug}/guide-{id}.pdf`
3. Sube el mismo PDF a S3 con la misma clave relativa
4. Persiste `efsPath`, `s3Key` y el status `UPLOADED_TO_S3`

Si falla EFS o S3 durante POST/PUT, la operación completa falla.

### EFS en EC2 (deploy con Docker Hub)

Antes del primer deploy, monta tu EFS en el EC2 siguiendo **[docs/configuracion-efs-ec2.md](docs/configuracion-efs-ec2.md)** (comandos manuales; no hace falta clonar el repo en el servidor).

El workflow enlaza `-v /mnt/dispatch-flow-efs:/app/efs`. Resumen del despliegue: [docs/guia-despliegue-ec2.md](docs/guia-despliegue-ec2.md).

### EFS local (`./run-prod`)

En `.env` puedes usar `EFS_BASE_PATH=./tmp/efs` sin montar AWS EFS. Opcionalmente existe `scripts/setup-efs-mount.sh` (solo referencia local; ver disclaimer en el script).

## Almacenamiento S3

| Variable | Local (LocalStack) | Producción |
|----------|-------------------|------------|
| `DISPATCH_STORAGE_S3_ENABLED` | `true` | `true` |
| `S3_BUCKET_NAME` | `dispatch-flow-local` | `dispatch-flow-prod` |
| `AWS_S3_ENDPOINT` | `http://localhost:4566` | *(vacío — AWS real)* |
| `AWS_ACCESS_KEY_ID` | `test` | IAM / env |
| `AWS_SECRET_ACCESS_KEY` | `test` | IAM / env |
| `AWS_SESSION_TOKEN` | *(opcional)* | *(si aplica)* |
| `AWS_REGION` | `us-east-1` | región del bucket |

Estructura de claves S3 (igual que rutas EFS relativas):

```
guides/{fecha}/{transportista-slug}/guide-{id}.pdf
```

Verificar objetos en local:

```bash
awslocal s3 ls s3://dispatch-flow-local/guides/ --recursive
```

**Delete:** borra el objeto S3 (si existe) y marca la guía como `DELETED` en BD.

**Download:** lee desde S3 si hay `s3Key`; fallback a EFS para datos legacy.

## Consola H2

Con la aplicación en ejecución:

| Campo | Valor |
|-------|-------|
| URL | http://localhost:8080/h2-console |
| JDBC URL | `jdbc:h2:mem:dispatchflow` |
| Usuario | `sa` |
| Contraseña | *(vacía)* |

## Endpoints Protegidos

Todas las peticiones en producción deben incluir el header `Authorization: Bearer <Token>`.

| Método | Ruta | Descripción | Rol Requerido |
|--------|------|-------------|---------------|
| POST | `/api/guides` | Crear guía, PDF en EFS y S3 | `ROLE_ADMIN` |
| GET | `/api/guides/{id}` | Obtener por ID | `ROLE_ADMIN` |
| GET | `/api/guides/{id}/download` | Descargar PDF (S3 preferido) | `ROLE_DESCARGA` o `ADMIN` |
| GET | `/api/guides` | Listar guías activas | `ROLE_ADMIN` |
| PUT | `/api/guides/{id}` | Actualizar guía y regenerar PDF + S3 | `ROLE_ADMIN` |
| DELETE | `/api/guides/{id}` | Borrar objeto S3 + eliminación lógica | `ROLE_ADMIN` |
| GET | `/api/guides/search?carrierName=&date=` | Buscar por transportista y fecha | `ROLE_ADMIN` |

La eliminación es lógica (`status = DELETED`); las guías eliminadas no aparecen en listados ni búsquedas.

## Ejemplo Postman: crear guía (Producción)

**POST** `https://<TU-API-GATEWAY-URL>/api/guides`  
**Headers:** `Authorization: Bearer <TOKEN_ADMIN>`

```json
{
  "carrierName": "Transportes Rápidos",
  "recipientName": "María González",
  "originAddress": "Av. Providencia 1234, Santiago",
  "destinationAddress": "Calle Huérfanos 567, Santiago",
  "description": "Electrónicos",
  "dispatchDate": "2026-06-02",
  "ownerEmail": "responsable@empresa.cl"
}
```

Respuesta esperada: `201 Created` con `id`, `guideNumber`, `efsPath`, `s3Key` y `status: UPLOADED_TO_S3`.

## Ejemplo Postman: descargar PDF

**GET** `https://<TU-API-GATEWAY-URL>/api/guides/{id}/download`  
**Headers:** `Authorization: Bearer <TOKEN_DESCARGA>`

Respuesta: `200 OK`, `Content-Type: application/pdf`, archivo adjunto `guide-{id}.pdf`.

## Ejemplo Postman: búsqueda

**GET** `https://<TU-API-GATEWAY-URL>/api/guides/search?carrierName=Transportes%20Rápidos&date=2026-06-02`  
**Headers:** `Authorization: Bearer <TOKEN_ADMIN>`

## Arquitectura

El proyecto sigue arquitectura hexagonal (inside-out):

- **Dominio**: entidades, value objects, `GuidePdfPathBuilder`, repositorio
- **Aplicación**: casos de uso, `GuidePdfEfsStorage`, `GuidePdfS3Storage`, puertos PDF/EFS/S3
- **Infraestructura**: JPA (H2 local / Oracle prod), PDFBox, `LocalEfsStorageAdapter`, `S3ObjectStorageAdapter`, controladores REST, Spring Security (JWT)

## Health check (Público)

```bash
curl https://<TU-API-GATEWAY-URL>/actuator/health
# o localmente: curl http://localhost:8080/actuator/health
```