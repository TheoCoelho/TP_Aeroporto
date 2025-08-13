// Célula para lista duplamente encadeada circular
public class CelulaDEC {
    Aviao aviao;
    CelulaDEC prox;
    CelulaDEC ant;

    public CelulaDEC(Aviao aviao) {
        this.aviao = aviao;
        this.prox = null;
        this.ant = null;
    }
}
