package br.com.fiap.copiloto.algoritmo;

import br.com.fiap.copiloto.model.ArestaMapa;
import br.com.fiap.copiloto.model.NoMapa;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlgoritmosTest {

    @Test
    void mergeSortIsStableAndSorts() {
        record Item(int chave, String id) {
        }
        List<Item> itens = List.of(new Item(3, "a"), new Item(1, "b"), new Item(3, "c"), new Item(2, "d"), new Item(1, "e"));
        List<Item> ordenados = Ordenacao.mergeSort(itens, Comparator.comparingInt(Item::chave));
        assertEquals(List.of("b", "e", "d", "a", "c"), ordenados.stream().map(Item::id).toList());
    }

    @Test
    void binarySearchFindsEveryKeyAndRejectsMissing() {
        List<String> skus = List.of("LM1001", "LM10002", "LM5003", "LM7001", "LM9001");
        List<String> ordenados = Ordenacao.mergeSort(skus, Comparator.naturalOrder());
        for (String sku : skus) {
            assertEquals(sku, ordenados.get(BuscaBinaria.buscar(ordenados, sku, Function.identity())));
        }
        assertEquals(-1, BuscaBinaria.buscar(ordenados, "LM0000", Function.identity()));
    }

    @Test
    void dijkstraPrefersShorterWeightedPath() {
        List<NoMapa> nos = List.of(no(1), no(2), no(3), no(4));
        List<ArestaMapa> arestas = List.of(
                new ArestaMapa(1, 2, 10), new ArestaMapa(2, 4, 10),
                new ArestaMapa(1, 3, 2), new ArestaMapa(3, 4, 3));
        GrafoLoja.CaminhosMinimos minimos = new GrafoLoja(nos, arestas).dijkstra(1);
        assertEquals(5.0, minimos.distanciaAte(4));
        assertEquals(List.of(1L, 3L, 4L), minimos.caminhoAte(4));
    }

    @Test
    void heldKarpMatchesBruteForce() {
        Random aleatorio = new Random(42);
        for (int rodada = 0; rodada < 50; rodada++) {
            int n = 1 + aleatorio.nextInt(7);
            double[] inicio = new double[n];
            double[] fim = new double[n];
            double[][] entre = new double[n][n];
            for (int i = 0; i < n; i++) {
                inicio[i] = 1 + aleatorio.nextInt(100);
                fim[i] = 1 + aleatorio.nextInt(100);
                for (int j = 0; j < n; j++) {
                    entre[i][j] = i == j ? 0 : 1 + aleatorio.nextInt(100);
                }
            }
            List<Integer> ordem = MelhorOrdemVisita.resolver(inicio, entre, fim);
            assertEquals(n, ordem.size());
            assertEquals(forcaBruta(inicio, entre, fim), custo(ordem, inicio, entre, fim), 1e-9);
        }
    }

    private static NoMapa no(long id) {
        return new NoMapa(id, "N" + id, "PASSAGEM", 0, 0);
    }

    private static double custo(List<Integer> ordem, double[] inicio, double[][] entre, double[] fim) {
        double total = inicio[ordem.get(0)];
        for (int i = 1; i < ordem.size(); i++) {
            total += entre[ordem.get(i - 1)][ordem.get(i)];
        }
        return total + fim[ordem.get(ordem.size() - 1)];
    }

    private static double forcaBruta(double[] inicio, double[][] entre, double[] fim) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < inicio.length; i++) {
            indices.add(i);
        }
        return permutar(indices, 0, inicio, entre, fim);
    }

    private static double permutar(List<Integer> a, int k, double[] inicio, double[][] entre, double[] fim) {
        if (k == a.size()) {
            return custo(a, inicio, entre, fim);
        }
        double melhor = Double.POSITIVE_INFINITY;
        for (int i = k; i < a.size(); i++) {
            java.util.Collections.swap(a, k, i);
            melhor = Math.min(melhor, permutar(a, k + 1, inicio, entre, fim));
            java.util.Collections.swap(a, k, i);
        }
        return melhor;
    }
}
