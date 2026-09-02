package com.example.protpetverso_1;
// Define o pacote do projeto.

public class AlertaSaude {
// Classe modelo (Model) que representa um alerta de saúde de um pet.
// É usada para armazenar os dados que aparecem na RecyclerView de Alertas de Saúde.

    private String titulo, desc;
    // titulo → texto principal do alerta (ex: "Vacina Antirrábica")
    // desc   → descrição do alerta (ex: "A data para renovar a vacina está próxima")

    private int imagemResId;
    // Guarda o ID do drawable (ícone/imagem) que será mostrado no card do alerta.
    // Ex: R.drawable.petversologo ou R.drawable.racao

    public AlertaSaude(String titulo, String desc, int imagemResId) {
        // Construtor. É chamado quando se cria um novo alerta:
        // new AlertaSaude("Título", "Descrição", R.drawable.icone)
        this.titulo = titulo;
        this.desc = desc;
        this.imagemResId = imagemResId;
    }

    public String getTitulo() {
        return titulo;
        // Retorna o título. Usado pelo AlertaSaudeAdapter para preencher o TextView do card.
    }

    public String getDesc() {
        return desc;
        // Retorna a descrição. Usado pelo AlertaSaudeAdapter.
    }

    public int getImagemResId() {
        return imagemResId;
        // Retorna o ID da imagem. Usado pelo AlertaSaudeAdapter para colocar a imagem no ImageView.
    }
}