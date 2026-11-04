# 🐾 PetVerso (app Android)

Aplicativo Android do **PetVerso**, voltado a **compartilhar o cuidado de pets entre várias pessoas**. O foco não é compartilhar dados do usuário, e sim os dados do pet: tutores vinculados ao mesmo pet enxergam o perfil dele, uma agenda de tarefas e a carteira de vacinas em comum, e acompanham o que já foi feito.

Projeto desenvolvido como TCC. O app consome a [API REST do PetVerso](https://github.com/justletthefatherlead/petVerso-API), que é desenvolvida separadamente.

---

## ✨ Funcionalidades

**Conta**
- Cadastro com nome, e-mail, telefone (máscara `(DD) NNNNN-NNNN`) e senha
- Aceite obrigatório dos **Termos de Uso** e da **Política de Privacidade / Termo de Consentimento**: o usuário abre o texto, rola até o fim e só então o botão "Li e aceito" é liberado
- Login, logout (`POST /api/logout`) e sessão mantida no aparelho
- Perfil do usuário com apelido e foto, edição de dados e troca de senha

**Pets e vínculo**
- Cadastro de pet com foto, e informações adicionais (sensibilidades e personalidades)
- Código de vínculo para compartilhar o pet; outro usuário solicita ser tutor de suporte
- O tutor principal (DONO) aceita ou recusa as solicitações
- Usuário sem pet fica **bloqueado na tela de escolha** até cadastrar um pet ou ter um vínculo aceito; o acesso é liberado automaticamente quando o tutor aceita

**Agenda**
- Calendário mensal e lista de tarefas do dia, somando os pets vinculados
- Criar, editar, concluir e excluir tarefas (registra quem executou)

**Vacinas**
- Carteira de vacinas por pet, com status `PENDENTE`, `APLICADA` e `ATRASADA`
- Cadastro, edição, exclusão e "marcar como aplicada"
- Alertas de saúde na Home

**Funciona com cache local**: pets, tarefas, vacinas e fotos são salvos no aparelho e aparecem imediatamente enquanto a API responde (útil porque a API gratuita no Render pode demorar a "acordar").

---

## 🧱 Stack

| Camada | Tecnologia |
| --- | --- |
| Linguagem | Java 11 (compatibilidade) |
| Plataforma | Android, `minSdk` 26, `targetSdk` 36 |
| Build | Gradle (Kotlin DSL) |
| UI | Material Components, RecyclerView, ViewPager2, DrawerLayout, BottomNavigationView, XML (sem Compose) |
| Rede | Volley (`StringRequest`, `JsonObjectRequest`, `JsonArrayRequest`) |
| Sessão | `EncryptedSharedPreferences` (`androidx.security:security-crypto`) |
| Imagens | uCrop (recorte) e cache próprio em disco |
| Backend | API Spring Boot no Render (`https://petversorestfull.onrender.com`) |

Pacote base: `com.example.protpetverso_1`

---

## 🏗️ Arquitetura

O app é **Activity + Fragment** em Java, organizado **por funcionalidade** (não por camada técnica). Não usa MVVM nem injeção de dependência: cada tela chama a API diretamente com o Volley e usa um cache central para ler e guardar os dados.

```
app/src/main/java/com/example/protpetverso_1/
├── ApiConfig.java            # base URL e todas as URLs da API
├── VolleySingleton.java      # fila única de requisições
├── SessionManager.java       # token e dados do usuário (criptografados)
├── SplashActivity.java       # decide para onde ir ao abrir o app
├── MenuActivity.java         # tela principal: toolbar, drawer, bottom nav e fragments
├── HomeFragment.java         # início (pets, tarefas do dia, alertas)
├── account/                  # login, cadastro, termos, logout, recuperar senha
├── user/                     # perfil, edição e criação do perfil do usuário
├── pet/                      # cadastro, escolha/vínculo, perfil, lista e solicitações
├── schedule/                 # agenda: calendário, tarefas e criação de tarefa
├── vaccine/                  # carteira de vacinas e alertas de saúde
├── data/                     # AppDataCache, PhotoDiskCache e DataPreloader
└── util/                     # Formatadores e MascaraTelefone
```

Recursos em `app/src/main/res/`: `layout/` (telas e itens de lista), `drawable/`, `menu/` (bottom nav e drawer), `raw/` (textos dos termos e da política), `values/` (cores, temas, estilos) e `xml/` (regras de backup).

### Fluxo de telas

```
Splash ──┬─ sem sessão ──────────────► Login ──► Cadastro ──► Termos
         │                                  │
         │                                  ▼
         ├─ logado, sem pet ─────────► EscolhaPet (obrigatória) ──► Cadastrar pet
         │                                  │  (polling: libera quando o vínculo é aceito)
         ▼                                  ▼
     MenuActivity (Home | Agenda | Vacinas | Pets  +  drawer: Perfil, Solicitações, Sair)
```

- `SplashActivity` lê o cache do disco e decide: Login, escolha de pet obrigatória ou Menu.
- `MenuActivity` troca fragments com `FragmentManager.replace` (bottom nav para Home, Agenda, Vacinas e Pets; drawer para Perfil do Usuário, Solicitações de vínculo e Sair).
- O vínculo é verificado **no servidor** (`GET /api/pets/meusPets` devolve só vínculos aceitos), então fechar o app ou limpar os dados não burla o bloqueio.

### Principais decisões

- **Uma URL por constante** em `ApiConfig`; nenhuma tela monta URL "na mão" fora dela.
- **Token na sessão criptografada**: `SessionManager` guarda token, id, nome, e-mail e telefone em `EncryptedSharedPreferences`, com recuperação automática se o Keystore falhar (arquivo apagado e recriado) e um fallback.
- **Cache em duas camadas** (`data/`):
  - `AppDataCache` (singleton): mantém dados em memória e persiste o JSON em `SharedPreferences`; as telas leem dele primeiro (*cache-first*) e atualizam quando a API responde.
  - `PhotoDiskCache`: salva as fotos (Base64 da API) como arquivos e carrega o `Bitmap` reduzido, evitando guardar imagens grandes em memória ou em preferências.
  - `DataPreloader`: após login ou ao abrir o menu, busca usuário, pets, perfil, tarefas e vacinas do pet selecionado e preenche o cache.
- **Logout central**: `LogoutHelper` cancela requisições, limpa cache, fotos e sessão, avisa a API e volta ao Login. A saída local nunca depende da rede.
- **Fotos em Base64** no JSON (sem multipart), como a API.
- **Máscaras e formatos**: telefone `(DD) NNNNN-NNNN` e datas `DD/MM/AAAA` na tela; a API recebe só os dígitos do telefone e datas ISO (`yyyy-MM-dd`). `Formatadores` e `MascaraTelefone` concentram isso.
- **Papéis**: DONO pode editar pet, tarefas e vacinas e processar solicitações; SUPORTE vê e conclui tarefas e aplica vacinas. O app esconde ou bloqueia o que o papel não permite, mas **a regra de verdade é da API**.

---

## 🧩 Como as classes são construídas

Cada funcionalidade segue o mesmo padrão. Ao criar uma nova, siga estes passos.

### 1. Modelo (`Pet`, `Tarefa`, `Vacina`, `PetResumo`...)
Classe simples com atributos privados, getters e, quando vem da API, um método estático `fromJson(JSONObject)`:

```java
public static Tarefa fromJson(JSONObject o) {
    Tarefa t = new Tarefa(o.optString("titulo", ""), o.optString("descricao", ""), 0, null, null);
    // ... lê os demais campos com optString/optLong/optBoolean (nunca quebra se faltar campo)
    return t;
}
```

Use sempre `opt*` em vez de `get*`: a resposta pode vir sem um campo (como a foto) e o app não pode quebrar por isso.

### 2. Request (`CadastroRequest`, `LoginRequest`...)
Classe de envio que recebe os campos no construtor e devolve o corpo com `toJsonObject()`. É o equivalente aos DTOs `Request` da API.

### 3. Response (`CadastroResponse`, `LoginResponse`...)
Classe de resposta com `fromJsonObject(JSONObject)`, equivalente aos DTOs `Response` da API.

### 4. Activity ou Fragment (a tela)
Responsável por: ligar as views, validar campos, chamar a API e atualizar a tela. Padrão de uma chamada autenticada:

```java
JsonObjectRequest request = new JsonObjectRequest(
        Request.Method.POST, ApiConfig.URL_CADASTRAR_PET, body,
        response -> { /* sucesso: atualizar tela e cache */ },
        error -> { /* tratar por statusCode: 400, 403, 404, 409; sem resposta = sem conexão */ }
) {
    @Override public Map<String, String> getHeaders() {
        Map<String, String> h = new HashMap<>();
        h.put("Authorization", "Bearer " + sessionManager.getToken());
        return h;
    }
};
VolleySingleton.getInstance(this).addToRequestQueue(request);
```

Regras que as telas seguem:
- validar no app **antes** de enviar (campo vazio, e-mail, telefone, tamanho de senha) e mostrar o erro no próprio campo (`TextInputLayout.setError`);
- desabilitar o botão durante a requisição e reabilitar no erro, para evitar envio duplo;
- tratar o `statusCode` da API (400 dados inválidos, 403 sem permissão ou token inválido, 404 não encontrado, 409 conflito) e o caso sem conexão;
- antes de atualizar a UI em callback, checar se a tela ainda existe (`isAdded()` / `isFinishing()`).

### 5. Adapter (listas)
`RecyclerView.Adapter` com um `ViewHolder` interno estático (`VH`). A tela passa os dados e um `Listener` (interface) para os cliques; o adapter **não** chama a API.

### 6. Layout XML
Um arquivo por tela (`..._layout.xml` ou `activity_...xml`) e um por item de lista (`item_...xml`). Paleta fixa do app:

| Uso | Cor |
| --- | --- |
| Botões de confirmar | `#DF945D` |
| Botões de cancelar / voltar | `#F7D4A0` |
| Fundo das telas | `#FFE8CE` |
| Barras e menus | `#F08C4B` |
| Bordas | `#FF9800` |
| Campos de texto | `#FFFFFF` (`bg_campo_branco`) |
| Texto | `#000000` |

### 7. Registro
Toda Activity nova precisa ser declarada no `AndroidManifest.xml`. Textos longos (como termos) ficam em `res/raw/`.

### Exemplo: criar uma nova tela "Peso do pet"

1. Adicionar a URL em `ApiConfig` (`URL_PESO = BASE_URL + "/api/pets/"`).
2. Criar `pet/PesoRegistro.java` (modelo com `fromJson`) e, se houver envio, `pet/PesoRequest.java` (`toJsonObject`).
3. Criar `res/layout/activity_peso.xml` e `res/layout/item_peso.xml`.
4. Criar `pet/PesoAdapter.java` e `pet/PesoActivity.java` (valida, chama a API, atualiza a lista).
5. Se for dado que precisa aparecer offline, guardar no `AppDataCache` e carregar no `DataPreloader`.
6. Declarar `PesoActivity` no manifesto.

---

## 🔐 Sessão, privacidade e segurança

- O token da API é **opaco e não expira sozinho**: a sessão termina apenas com o logout (`POST /api/logout`).
- Senha nunca é guardada no aparelho; só o token e os dados básicos, em armazenamento criptografado.
- As regras de backup excluem os arquivos de sessão (`res/xml/backup_rules.xml` e `data_extraction_rules.xml`).
- O cadastro exige aceite dos Termos de Uso e da Política de Privacidade (`res/raw/termos_uso.txt` e `politica_privacidade.txt`, exibidos por `TermosActivity`). Preencha os campos `[PREENCHER]` com o responsável e o contato antes de publicar.
- Permissões: `INTERNET` e leitura de mídia (só para escolher a foto do pet).

---

## 📡 Endpoints consumidos

Todas as URLs ficam em `ApiConfig`. Fora login e cadastro, toda chamada envia `Authorization: Bearer <token>`.

| Área | Rotas |
| --- | --- |
| Conta | `POST /api/usuarios/cadastrar`, `POST /api/login`, `POST /api/logout` |
| Usuário | `GET /api/usuarios/perfil`, `PUT /api/usuarios/atualizarPerfil`, `PUT /api/usuarios/dados`, `PUT /api/usuarios/senha` |
| Pets | `POST /api/pets/cadastrar`, `GET /api/pets/meusPets`, `GET /api/pets/{id}/perfil`, `PUT /api/pets/{id}/dados`, `PUT /api/pets/{id}/atualizarPetPerfil`, `PUT /api/pets/{id}/foto` |
| Vínculo | `POST /api/pets/solicitarVinculo`, `GET /api/pets/listarSolicitacoes`, `POST /api/pets/processarSolicitacao` |
| Agenda | `GET` e `POST /api/pets/{id}/tarefas`, `GET /api/tarefas?data=`, `PUT` e `DELETE /api/tarefas/{id}`, `PATCH /api/tarefas/{id}/concluir` |
| Vacinas | `GET` e `POST /api/pets/{id}/vacinas`, `PUT` e `DELETE /api/vacinas/{id}`, `PATCH /api/vacinas/{id}/aplicar` |

Detalhes de regras, papéis e códigos de erro estão no README da API.

---

## 🚀 Como rodar

### Pré-requisitos
- Android Studio (versão recente) com JDK 17 ou superior
- Dispositivo ou emulador com Android 8.0 (API 26) ou superior
- Acesso à internet (a API roda no Render; a primeira chamada pode levar alguns segundos enquanto o servidor "acorda")

### Passos

```bash
git clone https://github.com/Zittko/ProtPetverso-1.git
cd ProtPetverso-1
git checkout Cadu
```

1. Abra a pasta no Android Studio e aguarde a sincronização do Gradle.
2. Para usar outra API (por exemplo local), altere `BASE_URL` em `ApiConfig.java`. No emulador, o `localhost` do computador é `http://10.0.2.2:8080`.
3. Execute em um dispositivo ou emulador (**Run ▶**).

---

## 📝 Notas de desenvolvimento

- O app foi testado manualmente em dispositivo; ainda não há testes automatizados.
- O `nav_graph.xml` é um resquício do template do Android Studio (`FirstFragment` e `SecondFragment`) e não é usado pela navegação real, que é feita pelo `MenuActivity`.
- Ainda não existe exclusão de conta pela API; a Política de Privacidade orienta a pedir por e-mail.
- A API não registra o aceite dos termos; ele é exigido apenas no app, no cadastro.

---

## 🗺️ Roadmap

- [x] Cadastro, login e logout com sessão por token
- [x] Perfil do usuário (apelido, foto, dados e senha)
- [x] Cadastro de pet, perfil e fotos
- [x] Vínculo por código, com bloqueio para quem não tem pet
- [x] Agenda de tarefas e carteira de vacinas
- [x] Cache local de dados e fotos
- [x] Termos de Uso e Política de Privacidade no cadastro
- [ ] Solicitação de alteração: o suporte propõe a mudança e o dono aprova
- [ ] Notificações de vacinas e tarefas
- [ ] Verificação de e-mail no cadastro
- [ ] Testes automatizados

---

## 👤 Autores

Desenvolvido por [@Zittko](https://github.com/Zittko) e equipe. API por [@justletthefatherlead](https://github.com/justletthefatherlead).
