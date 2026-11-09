# Help Desk API

API REST para gerenciamento de chamados de suporte, desenvolvida com **Java, Spring Boot, Spring Security, JWT e PostgreSQL**.

O projeto simula o funcionamento de um sistema de Help Desk, permitindo que clientes abram chamados, profissionais de suporte realizem o atendimento e administradores gerenciem os usuários da aplicação.

O sistema possui autenticação, autorização por perfil, workflow de atendimento, histórico de alterações, comentários e testes automatizados.

---

## Funcionalidades

A aplicação possui três perfis de usuário:

### CLIENTE

O cliente pode:

- Realizar login
- Criar chamados
- Visualizar seus próprios chamados
- Editar chamados enquanto estiverem com status `ABERTO`
- Visualizar detalhes dos próprios chamados
- Enviar comentários
- Consultar comentários e histórico
- Fechar chamados que já foram resolvidos

### SUPORTE

O usuário de suporte pode:

- Realizar login
- Visualizar chamados disponíveis
- Visualizar chamados atribuídos a ele
- Iniciar a análise de um chamado
- Assumir um chamado
- Tornar-se automaticamente responsável pelo chamado assumido
- Enviar comentários
- Alterar o chamado para aguardando cliente
- Retomar o atendimento
- Resolver chamados

### ADMIN

O administrador pode:

- Realizar login
- Visualizar todos os chamados
- Listar usuários
- Consultar usuários por ID
- Criar novos usuários
- Criar usuários dos perfis `CLIENTE`, `SUPORTE` e `ADMIN`
- Desativar usuários
- Reativar usuários

---

# Workflow dos chamados

Os chamados possuem um fluxo controlado de status:

```text
ABERTO
   ↓
EM_ANALISE
   ↓
EM_ATENDIMENTO
   ↓
AGUARDANDO_CLIENTE
   ↓
EM_ATENDIMENTO
   ↓
RESOLVIDO
   ↓
FECHADO
```

Algumas etapas podem ser puladas dependendo da operação.

Por exemplo, um chamado `ABERTO` pode ser assumido diretamente por um usuário de suporte e passar para `EM_ATENDIMENTO`.

As alterações importantes também são registradas automaticamente no histórico do chamado.

---

# Segurança

A aplicação utiliza **Spring Security** para autenticação e autorização.

## Autenticação

O login é realizado utilizando:

```text
email
senha
```

As senhas não são armazenadas em texto puro.

Antes de serem salvas no banco de dados, são transformadas em hash utilizando:

```text
BCrypt
```

Após um login válido, a API gera um **JWT — JSON Web Token**.

Exemplo:

```http
Authorization: Bearer eyJ...
```

Esse token deve ser enviado nas requisições protegidas.

---

## Autorização por perfil

O JWT contém informações sobre o usuário autenticado, incluindo seu perfil.

Os perfis disponíveis são:

```text
CLIENTE
SUPORTE
ADMIN
```

O Spring Security utiliza essas informações para controlar quais endpoints cada perfil pode acessar.

Além da autorização por rota, o sistema também realiza validações no `Service`.

Isso impede, por exemplo, que um cliente autenticado acesse o chamado pertencente a outro cliente.

---

# Tecnologias utilizadas

- Java 22
- Spring Boot 4
- Spring Web MVC
- Spring Security
- OAuth2 Resource Server
- JWT
- BCrypt
- Spring Data JPA
- Hibernate
- PostgreSQL
- Bean Validation
- Maven
- JUnit
- Mockito
- MockMvc
- Postman
- IntelliJ IDEA

---

# Arquitetura

A aplicação foi organizada utilizando arquitetura em camadas.

```text
HTTP Request
      ↓
Controller
      ↓
DTO
      ↓
Service
      ↓
Repository
      ↓
JPA / Hibernate
      ↓
PostgreSQL
```

## Controller

Responsável por receber as requisições HTTP e retornar as respostas da API.

Exemplos:

```text
ChamadoController
UsuarioController
AuthController
```

---

## DTO

Os DTOs são utilizados para controlar os dados recebidos e retornados pela API.

Exemplos:

```text
CriarChamadoDTO
AtualizarChamadoDTO
ChamadoResponseDTO

CriarUsuarioDTO
UsuarioResponseDTO

LoginDTO
LoginResponseDTO

CriarComentarioDTO
ComentarioResponseDTO
```

Dessa forma, as entidades do banco não precisam ser expostas diretamente.

---

## Service

Contém as principais regras de negócio da aplicação.

Exemplos:

```text
ChamadoService
UsuarioService
AuthService
TokenService
ComentarioChamadoService
```

É nessa camada que são realizadas validações como:

```text
usuário está ativo?
usuário possui o perfil correto?
chamado está no status correto?
usuário é o solicitante?
usuário é o responsável?
```

---

## Repository

Responsável pelo acesso ao banco de dados utilizando Spring Data JPA.

Exemplos:

