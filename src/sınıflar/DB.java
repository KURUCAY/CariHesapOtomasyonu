/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sınıflar;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import uygulama.MainForm;

/**
 *
 * @author Morgoyun
 */
public class DB {

    final private String url = "org.sqlite.JDBC";
    final private String db = "jdbc:sqlite:db/";
    private String dbName = "CariHesapTakip.db";

    private Connection conn = null;
    private Statement st = null;
    public static int rndSayi = 0;

    public DB() {
//        rndSayi = new Random().nextInt(99999);
    }

    public DB(String dbName) {
        this.dbName = dbName;
    }
    public static String degisiklikYapan = "";

    public static boolean kontrol = false;
    // bağlan methodu kuruluyor

    public Statement baglan(String ad, String sifre) {

        KullaniciProperty kp = new KullaniciProperty();

        try {
            Class.forName(url);
            conn = DriverManager.getConnection((db + dbName), ad, sifre);
            st = conn.createStatement();
           // System.out.println("Bağlantı başarılı");
            String query = "select * from kullanicilar where kAd='"+ad+"' and kSifre='"+sifre+"' ";
            ResultSet rs = st.executeQuery(query);
           if(rs.next()) {
               degisiklikYapan = rs.getString("kNo");

                    System.out.println("login basarili");
                    MainForm mainn = new MainForm();


                  mainn.setVisible(true);   

                    
                    
                    kontrol = true;
               

            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Bağlantı Hatası : " + e);
        }
        return st;
    }

    public Statement baglan() {
        try {
            Class.forName(url);
            conn = DriverManager.getConnection(db + dbName);
            st = conn.createStatement();
            System.out.println("Bağlantı başarılı");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Bağlantı Hatası : " + e);
        }
        return st;
    }

    // bağlantı kapat
    public void kapat() {
        if (st != null && conn != null) {
            try {
                st.close();
                conn.close();
                st = null;
                conn = null;
                System.out.println("Kapatma Yapıldı");
            } catch (SQLException e) {
                System.err.println("Kapatma Hatası : " + e);
            }
        }
    }
}
