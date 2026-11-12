package com.gestion.bibliotheque.entities;

import jakarta.persistence.*;

@Table(name = "livre")
@Entity
public class LivreEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "numero")
    private int numero;

    private String titre;

    private String auteur;

    private String categorie;

    // false tant que le livre est emprunte (gere automatiquement par les emprunts)
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean disponible = true;

    public LivreEntity() {
        super();
    }

    public LivreEntity(int numero, String titre, String auteur, String categorie) {
        super();
        this.numero = numero;
        this.titre = titre;
        this.auteur = auteur;
        this.categorie = categorie;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    @Override
    public String toString() {
        return "Livre [numero=" + numero + ", titre=" + titre + ", auteur=" + auteur + ", categorie=" + categorie
                + ", disponible=" + disponible + " ]";
    }
}
