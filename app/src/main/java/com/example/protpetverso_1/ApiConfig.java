package com.example.protpetverso_1;

public class ApiConfig {

    public static final String BASE_URL = "https://petversorestfull.onrender.com";

    // Usuário
    public static final String URL_CADASTRAR = BASE_URL + "/api/usuarios/cadastrar";
    public static final String URL_LOGIN = BASE_URL + "/api/login";
    public static final String URL_ATUALIZAR_PERFIL = BASE_URL + "/api/usuarios/atualizarPerfil";
    public static final String URL_USUARIO_PERFIL = BASE_URL + "/api/usuarios/perfil";

    // Pets
    public static final String URL_CADASTRAR_PET = BASE_URL + "/api/pets/cadastrar";

    // Base do perfil do pet: complete com o id na hora da requisição
    // Exemplo final: https://petversorestfull.onrender.com/api/pets/3/perfil
    public static final String URL_PET_PERFIL = BASE_URL + "/api/pets/";

    public static final String URL_ATUALIZAR_PERFIL_PET = BASE_URL + "/api/pets/";

    public static final String URL_SOLICITAR_VINCULO = BASE_URL + "/api/pets/solicitarVinculo";
    public static final String URL_LISTAR_SOLICITACOES = BASE_URL + "/api/pets/listarSolicitacoes";
    public static final String URL_PROCESSAR_SOLICITACAO = BASE_URL + "/api/pets/processarSolicitacao";
    public static final String URL_MEUS_PETS = BASE_URL + "/api/pets/meusPets";

    // Tarefas
    public static final String URL_TAREFAS_DO_PET = BASE_URL + "/api/pets/"; // + id + "/tarefas"
    public static final String URL_TAREFAS_POR_DIA = BASE_URL + "/api/tarefas"; // ?data=yyyy-MM-dd
    public static final String URL_PET_TAREFAS = BASE_URL + "/api/pets/"; // + id + "/tarefas"-dd
    public static final String URL_CONCLUIR_TAREFA = BASE_URL + "/api/tarefas/"; // + idTarefa + "/concluir"
    public static final String URL_FOTO_PET = BASE_URL + "/api/pets/";
    public static final String URL_ATUALIZAR_TAREFA = BASE_URL + "/api/tarefas/";     // + idTarefa  (PUT)

    // Vacinas
    public static final String URL_VACINAS_DO_PET = BASE_URL + "/api/pets/";
    public static final String URL_VACINA = BASE_URL + "/api/vacinas/";

}