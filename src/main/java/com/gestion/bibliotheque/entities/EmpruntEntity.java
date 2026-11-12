package com.gestion.bibliotheque.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Table(name = "emprunt")
@Entity
public class EmpruntEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "livre_numero")
    private LivreEntity livre;

    @ManyToOne(optional = false)
    @JoinColumn(name = "membre_id")
    private MembreEntity membre;

    private LocalDate dateEmprunt;

    private LocalDate dateRetourPrevue;

    private LocalDate dateRetourEffective;

    @Enumerated(EnumType.STRING)
    private StatutEmprunt statut;

    // Cle de l'instance de processus Camunda associee a cet emprunt
    private Long processInstanceKey;

    public EmpruntEntity() {
        super();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LivreEntity getLivre() {
        return livre;
    }

    public void setLivre(LivreEntity livre) {
        this.livre = livre;
    }

    public MembreEntity getMembre() {
        return membre;
    }

    public void setMembre(MembreEntity membre) {
        this.membre = membre;
    }

    public LocalDate getDateEmprunt() {
        return dateEmprunt;
    }

    public void setDateEmprunt(LocalDate dateEmprunt) {
        this.dateEmprunt = dateEmprunt;
    }

    public LocalDate getDateRetourPrevue() {
        return dateRetourPrevue;
    }

    public void setDateRetourPrevue(LocalDate dateRetourPrevue) {
        this.dateRetourPrevue = dateRetourPrevue;
    }

    public LocalDate getDateRetourEffective() {
        return dateRetourEffective;
    }

    public void setDateRetourEffective(LocalDate dateRetourEffective) {
        this.dateRetourEffective = dateRetourEffective;
    }

    public StatutEmprunt getStatut() {
        return statut;
    }

    public void setStatut(StatutEmprunt statut) {
        this.statut = statut;
    }

    public Long getProcessInstanceKey() {
        return processInstanceKey;
    }

    public void setProcessInstanceKey(Long processInstanceKey) {
        this.processInstanceKey = processInstanceKey;
    }
}
