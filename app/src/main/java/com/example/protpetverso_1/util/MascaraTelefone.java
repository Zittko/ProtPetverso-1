package com.example.protpetverso_1.util;

import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Máscara de telefone enquanto o usuário digita: (DD) NNNNN-NNNN (celular) ou (DD) NNNN-NNNN (fixo).
 * Funciona com apagar (inclusive sobre os separadores), colar e edição no meio do texto.
 */
public class MascaraTelefone implements TextWatcher {

    private boolean atualizando;
    private boolean apagando;
    private int digitosAntes;

    public static void aplicar(EditText campo) {
        if (campo != null) campo.addTextChangedListener(new MascaraTelefone());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (atualizando) return;
        digitosAntes = Formatadores.apenasDigitos(s).length();
        apagando = after < count;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // tudo é feito em afterTextChanged
    }

    @Override
    public void afterTextChanged(Editable e) {
        if (atualizando) return;
        atualizando = true;
        try {
            String bruto = e.toString();
            int cursor = Selection.getSelectionStart(e);
            if (cursor < 0 || cursor > bruto.length()) cursor = bruto.length();

            String digitos = Formatadores.apenasDigitos(bruto);
            int digitosAteCursor = Formatadores.apenasDigitos(bruto.substring(0, cursor)).length();

            // Apagou só um separador (parêntese, espaço ou hífen): apaga também o dígito anterior,
            // senão a máscara recolocaria o separador e o usuário não conseguiria apagar.
            if (apagando && digitos.length() == digitosAntes && digitosAteCursor > 0) {
                digitos = digitos.substring(0, digitosAteCursor - 1) + digitos.substring(digitosAteCursor);
                digitosAteCursor--;
            }

            // Número colado com código do país (+55)
            if (digitos.length() > 11 && digitos.startsWith("55")) {
                digitos = digitos.substring(2);
                digitosAteCursor = Math.max(0, digitosAteCursor - 2);
            }
            if (digitos.length() > 11) digitos = digitos.substring(0, 11);
            digitosAteCursor = Math.min(digitosAteCursor, digitos.length());

            String formatado = Formatadores.telefoneParcial(digitos);
            if (!formatado.equals(bruto)) {
                e.replace(0, e.length(), formatado);
            }
            Selection.setSelection(e, posicaoApos(formatado, digitosAteCursor));
        } finally {
            atualizando = false;
        }
    }

    /** Posição do cursor logo depois do n-ésimo dígito do texto formatado. */
    private static int posicaoApos(String formatado, int digitos) {
        if (digitos <= 0) return 0;
        int vistos = 0;
        for (int i = 0; i < formatado.length(); i++) {
            char c = formatado.charAt(i);
            if (c >= '0' && c <= '9' && ++vistos == digitos) return i + 1;
        }
        return formatado.length();
    }
}