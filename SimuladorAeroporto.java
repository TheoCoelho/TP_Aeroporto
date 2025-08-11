import java.util.Random;

class Aviao {
    final int id;
    final boolean pouso;
    int combustivel;
    final int tickChegada;
    final long ordemChegada;
    Fila.No<Aviao> noFila;

    Aviao(int id, boolean pouso, int combustivel, int tickChegada, long ordemChegada) {
        this.id = id;
        this.pouso = pouso;
        this.combustivel = combustivel;
        this.tickChegada = tickChegada;
        this.ordemChegada = ordemChegada;
    }

    @Override
    public String toString() {
        if (pouso) return String.format("L[%d,f=%d]", id, combustivel);
        return String.format("D[%d]", id);
    }
}

class Fila<T> {
    static class No<T> {
        T valor;
        No<T> prox;
        No<T> ant;
        No(T v) { valor = v; }
    }

    private No<T> cabeca;
    private No<T> cauda;
    private int tamanho;

    public int tamanho() { return tamanho; }
    public boolean vazia() { return tamanho == 0; }

    public No<T> enfileirar(T valor) {
        No<T> n = new No<>(valor);
        if (cauda == null) {
            cabeca = cauda = n;
        } else {
            cauda.prox = n;
            n.ant = cauda;
            cauda = n;
        }
        tamanho++;
        return n;
    }

    public T desenfileirar() {
        if (cabeca == null) return null;
        No<T> n = cabeca;
        cabeca = cabeca.prox;
        if (cabeca != null) cabeca.ant = null;
        else cauda = null;
        tamanho--;
        n.prox = n.ant = null;
        return n.valor;
    }

    public T removerNo(No<T> n) {
        if (n == null) return null;
        if (n.ant != null) n.ant.prox = n.prox;
        else cabeca = n.prox;
        if (n.prox != null) n.prox.ant = n.ant;
        else cauda = n.ant;
        tamanho--;
        T v = n.valor;
        n.prox = n.ant = null;
        return v;
    }

