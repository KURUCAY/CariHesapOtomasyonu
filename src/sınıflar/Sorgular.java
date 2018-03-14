/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sınıflar;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Morgoyun
 */
public class Sorgular {

    DB db = new DB();

    public boolean ekle(String tabloAdi, String... n) {

        try {
            String query = "insert into " + tabloAdi + " values(";
            for (String item : n) {
                if (item.equals("null")) {
                    query += "null";
                } else {
                    query += ",'" + item + "' ";
                }
            }
            query += ",'" + DB.degisiklikYapan + "' ";
            query += ")";
            System.out.println(query);
            int yazsonuc = db.baglan().executeUpdate(query);
            if (yazsonuc > 0) {
                return true;
            }

        } catch (SQLException ex) {
            System.err.println("sorgu hatası: " + ex);
        } finally {
            db.kapat();
        }
        return false;
    }

    public ArrayList<KategorilerProperty> kategorilerDataGetir() {
        ArrayList<KategorilerProperty> kategorilerNo = new ArrayList<>();
        try {
            String query = "select katAd,katNo from kategoriler";
            ResultSet rs = db.baglan().executeQuery(query);
            while (rs.next()) {
                KategorilerProperty kp = new KategorilerProperty();
                kp.setKatNo(rs.getString("katNo"));
                kp.setKatAd(rs.getString("katAd"));
                kategorilerNo.add(kp);
            }
        } catch (SQLException e) {
            System.err.println("sorgu hatası:" + e);
        } finally {
            db.kapat();

        }
        return kategorilerNo;
    }

}
