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
// uso: URL_PET_PERFIL + id + "/perfil"
}