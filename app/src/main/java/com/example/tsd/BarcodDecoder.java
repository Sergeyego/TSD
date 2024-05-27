package com.example.tsd;

public class BarcodDecoder {
    public static class Barcod {
        String numPart;
        String type;
        String ean;
        String barcodeCont;
        int id_part;
        int kvom;
        double kvo;
        int yearPart;
        boolean ok;

        Barcod(boolean ok, int id_part, double kvo, int kvom, String type, String numPart, int yearPart, String ean, String barcodeCont) {
            this.ok = ok;
            this.id_part = id_part;
            this.kvo = kvo;
            this.kvom = kvom;
            this.type = type;
            this.numPart = numPart;
            this.yearPart = yearPart;
            this.ean = ean;
            this.barcodeCont = barcodeCont;
        }
    }

    public static int getInt(String str, int def) {
        int num = def;
        try {
            num = Integer.parseInt(str);
        } catch (NumberFormatException e) {
        }
        return num;
    }

    public static Barcod decode(String str) {
        String numPart = "";
        String type = "";
        String ean = "";
        String barcodeCont = "";
        int id_part = -1;
        int kvom = 0;
        double kvo = 0;
        int yearPart = -1;
        boolean ok = str.length() == 13 || str.length() == 30 || str.length() == 40 || str.length() == 50;
        if (ok) {
            ean = str.substring(0, 13);
            if (str.length() == 30 || str.length() == 40 || str.length() == 50) {
                type = str.substring(13, 14);
                String id_p = str.substring(14, 21);
                id_p = id_p.replace("_", "");
                id_part = getInt(id_p, id_part);
                numPart = str.substring(21, 25);
                yearPart = getInt(str.substring(26, 30), yearPart);
                if (str.length() == 40 || str.length() == 50) {
                    int mas = getInt(str.substring(30, 36), 0);
                    kvo = mas / 100.0;
                    kvom = getInt(str.substring(36, 40), kvom);
                }
                if (str.length() == 50) {
                    barcodeCont = str.substring(40);
                }
            }
        }
        return new Barcod(ok, id_part, kvo, kvom, type, numPart, yearPart, ean, barcodeCont);
    }

    ;
}
