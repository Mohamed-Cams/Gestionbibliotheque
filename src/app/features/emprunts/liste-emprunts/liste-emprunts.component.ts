import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Api } from '../../../core/services/api.service';
import { aujourdhui, enRetard, fr } from '../../../dates';
import { Emprunt, EmpruntRequest, Livre, Membre } from '../../../core/models';
import { Notif } from '../../../core/services/notif.service';

const vide = (): EmpruntRequest => ({ livreNumero: null, membreId: null, dateRetourPrevue: null });

@Component({
  selector: 'app-liste-emprunts',
  imports: [FormsModule],
  templateUrl: './liste-emprunts.component.html',
  styleUrl: './liste-emprunts.component.css'
})
export class ListeEmpruntsComponent {
  private api = inject(Api);
  private notif = inject(Notif);

  emprunts = signal<Emprunt[]>([]);
  livres = signal<Livre[]>([]);
  membres = signal<Membre[]>([]);
  disponibles = computed(() => this.livres().filter(l => l.disponible));

  panneau = signal(false);
  envoi = signal(false);
  edition = signal<Emprunt | null>(null);
  form: EmpruntRequest = vide();
  nouvelleDate = '';

  protected fr = fr;
  protected retard = enRetard;
  protected aujourdhui = aujourdhui();

  constructor() { this.charger(); }

  charger() {
    this.api.emprunts.lister().subscribe({ next: l => this.emprunts.set(l), error: e => this.notif.ko(e) });
    this.api.livres.lister().subscribe({ next: l => this.livres.set(l), error: e => this.notif.ko(e) });
    this.api.membres.lister().subscribe({ next: m => this.membres.set(m), error: e => this.notif.ko(e) });
  }

  nouveau() { this.form = vide(); this.edition.set(null); this.panneau.set(true); }

  creer() {
    this.envoi.set(true);
    this.api.emprunts.creer({ ...this.form, dateRetourPrevue: this.form.dateRetourPrevue || null }).subscribe({
      next: e => {
        this.envoi.set(false);
        this.panneau.set(false);
        this.notif.ok(`Emprunt créé : ${e.livreTitre}`);
        this.charger();
      },
      error: err => { this.envoi.set(false); this.notif.ko(err); }
    });
  }

  ouvrirDate(e: Emprunt) { this.panneau.set(false); this.nouvelleDate = e.dateRetourPrevue; this.edition.set(e); }

  changerDate() {
    const e = this.edition()!;
    this.api.emprunts.modifier(e.id, { dateRetourPrevue: this.nouvelleDate }).subscribe({
      next: () => { this.edition.set(null); this.notif.ok('Date de retour modifiée'); this.charger(); },
      error: err => this.notif.ko(err)
    });
  }

  rendre(e: Emprunt) {
    this.api.retour(e.id).subscribe({
      next: () => { this.notif.ok(`Retour enregistré : ${e.livreTitre}`); this.charger(); },
      error: err => this.notif.ko(err)
    });
  }

  supprimer(e: Emprunt) {
    const suite = e.statut === 'EN_COURS' ? ' Le suivi Camunda sera annulé et le livre redeviendra disponible.' : '';
    if (!confirm(`Supprimer cet emprunt ?${suite}`)) return;
    this.api.emprunts.supprimer(e.id).subscribe({
      next: () => { this.notif.ok('Emprunt supprimé'); this.charger(); },
      error: err => this.notif.ko(err)
    });
  }
}
