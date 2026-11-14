import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Api } from '../../../core/services/api.service';
import { Notif } from '../../../core/services/notif.service';
import { Livre } from '../../../core/models';

@Component({ selector: 'app-detail-livre', imports: [RouterLink], templateUrl: './detail-livre.component.html' })
export class DetailLivreComponent {
  private api = inject(Api);
  private notif = inject(Notif);
  private route = inject(ActivatedRoute);
  livre = signal<Livre | null>(null);
  chargement = signal(true);
  erreur = signal(false);

  constructor() { this.charger(); }

  charger() {
    this.chargement.set(true);
    this.erreur.set(false);
    this.api.livres.obtenir(Number(this.route.snapshot.paramMap.get('numero'))).subscribe({
      next: livre => { this.livre.set(livre); this.chargement.set(false); },
      error: e => { this.chargement.set(false); this.erreur.set(true); this.notif.ko(e); }
    });
  }
}
