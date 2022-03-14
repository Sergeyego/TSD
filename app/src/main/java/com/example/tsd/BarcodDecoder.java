package com.example.tsd;

public class BarcodDecoder {
    public static class Barcod {
        String numPart;
        String type;
        String ean;
        int id_part;
        int kvom;
        double kvo;
        int yearPart;
        boolean ok;

        Barcod(boolean ok, int id_part, double kvo, int kvom, String type, String numPart, int yearPart, String ean) {
            this.ok = ok;
            this.id_part=id_part;
            this.kvo=kvo;
            this.kvom=kvom;
            this.type = type;
            this.numPart = numPart;
            this.yearPart=yearPart;
            this.ean = ean;
        }
    }

    public static Barcod decode(String str){
        String numPart="";
        String type="";
        String ean="";
        int id_part=-1;
        int kvom=0;
        double kvo=0;
        int yearPart=-1;
        boolean ok=str.length()==13 || str.length()==30 || str.length()==40;
        if (ok) {
            ean = str.substring(0, 13);
            if (str.length() == 30 || str.length() == 40) {

                type = str.substring(13, 14);

                String id_p = str.substring(14, 21);
                id_p = id_p.replace("_", "");
                id_part = Integer.parseInt(id_p);

                numPart = str.substring(21, 25);

                yearPart = Integer.parseInt(str.substring(26, 30));

                if (str.length() == 40) {
                    kvo = Integer.parseInt(str.substring(30, 36)) / 100.0;
                    kvom = Integer.parseInt(str.substring(36));
                }

            }
        }
        return new Barcod(ok,id_part,kvo,kvom,type,numPart,yearPart,ean);
    };
}
