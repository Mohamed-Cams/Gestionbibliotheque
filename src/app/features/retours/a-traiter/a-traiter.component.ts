import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { interval } from 'rxjs';
import { Api } from '../../../core/services/api.service';
import { enRetard, fr, frHeure } from '../../../dates';
import { EmpruntATraiter } from '../../../core/models';
import { Notif } from '../../../core/services/notif.service';

@Component({
  selector: 'app-a-traiter',
  templateUrl: './a-traiter.component.html',
  styleUrl: './a-traiter.component.css'
})
export class ATraiterComponent {
  private api = inject(Api);
  private notif = inject(Notif);

  taches = signal<EmpruntATraiter[]>([]);
  charge = signal(false);
  injoignable = signal(false);
  occupe = signal<number | null>(null);

  protected fr = fr;
  protected frHeure = frHeure;
  protected retard = (t: EmpruntATraiter) => enRetard(t.emprunt);

  constructor() {
    this.charger();
    interval(8000).pipe(takeUntilDestroyed(inject(DestroyRef))).subscribe(() => this.charger());
  }

  charger(manuel = false) {
    this.api.aTraiter().subscribe({
      next: liste => { this.taches.set(liste); this.charge.set(true); this.injoignable.set(false); },
      error: e => { this.injoignable.set(true); if (manuel) this.notif.ko(e); }
    });
  }

  rendre(t: EmpruntATraiter) {
    this.occupe.set(t.emprunt.id);
    this.api.retour(t.emprunt.id).subscribe({
      next: () => {
        this.occupe.set(null);
        this.notif.ok(`Retour enregistré : ${t.emprunt.livreTitre}`);
        this.taches.update(l => l.filter(x => x.userTaskKey !== t.userTaskKey));
      },
      error: e => { this.occupe.set(null); this.notif.ko(e); }
    });
  }
}
