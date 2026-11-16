package br.com.fiap.sbp.domain.enums;

/**
 * As oito areas de operacao da rede municipal de Sao Paulo.
 * No GTFS da SPTrans cada area e identificada pela cor oficial da linha.
 */
public enum AreaOperacao {

    NOROESTE("509E2F"),
    NORTE("002F6C"),
    NORDESTE("FFD100"),
    LESTE("DA291C"),
    SUDESTE("006341"),
    SUL("0082BA"),
    SUDOESTE("782F40"),
    OESTE("FF671F");

    private final String corHex;

    AreaOperacao(String corHex) {
        this.corHex = corHex;
    }

    public String getCorHex() {
        return corHex;
    }

    /** Converte a cor publicada no GTFS na area de operacao correspondente. */
    public static AreaOperacao porCorGtfs(String corHex) {
        if (corHex == null) {
            return null;
        }
        for (AreaOperacao area : values()) {
            if (area.corHex.equalsIgnoreCase(corHex.trim())) {
                return area;
            }
        }
        return null;
    }
}