```text
ChamadoRepository
UsuarioRepository
ComentarioChamadoRepository
HistoricoChamadoRepository
```

---

## Model / Entity

Representa as entidades persistidas no PostgreSQL.

Principais entidades:

```text
Usuario
Chamado
ComentarioChamado
HistoricoChamado
```

---

# Principais entidades

## Usuario

Representa os usuários da aplicação.

Campos principais:

```text
id
nome
email
senha
perfil
ativo
```

Perfis:

```text
CLIENTE
SUPORTE
ADMIN
```

Um usuário pode ser desativado sem ser removido do banco, preservando seus relacionamentos com chamados, comentários e históricos.

---

## Chamado

Representa uma solicitação de suporte.

Campos principais:

```text
id
titulo
descricao
prioridade
status
solicitante
responsavel
```

Prioridades disponíveis:

```text
BAIXA
MEDIA
ALTA
URGENTE
```

Status disponíveis:

```text
ABERTO
EM_ANALISE
EM_ATENDIMENTO
AGUARDANDO_CLIENTE
RESOLVIDO
FECHADO
```

---

## ComentarioChamado

Representa as mensagens trocadas durante um atendimento.

Campos principais:

```text
id
chamado
usuario
mensagem
dataHora
```

Apenas participantes autorizados podem comentar em um chamado.

---

## HistoricoChamado

Registra eventos importantes ocorridos durante o ciclo de vida do chamado.

Exemplos:

```text
ABERTURA
ANALISE_INICIADA
ASSUMIDO
AGUARDANDO_CLIENTE
ATENDIMENTO_RETOMADO
RESOLVIDO
FECHADO
```

---

# Endpoints

## Autenticação

### Login

```http
POST /auth/login
```

Exemplo:

```json
{
  "email": "usuario@email.com",
  "senha": "Senha12345"
}
```

Resposta:

```json
{
  "token": "eyJ...",
  "tipo": "Bearer",
  "usuario": {
    "id": 1,
    "nome": "Usuario",
    "email": "usuario@email.com",
    "perfil": "CLIENTE"
  }
}
```

---

# Chamados

### Listar todos os chamados

```http
GET /chamados
```

Acesso:

```text
ADMIN
```

---

### Listar chamados relacionados ao usuário

```http
GET /chamados/meus
```

Para `CLIENTE`:

```text
retorna os chamados em que ele é o solicitante
```

Para `SUPORTE`:

```text
retorna os chamados atribuídos a ele
```

---

### Listar chamados disponíveis

```http
GET /chamados/disponiveis
```

Acesso:

```text
SUPORTE
```

Retorna chamados sem responsável que ainda podem ser assumidos.

---

### Buscar chamado

```http
GET /chamados/{id}
```

O acesso também é validado de acordo com o usuário autenticado.

---

### Criar chamado

```http
POST /chamados
```

Acesso:

```text
CLIENTE
```

Body:

```json
{
  "titulo": "Computador não inicializa",
  "descricao": "O computador não apresenta nenhum sinal ao pressionar o botão de ligar.",
  "prioridade": "ALTA"
}
```

O solicitante não precisa ser enviado.

Ele é identificado automaticamente através do JWT.

---

### Atualizar chamado

```http
PUT /chamados/{id}
```

Apenas o próprio solicitante pode editar o chamado enquanto ele estiver com status:

```text
ABERTO
```

---

### Iniciar análise

```http
PATCH /chamados/{id}/analisar
```

Acesso:

```text
SUPORTE
```

---

### Assumir chamado

```http
PATCH /chamados/{id}/assumir
```

Acesso:

```text
SUPORTE
```

O usuário autenticado passa automaticamente a ser o responsável.

---

### Aguardar cliente

```http
PATCH /chamados/{id}/aguardar-cliente
```

Acesso:

```text
SUPORTE
```

---

### Retomar atendimento

```http
PATCH /chamados/{id}/retomar
```

Acesso:

```text
SUPORTE
```

---

### Resolver chamado

```http
PATCH /chamados/{id}/resolver
```

Acesso:

```text
SUPORTE
```

---

### Fechar chamado

```http
PATCH /chamados/{id}/fechar
```

Acesso:

```text
CLIENTE
```

Somente o solicitante pode fechar o chamado e ele precisa estar com status:

```text
RESOLVIDO
```

---

# Comentários

### Criar comentário

```http
POST /chamados/{id}/comentarios
```

Body:

```json
{
  "mensagem": "O problema continua acontecendo."
}
```

O autor do comentário é identificado automaticamente através do JWT.

---

### Listar comentários

```http
GET /chamados/{id}/comentarios
```

---

# Histórico

### Consultar histórico

```http
GET /chamados/{id}/historico
```

O histórico registra automaticamente as principais alterações realizadas no chamado.

---

# Administração de usuários

Todos os endpoints abaixo exigem:

```text
ADMIN
```

### Listar usuários

```http
GET /usuarios
```

---

