package br.com.fiap.copiloto.algoritmo;

import java.util.List;
import java.util.function.Function;

public final class BuscaBinaria {

    private BuscaBinaria() {
    }

    /**
     * O(log n) lookup. The list must already be sorted by the extracted key.
     * Returns the index, or -1 when the key is absent.
     */
    public static <T, K extends Comparable<K>> int buscar(List<T> ordenada, K chave, Function<T, K> extrairChave) {
        int inicio = 0;
        int fim = ordenada.size() - 1;
        while (inicio <= fim) {
            int meio = (inicio + fim) >>> 1;
            int comparacao = extrairChave.apply(ordenada.get(meio)).compareTo(chave);
            if (comparacao == 0) {
                return meio;
            }
            if (comparacao < 0) {
                inicio = meio + 1;
            } else {
                fim = meio - 1;
            }
        }
        return -1;
    }
}
