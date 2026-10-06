# Acesso local ao Swagger e ao H2

## 1. Iniciar o backend com o perfil H2

Abra o PowerShell e entre na pasta do backend:

```powershell
cd C:\Users\dti\TatooFlow\backend
```

Neste computador, o Maven está disponível na instalação do IntelliJ. Execute:

```powershell
& 'C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2021.3\plugins\maven\lib\maven3\bin\mvn.cmd' '-Dmaven.repo.local=C:\Users\dti\.m2\repository' '-Dspring-boot.run.profiles=h2' spring-boot:run
```

Se o Maven estiver configurado no `PATH`, também é possível usar:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=h2"
```

Aguarde a mensagem de inicialização da aplicação e mantenha o terminal aberto. A API usa a porta `8080`. O perfil `h2` permite executar com o banco em memória, sem iniciar o MySQL.

## 2. Acessar o Swagger

1. Abra [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) no navegador.
2. Para testar uma rota, expanda o endpoint e clique em **Try it out**.
3. Preencha os parâmetros ou o corpo JSON solicitado e clique em **Execute**.

Para acessar endpoints protegidos:

1. Expanda `POST /auth/login` e clique em **Try it out**.
2. Informe o PIN e o ID do aparelho de um usuário ativo já cadastrado. Substitua os valores do exemplo pelas credenciais cadastradas:

   ```json
   {
     "pin": "012345",
     "aparelhoId": "aparelho-tatuador-01"
   }
   ```

3. Clique em **Execute** e copie somente o token retornado no campo `data`, sem as aspas.
4. Clique em **Authorize**, cole o token no campo `bearerAuth`, sem adicionar `Bearer`, e confirme em **Authorize**.
5. Clique em **Close** e execute o endpoint desejado. O Swagger inclui o token nas requisições.

Se o token expirar ou o backend reiniciar, faça login novamente e atualize o token em **Authorize**. O exemplo acima não cria um usuário; para provisionar o primeiro administrador, consulte [o README do backend](../../../README.md#primeiro-administrador-e-usuários-antigos).

## 3. Acessar o console H2

1. Com o backend em execução no perfil `h2`, abra [http://localhost:8080/h2-console](http://localhost:8080/h2-console).
2. Preencha os campos conforme a configuração de `application-h2.yml`:

   | Campo | Valor |
   | --- | --- |
   | Driver Class | `org.h2.Driver` |
   | JDBC URL | `jdbc:h2:mem:tattooflow_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE` |
   | User Name | `sa` |
   | Password | Deixe vazio |

3. Clique em **Connect**.
4. Para consultar os usuários cadastrados, execute:

   ```sql
   SELECT id, nome, email, aparelho_id, perfil, ativo FROM usuarios;
   ```

O banco H2 fica em memória: os dados são perdidos ao encerrar ou reiniciar o backend. O console acessa o banco da instância da API que está em execução.

## Problemas comuns

- **`mvn` não é reconhecido:** use o comando com o caminho completo do Maven do IntelliJ apresentado no passo 1. Em outro computador, ajuste o caminho para a instalação local.
- **O navegador não consegue abrir os endereços:** confira se o backend terminou de iniciar e continua em execução na porta `8080`.
- **O console H2 não está disponível:** confira se iniciou a API com `-Dspring-boot.run.profiles=h2`.
- **Erro ao conectar ao H2:** copie a JDBC URL completa e confira o usuário `sa` e a senha vazia.
- **Erro de autenticação em uma rota protegida:** faça login com um usuário cadastrado e atualize o token em **Authorize**.
