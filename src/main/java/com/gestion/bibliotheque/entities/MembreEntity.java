package com.gestion.bibliotheque.entities;

import jakarta.persistence.*;

@Table(name = "membre")
@Entity
public class MembreEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;

    private String nom;

    private String prenom;

    // Adresse utilisee pour la notification Gmail d'emprunt
    @Column(nullable = false)
    private String email;

    private String telephone;

    public MembreEntity() {
        super();
    }

    public MembreEntity(int id, String nom, String prenom, String email, String telephone) {
        super();
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Override
    public String toString() {
        return "Membre [id=" + id + ", nom=" + nom + ", prenom=" + prenom + ", email=" + email + " ]";
    }
}
