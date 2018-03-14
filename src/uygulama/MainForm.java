/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uygulama;

import java.awt.Component;
import java.awt.HeadlessException;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import javax.swing.table.DefaultTableModel;
import sınıflar.DB;

import sınıflar.KategorilerProperty;
import sınıflar.MusterilerProperty;

import sınıflar.Sorgular;
import sınıflar.UrunlerProperty;

/**
 *
 * @author Morgoyun
 */
public class MainForm extends javax.swing.JFrame {

    /**
     * Creates new form MainForm
     */
    DB db = new DB();
    Sorgular sr = new Sorgular();

    public MainForm() {
        initComponents();

        DB db = new DB();
        if (urunBitmisMi) {
            JOptionPane.showConfirmDialog(rootPane, "Biten Ürünler Var Stok Kontrolü Yapınız!");
        }
        btnUrunlerSil.setEnabled(false);
        btnMusteriSil.setEnabled(false);

        urunlerDataGetir();
        kategoriGetir();
        MusteriGetir();
        satisGoruntule();

    }
    int bitenUrunSayisi = 0;
    boolean urunBitmisMi = false;

    public void aramalar() {
        DefaultTableModel dtm = new DefaultTableModel();
        dtm.addColumn("UrunlerID");
        dtm.addColumn("Ad");
        dtm.addColumn("Kategori");
        dtm.addColumn("Alış");
        dtm.addColumn("Satış");
        dtm.addColumn("stok");
        dtm.addColumn("Açıklama");

        for (UrunlerProperty item : urunlerPropertyArray) {
            if (item.getUrunAdi().contains(txtArama.getText())) {
                dtm.addRow(new String[]{item.getID(), item.getUrunAdi(), item.getKatAd(), item.getUrunAlis(), item.getUrunSatis(), item.getUrunStok(), item.getAciklama()});
            } else if (item.getKatAd().contains(txtArama.getText())) {
                dtm.addRow(new String[]{item.getID(), item.getUrunAdi(), item.getKatAd(), item.getUrunAlis(), item.getUrunSatis(), item.getUrunStok(), item.getAciklama()});
            } else if (item.getUrunStok().contains(txtArama.getText())) {
                dtm.addRow(new String[]{item.getID(), item.getUrunAdi(), item.getKatAd(), item.getUrunAlis(), item.getUrunSatis(), item.getUrunStok(), item.getAciklama()});
            } else if (item.getUrunSatis().contains(txtArama.getText())) {
                dtm.addRow(new String[]{item.getID(), item.getUrunAdi(), item.getKatAd(), item.getUrunAlis(), item.getUrunSatis(), item.getUrunStok(), item.getAciklama()});
            }
        }
        tblUrunler.setModel(dtm);

    }

    public void bitenStok() {
        try {
            DefaultListModel<String> dlm = new DefaultListModel<>();
            DefaultListModel<String> dlm2 = new DefaultListModel<>();
            String query = "select uStok,uAd from urunler where uStok=0 ";
            ResultSet rs = db.baglan().executeQuery(query);
            while (rs.next()) {
                dlm.addElement(rs.getString("uAd"));
                dlm2.addElement((rs.getString("uAd")) + "/" + rs.getString("uStok"));
                bitenUrunSayisi++;
                urunBitmisMi = true;

            }

            listeBitenUrunler.setModel(dlm);
            listeUrunAz.setModel(dlm2);
        } catch (SQLException e) {
            System.err.println("depo urunlerini cekerken hata! " + e);
        } finally {
            db.kapat();
        }

    }

    public void azalanStok() {
        try {
            DefaultListModel<String> dlm = new DefaultListModel<>();

            String query = "select uStok,uAd from urunler where uStok>=0 and uStok<='" + txtUrunKalanMiktar.getText().trim() + "' ";
            ResultSet rs = db.baglan().executeQuery(query);
            while (rs.next()) {
                dlm.addElement(rs.getString("uAd") + " - " + rs.getString("uStok"));

                bitenUrunSayisi++;
                urunBitmisMi = true;

            }

            listeUrunAz.setModel(dlm);
        } catch (SQLException e) {
            System.err.println("depo urunlerini cekerken hata! " + e);
        } finally {
            db.kapat();
        }

    }

    public String tarihDuzenle(String tarihGirilen) {
        //Date date=new Date(tarihGirilen);

        return df.format(tarihGirilen);
    }
    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    ArrayList<String> urunlerIDKategoriList = new ArrayList<>();
    ArrayList<String> urunlerIDList = new ArrayList<>();

    public void raporlama(String tarih1, String tarih2) {
        DefaultTableModel dtm = new DefaultTableModel();
        dtm.addColumn("SatışID");
        dtm.addColumn("Müşteri Adı");
        dtm.addColumn("Ürün Ad");
        dtm.addColumn("Satış Fiyatı");
        dtm.addColumn("Adet");
        dtm.addColumn("Tarih");
        try {
            String query = "select *  from satis join urunler on satis.uNo=urunler.uNo join musteriler on satis.mNo=musteriler.mNo where sTarih  between '" + tarih1 + "' and '" + tarih2 + "' ";
            ResultSet rs = db.baglan().executeQuery(query);
            while (rs.next()) {
                dtm.addRow(new String[]{rs.getString("sNo"), rs.getString("mAd"), rs.getString("uAd"), rs.getString("sSatisFiyat"), rs.getString("sMiktar"), rs.getString("sTarih")});
            }
            tblRaporlama.setModel(dtm);
            System.out.println("qqq" + query);
        } catch (Exception e) {
            System.err.println("satış listeleme hatasi! " + e);
        } finally {
            db.kapat();
        }

    }

    ArrayList<UrunlerProperty> urunlerPropertyArray = new ArrayList<>();
    ArrayList<String> urunAdList = new ArrayList<>();

    public void urunlerDataGetir() {
        try {
            urunlerPropertyArray.clear();

            String musteriQuery = "select urunler.katNo,urunler.uNo,urunler.uAd,kategoriler.katAd,urunler.uAlis,urunler.uSatis,urunler.uStok,kategoriler.katAciklama from urunler  join kategoriler on urunler.katNo = kategoriler.katNo ";
            urunlerIDKategoriList.clear();
            urunlerIDList.clear();
            urunAdList.clear();
            ResultSet rs = db.baglan().executeQuery(musteriQuery);
            DefaultTableModel dtm = new DefaultTableModel();
            dtm.addColumn("UrunlerID");
            dtm.addColumn("Ad");
            dtm.addColumn("Kategori");
            dtm.addColumn("Alış");
            dtm.addColumn("Satış");
            dtm.addColumn("stok");
            dtm.addColumn("Açıklama");
            //satış sayfasındaki table dolduruluyor.
            DefaultTableModel dtm1 = new DefaultTableModel();
            dtm1.addColumn("ID");
            dtm1.addColumn("Ad");
            dtm1.addColumn("Satış");
            dtm1.addColumn("stok");
            // satırlara gelecek datalar alınıyor
            while (rs.next()) {
                dtm.addRow(new String[]{rs.getString("uNo"), rs.getString("uAd"), rs.getString("katAd"), rs.getString("uAlis"), rs.getString("uSatis"), rs.getString("uStok"), rs.getString("katAciklama")});
                dtm1.addRow(new String[]{rs.getString("uNo"), rs.getString("uAd"),
                    rs.getString("uSatis"), rs.getString("uStok")});

                urunlerIDKategoriList.add(rs.getString("katNo"));
                urunlerIDList.add(rs.getString("uNo"));
                urunAdList.add(rs.getString("uAd"));

                UrunlerProperty up = new UrunlerProperty();

                up.setID(rs.getString("uNo"));

                up.setUrunAdi(rs.getString("uAd"));

                up.setUrunAlis(rs.getString("uAlis"));
                up.setUrunSatis(rs.getString("uSatis"));
                up.setUrunStok(rs.getString("uStok"));
                up.setKatAd(rs.getString("katAd"));
                up.setAciklama(rs.getString("katAciklama"));
                urunlerPropertyArray.add(up);

            }
            tblUrunler.setModel(dtm);
            tblurunlerSatis.setModel(dtm1);
        } catch (SQLException e) {
            System.err.println("Data getirme hatası : " + e);
        } finally {
            db.kapat();
        }
    }

