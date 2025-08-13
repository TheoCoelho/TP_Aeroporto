// Implementação de uma fila encadeada 
public class FilaEncadeada {
    Celula frente; 
    Celula tras;   
    int tamanho;   

    public FilaEncadeada() {
        frente = new Celula(null); 
        tras = frente;
        tamanho = 0;
    }

    public boolean vazia() {
        return frente == tras;
    }

    public void enfileirar(Aviao aviao) {
        Celula nova = new Celula(aviao);
        tras.prox = nova;
        tras = nova;
        tamanho++;
    }

    public Aviao desenfileirar() {
        if (vazia()) return null;
        Celula primeira = frente.prox;
        frente.prox = primeira.prox;
        if (tras == primeira) tras = frente;
        tamanho--;
        return primeira.aviao;
    }

    public Aviao espiar() {
        if (vazia()) return null;
        return frente.prox.aviao;
    }

    public int tamanho() {
        return tamanho;
    }

    public void imprimirFila() {
        Celula aux = frente.prox;
        System.out.print("[");
        while (aux != null) {
            Aviao a = aux.aviao;
            System.out.print("ID" + a.id + (a.combustivel > 0 ? "(C:" + a.combustivel + ")" : ""));
            aux = aux.prox;
            if (aux != null) System.out.print(" | ");
        }
        System.out.println("]");
    }
}
