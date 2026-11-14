import { Component, DestroyRef, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EMPTY, catchError, exhaustMap, tap, timer } from 'rxjs';
import { Api } from '../../../core/services/api.service';
import { Notif } from '../../../core/services/notif.service';
import { Emprunt, EmpruntATraiter } from '../../../core/models';
import { fr } from '../../../dates';

@Component({ selector: 'app-finaliser-emprunt', imports: [RouterLink], templateUrl: './finaliser-emprunt.component.html' })
export class FinaliserEmpruntComponent {
  private api = inject(Api);
  private notif = inject(Notif);
  private route = inject(ActivatedRoute);
  private destroyRef = inject(DestroyRef);
  emprunt = signal<Emprunt | null>(null);
  tache = signal<EmpruntATraiter | null>(null);
  chargement = signal(true);
  erreur = signal(false);
  injoignable = signal(false);
  verification = signal(false);
  envoi = signal(false);
  protected fr = fr;

  constructor() {
    this.charger();
    timer(0, 8000).pipe(
      exhaustMap(() => {
        if (this.emprunt()?.statut === 'RETOURNE') return EMPTY;
        return this.api.aTraiter().pipe(
          tap(taches => {
            const id = Number(this.route.snapshot.paramMap.get('id'));
            this.tache.set(taches.find(t => t.emprunt.id === id) ?? null);
            this.verification.set(true);
            this.injoignable.set(false);
          }),
          catchError(() => { this.tache.set(null); this.injoignable.set(true); return EMPTY; })
        );
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe();
  }

  charger() {
    this.chargement.set(true);
    this.erreur.set(false);
    this.api.emprunts.obtenir(Number(this.route.snapshot.paramMap.get('id'))).subscribe({
      next: e => { this.emprunt.set(e); this.chargement.set(false); },
      error: e => { this.chargement.set(false); this.erreur.set(true); this.notif.ko(e); }
    });
  }

  finaliser() {
    const e = this.emprunt();
    if (!e || !this.tache() || this.envoi() || e.statut !== 'EN_COURS') return;
    this.envoi.set(true);
    this.api.retour(e.id).subscribe({
      next: retour => {
        this.emprunt.set(retour);
        this.tache.set(null);
        this.envoi.set(false);
        this.notif.ok(`Retour enregistré : ${retour.livreTitre}`);
      },
      error: err => { this.envoi.set(false); this.notif.ko(err); }
    });
  }
}
