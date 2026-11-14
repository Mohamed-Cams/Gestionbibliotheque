import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Api } from '../../../core/services/api.service';
import { enRetard, fr } from '../../../dates';
import { Emprunt } from '../../../core/models';
import { Notif } from '../../../core/services/notif.service';


@Component({
  selector: 'app-liste-emprunts',
  imports: [FormsModule, RouterLink],
  templateUrl: './liste-emprunts.component.html',
  styleUrl: './liste-emprunts.component.css'
})
export class ListeEmpruntsComponent {
  private api = inject(Api);
  private notif = inject(Notif);

  emprunts = signal<Emprunt[]>([]);

  envoi = signal(false);
  edition = signal<Emprunt | null>(null);
  nouvelleDate = '';

  protected fr = fr;
  protected retard = enRetard;

  constructor() { this.charger(); }

  charger() {
    this.api.emprunts.lister().subscribe({ next: l => this.emprunts.set(l), error: e => this.notif.ko(e) });
  }

  ouvrirDate(e: Emprunt) { this.nouvelleDate = e.dateRetourPrevue; this.edition.set(e); }

  changerDate() {
    const e = this.edition();
    if (!e || this.envoi()) return;
    this.envoi.set(true);
    this.api.emprunts.modifier(e.id, { dateRetourPrevue: this.nouvelleDate }).subscribe({
      next: () => { this.envoi.set(false); this.edition.set(null); this.notif.ok('Date de retour modifiée'); this.charger(); },
      error: err => { this.envoi.set(false); this.notif.ko(err); }
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