    public boolean urunKategoriKontrol(String secilenID) {
        boolean kayitVar = false;
        try {
            String query = "select urunler.katNo  from kategoriler join kullanicilar on kategoriler.katNo=urunler.katNo where katno='" + secilenID + "'";

            ResultSet rs = db.baglan().executeQuery(query);
            while (rs.next()) {
                kayitVar = true;
            }

        } catch (SQLException ex) {
            System.err.println("kategori kontrol ederken bir hata olustu! " + ex);;
        } finally {
            db.kapat();
        }
        return kayitVar;
    }
    ArrayList<String> kategoriIDList = new ArrayList<>();
    ArrayList<String> kategoriAdList = new ArrayList<>();

    public void kategoriGetir() {

        try {
            String query = "select kategoriler.katNo,kategoriler.katAd,kategoriler.katAciklama from kategoriler";
            DefaultTableModel dtm = new DefaultTableModel();
            DefaultComboBoxModel dcbm = new DefaultComboBoxModel();
            DefaultComboBoxModel dcbm2 = new DefaultComboBoxModel();
            kategoriIDList.clear();
            kategoriAdList.clear();
            dtm.addColumn("KategoriID");
            dtm.addColumn("Kategori Adı");
            dtm.addColumn("Kategori Açıklama");

            ResultSet rs = db.baglan().executeQuery(query);
            while (rs.next()) {
                dtm.addRow(new String[]{rs.getString("katNo"), rs.getString("katAd"), rs.getString("katAciklama")});
                dcbm.addElement(rs.getString("katAd"));
                dcbm2.addElement(rs.getString("katAd"));
                kategoriIDList.add(rs.getString("katNo"));
                kategoriAdList.add(rs.getString("katAd"));
            }
            tblKategori.setModel(dtm);
            cmbKategori.setModel(dcbm);
            cmbKategoriYonetim.setModel(dcbm2);

        } catch (SQLException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            db.kapat();
        }

    }

    String kategoriAd;

    public void kategoriEkle() {
        //kategoriler benzersizdir isimleri kontrol yapısı olustur

        KategorilerProperty kp = new KategorilerProperty();
        kp.setKatAd(txtKategoriAd.getText().trim());
        kp.setKatAciklama(taAciklama.getText().trim());
        kategoriAd = txtKategoriAd.getText();
        boolean kontrol = false;

        if (!(kategoriAdList.contains(txtKategoriAd.getText()))) {
            kontrol = true;
        }

        if (kontrol == true) {

            try {
                String query = "insert into kategoriler values (null,'" + kp.getKatAd() + "','" + kp.getKatAciklama() + "') ";
                int sonuc = db.baglan().executeUpdate(query);
                if (sonuc > 0) {

                    JOptionPane.showMessageDialog(rootPane, "kayıt başarıyla eklendi");
                    kategoriGetir();
                    panelTemizle(panelKategoriBilgi);

                }

            } catch (HeadlessException | SQLException e) {
                System.err.println("kategori eklerken hata oluştu! " + e);
            } finally {
                db.kapat();
            }
        } else {

            JOptionPane.showMessageDialog(rootPane, "eklemek istediğiniz kategori sistemde kayıtlı!");
            txtKategoriAd.setText("");
            txtKategoriAd.requestFocus();
        }

    }

    public void stokGuncelleKayit() {

        int sifirStok = Integer.valueOf(txtUrunSatisAdet.getText());

        if (secilenStokint < sifirStok) {
            sifirStok = secilenStokint;

        }

        try {
            String query = "update urunler set uStok= '" + (secilenStokint - sifirStok) + "' where uNo='" + secilenStokID + "' and uStok>0 ";
            int guncelleSonuc = db.baglan().executeUpdate(query);
            if (guncelleSonuc > 0) {
                JOptionPane.showMessageDialog(rootPane, "stoktan düşüldü işlemi başarılı");
                urunlerDataGetir();
            }
        } catch (HeadlessException | SQLException e) {
            System.err.println("müşteri değiştirme hatası " + e);
        } finally {
            db.kapat();
        }
    }

    public void stokGuncelleTersKayit() {

        UrunlerProperty up = new UrunlerProperty();

        up.setUrunStok(String.valueOf(spStokMiktar.getValue()));

        try {
            String query = "update urunler set uStok= '" + (secilenStokint + Integer.valueOf(txtUrunSatisAdet.getText())) + "' where uNo='" + secilenStokID + "'  ";
            int guncelleSonuc = db.baglan().executeUpdate(query);
            if (guncelleSonuc > 0) {
                JOptionPane.showMessageDialog(rootPane, "stoktan düşüldü işlemi başarılı");
                urunlerDataGetir();
            }
        } catch (HeadlessException | SQLException e) {
            System.err.println("müşteri değiştirme hatası " + e);
        } finally {
            db.kapat();
        }

    }

    /**
     *
     * @param secilenID jtable dan secim sonucu dönen ID
     * @param table tablo ismi
     * @param tableID veritabanında tutulan tablo id ismi
     * @return true sonuc döndürürse verileri yukleme metodunu cağır
     */
    public void kategoriSil() {
        if (kategoriNoTut.equals("")) {
            JOptionPane.showMessageDialog(rootPane, "Lütfen Tablodan Bir Seçim Yapınız!");
        } else if (urunlerIDKategoriList.contains(kategoriNoTut)) {
            JOptionPane.showMessageDialog(rootPane, "silmek istediğiniz kategoride ürünler kayıtlı önce ürünleri siliniz!");
        } else {
            int silSecim = JOptionPane.showConfirmDialog(rootPane, "silmek istediğinize emin misiniz?", "iptal", JOptionPane.YES_NO_OPTION);
            if (silSecim == 0) {
                try {
                    String query = "delete from kategoriler where katNo='" + kategoriNoTut + "'";
                    int silSonuc = db.baglan().executeUpdate(query);
                    if (silSonuc > 0) {
                        JOptionPane.showMessageDialog(rootPane, "silme işlemi gerçekleştirildi");
                        kategoriGetir();
                        cmbKategoriSecim.setSelectedIndex(0);
                        panelTemizle(panelKategoriBilgi);

                    }

                } catch (HeadlessException | SQLException e) {
                    System.err.println("silme sırasında hata oluştu: " + e);
                } finally {
                    db.kapat();
                }

            }

        }

    }

