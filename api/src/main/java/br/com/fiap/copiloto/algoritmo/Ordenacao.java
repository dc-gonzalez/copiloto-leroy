package br.com.fiap.copiloto.algoritmo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Merge sort: stable and O(n log n) in every case, so equal keys keep their original order. */
public final class Ordenacao {

    private Ordenacao() {
    }

    public static <T> List<T> mergeSort(List<T> lista, Comparator<? super T> comparador) {
        if (lista.size() <= 1) {
            return new ArrayList<>(lista);
        }
        int meio = lista.size() / 2;
        List<T> esquerda = mergeSort(lista.subList(0, meio), comparador);
        List<T> direita = mergeSort(lista.subList(meio, lista.size()), comparador);
        return intercalar(esquerda, direita, comparador);
    }

    private static <T> List<T> intercalar(List<T> esquerda, List<T> direita, Comparator<? super T> comparador) {
        List<T> resultado = new ArrayList<>(esquerda.size() + direita.size());
        int i = 0;
        int j = 0;
        while (i < esquerda.size() && j < direita.size()) {
            if (comparador.compare(esquerda.get(i), direita.get(j)) <= 0) {
                resultado.add(esquerda.get(i++));
            } else {
                resultado.add(direita.get(j++));
            }
        }
        while (i < esquerda.size()) {
            resultado.add(esquerda.get(i++));
        }
        while (j < direita.size()) {
            resultado.add(direita.get(j++));
        }
        return resultado;
    }
}
