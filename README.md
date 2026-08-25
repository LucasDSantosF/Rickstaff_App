# 👽 Rickstaff App

Aplicação Android integrada a um backend próprio para gerenciamento de personagens e funcionários, combinando consumo de API pública, autenticação, persistência de dados e operações CRUD.

O projeto foi desenvolvido como exercício prático de integração entre **Android, backend e banco de dados**, com foco na organização das camadas e na construção de um fluxo completo de aplicação.

## 🎯 Objetivos

O projeto reúne diferentes cenários comuns em aplicações reais:

* autenticação de usuários;
* consumo de APIs REST;
* listagem e filtros;
* operações CRUD;
* persistência de dados;
* upload e armazenamento local de imagens;
* integração entre aplicativo e backend;
* testes unitários.

## 🏗️ Arquitetura

```text
Android App
│
├── UI
├── ViewModels
├── Repositories
├── Local Storage
└── API Client
       │
       ▼
Backend REST
       │
       ▼
MySQL
```

O aplicativo Android mantém a separação entre interface, estado e acesso aos dados, enquanto o backend concentra os endpoints e persistência das informações.

## 📱 Android

Principais responsabilidades do aplicativo:

* autenticação;
* cadastro de usuários;
* listagem de personagens;
* filtros por status, gênero e espécie;
* visualização de detalhes;
* captura de imagens utilizando a câmera;
* gerenciamento de funcionários;
* persistência local de sessão e imagens.

O projeto também utiliza ViewModels e separação entre camadas de UI e dados.

## ⚙️ Backend

Backend desenvolvido com **Grails/Groovy**, responsável por:

* autenticação;
* cadastro de usuários;
* gerenciamento de funcionários;
* endpoints REST;
* persistência dos dados em MySQL.

## 🌐 APIs utilizadas

### Rick and Morty API

Utilizada para consulta dos personagens.

### JSONPlaceholder

Utilizada em uma operação POST simulada.

## 🧪 Testes

O projeto possui testes unitários para ViewModels.

## 🔐 Autenticação

O fluxo de autenticação utiliza o backend próprio para validação das credenciais, com o token retornado mantido na sessão local da aplicação.

## 📌 Principais pontos técnicos

* Android nativo
* MVVM
* API REST
* Grails / Groovy
* MySQL
* gerenciamento de sessão
* consumo de API externa
* filtros e paginação
* persistência local
* testes unitários
* integração entre frontend e backend

## 📸 Demonstração

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


## ℹ️ Contexto

Este projeto foi originalmente desenvolvido no contexto de uma avaliação técnica e posteriormente mantido como projeto público para demonstração de práticas de desenvolvimento e integração entre Mobile e Backend.

## 👤 Autor

**Lucas dos Santos Francisco**

[GitHub](https://github.com/LucasDSantosF) · [LinkedIn](https://www.linkedin.com/in/lucas-dos-santos-francisco/)