    public void kategoriDuzenle(String kategoriID) {

        try {
            String query = "select katAd,katAciklama,katNo from kategoriler where katNo='" + kategoriID + "'";
            ResultSet rs = db.baglan().executeQuery(query);
            while (rs.next()) {
                txtKategoriAd.setText(rs.getString("katAd"));
                taAciklama.setText(rs.getString("katAciklama"));
            }
        } catch (SQLException e) {
            System.err.println("kategorileri çekerken hata! " + e);
        } finally {
            db.kapat();
        }
    }

    public void kategoriDuzenle() {
        //kategori propert sınıfı ile validation işlemlerini gerçekleştir
        String adi = txtKategoriAd.getText().trim();
        String aciklama = taAciklama.getText().trim();
        try {

            String query = "update kategoriler set katAd='" + adi + "', katAciklama='" + aciklama + "' where katNo='" + kategoriNoTut + "'  ";
            System.out.println("sorgu :" + query);
            int guncelleSonuc = db.baglan().executeUpdate(query);

            if (guncelleSonuc > 0) {
                JOptionPane.showMessageDialog(rootPane, "guncelleme basarili");
                kategoriGetir();
                panelTemizle(panelKategoriBilgi);
            }
        } catch (HeadlessException | SQLException e) {
            System.err.println("guncelleme hatası " + e);
        } finally {
            db.kapat();
        }

    }

    public void urunEkle() {
        //id için null değeri gönderirken '' "" ifadelerini ayrıca kullanma!!!!
        ad = txtUrunAdi.getText();
        urunAlis = txtUrunAlis.getText();
        urunSatis = txtUrunSatis.getText();
        urunStok = String.valueOf(spStokMiktar.getValue());

        UrunlerProperty pro = new UrunlerProperty();
        pro.setUrunAdi(ad);
        pro.setUrunAlis(urunAlis);
        pro.setUrunSatis(urunSatis);
        pro.setUrunStok(urunStok);
        boolean kontrol = false;

//        for (String item : urunAdList) {
//            if(!item.equals(ad)){kontrol=true;}
//        }
//        
//        
        if (!urunAdList.contains(ad)) {
            kontrol = true;
        }

        if (kontrol == true) {
            String query = "insert into urunler values (null,'" + pro.getUrunAdi() + "','" + pro.getUrunAlis() + "','" + pro.getUrunSatis() + "','" + pro.getUrunStok() + "','" + kategoriIDList.get(cmbKategori.getSelectedIndex()) + "')";

            try {
                int silSonuc = db.baglan().executeUpdate(query);
                if (silSonuc > 0) {
                    urunlerDataGetir();
                    JOptionPane.showMessageDialog(rootPane, "ekleme işlemi başarılı");
                    panelTemizle(panelUrunBilgi);
                } else {
                    JOptionPane.showMessageDialog(rootPane, "ekleme basarili hatası !");
                }
            } catch (HeadlessException | SQLException e) {
                System.out.println("ex: " + e);
            } finally {
                db.kapat();
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "eklemek istediğiniz ürün adı kayıtlı");
            txtUrunAdi.setText("");
            txtUrunAdi.requestFocus();
        }
    }

    public void urunSil(String urunlerIDTut) {
        if (urunlerIDTut.equals("")) {
            JOptionPane.showMessageDialog(rootPane, "Lütfen Tablodan Bir Seçim Yapınız!");
        } else {
            int silSecim = JOptionPane.showConfirmDialog(rootPane, "silmek istediğinize emin misiniz?", "iptal", JOptionPane.YES_NO_OPTION);
            if (silSecim == 0) {
                try {
                    String query = "delete from urunler where uNo='" + urunlerIDTut + "'";
                    int silSonuc = db.baglan().executeUpdate(query);
                    if (silSonuc > 0) {
                        JOptionPane.showMessageDialog(rootPane, "silme işlemi gerçekleştirildi");
                        urunlerDataGetir();
                        btnUrunlerSil.setEnabled(false);
                        panelTemizle(panelUrunBilgi);

                    }

                } catch (HeadlessException | SQLException e) {
                    System.err.println("urun silme sırasında hata oluştu: " + e);
                } finally {
                    db.kapat();
                }

            }

        }
    }

    public void urunGuncelle(String urunGelenIDTut) {
        try {
            String query = "select * from urunler where uNo='" + urunGelenIDTut + "'";
            ResultSet rs = db.baglan().executeQuery(query);
            while (rs.next()) {
                txtUrunAdi.setText(rs.getString("uAd"));
                txtUrunAlis.setText(rs.getString("uAlis"));
                txtUrunSatis.setText(rs.getString("uSatis"));
                spStokMiktar.setValue(Integer.valueOf(rs.getString("uStok")));
                //her seferinde tablodan verileri çekip componentlere atmak yerine aşağıdaki yapıyı kullanarak table üzerinde tıklanan verilerin değerlerini alarak componentlere alabiliriz 
                cmbKategori.setSelectedItem(tblUrunler.getValueAt(tblUrunler.getSelectedRow(), 2));

            }
        } catch (SQLException e) {
            System.err.println("urunleri çekerken hata! " + e);
        } finally {
            db.kapat();
        }

    }

    public void urunGuncelle() {

        UrunlerProperty up = new UrunlerProperty();

        up.setUrunAdi(txtUrunAdi.getText());
        up.setUrunAlis(txtUrunAlis.getText());
        up.setUrunSatis(txtUrunSatis.getText());
        up.setUrunStok(String.valueOf(spStokMiktar.getValue()));

        try {
            String query = "update urunler set uAd='" + up.getUrunAdi() + "', uAlis='" + up.getUrunAlis() + "',uSatis='" + up.getUrunSatis() + "',uStok='" + up.getUrunStok() + "' ,katNo='" + kategoriIDList.get(cmbKategori.getSelectedIndex()) + "' where uNo='" + urunIDTut + "' ";
            int guncelleSonuc = db.baglan().executeUpdate(query);
            if (guncelleSonuc > 0) {
                JOptionPane.showMessageDialog(rootPane, "guncelleme işlemi başarılı");
                urunlerDataGetir();
                panelTemizle(panelUrunBilgi);
            }
        } catch (HeadlessException | SQLException e) {
            System.err.println("müşteri değiştirme hatası " + e);
        } finally {
            db.kapat();
        }

    }

    public void MusteriGetir(String musteriID) {

        MusterilerProperty mp = new MusterilerProperty();

        try {

            String query = "select * from musteriler where mNo='" + musteriID + "'";
            ResultSet rs = db.baglan().executeQuery(query);
            if (rs.next()) {
                mp.setAd(rs.getString("mAd"));
                mp.setSoyad(rs.getString("mSoyad"));
                mp.setTelefon(rs.getString("mTelefon"));
                mp.setMail(rs.getString("mMail"));
                mp.setAdres(rs.getString("mAdres"));
                txtMusteriAdi.setText(mp.getAd());
                txtMusteriSoyadi.setText(mp.getSoyad());
                txtMusteriMail.setText(mp.getMail());
                txtMusteriTel.setText(mp.getTelefon());
                txaAdres.setText(mp.getAdres());
            }

        } catch (SQLException e) {

            System.err.println("müşteri getirirken hata! " + e);
        } finally {
            db.kapat();
        }

    }
    ArrayList<String> musteriIDList = new ArrayList<>();
    String satisMusteriNo = "";

