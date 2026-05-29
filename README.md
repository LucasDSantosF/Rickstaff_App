# RickStaff App

Aplicativo Android nativo desenvolvido como teste técnico. Integra a API pública do Rick and Morty com um backend próprio em Grails/Groovy e banco de dados MySQL local.

---

## Tecnologias utilizadas

**Android**
- Java (linguagem principal)
- MVVM (arquitetura)
- OkHttp3 (requisições HTTP)
- Gson (parsing de JSON)
- Glide (carregamento de imagens)
- RecyclerView + DiffUtil
- LiveData + ViewModel
- Activity Result API (câmera)
- FileProvider (captura segura de imagem)
- SharedPreferences (sessão local + persistência de imagens por usuário)
- Testes unitários nos ViewModels

**Backend**
- Grails 5.3.4 / Groovy
- MySQL 8.0 (persistência local)
- API REST com endpoints de autenticação e CRUD

---

## Pré-requisitos

- Android Studio Hedgehog ou superior
- JDK 11 (via SDKMAN recomendado)
- MySQL 8.0 rodando localmente
- Grails 5.3.4 (via SDKMAN)
- Git Bash (Windows) para rodar SDKMAN

---

## Configuração do Backend

### 1. Instalar dependências

```bash
# Instalar SDKMAN (Git Bash no Windows)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Instalar Java 11 e Grails
sdk install java 11.0.23-tem
sdk use java 11.0.23-tem
sdk install grails 5.3.4
```

### 2. Criar banco de dados MySQL

```sql
CREATE DATABASE rickstaffdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rickstaffdb;

-- Inserir usuário administrador padrão (após subir o backend pela primeira vez)
INSERT INTO usuario (version, nome, email, senha)
VALUES (0, 'Administrador', 'admin@empresa.com', '123456');
```

### 3. Configurar credenciais do MySQL

Abra `backend/grails-app/conf/application.yml` e ajuste:

```yaml
dataSource:
    username: root
    password: SUA_SENHA_AQUI
```

### 4. Subir o backend

```bash
cd backend
grails run-app
```

Backend disponível em: `http://localhost:8080`

### 5. Verificar endpoints

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@empresa.com","senha":"123456"}'

# Registrar novo usuário
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nome":"Teste","email":"teste@empresa.com","senha":"123456"}'

# Listar funcionários
curl http://localhost:8080/api/funcionarios
```

---

## Configuração do App Android

### 1. Abrir o projeto

Abra a pasta `android/` no Android Studio.

### 2. Configurar a URL base

Edite o arquivo `app/src/main/java/com/rickstaff/app/data/remote/ApiClient.java`:

```java
// Para emulador Android
public static final String BASE_URL = "http://10.0.2.2:8080";

// Para dispositivo físico via adb reverse
public static final String BASE_URL = "http://localhost:8080";

// Para dispositivo físico via rede Wi-Fi
public static final String BASE_URL = "http://192.168.x.x:8080";
```

### 3. Dispositivo físico — opção A (adb reverse)

Conecte o celular via USB com depuração USB ativada e rode:

```bash
adb reverse tcp:8080 tcp:8080
```

Use `BASE_URL = "http://localhost:8080"` no `ApiClient.java`.

### 4. Dispositivo físico — opção B (rede Wi-Fi)

Descubra o IP da sua máquina:

```bash
# Windows
ipconfig
# Procure IPv4 em "Adaptador de Rede sem Fio Wi-Fi"
```

Use `BASE_URL = "http://192.168.x.x:8080"` substituindo pelo IP encontrado.

### 5. Compilar e rodar

Clique em **Run** no Android Studio ou pressione `Shift+F10`.

---

## Credenciais de teste

> O usuário administrador é criado automaticamente na primeira inicialização do backend via BootStrap. Não é necessário inserir dados manualmente no banco.


| Campo | Valor |
|-------|-------|
| Email | admin@empresa.com |
| Senha | 123456 |


> Também é possível criar uma nova conta pela tela de registro no próprio app.

---

## Fluxo do aplicativo

```
Splash Activity (animada)
    → Tela Principal (MainActivity)
        ├── Login (autenticação via backend Grails)
        │       → Menu Principal
        │           ├── Personagens
        │           │     → Listagem paginada (até 3 páginas)
        │           │     → Filtros: status, gênero, espécie
        │           │     → Perfil do personagem
        │           │           → Câmera nativa
        │           │           → Foto persistida por usuário + por personagem
        │           │           → POST simulado (jsonplaceholder)
        │           └── Funcionários
        │                 → Listagem (backend Grails + MySQL)
        │                 → Cadastrar novo funcionário
        │                 → Editar funcionário
        │                 → Excluir funcionário
        └── Registro (criar nova conta no backend)
```

---

## Endpoints do backend

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/auth/login` | Autenticar usuário |
| POST | `/api/auth/register` | Registrar novo usuário |
| GET | `/api/funcionarios` | Listar funcionários |
| GET | `/api/funcionarios/{id}` | Buscar funcionário |
| POST | `/api/funcionarios` | Criar funcionário |
| PUT | `/api/funcionarios/{id}` | Atualizar funcionário |
| DELETE | `/api/funcionarios/{id}` | Excluir funcionário |

---

## APIs públicas utilizadas

| API | URL |
|-----|-----|
| Rick and Morty | `https://rickandmortyapi.com/api/character` |
| POST simulado | `https://jsonplaceholder.typicode.com/posts` |

---

## Estrutura do projeto

