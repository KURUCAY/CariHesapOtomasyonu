/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sınıflar;

/**
 *
 * @author Morgoyun
 */
public class UrunlerProperty {

    private String urunAdi, urunNo,
            urunAlis, urunSatis,
            urunStok, katAd,ID,aciklama;

    public String getID() {
        return ID;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getUrunNo() {
        return urunNo;
    }

    public void setUrunNo(String urunNo) {
        this.urunNo = urunNo;
    }

    public String getUrunAdi() {
        return urunAdi;
    }

    public void setUrunAdi(String urunAdi)  {
        
        this.urunAdi =urunAdi;
    }

    public String getUrunAlis() {
        return urunAlis;
    }

    public void setUrunAlis(String urunAlis) {
        this.urunAlis = urunAlis;
    }

    public String getUrunSatis() {
        return urunSatis;
    }

    public void setUrunSatis(String urunSatis) {
        this.urunSatis = urunSatis;
    }

    public String getUrunStok() {
        return urunStok;
    }

    public void setUrunStok(String urunStok) {
        this.urunStok = urunStok;
    }

    public String getKatAd() {
        return katAd;
    }

    public void setKatAd(String katAd) {
        this.katAd = katAd;
    }

    

}