    public void MusteriGetir() {
        try {
            musteriIDList.clear();
            String query = "select * from musteriler ";
            DefaultTableModel dtm = new DefaultTableModel();
            dtm.addColumn("MüşteriID");
            dtm.addColumn("Adı");
            dtm.addColumn("Soyad");
            dtm.addColumn("Telefon");
            dtm.addColumn("Adres");
            dtm.addColumn("Mail");
            satisMusteriNo = "";
            ResultSet rs = db.baglan().executeQuery(query);
            while (rs.next()) {
                dtm.addRow(new String[]{rs.getString("mNo"), rs.getString("mAd"), rs.getString("mSoyad"),
                    rs.getString("mTelefon"), rs.getString("mAdres"), rs.getString("mMail")});
                if (txtSatisMusteriTelefon.getText().trim().equals(rs.getString("mTelefon"))) {
                    satisMusteriNo = rs.getString("mNo");
                }
                musteriIDList.add(rs.getString("mNo"));
            }
            tblMusteriler.setModel(dtm);
        } catch (SQLException e) {
            System.err.println("müşteri select hatası " + e);
        } finally {
            db.kapat();
        }
    }

    public void musteriGuncelle(String musteriIDTut) {
        MusterilerProperty mp = new MusterilerProperty();

        mp.setAd(txtMusteriAdi.getText());
        mp.setSoyad(txtMusteriSoyadi.getText());
        mp.setTelefon(txtMusteriTel.getText());
        mp.setAdres(txaAdres.getText());
        mp.setMail(txtMusteriMail.getText());
        try {
            String query = "update musteriler set mAd='" + mp.getAd() + "', mSoyad='" + mp.getSoyad() + "',mMail='" + mp.getMail() + "',mAdres='" + mp.getAdres() + "' ,mTelefon='" + mp.getTelefon() + "' where mNo='" + musteriIDTut + "' ";
            int guncelleSonuc = db.baglan().executeUpdate(query);
            if (guncelleSonuc > 0) {
                JOptionPane.showMessageDialog(rootPane, "guncelleme işlemi başarılı");
                panelTemizle(panelMusteriBilgi);
            }
        } catch (HeadlessException | SQLException e) {
            System.err.println("müşteri değiştirme hatası " + e);
        } finally {
            db.kapat();
        }

    }

    public void musteriSil(String musteriIDTut) {
        if (musteriIDTut.equals("")) {
            JOptionPane.showMessageDialog(rootPane, "Lütfen Tablodan Bir Seçim Yapınız!");
        } else if (satisMusteriList.contains(musteriNoTut)) {
            JOptionPane.showMessageDialog(rootPane, "silmek istediğiniz müşterinin kayıtlı ticari işlemleri var önce onları temizleyiniz!");
        } else {
            int silSecim = JOptionPane.showConfirmDialog(rootPane, "silmek istediğinize emin misiniz?", "iptal", JOptionPane.YES_NO_OPTION);
            if (silSecim == 0) {
                try {
                    String query = "delete from musteriler where mNo='" + musteriIDTut + "'";
                    int silSonuc = db.baglan().executeUpdate(query);
                    if (silSonuc > 0) {
                        JOptionPane.showMessageDialog(rootPane, "silme işlemi gerçekleştirildi");
                        MusteriGetir();
                        btnMusteriSil.setEnabled(false);
                        panelTemizle(panelMusteriBilgi);

                    }

                } catch (HeadlessException | SQLException e) {
                    System.err.println("silme sırasında hata oluştu: " + e);
                } finally {
                    db.kapat();
                }

            }

        }

    }

    public void musteriEkle() {

        MusterilerProperty mp = new MusterilerProperty();
        String mAdi = txtMusteriAdi.getText().trim();
        String mSoyadi = txtMusteriSoyadi.getText().trim();
        String mTelefon = txtMusteriTel.getText().trim();
        String mMail = txtMusteriMail.getText().trim();
        String mAdres = txaAdres.getText().trim();
        mp.setAd(mAdi);
        mp.setSoyad(mSoyadi);
        mp.setTelefon(mTelefon);
        mp.setAdres(mAdres);
        mp.setMail(mMail);
        try {
            String query = "insert into musteriler values ( null , '" + mp.getAd() + "', '" + mp.getSoyad() + "','" + mp.getTelefon() + "','" + mp.getAdres() + "','" + mp.getMail() + "')";
            int eklemeSonuc = db.baglan().executeUpdate(query);
            if (eklemeSonuc > 0) {

                JOptionPane.showMessageDialog(rootPane, "müşteri ekleme işlemi tamamlandı");
                MusteriGetir();
                panelTemizle(panelKategoriBilgi);
                panelTemizle(panelMusteriBilgi);

            }

        } catch (HeadlessException | SQLException e) {
            if (e.toString().contains("UNIQUE constraint failed: musteriler.mTelefon")) {
                JOptionPane.showMessageDialog(rootPane, "bu telefon numarası sistemde zaten kayıtlı lütfen farklı bir numara kullanın!");
            }

        } finally {
            db.kapat();
        }

    }
    ArrayList<String> satisMusteriList = new ArrayList();

    public void satisGoruntule() {
        DefaultTableModel dtm = new DefaultTableModel();
        dtm.addColumn("SatışID");
        dtm.addColumn("Müşteri Adı");
        dtm.addColumn("Ürün Ad");
        dtm.addColumn("Satış Fiyatı");
        dtm.addColumn("Adet");
        dtm.addColumn("Tarih");
        dtm.addColumn("Kayıt Durumu");
        satisMusteriList.clear();
        try {
            String query = "select *  from satis join urunler on satis.uNo=urunler.uNo join musteriler on satis.mNo=musteriler.mNo";
            ResultSet rs = db.baglan().executeQuery(query);
            while (rs.next()) {
                dtm.addRow(new String[]{rs.getString("sNo"), rs.getString("mAd"), rs.getString("uAd"), rs.getString("sSatisFiyat"), rs.getString("sMiktar"), rs.getString("sTarih"), rs.getString("sKayitDurumu")});
                satisMusteriList.add(rs.getString("mNo"));
            }
            tblSatis.setModel(dtm);

        } catch (SQLException e) {
            System.err.println("satış listeleme hatasi! " + e);
        } finally {
            db.kapat();
        }

    }

    public void satisEkle(String durum) {
        //satisMusteriNo mevcut

        String satisUrunNo = "" + tblurunlerSatis.getValueAt(tblurunlerSatis.getSelectedRow(), 0);

        String satisUrunFiyati = txtUrunSatisFiyati.getText().trim();
        String satisMiktari = txtUrunSatisAdet.getText().trim();
        //tarih için bir validator yap yıl ay gün girilecek şekilde aralarda - oalcak

        Date simdiki = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String satisTarih = sdf.format(simdiki);

        try {
            String query = "insert into satis values(null,'" + satisMusteriNo + "','" + satisUrunFiyati + "','" + secilenStokID + "','" + satisMiktari + "','" + satisTarih + "','" + durum + "'  )";
            int ekleSonuc = db.baglan().executeUpdate(query);
            if (ekleSonuc > 0) {
                JOptionPane.showMessageDialog(rootPane, "satış tablosuna iade işlemi başarılı");
                satisGoruntule();
                panelTemizle(panelSatisBilgi);

            }
        } catch (HeadlessException | SQLException e) {
            System.err.println("satış tablosuna iade hatası!" + e);
        } finally {
            db.kapat();
        }

    }

