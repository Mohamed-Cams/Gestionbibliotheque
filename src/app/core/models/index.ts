export interface Livre {
  numero?: number;
  titre: string;
  auteur: string;
  categorie: string;
  disponible?: boolean;
}

export interface Membre {
  id?: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
}

export type StatutEmprunt = 'EN_COURS' | 'RETOURNE';

export interface Emprunt {
  id: number;
  livreNumero: number;
  livreTitre: string;
  membreId: number;
  membreNomComplet: string;
  dateEmprunt: string;
  dateRetourPrevue: string;
  dateRetourEffective: string | null;
  statut: StatutEmprunt;
  processInstanceKey: number | null;
}

export interface EmpruntRequest {
  livreNumero: number | null;
  membreId: number | null;
  dateRetourPrevue: string | null;
}

export interface EmpruntATraiter {
  userTaskKey: number;
  tache: string;
  creeLe: string;
  emprunt: Emprunt;
}
