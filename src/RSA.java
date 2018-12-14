import java.util.Random;

class RSA {
    private Long p,
            q,
            n,
            f,
            e,
            d,
            recievedE,
            recievedN;

    Long getN() {
        return n;
    }

    Long getE() {
        return e;
    }

    Long getP() {
        return p;
    }

    Long getQ() {
        return q;
    }

    Long getF() {
        return f;
    }

    Long getD() {
        return d;
    }

    Long getRecievedE() {
        return recievedE;
    }

    Long getRecievedN() {
        return recievedN;
    }

    void setRecievedE(Long recievedE) {
        this.recievedE = recievedE;
    }

    void setRecievedN(Long recievedN) {
        this.recievedN = recievedN;
    }

    RSA() {
        Random random = new Random();
        int loopCount = Main.limitLoops;

        do {
            p = Main.SimpleNumbers[random.nextInt(Main.SimpleNumbers.length)];
        } while (p < 256);

        do {
            q = Main.SimpleNumbers[random.nextInt(Main.SimpleNumbers.length)];
            if (loopCount == 0) break;
            loopCount--;
        } while (q < 256 || q.equals(p));

        n = p * q;
        f = (p - 1) * (q - 1);

        loopCount = Main.limitLoops;
        do {
            e = Main.SimpleNumbers[random.nextInt(Main.SimpleNumbers.length)];
            if (loopCount == 0) break;
            loopCount--;
        } while (e <= 1 || e >= f || !coprimeIntegers(e, f));

        d = enhancedEuclideanAlgorithm(e, f).s;
        while (d < 1) {
            d += f;
        }
    }

    private boolean coprimeIntegers(long i, long j) {
        return enhancedEuclideanAlgorithm(i, j).d == 1;
    }

    private Triple enhancedEuclideanAlgorithm(long i, long j) {
        if (j == 0) {
            return new Triple(i, 1, 0);
        } else {
            Triple tmp = enhancedEuclideanAlgorithm(j, i % j);
            return new Triple(tmp.d, tmp.t, tmp.s - (i / j * tmp.t));
        }
    }

    private class Triple {
        long d,
                s,
                t;

        Triple(long d, long s, long t) {
            this.d = d;
            this.s = s;
            this.t = t;
        }

        @Override
        public String toString() {
            return "Triple{" +
                    "d=" + d +
                    ", s=" + s +
                    ", t=" + t +
                    '}';
        }
    }

}
