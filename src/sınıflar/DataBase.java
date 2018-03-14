/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sınıflar;

import java.sql.*;
import java.util.*;

/**
 *
 * @author Morgoyun
 */
public class DataBase {

    static Connection baglanti = null;
    final private String url = "org.sqlite.JDBC";
    final private String db = "jdbc:sqlite:db/CariHesapTakip.db";
    // private String dbName = "CariHesapTakip.db";

    public Connection baglantiAc() {

        try {
            Class.forName(url);
            baglanti = DriverManager.getConnection(db);
            System.out.println("baglanti acıldı");

        } catch (Exception e) {
            System.err.println("baglanti hatsı olustu: " + e);
        }

        return baglanti;
    }

    public void baglantiKapat() {
        try {
            baglanti.close();
            System.out.println("baglantı kapatıldı");
        } catch (Exception e) {
            System.err.println("baglanti kapatma hatasi: " + e);
        }

    }

    public ArrayList<UrunlerProperty> urunlerBilgisiCek() {
        ArrayList<UrunlerProperty> urunBilgileri = new ArrayList<>();
        try {
            Connection baglanti = baglantiAc();
            String query = "select urunler.uNo,urunler.uAd,kategoriler.katAd,urunler.uAlis,urunler.uSatis,urunler.uStok,kategoriler.katAciklama,kullanicilar.kAd from urunler  join kategoriler on urunler.katNo = kategoriler.katNo join kullanicilar on kullanicilar.kNo=urunler.kNo ";
            PreparedStatement ps = baglanti.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UrunlerProperty up = new UrunlerProperty();
                up.setUrunNo(rs.getString("uNo"));
                up.setUrunAdi(rs.getString("uAd"));
                up.setUrunAlis(rs.getString("uAlis"));
                up.setUrunSatis(rs.getString("uSatis"));
                up.setUrunStok(rs.getString("uStok"));
                up.setKatAd(rs.getString("katAd"));
                urunBilgileri.add(up);
            }
            ps.close();
            rs.close();
            baglantiKapat();
            
        } catch (Exception e) {
        }
        return urunBilgileri;

    }

}
