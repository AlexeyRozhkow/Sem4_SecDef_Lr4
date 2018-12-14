class Coder {
    static String codeRSA(Long e, Long n, String message) {
        char[] string = message.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        long res;

        for (int i = 0; i < string.length; i++) {
            res = string[i];
            for (int j = 0; j < e - 1; j++) {
                res = ((res % n) * (string[i] % n)) % n;
            }
            stringBuilder.append(res);
            if (i != string.length - 1) stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    static String decodeRSA(Long d, Long n, String message)  {
        String[] symbols = message.split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        long res;

        for (String symbol : symbols) {
            res = Long.parseLong(symbol);
            for (int j = 0; j < d - 1; j++) {
                res = ((res % n) * (Long.parseLong(symbol) % n)) % n;
            }
            stringBuilder.append((char) res);
        }
        return stringBuilder.toString();
    }
}