```
rickstaff-app/
├── android/                  ← Projeto Android Studio
│   └── app/src/main/java/com/rickstaff/app/
│       ├── data/
│       │   ├── local/        ← SessionManager, ImageStorageManager
│       │   ├── model/        ← POJOs (Character, Employee, LoginResponse, Usuario)
│       │   └── remote/       ← ApiClient, AuthRepository, CharacterRepository, EmployeeRepository
│       ├── ui/
│       │   ├── splash/       ← SplashActivity
│       │   ├── main/         ← MainActivity (entrada com login e registro)
│       │   ├── login/        ← LoginActivity + LoginViewModel
│       │   ├── register/     ← RegisterActivity + RegisterViewModel
│       │   ├── menu/         ← MenuActivity
│       │   ├── characters/   ← CharacterListActivity + ViewModel + Adapter
│       │   ├── profile/      ← ProfileActivity + ProfileViewModel
│       │   └── employees/    ← EmployeeListActivity + EmployeeFormActivity + ViewModel + Adapter
│       └── util/             ← Constants, NetworkUtils
└── backend/                  ← Projeto Grails
    └── grails-app/
        ├── controllers/com/rickstaff/
        │   ├── AuthController.groovy
        │   └── FuncionarioController.groovy
        └── domain/com/rickstaff/
            ├── Funcionario.groovy
            └── Usuario.groovy
```

---

## Diferenciais implementados

- ✅ DiffUtil nos adapters de personagens e funcionários
- ✅ Separação clara entre camadas UI, ViewModel e dados (MVVM)
- ✅ Tratamento de estados: loading, erro e lista vazia
- ✅ Splash animada com transição fluida
- ✅ Sessão persistida com SharedPreferences após login
- ✅ Suporte a modo escuro via tema DayNight
- ✅ Registro de usuário real no backend
- ✅ Layout responsivo com NestedScrollView
- ✅ Filtros por status, gênero e espécie na listagem
- ✅ **Testes unitários nos ViewModels**
- ✅ **Persistência local da imagem capturada por usuário e por personagem**
- ✅ **Cada personagem mantém sua própria foto capturada, isolada por usuário logado**

---

## Observações técnicas

**Persistência de imagens:** as fotos capturadas pela câmera são armazenadas localmente via SharedPreferences usando uma chave composta por `usuarioEmail_characterId`, garantindo que cada usuário tenha suas próprias fotos e cada personagem mantenha sua imagem independente dos demais.

**Autenticação:** o backend valida credenciais contra a tabela `usuario` no MySQL. O token retornado é armazenado na sessão local e mantido enquanto o fluxo estiver ativo.

**Comunicação local:** o app se comunica com o backend local via IP da rede Wi-Fi ou `adb reverse`. Não é necessário deploy externo para demonstração.

---
 
## Screenshots
 
> Todas as capturas foram feitas em modo escuro.
 
### Fluxo de autenticação
 
| Splash | Tela Inicial |
|--------|--------------|
| <img width="250" height="550" alt="Screenshot_20260528_232801_RickStaff" src="https://github.com/user-attachments/assets/e7644403-b40e-4cf2-b31b-a048244b9a05" /> | <img width="250" height="550" alt="Screenshot_20260528_232801_RickStaff" src="https://github.com/user-attachments/assets/ceba0d73-8c60-4c72-8ba4-a43c98baae01" /> |

| Login | Cadastro |
|-------|----------|
| <img width="250" height="550" alt="Screenshot_20260528_232801_RickStaff" src="https://github.com/user-attachments/assets/ce842ad0-1fe6-48fb-b595-e5b091123256" /> | <img width="250" height="550" alt="Screenshot_20260528_232801_RickStaff" src="https://github.com/user-attachments/assets/840c099f-b6dc-4e51-aa76-7aefbaf7cbd2" /> |

 
### Menu e Personagens
 
| Menu Principal | Listagem | Filtros ativos |
|---------------|----------|----------------|
| <img width="250" height="550" alt="Screenshot_20260528_232737_RickStaff" src="https://github.com/user-attachments/assets/94461857-0916-413f-bbb6-48963f5875b1" /> | <img width="250" height="550" alt="Screenshot_20260528_232456_RickStaff" src="https://github.com/user-attachments/assets/95573280-27a4-4805-8b07-0680f2b51044" /> | <img width="250" height="550" alt="Screenshot_20260528_232517_RickStaff" src="https://github.com/user-attachments/assets/f906040b-3141-449b-bc37-4adda9a00162" /> | 

 
### Perfil e Câmera
 
| Perfil (foto original) | Perfil (foto capturada) |
|-----------------------|------------------------|
| <img width="250" height="550" alt="Screenshot_20260528_232630_RickStaff" src="https://github.com/user-attachments/assets/b5b4765f-e155-4b6c-bf67-808ac3c3fca5" /> | <img width="250" height="550" alt="Screenshot_20260528_232724_RickStaff" src="https://github.com/user-attachments/assets/e6194ad7-7011-45a1-8117-68879bf21f8e" /> |
 

### Módulo Funcionários
 
| Listagem | Formulário |
|----------|------------|
| <img width="250" height="550" alt="Screenshot_20260528_232535_RickStaff" src="https://github.com/user-attachments/assets/a5eccf31-f1f8-4bd9-acbd-412af8f92f07" /> | <img width="250" height="550" alt="Screenshot_20260528_232550_RickStaff" src="https://github.com/user-attachments/assets/8ba0f393-27ff-47a5-8ec0-059007576d78" /> |
