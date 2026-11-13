import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Emprunt, EmpruntATraiter, EmpruntRequest, Livre, Membre } from '../models';

class Crud<T, C = T> {
  constructor(private http: HttpClient, private base: string) {}
  lister() { return this.http.get<T[]>(`${this.base}/list`); }
  creer(corps: C) { return this.http.post<T>(this.base, corps); }
  modifier(id: number, corps: unknown) { return this.http.put<T>(`${this.base}/${id}`, corps); }
  supprimer(id: number) { return this.http.delete<void>(`${this.base}/${id}`); }
}

@Injectable({ providedIn: 'root' })
export class Api {
  private http = inject(HttpClient);

  livres = new Crud<Livre>(this.http, '/api/livres');
  membres = new Crud<Membre>(this.http, '/api/membres');
  emprunts = new Crud<Emprunt, EmpruntRequest>(this.http, '/api/emprunts');

  /** Termine la tâche « Retour » dans Camunda et clôture l'emprunt */
  retour(id: number) { return this.http.post<Emprunt>(`/api/emprunts/${id}/retour`, null); }

  /** Liste issue de Camunda */
  aTraiter() { return this.http.get<EmpruntATraiter[]>('/api/emprunts/a-traiter'); }
}
