package br.com.fiap.copiloto.algoritmo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Exact visiting order for a few stops with fixed start and end (Held-Karp dynamic programming).
 * custo[mascara][ultimo] = shortest walk that starts at the entrance, visits exactly the stops
 * in mascara and ends at ultimo. O(n² · 2ⁿ): instant for the 12 corridors of a store.
 */
public final class MelhorOrdemVisita {

    public static final int MAX_PARADAS = 12;

    private MelhorOrdemVisita() {
    }

    /**
     * @param doInicio    distance from the start to each stop
     * @param entreParadas distance between every pair of stops
     * @param ateFim      distance from each stop to the end
     * @return stop indexes in the best order
     */
    public static List<Integer> resolver(double[] doInicio, double[][] entreParadas, double[] ateFim) {
        int n = doInicio.length;
        if (n == 0) {
            return List.of();
        }
        if (n > MAX_PARADAS) {
            throw new IllegalArgumentException("Too many stops for exact search: " + n);
        }
        int todas = (1 << n) - 1;
        double[][] custo = new double[1 << n][n];
        int[][] anterior = new int[1 << n][n];
        for (double[] linha : custo) {
            Arrays.fill(linha, Double.POSITIVE_INFINITY);
        }
        for (int i = 0; i < n; i++) {
            custo[1 << i][i] = doInicio[i];
            anterior[1 << i][i] = -1;
        }
        for (int mascara = 1; mascara <= todas; mascara++) {
            for (int ultimo = 0; ultimo < n; ultimo++) {
                if ((mascara & (1 << ultimo)) == 0 || custo[mascara][ultimo] == Double.POSITIVE_INFINITY) {
                    continue;
                }
                for (int proximo = 0; proximo < n; proximo++) {
                    if ((mascara & (1 << proximo)) != 0) {
                        continue;
                    }
                    int novaMascara = mascara | (1 << proximo);
                    double novoCusto = custo[mascara][ultimo] + entreParadas[ultimo][proximo];
                    if (novoCusto < custo[novaMascara][proximo]) {
                        custo[novaMascara][proximo] = novoCusto;
                        anterior[novaMascara][proximo] = ultimo;
                    }
                }
            }
        }
        int melhorUltimo = 0;
        double melhorTotal = Double.POSITIVE_INFINITY;
        for (int ultimo = 0; ultimo < n; ultimo++) {
            double total = custo[todas][ultimo] + ateFim[ultimo];
            if (total < melhorTotal) {
                melhorTotal = total;
                melhorUltimo = ultimo;
            }
        }
        List<Integer> ordem = new ArrayList<>();
        int mascara = todas;
        int atual = melhorUltimo;
        while (atual != -1) {
            ordem.add(atual);
            int antes = anterior[mascara][atual];
            mascara &= ~(1 << atual);
            atual = antes;
        }
        Collections.reverse(ordem);
        return ordem;
    }
}
