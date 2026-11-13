import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Api } from '../../../core/services/api.service';
import { Membre } from '../../../core/models';
import { Notif } from '../../../core/services/notif.service';

const vide = (): Membre => ({ nom: '', prenom: '', email: '', telephone: '' });

@Component({
  selector: 'app-gestion-membres',
  imports: [FormsModule],
  templateUrl: './gestion-membres.component.html',
  styleUrl: './gestion-membres.component.css'
})
export class GestionMembresComponent {
  private api = inject(Api);
  private notif = inject(Notif);

  membres = signal<Membre[]>([]);
  panneau = signal(false);
  form: Membre = vide();

  constructor() { this.charger(); }

  charger() {
    this.api.membres.lister().subscribe({ next: m => this.membres.set(m), error: e => this.notif.ko(e) });
  }

  nouveau() { this.form = vide(); this.panneau.set(true); }
  modifier(m: Membre) { this.form = { ...m }; this.panneau.set(true); }

  enregistrer() {
    const { id, nom, prenom, email, telephone } = this.form;
    const requete = id
      ? this.api.membres.modifier(id, { nom, prenom, email, telephone })
      : this.api.membres.creer({ nom, prenom, email, telephone });
    requete.subscribe({
      next: () => {
        this.notif.ok(id ? 'Membre modifié' : 'Membre ajouté');
        this.panneau.set(false);
        this.charger();
      },
      error: e => this.notif.ko(e)
    });
  }

  supprimer(m: Membre) {
    if (!confirm(`Supprimer ${m.prenom} ${m.nom} ?`)) return;
    this.api.membres.supprimer(m.id!).subscribe({
      next: () => { this.notif.ok('Membre supprimé'); this.charger(); },
      error: e => this.notif.ko(e)
    });
  }
}