### Buscar usuário

```http
GET /usuarios/{id}
```

---

### Criar usuário

```http
POST /usuarios
```

Exemplo:

```json
{
  "nome": "Rafael Suporte",
  "email": "rafael@email.com",
  "senha": "Senha12345",
  "perfil": "SUPORTE"
}
```

A senha será armazenada utilizando BCrypt.

---

### Desativar usuário

```http
PATCH /usuarios/{id}/desativar
```

O usuário permanece no banco, porém não pode utilizar normalmente o sistema.

---

### Reativar usuário

```http
PATCH /usuarios/{id}/reativar
```

---

# Tratamento de erros

A aplicação possui tratamento global de exceções.

Alguns códigos HTTP utilizados:

```text
200 OK
201 Created
204 No Content
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
```

Exemplo de erro de validação:

```json
{
  "status": 400,
  "erros": {
    "titulo": "O título deve ter entre 5 e 100 caracteres"
  }
}
```

---

# Configuração do banco

O projeto utiliza PostgreSQL.

Banco utilizado durante o desenvolvimento:

```text
helpdesk
```

Configuração padrão:

```text
host: localhost
porta: 5433
database: helpdesk
usuario: postgres
```

Informações sensíveis não ficam armazenadas diretamente no código.

---

# Variáveis de ambiente

A aplicação utiliza variáveis de ambiente para informações sensíveis.

Variáveis obrigatórias:

```text
DB_PASSWORD
JWT_SECRET
```

Variáveis opcionais:

```text
DB_URL
DB_USERNAME
SHOW_SQL
```

O `application.properties` utiliza configurações semelhantes a:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5433/helpdesk}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}

app.jwt.secret=${JWT_SECRET}
```

---

# Como executar

## Pré-requisitos

É necessário possuir:

```text
Java
PostgreSQL
Git
```

O projeto possui Maven Wrapper, portanto não é obrigatório instalar o Maven separadamente.

---

## 1. Clone o projeto

```bash
git clone <URL_DO_REPOSITORIO>
```

Entre na pasta:

```bash
cd helpdesk-api
```

---

## 2. Crie o banco PostgreSQL

Crie um banco chamado:

```text
helpdesk
```

---

## 3. Configure as variáveis de ambiente

Configure:

```text
DB_PASSWORD
JWT_SECRET
```

Exemplo:

```text
DB_PASSWORD=sua_senha_postgres
JWT_SECRET=sua_chave_jwt
```

---

## 4. Execute a aplicação

No Windows:

```bash
mvnw.cmd spring-boot:run
```

Ou:

```bash
./mvnw spring-boot:run
```

A aplicação ficará disponível em:

```text
http://localhost:8080
```

---

# Testes

O projeto possui testes automatizados utilizando:

```text
JUnit
Mockito
MockMvc
Spring Security Test
```

Para executar:

### Windows

```bash
mvnw.cmd test
```

### Linux/macOS

```bash
./mvnw test
```

Entre os cenários testados estão:

```text
login com sucesso
senha inválida
usuário inativo
criação de usuário
hash de senha
proteção contra autodesativação de ADMIN
criação de chamado por CLIENTE
atribuição automática do solicitante
assunção de chamado por SUPORTE
controle de acesso entre usuários
requisições sem JWT
controle de acesso por perfil
```

---

# Principais conceitos aplicados

Durante o desenvolvimento foram utilizados conceitos como:

```text
API REST
CRUD
Arquitetura em camadas
DTO
Dependency Injection
Spring Data JPA
ORM
Hibernate
Relacionamentos entre entidades
Bean Validation
Tratamento global de exceções
Autenticação
Autorização
BCrypt
JWT
RBAC
Workflow
Auditoria
Testes unitários
Mocks
Testes de segurança
```

---

# Estrutura do projeto

```text
src
├── main
│   ├── java
│   │   └── com.gabriel.helpdesk
│   │       ├── config
│   │       ├── controller
│   │       ├── dto
│   │       ├── exception
│   │       ├── model
│   │       ├── repository
│   │       └── service
│   │
│   └── resources
│       └── application.properties
│
└── test
    └── java
        └── com.gabriel.helpdesk
            ├── security
            └── service
```

---

# Status do projeto

**MVP concluído.**

A versão atual possui autenticação, autorização, gerenciamento de chamados, administração de usuários, comentários, histórico e testes automatizados.

---

# Possíveis melhorias futuras

Algumas funcionalidades que podem ser adicionadas futuramente:

- Frontend web
- Docker
- Deploy em nuvem
- Refresh Token
- Recuperação de senha
- Paginação
- Filtros avançados
- Anexos nos chamados
- Notificações por e-mail
- Dashboard administrativo
- Métricas de atendimento
- Documentação automática com Swagger / OpenAPI

---

# Autor

**Gabriel Balbino Ferri**

Projeto desenvolvido para estudo e prática de desenvolvimento Backend utilizando Java e Spring Boot.