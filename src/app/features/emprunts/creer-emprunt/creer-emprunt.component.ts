import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Api } from '../../../core/services/api.service';
import { Notif } from '../../../core/services/notif.service';
import { EmpruntRequest, Livre, Membre } from '../../../core/models';
import { aujourdhui } from '../../../dates';

@Component({ selector: 'app-creer-emprunt', imports: [FormsModule, RouterLink], templateUrl: './creer-emprunt.component.html' })
export class CreerEmpruntComponent {
  private api = inject(Api);
  private notif = inject(Notif);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  livres = signal<Livre[]>([]);
  membres = signal<Membre[]>([]);
  disponibles = computed(() => this.livres().filter(l => l.disponible));
  chargement = signal(true);
  erreur = signal(false);
  envoi = signal(false);
  form: EmpruntRequest = { livreNumero: null, membreId: null, dateRetourPrevue: null };
  protected aujourdhui = aujourdhui();

  constructor() { this.charger(); }

  charger() {
    this.chargement.set(true);
    this.erreur.set(false);
    forkJoin({ livres: this.api.livres.lister(), membres: this.api.membres.lister() }).subscribe({
      next: ({ livres, membres }) => {
        this.livres.set(livres);
        this.membres.set(membres);
        const numero = Number(this.route.snapshot.queryParamMap.get('livre'));
        this.form.livreNumero = livres.some(l => l.numero === numero && l.disponible) ? numero : null;
        this.chargement.set(false);
      },
      error: e => { this.chargement.set(false); this.erreur.set(true); this.notif.ko(e); }
    });
  }

  creer() {
    if (this.envoi() || !this.form.livreNumero || !this.form.membreId) return;
    this.envoi.set(true);
    this.api.emprunts.creer({ ...this.form, dateRetourPrevue: this.form.dateRetourPrevue || null }).subscribe({
      next: e => {
        this.envoi.set(false);
        this.notif.ok(`Emprunt créé : ${e.livreTitre}`);
        void this.router.navigate(['/emprunts']);
      },
      error: e => { this.envoi.set(false); this.notif.ko(e); }
    });
  }
}
