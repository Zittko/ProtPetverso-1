package com.example.protpetverso_1.pet;

import com.example.protpetverso_1.data.AppDataCache;
import com.example.protpetverso_1.schedule.Tarefa;
import com.example.protpetverso_1.util.Formatadores;
import com.example.protpetverso_1.vaccine.Vacina;

import org.json.JSONObject;

/**
 * Pedido de alteração feito por um tutor SUPORTE (GET /api/solicitacoes/pendentes → "alteracoes").
 * Tipos: ALTERAR_TAREFA, ALTERAR_VACINA, ADICIONAR_VACINA.
 */
public class SolicitacaoAlteracao {

    public static final String TIPO_ALTERAR_TAREFA = "ALTERAR_TAREFA";
    public static final String TIPO_ALTERAR_VACINA = "ALTERAR_VACINA";
    public static final String TIPO_ADICIONAR_VACINA = "ADICIONAR_VACINA";

    public final long idSolicitacao;
    public final String tipo;
    public final long idAlvo;
    public final long idPet;
    public final String nomePet;
    public final String nomeSolicitante;
    public final String dataDeCriacao;
    /** Texto com os dados que o suporte propôs. */
    public final String proposto;
    /** Texto com os dados de hoje (quando o app os tem guardados); vazio se não houver. */
    public final String atual;

    private SolicitacaoAlteracao(long id, String tipo, long idAlvo, long idPet, String nomePet,
                                 String solicitante, String data, String proposto, String atual) {
        this.idSolicitacao = id;
        this.tipo = tipo;
        this.idAlvo = idAlvo;
        this.idPet = idPet;
        this.nomePet = nomePet;
        this.nomeSolicitante = solicitante;
        this.dataDeCriacao = data;
        this.proposto = proposto;
        this.atual = atual;
    }

    public boolean ehTarefa() { return TIPO_ALTERAR_TAREFA.equals(tipo); }

    public String tituloTipo() {
        if (TIPO_ALTERAR_TAREFA.equals(tipo)) return "Alterar tarefa";
        if (TIPO_ALTERAR_VACINA.equals(tipo)) return "Alterar vacina";
        if (TIPO_ADICIONAR_VACINA.equals(tipo)) return "Adicionar vacina";
        return "Alteração";
    }

    public static SolicitacaoAlteracao fromJson(JSONObject o) {
        long id = o.optLong("idSolicitacao", -1);
        if (id <= 0) return null;
        String tipo = o.optString("tipo", "");
        long idAlvo = o.optLong("idAlvo", -1);
        long idPet = o.optLong("idPet", -1);

        String proposto = "";
        String atual = "";
        JSONObject t = o.optJSONObject("dadosTarefa");
        JSONObject v = o.optJSONObject("dadosVacina");
        if (t != null) {
            proposto = textoTarefa(t);
            atual = tarefaAtual(idPet, idAlvo);
        } else if (v != null) {
            proposto = textoVacina(v);
            if (TIPO_ALTERAR_VACINA.equals(tipo)) atual = vacinaAtual(idPet, idAlvo);
        }
        return new SolicitacaoAlteracao(id, tipo, idAlvo, idPet,
                o.optString("nomePet", "—"), o.optString("nomeSolicitante", "—"),
                o.optString("dataDeCriacao", ""), proposto, atual);
    }

    private static String textoTarefa(JSONObject t) {
        StringBuilder sb = new StringBuilder();
        sb.append("Título: ").append(t.optString("titulo", "—"));
        String data = Formatadores.dataIsoParaBr(t.optString("dataTarefa", ""));
        String hora = t.optString("hora", "");
        if (hora.length() >= 5 && !"null".equals(hora)) hora = hora.substring(0, 5); else hora = "";
        sb.append("\nQuando: ").append(data.isEmpty() ? "—" : data);
        if (!hora.isEmpty()) sb.append(" às ").append(hora);
        String desc = t.optString("descricao", "");
        if (!desc.isEmpty() && !"null".equals(desc)) sb.append("\nDescrição: ").append(desc);
        return sb.toString();
    }

    private static String textoVacina(JSONObject v) {
        StringBuilder sb = new StringBuilder();
        sb.append("Vacina: ").append(v.optString("nome", "—"));
        boolean aplicada = v.optBoolean("jaAplicada", false);
        String data = Formatadores.dataIsoParaBr(v.optString("dataAplicacao", ""));
        if (aplicada) {
            sb.append("\nJá aplicada em: ").append(data.isEmpty() ? "—" : data);
        } else {
            sb.append("\nAinda não aplicada");
            if (!data.isEmpty() && !"null".equals(data)) sb.append(" (prevista: ").append(data).append(")");
        }
        if (v.optBoolean("dosagemUnica", false)) {
            sb.append("\nDose única");
        } else {
            int qtd = v.optInt("intervaloQuantidade", 0);
            String un = unidade(v.optString("intervaloUnidade", ""));
            if (qtd > 0) sb.append("\nRepete a cada ").append(qtd).append(" ").append(un);
        }
        String obs = v.optString("observacao", "");
        if (!obs.isEmpty() && !"null".equals(obs)) sb.append("\nObservação: ").append(obs);
        return sb.toString();
    }

    private static String unidade(String api) {
        switch (api) {
            case "DIAS": return "dia(s)";
            case "SEMANAS": return "semana(s)";
            case "MESES": return "mês(es)";
            case "ANOS": return "ano(s)";
            default: return api.toLowerCase();
        }
    }

    private static String tarefaAtual(long idPet, long idTarefa) {
        for (Tarefa t : AppDataCache.get().getTarefas(idPet)) {
            if (t != null && t.getIdTarefa() == idTarefa) {
                StringBuilder sb = new StringBuilder("Título: " + t.getTitulo());
                if (t.getData() != null) {
                    sb.append("\nQuando: ").append(Formatadores.dataIsoParaBr(t.getData().toString()));
                    if (t.getHorario() != null) {
                        String h = t.getHorario().toString();
                        sb.append(" às ").append(h.length() >= 5 ? h.substring(0, 5) : h);
                    }
                }
                if (!t.getDesc().isEmpty()) sb.append("\nDescrição: ").append(t.getDesc());
                return sb.toString();
            }
        }
        return "";
    }

    private static String vacinaAtual(long idPet, long idVacina) {
        for (Vacina v : AppDataCache.get().getVacinas(idPet)) {
            if (v != null && v.getIdVacina() == idVacina) {
                StringBuilder sb = new StringBuilder("Vacina: " + v.getNome());
                sb.append("\nAplicação: ").append(v.getDataAplicacaoFormatada());
                sb.append("\nStatus: ").append(v.getStatusLabel());
                return sb.toString();
            }
        }
        return "";
    }
}
