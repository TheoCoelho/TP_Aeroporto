public class ListaDEC {
    CelulaDEC cabeca;

    public ListaDEC() {
        cabeca = new CelulaDEC(null); // cabeça sem dado
        cabeca.prox = cabeca;
        cabeca.ant = cabeca;
    }

    public boolean vazia() {
        return cabeca.prox == cabeca;
    }

    public void inserirAoFinal(Aviao aviao) {
        CelulaDEC nova = new CelulaDEC(aviao);
        CelulaDEC ult = cabeca.ant;
        ult.prox = nova;
        nova.ant = ult;
        nova.prox = cabeca;
        cabeca.ant = nova;
    }

    public void imprimir() {
        CelulaDEC aux = cabeca.prox;
        while (aux != cabeca) {
            System.out.println("Histórico: ID" + aux.aviao.id + " (Comb:" + aux.aviao.combustivel + ")");
            aux = aux.prox;
        }
    }
}
