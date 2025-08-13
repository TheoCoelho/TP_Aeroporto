import java.util.Random;

public class SimuladorAeroporto {
    static final int TEMPO_MAX = 30;

    public static void main(String[] args) {
        Random random = new Random();

        FilaEncadeada[] prateleirasAterrissagem = {
            new FilaEncadeada(), new FilaEncadeada(),
            new FilaEncadeada(), new FilaEncadeada()
        };

        FilaEncadeada[] filasDecolagem = {
            new FilaEncadeada(), new FilaEncadeada(), new FilaEncadeada()
        };

        // Lista duplamente encadeada circular para histórico
        ListaDEC historico = new ListaDEC();

        int idAterrissagem = 1; // ímpares
        int idDecolagem = 2;    // pares
        int tempoAtual = 0;
        int totalEsperaAterrissagem = 0, totalEsperaDecolagem = 0;
        int avioesPousaram = 0, avioesDecolaram = 0, avioesSemCombustivel = 0;

        while (tempoAtual < TEMPO_MAX) {
            System.out.println("\n=== Tempo " + tempoAtual + " ===");

            int qtdAterrissagem = random.nextInt(4);
            for (int i = 0; i < qtdAterrissagem; i++) {
                int combustivel = 1 + random.nextInt(20);
                Aviao novo = new Aviao(idAterrissagem, combustivel, tempoAtual);
                idAterrissagem += 2;

                int indiceMenor = menorFila(prateleirasAterrissagem);
                prateleirasAterrissagem[indiceMenor].enfileirar(novo);
                System.out.println("Novo avião pouso ID" + novo.id + " -> prateleira " + (indiceMenor + 1));
            }

            int qtdDecolagem = random.nextInt(4);
            for (int i = 0; i < qtdDecolagem; i++) {
                Aviao novo = new Aviao(idDecolagem, 0, tempoAtual);
                idDecolagem += 2;

                int indiceMenor = menorFila(filasDecolagem);
                filasDecolagem[indiceMenor].enfileirar(novo);
                System.out.println("Novo avião decolagem ID" + novo.id + " -> pista " + (indiceMenor + 1));
            }

            for (FilaEncadeada f : prateleirasAterrissagem) {
                Celula aux = f.frente.prox;
                while (aux != null) {
                    aux.aviao.combustivel--;
                    aux = aux.prox;
                }
            }

            //  pistas
            for (int pista = 1; pista <= 3; pista++) {
                Aviao atendido = null;

                if (pista == 3) atendido = buscarEmergencia(prateleirasAterrissagem);
                if (atendido == null && pista != 3) atendido = buscarNormal(prateleirasAterrissagem);

                if (atendido == null) {
                    atendido = filasDecolagem[pista - 1].desenfileirar();
                    if (atendido != null) {
                        avioesDecolaram++;
                        totalEsperaDecolagem += tempoAtual - atendido.tempoEntradaFila;
                        historico.inserirAoFinal(atendido);
                        System.out.println("t=" + tempoAtual + ": Decolagem ID" + atendido.id + " pista " + pista);
                    }
                } else {
                    avioesPousaram++;
                    totalEsperaAterrissagem += tempoAtual - atendido.tempoEntradaFila;
                    if (atendido.combustivel < 0) avioesSemCombustivel++;
                    historico.inserirAoFinal(atendido);
                    System.out.println("t=" + tempoAtual + ": Pouso ID" + atendido.id + " pista " + pista +
                        (atendido.combustivel < 0 ? " **SEM COMBUSTÍVEL**" : ""));
                }
            }

            System.out.println("\nAterrissagem:");
            for (int i = 0; i < prateleirasAterrissagem.length; i++) {
                System.out.print("Prateleira " + (i + 1) + ": ");
                prateleirasAterrissagem[i].imprimirFila();
            }

            System.out.println("\nDecolagem:");
            for (int i = 0; i < filasDecolagem.length; i++) {
                System.out.print("Pista " + (i + 1) + ": ");
                filasDecolagem[i].imprimirFila();
            }

            tempoAtual++;
        }

        System.out.println("\n=== Estatísticas finais ===");
        System.out.println("Tempo médio espera decolagem: " + (avioesDecolaram > 0 ? (double) totalEsperaDecolagem / avioesDecolaram : 0));
        System.out.println("Tempo médio espera aterrissagem: " + (avioesPousaram > 0 ? (double) totalEsperaAterrissagem / avioesPousaram : 0));
        System.out.println("Aviões sem combustível: " + avioesSemCombustivel);

        System.out.println("\nHistórico de atendimentos:");
        historico.imprimir();
    }

    static int menorFila(FilaEncadeada[] filas) {
        int menor = 0;
        for (int i = 1; i < filas.length; i++) {
            if (filas[i].tamanho() < filas[menor].tamanho()) menor = i;
        }
        return menor;
    }

    static Aviao buscarEmergencia(FilaEncadeada[] prateleiras) {
        for (FilaEncadeada f : prateleiras) {
            if (!f.vazia() && f.espiar().combustivel <= 0) return f.desenfileirar();
        }
        return null;
    }

    static Aviao buscarNormal(FilaEncadeada[] prateleiras) {
        for (FilaEncadeada f : prateleiras) {
            if (!f.vazia()) return f.desenfileirar();
        }
        return null;
    }
}
