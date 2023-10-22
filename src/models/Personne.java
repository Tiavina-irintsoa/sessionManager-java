package models;

import java.sql.Date;

public class Personne {
    int idPersonne; 
    String nom; 
    Date dateNaissance;
    public Personne(String nom, Date dateNaissance) {
        this.nom = nom;
        this.dateNaissance = dateNaissance;
    }
    public int getIdPersonne() {
        return idPersonne;
    }
    public void setIdPersonne(int idPersonne) {
        this.idPersonne = idPersonne;
    }
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public Date getDateNaissance() {
        return dateNaissance;
    }
    public void setDateNaissance(Date dateNaissance) {
        this.dateNaissance = dateNaissance;
    }
    @Override
    public String toString() {
        return "Personne [idPersonne=" + idPersonne + ", nom=" + nom + ", dateNaissance=" + dateNaissance + "]";
    }
}