    public String listar() {
        StringBuilder sb = new StringBuilder("[");
        No<T> cur = cabeca;
        boolean first = true;
        while (cur != null) {
            if (!first) sb.append(", ");
            sb.append(cur.valor);
            cur = cur.prox;
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}

class ArvoreAVL {
    static class Chave implements Comparable<Chave> {
        final int combustivel;
        final long ordem;
        Chave(int c, long o) { combustivel = c; ordem = o; }
        @Override
        public int compareTo(Chave x) {
            if (combustivel != x.combustivel) return Integer.compare(combustivel, x.combustivel);
            return Long.compare(ordem, x.ordem);
        }
        @Override
        public String toString() { return "(" + combustivel + "," + ordem + ")"; }
    }

    static class No {
        Chave chave;
        Aviao aviao;
        No esq, dir;
        int altura;
        No(Chave k, Aviao a) { chave = k; aviao = a; altura = 1; }
    }

    private No raiz;

    private int h(No n) { return n == null ? 0 : n.altura; }
    private int fb(No n) { return (n == null) ? 0 : h(n.dir) - h(n.esq); }
    private void att(No n) { n.altura = 1 + Math.max(h(n.esq), h(n.dir)); }

    private No rotacaoEsq(No a) {
        No b = a.dir;
        No t2 = b.esq;
        b.esq = a;
        a.dir = t2;
        att(a); att(b);
        return b;
    }

    private No rotacaoDir(No a) {
        No b = a.esq;
        No t2 = b.dir;
        b.dir = a;
        a.esq = t2;
        att(a); att(b);
        return b;
    }

    private No reequilibrar(No n) {
        att(n);
        int b = fb(n);
        if (b > 1) {
            if (fb(n.dir) < 0) n.dir = rotacaoDir(n.dir);
            return rotacaoEsq(n);
        } else if (b < -1) {
            if (fb(n.esq) > 0) n.esq = rotacaoEsq(n.esq);
            return rotacaoDir(n);
        }
        return n;
    }

    public void inserir(Chave k, Aviao a) { raiz = inserir(raiz, k, a); }
    private No inserir(No n, Chave k, Aviao a) {
        if (n == null) return new No(k, a);
        int cmp = k.compareTo(n.chave);
        if (cmp < 0) n.esq = inserir(n.esq, k, a);
        else if (cmp > 0) n.dir = inserir(n.dir, k, a);
        else n.aviao = a;
        return reequilibrar(n);
    }

    public void remover(Chave k) { raiz = remover(raiz, k); }
    private No remover(No n, Chave k) {
        if (n == null) return null;
        int cmp = k.compareTo(n.chave);
        if (cmp < 0) n.esq = remover(n.esq, k);
        else if (cmp > 0) n.dir = remover(n.dir, k);
        else {
            if (n.esq == null || n.dir == null) {
                n = (n.esq != null) ? n.esq : n.dir;
            } else {
                No s = n.dir;
                while (s.esq != null) s = s.esq;
                n.chave = s.chave;
                n.aviao = s.aviao;
                n.dir = remover(n.dir, s.chave);
            }
        }
        if (n != null) n = reequilibrar(n);
        return n;
    }

    public Aviao minimo() {
        No cur = raiz;
        if (cur == null) return null;
        while (cur.esq != null) cur = cur.esq;
        return cur.aviao;
    }

    public boolean vazia() { return raiz == null; }
}

public class SimuladorAeroporto {
    private final int maxTicks;
    private final int imprimirCada;
    private final Random aleatorio;
    private final boolean verboso;

    private final Fila<Aviao>[] filasPouso = new Fila[4];
    private final Fila<Aviao>[] filasDecolagem = new Fila[3];

    private final ArvoreAVL indiceCombustivel = new ArvoreAVL();

    private long proximoIdImpar = 1;
    private long proximoIdPar = 2;
    private long proximaOrdem = 1;

    private long totalPousos = 0;
    private long totalDecolagens = 0;
    private long somaEsperaPouso = 0;
    private long somaEsperaDecolagem = 0;
    private long pousosEmergencia = 0;

    public SimuladorAeroporto(int maxTicks, int imprimirCada, long semente, boolean verboso) {
        this.maxTicks = maxTicks;
        this.imprimirCada = imprimirCada;
        this.aleatorio = new Random(semente);
        this.verboso = verboso;
        for (int i = 0; i < 4; i++) filasPouso[i] = new Fila<>();
        for (int i = 0; i < 3; i++) filasDecolagem[i] = new Fila<>();
    }

    private int totalFila(Fila<Aviao>[] qs) { int s = 0; for (Fila<Aviao> q : qs) s += q.tamanho(); return s; }
    private int totalPousoFilas() { return totalFila(filasPouso); }
    private int totalDecolagemFilas() { return totalFila(filasDecolagem); }

    private int indiceMaior(Fila<Aviao>[] qs) {
        int idx = -1, best = -1;
        for (int i = 0; i < qs.length; i++) {
            int sz = qs[i].tamanho();
            if (sz > best) { best = sz; idx = i; }
        }
        return (best <= 0) ? -1 : idx;
    }

    private int indiceMenor(Fila<Aviao>[] qs) {
        int idx = 0;
        int best = Integer.MAX_VALUE;
        for (int i = 0; i < qs.length; i++) {
            int sz = qs[i].tamanho();
            if (sz < best) { best = sz; idx = i; }
        }
        return idx;
    }

    public void executar() {
        for (int tick = 1; tick <= maxTicks; tick++) {
            chegadas(tick);
            decrementarCombustivel();
            int usadas = atenderEmergencias(tick);

            if (usadas < 3) {
                if (!servirDecolagem(tick, 2)) {
                    servirPouso(tick);
                }
                usadas++;
            }
            for (int p = 0; p < 2 && usadas < 3; p++, usadas++) {
                if (totalPousoFilas() >= totalDecolagemFilas()) {
                    if (!servirPouso(tick)) {
                        servirDecolagem(tick, p);
                    }
                } else {
                    if (!servirDecolagem(tick, p)) {
                        servirPouso(tick);
                    }
                }
            }

            if (tick % imprimirCada == 0 || tick == maxTicks) {
                imprimirEstado(tick);
            }
        }
        imprimirFinal();
    }

    private void chegadas(int tick) {
        int nPouso = aleatorio.nextInt(4);
        for (int i = 0; i < nPouso; i++) {
            int combustivel = 1 + aleatorio.nextInt(20);
            int id = (int) proximoIdImpar; proximoIdImpar += 2;
            Aviao a = new Aviao(id, true, combustivel, tick, proximaOrdem++);
            int qidx = indiceMenor(filasPouso);
            Fila.No<Aviao> no = filasPouso[qidx].enfileirar(a);
            a.noFila = no;
            indiceCombustivel.inserir(new ArvoreAVL.Chave(a.combustivel, a.ordemChegada), a);
        }
        int nDec = aleatorio.nextInt(4);
        for (int i = 0; i < nDec; i++) {
            int id = (int) proximoIdPar; proximoIdPar += 2;
            Aviao a = new Aviao(id, false, 0, tick, proximaOrdem++);
            int qidx = indiceMenor(filasDecolagem);
            Fila.No<Aviao> no = filasDecolagem[qidx].enfileirar(a);
            a.noFila = no;
        }
    }

    private void decrementarCombustivel() {
        for (Fila<Aviao> q : filasPouso) {
            Fila.No<Aviao> cur = obterCabeca(q);
            while (cur != null) {
                Aviao a = cur.valor;
                if (a.combustivel > 0) {
                    indiceCombustivel.remover(new ArvoreAVL.Chave(a.combustivel, a.ordemChegada));
                    a.combustivel -= 1;
                    indiceCombustivel.inserir(new ArvoreAVL.Chave(a.combustivel, a.ordemChegada), a);
                }
                cur = cur.prox;
            }
        }
    }

    private static Fila.No<Aviao> obterCabeca(Fila<Aviao> q) {
        try {
            java.lang.reflect.Field f = Fila.class.getDeclaredField("cabeca");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            Fila.No<Aviao> h = (Fila.No<Aviao>) f.get(q);
            return h;
        } catch (Exception e) {
            return null;
        }
    }

    private int atenderEmergencias(int tick) {
        int usadas = 0;
        while (usadas < 3) {
            Aviao min = indiceCombustivel.minimo();
            if (min == null || min.combustivel > 0) break;
            removerAviaoPouso(min);
            indiceCombustivel.remover(new ArvoreAVL.Chave(min.combustivel, min.ordemChegada));
            registrarPouso(min, tick, true);
            usadas++;
        }
        return usadas;
    }

    private void removerAviaoPouso(Aviao a) {
        for (Fila<Aviao> q : filasPouso) {
            Fila.No<Aviao> cur = obterCabeca(q);
            while (cur != null) {
                if (cur == a.noFila) {
                    q.removerNo(cur);
                    a.noFila = null;
                    return;
                }
                cur = cur.prox;
            }
        }
    }

    private boolean servirPouso(int tick) {
        int idx = indiceMaior(filasPouso);
        if (idx == -1) return false;
        Aviao a = filasPouso[idx].desenfileirar();
        if (a == null) return false;
        indiceCombustivel.remover(new ArvoreAVL.Chave(a.combustivel, a.ordemChegada));
        registrarPouso(a, tick, false);
        return true;
    }

    private void registrarPouso(Aviao a, int tick, boolean emergencia) {
        int espera = tick - a.tickChegada;
        totalPousos++;
        somaEsperaPouso += espera;
        if (emergencia) pousosEmergencia++;
        if (verboso) System.out.printf("Tick %d: POUSO %s %s (espera=%d)%n", tick, a, emergencia ? "[EMERGENCIA]" : "", espera);
    }

    private boolean servirDecolagem(int tick, int pistaIdx) {
        Fila<Aviao> q = filasDecolagem[pistaIdx];
        Aviao a = q.desenfileirar();
        if (a == null) return false;
        int espera = tick - a.tickChegada;
        totalDecolagens++;
        somaEsperaDecolagem += espera;
        if (verboso) System.out.printf("Tick %d: DECOLAGEM %s pista %d (espera=%d)%n", tick, a, pistaIdx + 1, espera);
        return true;
    }

    private void imprimirEstado(int tick) {
        System.out.println("--------------------------------------------------");
        System.out.printf("TICK %d%n", tick);
        System.out.println("Filas de POUSO (4 prateleiras):");
        for (int i = 0; i < 4; i++) System.out.printf("  L%d: %s%n", i + 1, filasPouso[i].listar());
        System.out.println("Filas de DECOLAGEM (3 pistas):");
        for (int i = 0; i < 3; i++) System.out.printf("  D%d: %s%n", i + 1, filasDecolagem[i].listar());
        double mediaL = totalPousos == 0 ? 0.0 : (double) somaEsperaPouso / totalPousos;
        double mediaD = totalDecolagens == 0 ? 0.0 : (double) somaEsperaDecolagem / totalDecolagens;
        System.out.printf("Total pousos: %d | media espera pouso: %.2f%n", totalPousos, mediaL);
        System.out.printf("Total decolagens: %d | media espera decolagem: %.2f%n", totalDecolagens, mediaD);
        System.out.printf("Pousos em EMERGENCIA: %d%n", pousosEmergencia);
        System.out.println("--------------------------------------------------");
    }

    private void imprimirFinal() {
        System.out.println("\n==================== RESUMO FINAL ====================");
        double mediaL = totalPousos == 0 ? 0.0 : (double) somaEsperaPouso / totalPousos;
        double mediaD = totalDecolagens == 0 ? 0.0 : (double) somaEsperaDecolagem / totalDecolagens;
        System.out.printf("Pousos: %d | Media de espera (pouso): %.2f%n", totalPousos, mediaL);
        System.out.printf("Decolagens: %d | Media de espera (decolagem): %.2f%n", totalDecolagens, mediaD);
        System.out.printf("Pousos em EMERGENCIA: %d%n", pousosEmergencia);
        System.out.println("======================================================");
    }

    public static void main(String[] args) {
        int ticks = 200;
        int imprimirCada = 10;
        long semente = 42L;
        boolean verboso = false;

        if (args.length >= 1) ticks = Integer.parseInt(args[0]);
        if (args.length >= 2) imprimirCada = Integer.parseInt(args[1]);
        if (args.length >= 3) semente = Long.parseLong(args[2]);
        if (args.length >= 4) verboso = Boolean.parseBoolean(args[3]);

        SimuladorAeroporto sim = new SimuladorAeroporto(ticks, imprimirCada, semente, verboso);
        sim.executar();
    }
}
