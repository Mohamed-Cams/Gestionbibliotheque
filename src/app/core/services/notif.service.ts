import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class Notif {
  message = signal<string | null>(null);
  erreur = signal(false);
  private minuteur?: ReturnType<typeof setTimeout>;

  ok(texte: string) { this.afficher(texte, false); }

  ko(e: unknown) {
    const h = e as HttpErrorResponse;
    const texte = h.status === 0
      ? 'Le serveur ne répond pas. Vérifiez que le backend tourne sur le port 8081.'
      : h.error?.message || h.error?.detail || `Erreur ${h.status}`;
    this.afficher(texte, true);
  }

  fermer() { this.message.set(null); }

  private afficher(texte: string, erreur: boolean) {
    clearTimeout(this.minuteur);
    this.erreur.set(erreur);
    this.message.set(texte);
    this.minuteur = setTimeout(() => this.fermer(), erreur ? 8000 : 4000);
  }
}
