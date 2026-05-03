# Sistema de Gestão de Biblioteca 📚

## 🚀 Tecnologias Utilizadas

- **Backend:** Java 21, Spring Boot 4.0.6, Spring Data JPA, Hibernate Validator
- **Frontend:** Angular 21.2, TypeScript, HTML/CSS
- **Banco de Dados:** MySQL 8.0 (via Docker)
- **Ferramentas:** Maven, Docker, Docker Compose, JUnit 5, Mockito

## ⚙️ Pré-requisitos

Para rodar este projeto na sua máquina, você vai precisar de:
- **[Java 21](https://jdk.java.net/21/)** ou superior.
- **[Node.js](https://nodejs.org/)** `v22` para rodar o Angular.
- **[Docker](https://www.docker.com/)** e **Docker Compose** para o banco de dados.

## 📂 Estrutura do Repositório (Monorepo)

O projeto está dividido em duas pastas principais dentro deste mesmo repositório:
- `/backend`: Contém a API REST em Spring Boot.
- `/frontend`: Contém a aplicação Web (SPA) em Angular.

---

## 🛠️ Como rodar o Banco de Dados

Nós utilizamos um container Docker para facilitar a subida do MySQL. Na pasta `backend` existe um arquivo `docker-compose.yml`.

1. Abra um terminal na pasta do backend:
   ```bash
   cd backend
   ```
2. Suba o banco de dados:
   ```bash
   docker-compose up -d
   ```
   *O MySQL estará rodando na porta `3306` com o usuário `root` e a base de dados `biblioteca` criada automaticamente.*

---

## ☕ Como rodar o Backend (API REST)

A API é a responsável por aplicar todas as regras de negócio de Usuários, Livros e Empréstimos.

1. Na pasta `backend`, instale as dependências e rode o projeto com o Maven Wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
   > **⚠️ Importante:** Para utilizar a busca na API do Google Books sem restrição de quota, você deve gerar uma API Key gratuita no Google Cloud Console e inseri-la no arquivo `backend/src/main/resources/application.properties` na variável `api.key.google-book=`. Caso contrário, a busca retornará erro de "Too Many Requests" (Cota excedida).

2. A API iniciará na porta padrão `8080`.
   - Acesse a **Documentação Interativa (Swagger UI)** em: `http://localhost:8080/swagger-ui.html`
3. Os testes unitários automatizados cobrem a lógica de negócio e podem ser rodados com:
   ```bash
   ./mvnw test
   ```

---

## 🌐 Como rodar o Frontend (Angular)

A interface gráfica consome a nossa API REST.

1. Abra um novo terminal e navegue até a pasta do frontend:
   ```bash
   cd frontend
   ```
2. Garanta que você está usando `Node 22`:
   ```bash
   nvm use 22
   ```
   > Se você não usa `nvm`, instale uma versão `22.x` do Node.js antes de continuar.
3. Instale as dependências do Node.js:
   ```bash
   npm install
   ```
4. Inicie o servidor de desenvolvimento do Angular:
   ```bash
   npm start
   ```
5. Acesse a aplicação no seu navegador através de: `http://localhost:4200`

---

## 📜 Funcionalidades (Requisitos) Implementadas

1. **Modelagem de Banco de Dados e Constraints:** Tabelas `usuarios`, `livros` e `emprestimos` com todos os campos obrigatórios (NOT NULL), validação restrita de E-mail via Regex, datas sem permissão para o futuro e relacionamentos Muitos-Para-Um.
2. **Operações CRUD:** Endpoints em `/usuarios`, `/livros` e `/emprestimos` testados e blindados com regras de negócio.
3. **Regra de Empréstimos:** Validação estrita para não permitir que o mesmo livro tenha múltiplos empréstimos ativos simultaneamente; bloqueio e liberação do livro com base no status do empréstimo.
4. **Recomendação de Livros:** Algoritmo que lê o histórico de empréstimos do usuário e sugere obras das mesmas categorias lidas (excluindo os que ele já leu).
5. **Automação Diária:** Job Agendado (`@Scheduled`) no Spring que roda à meia-noite cobrando e atrasando empréstimos que já passaram do dia de devolução.
6. **Cobertura de Testes Unitários:** Testes de serviços passando perfeitamente, utilizando JUnit 5 e Mockito cobrindo regras sensíveis da aplicação.
7. **[Questão Bônus] Frontend Angular Moderno:** Todo o sistema foi envelopado em um SPA com Design Premium em modo claro e escuro.
8. **[Questão Bônus] Integração Google Books API:** O backend possui um endpoint BFF que consulta a API do Google Books. O frontend consome essa API local permitindo buscar e adicionar livros reais com capa e ISBN na biblioteca em um clique.

---

## 🚀 Futuras Melhorias

- **Controle de Estoque (Quantidades):** Implementar no modelo de Livros a propriedade "quantidade", para permitir múltiplos exemplares do mesmo livro. Consequentemente, a regra de negócio do empréstimo passará a subtrair/adicionar saldo do estoque em vez de depender apenas de uma flag booleana de "disponível", permitindo que vários usuários peguem emprestado a mesma obra ao mesmo tempo (caso haja cópias).

- **Autenticação JWT com Spring Security:** Implementar autenticação e autorização com JWT para proteger os endpoints da API REST. 

- **Envio de e-mails:** Implementar envio de e-mails para notificar usuários sobre empréstimos, devoluções e atrasos. 