    public boolean urunlervalidation(JPanel panel) {
        boolean sonuc = false;
        Component[] dizi = panel.getComponents();
        label:
        for (Component item : dizi) {
            if (item instanceof JTextField) {
                JTextField txt = (JTextField) item;
                String baslik = txt.getToolTipText();
                if (txt.getText().trim().equals("")) {

                    JOptionPane.showMessageDialog(item, baslik.toUpperCase() + " Bölümü Boş, Lütfen değer giriniz !");
                    txt.setText("");
                    txt.requestFocus();
                    break label;
                } else if (baslik.equals("Alış Fiyatı")) {

                    String deger = txt.getText().trim();
                    String hata = "";
                    for (char c : deger.toCharArray()) {
                        if (!Character.isDigit(c) && !Character.isWhitespace(c)) {
                            hata = "lutfen alış için sayısal ifade giriniz";
                            JOptionPane.showMessageDialog(item, baslik.toUpperCase() + hata);
                            txt.setText("");
                            txt.requestFocus();
                            break label;
                        }

                    }

                } else if (baslik.equals("Satış Fiyatı")) {

                    String deger = txt.getText().trim();
                    String hata = "";
                    for (char c : deger.toCharArray()) {
                        if (!Character.isDigit(c) && !Character.isWhitespace(c)) {
                            hata = "lutfen satış için sayısal ifade giriniz";
                            JOptionPane.showMessageDialog(item, baslik.toUpperCase() + hata);
                            txt.setText("");
                            txt.requestFocus();
                            break label;
                        }

                    }

                } else {
                    txt.setText(txt.getText().trim());
                    sonuc = true;

                }

            }
        }
        return sonuc;
    }

    public boolean kategoriValidation(JPanel panel) {
        boolean sonuc = false;
        Component[] dizi = panel.getComponents();
        label:
        for (Component item : dizi) {
            if (item instanceof JTextField) {
                JTextField txt = (JTextField) item;
                String baslik = txt.getToolTipText();
                if (txt.getText().trim().equals("")) {

                    JOptionPane.showMessageDialog(item, baslik.toUpperCase() + " Bölümü Boş, Lütfen değer giriniz !");
                    txt.setText("");
                    txt.requestFocus();
                    break label;
                } else if (baslik.equals("Kategori Adı")) {

                    String deger = txt.getText().trim();
                    String hata = "";
                    for (char c : deger.toCharArray()) {
                        if (!(Character.isLetter(c)) && !Character.isWhitespace(c)) {
                            hata = "lutfen sadece harf giriniz!";
                            JOptionPane.showMessageDialog(rootPane, hata);
                            txt.setText("");
                            txt.requestFocus();
                            break label;
                        } else {
                            sonuc = true;
                        }

                    }

                } else {
                    txt.setText(txt.getText().trim());
                    sonuc = true;

                }

            }
        }
        return sonuc;
    }

    public boolean musteriValidation(JPanel panel) {
        boolean sonuc = false;
        Component[] dizi = panel.getComponents();
        label:
        for (Component item : dizi) {
            if (item instanceof JTextField) {
                JTextField txt = (JTextField) item;
                String baslik = txt.getToolTipText();
                if (txt.getText().trim().equals("")) {

                    JOptionPane.showMessageDialog(item, baslik.toUpperCase() + " Bölümü Boş, Lütfen değer giriniz !");
                    txt.setText("");
                    txt.requestFocus();
                    break label;
                } else if (baslik.equals("Müşteri Adı")) {

                    String deger = txt.getText().trim();
                    String hata = "";
                    for (char c : deger.toCharArray()) {
                        if (!Character.isLetter(c) && !Character.isWhitespace(c)) {
                            hata = "lütfen  müşteri adı için sadece harf giriniz";
                            JOptionPane.showMessageDialog(item, baslik.toUpperCase() + hata);
                            txt.setText("");
                            txt.requestFocus();
                            break label;
                        }

                    }

                } else if (baslik.equals("Müşteri Soyadı")) {

                    String deger = txt.getText().trim();
                    String hata = "";
                    for (char c : deger.toCharArray()) {
                        if (!Character.isLetter(c) && !Character.isWhitespace(c)) {
                            hata = "lutfen Müşteri Soyadı için sadece harf giriniz";
                            JOptionPane.showMessageDialog(item, baslik.toUpperCase() + hata);
                            txt.setText("");
                            txt.requestFocus();
                            break label;
                        }

                    }

                } else if (baslik.equals("Müşteri Telefon")) {

                    String deger = txt.getText().trim();
                    String hata = "";
                    for (char c : deger.toCharArray()) {
                        if (!Character.isDigit(c) && !Character.isWhitespace(c)) {
                            hata = "lutfen telefon bilgisi için sadece rakam giriniz";
                            JOptionPane.showMessageDialog(item, baslik.toUpperCase() + hata);
                            txt.setText("");
                            txt.requestFocus();
                            break label;
                        }

                    }

                } else if (baslik.equals("Müşteri Mail")) {

                    String deger = txt.getText().trim();
                    String hata = "";

                    if (!deger.matches("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$")) {
                        hata = "lutfen geçerli bir mail adresi giriniz";
                        JOptionPane.showMessageDialog(item, baslik.toUpperCase() + hata);
                        txt.setText("");
                        txt.requestFocus();
                    }

                } else {
                    txt.setText(txt.getText().trim());
                    sonuc = true;

                }

            }
        }
        return sonuc;
    }

