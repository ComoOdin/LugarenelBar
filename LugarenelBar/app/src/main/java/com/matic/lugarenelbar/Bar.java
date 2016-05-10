package com.matic.lugarenelbar;

import com.matic.lugarenelbar.com.matic.lugarenelbar.utils.Constants;

import java.io.File;

/**
 * Created by Luigi on 27/4/2016.
 */
public class Bar {
    private int id;
    private String nombreLegal;
    private String nombreFantasia;
    private String pais;
    private String ciudad;
    private String calle;
    private int nroCalle;
    private double lat;
    private double lon;
    private String telefono;
    private String logo;
    private int mesasLibres;

    public Bar() {
    }

    public Bar(int id,String nombreLegal, String nombreFantasia, String pais, String ciudad, String calle, int nroCalle, double latitud, double longitud, String telefono, String logo, int mesasLibres) {
        this.id=id;
        this.nombreLegal = nombreLegal;
        this.nombreFantasia = nombreFantasia;
        this.pais = pais;
        this.ciudad = ciudad;
        this.calle = calle;
        this.nroCalle = nroCalle;
        this.lat = latitud;
        this.lon = longitud;
        this.telefono = telefono;
        this.logo = logo;
        this.mesasLibres = mesasLibres;
    }

    public int getId(){return id;}

    public void setId(int id){this.id=id;}

    public String getNombreLegal() {
        return nombreLegal;
    }

    public void setNombreLegal(String nombreLegal) {
        this.nombreLegal = nombreLegal;
    }

    public String getNombreFantasia() {
        return nombreFantasia;
    }

    public void setNombreFantasia(String nombreFantasia) {
        this.nombreFantasia = nombreFantasia;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public int getNroCalle() {
        return nroCalle;
    }

    public void setNroCalle(int nroCalle) {
        this.nroCalle = nroCalle;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public int getMesasLibres() {
        return mesasLibres;
    }

    public void setMesasLibres(int mesasLibres) {
        this.mesasLibres = mesasLibres;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id);
        sb.append(Constants.FIELD_SEPARATOR);
        sb.append (nombreLegal);
        sb.append(Constants.FIELD_SEPARATOR);
        sb.append(nombreFantasia);
        sb.append(Constants.FIELD_SEPARATOR);
        sb.append(pais);
        sb.append(Constants.FIELD_SEPARATOR);
        sb.append(ciudad);
        sb.append(Constants.FIELD_SEPARATOR);
        sb.append(calle);
        sb.append(Constants.FIELD_SEPARATOR);
        sb.append(nroCalle);
        sb.append(Constants.FIELD_SEPARATOR);
        sb.append(lat);
        sb.append(Constants.FIELD_SEPARATOR);
        sb.append(lon);
        sb.append(Constants.FIELD_SEPARATOR);
        sb.append(telefono);
        sb.append(Constants.FIELD_SEPARATOR);
        sb.append(logo);
        sb.append(Constants.FIELD_SEPARATOR);
        sb.append(mesasLibres);
        return sb.toString();
    }

}