    public void panelTemizle(JPanel panel) {
        Component[] dizi = panel.getComponents();
        for (Component item : dizi) {
            if (item instanceof JTextField) {
                JTextField txt = (JTextField) item;

                txt.setText("");

            }
            if (item instanceof JComboBox) {
                JComboBox cmb = (JComboBox) item;

                cmb.setSelectedIndex(0);

            }
            if (item instanceof JSpinner) {
                JSpinner spn = (JSpinner) item;

                spn.setValue(0);

            }
            if (item instanceof JTextArea) {
                JTextArea ta = (JTextArea) item;

                ta.setText("");

            }

        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        panelUrunBilgi = new javax.swing.JPanel();
        cmbKategori = new javax.swing.JComboBox<>();
        txtUrunAdi = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtUrunAlis = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtUrunSatis = new javax.swing.JTextField();
        spStokMiktar = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btnUrunEkle = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        btnUrunlerSil = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblUrunler = new javax.swing.JTable();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        listeBitenUrunler = new javax.swing.JList<>();
        jButton1 = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        jScrollPane10 = new javax.swing.JScrollPane();
        listeUrunAz = new javax.swing.JList<>();
        txtUrunKalanMiktar = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        txtArama = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        panelKategoriBilgi = new javax.swing.JPanel();
        txtKategoriAd = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        taAciklama = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        cmbKategoriSecim = new javax.swing.JComboBox<>();
        cmbKategoriYonetim = new javax.swing.JComboBox<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblKategori = new javax.swing.JTable();
        jButton9 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        panelMusteriBilgi = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtMusteriAdi = new javax.swing.JTextField();
        txtMusteriSoyadi = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtMusteriTel = new javax.swing.JTextField();
        txtMusteriMail = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        txaAdres = new javax.swing.JTextArea();
        jLabel13 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        btnMusteriSil = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblMusteriler = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tblurunlerSatis = new javax.swing.JTable();
        jScrollPane7 = new javax.swing.JScrollPane();
        tblSatis = new javax.swing.JTable();
        panelSatisBilgi = new javax.swing.JPanel();
        jButton7 = new javax.swing.JButton();
        txtSatisMusteriTelefon = new javax.swing.JTextField();
        txtUrunSatisAdi = new javax.swing.JTextField();
        txtUrunSatisFiyati = new javax.swing.JTextField();
        txtUrunSatisAdet = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        txtSatisTarih = new javax.swing.JTextField();
        jToggleButton1 = new javax.swing.JToggleButton();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        tblRaporlama = new javax.swing.JTable();
        cmbAramaYilG = new javax.swing.JComboBox<>();
        cmbAramaGunG = new javax.swing.JComboBox<>();
        cmbAramaAyG = new javax.swing.JComboBox<>();
        cmbAramaYilS = new javax.swing.JComboBox<>();
        cmbAramaAyS = new javax.swing.JComboBox<>();
        cmbAramaGunS = new javax.swing.JComboBox<>();
        jLabel19 = new javax.swing.JLabel();
        jButton10 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTabbedPane1.setToolTipText("Müşteri Adı");
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(800, 600));

        jPanel1.setPreferredSize(new java.awt.Dimension(800, 600));

        panelUrunBilgi.setBorder(javax.swing.BorderFactory.createTitledBorder("Ürün Bilgileri"));

        cmbKategori.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cmbKategoriMouseClicked(evt);
            }
        });

        txtUrunAdi.setToolTipText("Ürün Adı");

        jLabel1.setText("ürün adı");

        jLabel2.setText("alış fiyatı");

        txtUrunAlis.setToolTipText("Alış Fiyatı");
        txtUrunAlis.setName(""); // NOI18N

        jLabel3.setText("satış fiyatı");

        txtUrunSatis.setToolTipText("Satış Fiyatı");

        jLabel4.setText("stok miktar");

        jLabel5.setText("kategori");

        btnUrunEkle.setText("Kayıt Ekle");
        btnUrunEkle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUrunEkleActionPerformed(evt);
            }
        });

        jButton8.setText("Güncelle");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        btnUrunlerSil.setText("Sil");
        btnUrunlerSil.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUrunlerSilActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelUrunBilgiLayout = new javax.swing.GroupLayout(panelUrunBilgi);
        panelUrunBilgi.setLayout(panelUrunBilgiLayout);
        panelUrunBilgiLayout.setHorizontalGroup(
            panelUrunBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelUrunBilgiLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelUrunBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelUrunBilgiLayout.createSequentialGroup()
                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnUrunEkle, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelUrunBilgiLayout.createSequentialGroup()
                        .addGroup(panelUrunBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelUrunBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(panelUrunBilgiLayout.createSequentialGroup()
                                    .addGroup(panelUrunBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel1)
                                        .addComponent(jLabel2)
                                        .addComponent(jLabel3))
                                    .addGap(39, 39, 39))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelUrunBilgiLayout.createSequentialGroup()
                                    .addComponent(jLabel4)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                            .addGroup(panelUrunBilgiLayout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(49, 49, 49)))
                        .addGroup(panelUrunBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbKategori, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(spStokMiktar)
                            .addComponent(txtUrunAdi, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtUrunAlis)
                            .addComponent(txtUrunSatis))))
                .addGap(29, 29, 29)
                .addComponent(btnUrunlerSil, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelUrunBilgiLayout.setVerticalGroup(
            panelUrunBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelUrunBilgiLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(panelUrunBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtUrunAdi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panelUrunBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(txtUrunAlis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panelUrunBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUrunSatis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(panelUrunBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spStokMiktar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(panelUrunBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addGroup(panelUrunBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnUrunEkle)
                    .addComponent(jButton8)
                    .addComponent(btnUrunlerSil))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel6.setText("jLabel6");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 695, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 172, Short.MAX_VALUE)
        );

        tblUrunler.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblUrunler.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblUrunlerMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblUrunler);

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Depo", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(255, 0, 0))); // NOI18N

        listeBitenUrunler.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane9.setViewportView(listeBitenUrunler);

        jButton1.setText("listele");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel20.setText("biten ürünler");

        listeUrunAz.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane10.setViewportView(listeUrunAz);

        jLabel21.setText("miktar?");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                        .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addGap(18, 18, 18)
                        .addComponent(txtUrunKalanMiktar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(37, 37, 37))))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel20))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtUrunKalanMiktar, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel21))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane10)
                    .addComponent(jScrollPane9)))
        );

        txtArama.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtAramaKeyReleased(evt);
            }
        });

        jLabel23.setText("Arama Yap:");

        jButton4.setText("jButton4");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(panelUrunBilgi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(54, 54, 54)
                                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 721, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(38, 38, 38)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(217, 217, 217)
                        .addComponent(jButton4)
                        .addGap(261, 261, 261)
                        .addComponent(jLabel6))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jLabel23)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtArama, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jButton4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelUrunBilgi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 88, Short.MAX_VALUE)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtArama, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel23))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        jTabbedPane1.addTab("ÜRÜN YÖNETİMİ", jPanel1);

        panelKategoriBilgi.setBorder(javax.swing.BorderFactory.createTitledBorder("Kategori Bilgileri"));

        txtKategoriAd.setToolTipText("Kategori Adı");

        taAciklama.setColumns(20);
        taAciklama.setRows(5);
        taAciklama.setToolTipText("textArea");
        jScrollPane1.setViewportView(taAciklama);

        jLabel7.setText("Kategori Adı");

        jLabel8.setText("Açıklama");

        jButton6.setText("İşlem");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        cmbKategoriSecim.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ekle", "sil", "güncelle" }));

        cmbKategoriYonetim.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout panelKategoriBilgiLayout = new javax.swing.GroupLayout(panelKategoriBilgi);
        panelKategoriBilgi.setLayout(panelKategoriBilgiLayout);
        panelKategoriBilgiLayout.setHorizontalGroup(
            panelKategoriBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKategoriBilgiLayout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addGroup(panelKategoriBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelKategoriBilgiLayout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtKategoriAd))
                    .addGroup(panelKategoriBilgiLayout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(27, 27, 27)
                        .addGroup(panelKategoriBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelKategoriBilgiLayout.createSequentialGroup()
                                .addComponent(cmbKategoriYonetim, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cmbKategoriSecim, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton6))
                            .addComponent(jScrollPane1))))
                .addGap(21, 21, 21))
        );
        panelKategoriBilgiLayout.setVerticalGroup(
            panelKategoriBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelKategoriBilgiLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(panelKategoriBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtKategoriAd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addGap(18, 18, 18)
                .addGroup(panelKategoriBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelKategoriBilgiLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(panelKategoriBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton6)
                            .addComponent(cmbKategoriSecim, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbKategoriYonetim, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel8))
                .addContainerGap(43, Short.MAX_VALUE))
        );

        tblKategori.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblKategori.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblKategoriMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblKategori);

        jButton9.setText("jButton9");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(panelKategoriBilgi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(113, 113, 113)
                        .addComponent(jButton9))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 604, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(159, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(panelKategoriBilgi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(62, 62, 62)
                        .addComponent(jButton9)))
                .addGap(38, 38, 38)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(80, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("KATEGORİ YÖNETİMİ", jPanel3);

        panelMusteriBilgi.setBorder(javax.swing.BorderFactory.createTitledBorder("Kişi Bilgileri"));

        jLabel9.setText("Ad");

        jLabel10.setText("Soyad");

        txtMusteriAdi.setToolTipText("Müşteri Adı");

        txtMusteriSoyadi.setToolTipText("Müşteri Soyadı");

        jLabel11.setText("Telefon");

        txtMusteriTel.setToolTipText("Müşteri Telefon");

        txtMusteriMail.setToolTipText("Müşteri Mail");

        jLabel12.setText("Mail");

        txaAdres.setColumns(20);
        txaAdres.setRows(5);
        jScrollPane4.setViewportView(txaAdres);

        jLabel13.setText("Adres");

        jButton2.setText("Ekle");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Güncelle");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        btnMusteriSil.setText("Sil");
        btnMusteriSil.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMusteriSilActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelMusteriBilgiLayout = new javax.swing.GroupLayout(panelMusteriBilgi);
        panelMusteriBilgi.setLayout(panelMusteriBilgiLayout);
        panelMusteriBilgiLayout.setHorizontalGroup(
            panelMusteriBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMusteriBilgiLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(panelMusteriBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10)
                    .addComponent(jLabel13))
                .addGroup(panelMusteriBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMusteriBilgiLayout.createSequentialGroup()
                        .addGroup(panelMusteriBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelMusteriBilgiLayout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addComponent(txtMusteriAdi, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelMusteriBilgiLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(txtMusteriSoyadi, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(57, 57, 57)
                        .addGroup(panelMusteriBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12)
                            .addComponent(jLabel11))
                        .addGap(29, 29, 29)
                        .addGroup(panelMusteriBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtMusteriTel, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtMusteriMail, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelMusteriBilgiLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(panelMusteriBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelMusteriBilgiLayout.createSequentialGroup()
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(28, 28, 28)
                        .addComponent(btnMusteriSil, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(64, Short.MAX_VALUE))
        );
        panelMusteriBilgiLayout.setVerticalGroup(
            panelMusteriBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMusteriBilgiLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMusteriBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtMusteriAdi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(txtMusteriTel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(panelMusteriBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(txtMusteriSoyadi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMusteriMail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addGap(30, 30, 30)
                .addGroup(panelMusteriBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMusteriBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                    .addComponent(btnMusteriSil, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        tblMusteriler.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblMusteriler.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblMusterilerMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(tblMusteriler);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane5)
                    .addComponent(panelMusteriBilgi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(244, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelMusteriBilgi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(74, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("MÜŞTERİ YÖNETİMİ", jPanel7);

        tblurunlerSatis.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblurunlerSatis.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblurunlerSatisMouseClicked(evt);
            }
        });
        jScrollPane6.setViewportView(tblurunlerSatis);

        tblSatis.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane7.setViewportView(tblSatis);

        panelSatisBilgi.setBorder(javax.swing.BorderFactory.createTitledBorder("satış Bilgileri"));

        jButton7.setText("SAT");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jLabel14.setText("müşteri tel");

        jLabel15.setText("ürün adı");

        jLabel16.setText("satış fiyatı");

        jLabel17.setText("adet");

        jLabel18.setText("tarih");

        txtSatisTarih.setEnabled(false);

        jToggleButton1.setText("satış iade");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelSatisBilgiLayout = new javax.swing.GroupLayout(panelSatisBilgi);
        panelSatisBilgi.setLayout(panelSatisBilgiLayout);
        panelSatisBilgiLayout.setHorizontalGroup(
            panelSatisBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSatisBilgiLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSatisBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(panelSatisBilgiLayout.createSequentialGroup()
                        .addGroup(panelSatisBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14)
                            .addComponent(jLabel15)
                            .addComponent(jLabel16)
                            .addComponent(jLabel17)
                            .addComponent(jLabel18))
                        .addGap(55, 55, 55)
                        .addGroup(panelSatisBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtUrunSatisAdi, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtUrunSatisFiyati, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtUrunSatisAdet, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtSatisMusteriTelefon, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                            .addComponent(txtSatisTarih, javax.swing.GroupLayout.Alignment.LEADING)))
                    .addGroup(panelSatisBilgiLayout.createSequentialGroup()
                        .addComponent(jToggleButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 81, Short.MAX_VALUE)
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(54, Short.MAX_VALUE))
        );
        panelSatisBilgiLayout.setVerticalGroup(
            panelSatisBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSatisBilgiLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSatisBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSatisMusteriTelefon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSatisBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUrunSatisAdi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSatisBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUrunSatisFiyati, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSatisBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUrunSatisAdet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSatisBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(txtSatisTarih, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSatisBilgiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jToggleButton1)
                    .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 753, Short.MAX_VALUE)
                    .addComponent(jScrollPane6)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(panelSatisBilgi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelSatisBilgi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("SATIŞ YÖNETİMİ", jPanel4);

        tblRaporlama.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane8.setViewportView(tblRaporlama);

        cmbAramaYilG.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2000", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030" }));

        cmbAramaGunG.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" }));

        cmbAramaAyG.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }));

        cmbAramaYilS.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2000", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030" }));

        cmbAramaAyS.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }));

        cmbAramaGunS.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" }));

        jLabel19.setText("<>");

        jButton10.setText("jButton10");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane8))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(cmbAramaYilG, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbAramaAyG, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbAramaGunG, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(38, 38, 38)
                        .addComponent(jLabel19)
                        .addGap(33, 33, 33)
                        .addComponent(cmbAramaYilS, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbAramaAyS, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbAramaGunS, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(59, 59, 59)
                        .addComponent(jButton10)
                        .addGap(0, 58, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(221, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbAramaYilG, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbAramaGunG, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbAramaAyG, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbAramaYilS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbAramaAyS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbAramaGunS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19)
                    .addComponent(jButton10))
                .addGap(50, 50, 50)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(57, 57, 57))
        );

        jTabbedPane1.addTab("RAPORLAMA", jPanel5);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 778, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed

        if (cmbKategoriSecim.getSelectedItem().equals("ekle")) {
            boolean kontrol = kategoriValidation(panelKategoriBilgi);
            if (kontrol == true) {
                kategoriEkle();

            }

        } else if (cmbKategoriSecim.getSelectedItem().equals("sil")) {

            kategoriSil();
        } else if (cmbKategoriSecim.getSelectedItem().equals("güncelle")) {
            kategoriDuzenle();

        }

    }//GEN-LAST:event_jButton6ActionPerformed
    String urunIDTut = "";
    private void tblUrunlerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblUrunlerMouseClicked

        int row = tblUrunler.getSelectedRow();
        if (row > -1) {
            urunIDTut = String.valueOf(tblUrunler.getValueAt(row, 0));
        }
        btnUrunlerSil.setEnabled(true);
        urunGuncelle(urunIDTut);

    }//GEN-LAST:event_tblUrunlerMouseClicked

    private void btnUrunEkleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUrunEkleActionPerformed

        boolean b = urunlervalidation(panelUrunBilgi);
        if (b == true) {
            try {
                urunEkle();
            } catch (Exception ex) {
                System.out.println("hata mesajı: " + ex);
            }
        }


    }//GEN-LAST:event_btnUrunEkleActionPerformed

    private void cmbKategoriMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cmbKategoriMouseClicked
        //kontrol yap ve sil blogu
        String query = "select katNo from kategoriler where katAd='" + cmbKategori.getSelectedItem() + "' ";
        try {
            ResultSet rs = db.baglan().executeQuery(query);
            while (rs.next()) {
                cmbKategoriTut = rs.getString("katNo");
                kategoriIDTut = rs.getString("katNo");
            }

        } catch (SQLException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            db.kapat();
        }

    }//GEN-LAST:event_cmbKategoriMouseClicked

    String kategoriNoTut = "";
    private void tblKategoriMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblKategoriMouseClicked

        int row = tblKategori.getSelectedRow();
        if (row > -1) {
            kategoriNoTut = "" + tblKategori.getValueAt(row, 0);
            System.out.println("kategori secilen : " + kategoriNoTut);

        }
        kategoriDuzenle(kategoriNoTut);

    }//GEN-LAST:event_tblKategoriMouseClicked

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        boolean b = musteriValidation(panelMusteriBilgi);
        if (b == true) {
            try {
                musteriEkle();
            } catch (Exception ex) {
                System.out.println("hata mesajı: " + ex);
            }
        }


    }//GEN-LAST:event_jButton2ActionPerformed

    String musteriNoTut = "";
    int musterilerRowCount = 0;
    private void tblMusterilerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblMusterilerMouseClicked

        int row = tblMusteriler.getSelectedRow();
        musterilerRowCount = tblMusteriler.getSelectedRowCount();
        if (row > -1) {
            musteriNoTut = "" + tblMusteriler.getValueAt(row, 0);
            System.out.println("musteri secilen : " + musteriNoTut);

        }
        MusteriGetir(musteriNoTut);
        btnMusteriSil.setEnabled(true);
    }//GEN-LAST:event_tblMusterilerMouseClicked

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        musteriGuncelle(musteriNoTut);
        MusteriGetir();

    }//GEN-LAST:event_jButton3ActionPerformed

    private void btnMusteriSilActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMusteriSilActionPerformed

        musteriSil(musteriNoTut);


    }//GEN-LAST:event_btnMusteriSilActionPerformed

    private void btnUrunlerSilActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUrunlerSilActionPerformed
        urunSil(urunIDTut);

    }//GEN-LAST:event_btnUrunlerSilActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed

        try {
            urunGuncelle();
        } catch (Exception ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed

        MusteriGetir();
        if (jToggleButton1.isSelected()) {

            if (satisMusteriNo.equals("")) {
                JOptionPane.showMessageDialog(rootPane, "böyle bir müşteri mevcut değildir!");
            } else {

                satisEkle("iade");
                stokGuncelleTersKayit();
                urunlerDataGetir();
                satisGoruntule();

            }
        } else {
            jToggleButton1.setText("satış");
            if (satisMusteriNo.equals("")) {
                JOptionPane.showMessageDialog(rootPane, "böyle bir müşteri mevcut değildir!");
            } else {

                satisEkle("satis");
                stokGuncelleKayit();

                urunlerDataGetir();
                satisGoruntule();

            }

        }


    }//GEN-LAST:event_jButton7ActionPerformed
    String secilenStok = "";
    String secilenStokID = "";
    int secilenStokint = 0;

    private void tblurunlerSatisMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblurunlerSatisMouseClicked
        txtUrunSatisAdi.setText(("" + tblurunlerSatis.getValueAt(tblurunlerSatis.getSelectedRow(), 1)));

        int row = tblurunlerSatis.getSelectedRow();
        if (row > -1) {
            secilenStokint = Integer.valueOf((String.valueOf(tblurunlerSatis.getValueAt(row, 3))));
            secilenStokID = String.valueOf(tblurunlerSatis.getValueAt(row, 0));
        }


    }//GEN-LAST:event_tblurunlerSatisMouseClicked

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = new Date();
        Date date2 = new Date();
        String tarih1 = "", tarih2 = "";

        tarih1 = String.valueOf(cmbAramaYilG.getSelectedItem() + "-" + cmbAramaAyG.getSelectedItem() + "-" + cmbAramaGunG.getSelectedItem());
        tarih2 = String.valueOf(cmbAramaYilS.getSelectedItem() + "-" + cmbAramaAyS.getSelectedItem() + "-" + cmbAramaGunS.getSelectedItem());
        raporlama(tarih1, tarih2);
        System.out.println("tarih1 : " + tarih1 + "***" + "tarih 2 " + tarih2);

    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        bitenStok();
        azalanStok();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        if (jToggleButton1.isSelected()) {
            jToggleButton1.setText("satış kayıt");
        } else {
            jToggleButton1.setText("satış iade");
        }
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void txtAramaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtAramaKeyReleased
        aramalar();

    }//GEN-LAST:event_txtAramaKeyReleased

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        panelTemizle(panelKategoriBilgi);

        // TODO add your handling code here:
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed

        panelTemizle(panelUrunBilgi);        // TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    String ad = "";
    String urunAlis = "", urunSatis = "";
    String urunStok = "", urunKategori = "";

    public static String cmbKategoriTut = "";
    String kategoriIDTut = "";

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnMusteriSil;
    private javax.swing.JButton btnUrunEkle;
    private javax.swing.JButton btnUrunlerSil;
    private javax.swing.JComboBox<String> cmbAramaAyG;
    private javax.swing.JComboBox<String> cmbAramaAyS;
    private javax.swing.JComboBox<String> cmbAramaGunG;
    private javax.swing.JComboBox<String> cmbAramaGunS;
    private javax.swing.JComboBox<String> cmbAramaYilG;
    private javax.swing.JComboBox<String> cmbAramaYilS;
    private javax.swing.JComboBox<String> cmbKategori;
    private javax.swing.JComboBox<String> cmbKategoriSecim;
    private javax.swing.JComboBox<String> cmbKategoriYonetim;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JList<String> listeBitenUrunler;
    private javax.swing.JList<String> listeUrunAz;
    private javax.swing.JPanel panelKategoriBilgi;
    private javax.swing.JPanel panelMusteriBilgi;
    private javax.swing.JPanel panelSatisBilgi;
    private javax.swing.JPanel panelUrunBilgi;
    private javax.swing.JSpinner spStokMiktar;
    private javax.swing.JTextArea taAciklama;
    private javax.swing.JTable tblKategori;
    private javax.swing.JTable tblMusteriler;
    private javax.swing.JTable tblRaporlama;
    private javax.swing.JTable tblSatis;
    private javax.swing.JTable tblUrunler;
    private javax.swing.JTable tblurunlerSatis;
    private javax.swing.JTextArea txaAdres;
    private javax.swing.JTextField txtArama;
    private javax.swing.JTextField txtKategoriAd;
    private javax.swing.JTextField txtMusteriAdi;
    private javax.swing.JTextField txtMusteriMail;
    private javax.swing.JTextField txtMusteriSoyadi;
    private javax.swing.JTextField txtMusteriTel;
    private javax.swing.JTextField txtSatisMusteriTelefon;
    private javax.swing.JTextField txtSatisTarih;
    private javax.swing.JTextField txtUrunAdi;
    private javax.swing.JTextField txtUrunAlis;
    private javax.swing.JTextField txtUrunKalanMiktar;
    private javax.swing.JTextField txtUrunSatis;
    private javax.swing.JTextField txtUrunSatisAdet;
    private javax.swing.JTextField txtUrunSatisAdi;
    private javax.swing.JTextField txtUrunSatisFiyati;
    // End of variables declaration//GEN-END:variables

}
