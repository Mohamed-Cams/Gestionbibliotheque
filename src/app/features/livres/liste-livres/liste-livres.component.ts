import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Api } from '../../../core/services/api.service';
import { Livre } from '../../../core/models';
import { Notif } from '../../../core/services/notif.service';

const vide = (): Livre => ({ titre: '', auteur: '', categorie: '' });

@Component({
  selector: 'app-liste-livres',
  imports: [FormsModule, RouterLink],
  templateUrl: './liste-livres.component.html',
  styleUrl: './liste-livres.component.css'
})
export class ListeLivresComponent {
  private api = inject(Api);
  private notif = inject(Notif);

  livres = signal<Livre[]>([]);
  recherche = signal('');
  panneau = signal(false);
  form: Livre = vide();

  visibles = computed(() => {
    const q = this.recherche().trim().toLowerCase();
    return this.livres().filter(l => !q || `${l.titre} ${l.auteur}`.toLowerCase().includes(q));
  });

  constructor() { this.charger(); }

  charger() {
    this.api.livres.lister().subscribe({ next: l => this.livres.set(l), error: e => this.notif.ko(e) });
  }

  nouveau() { this.form = vide(); this.panneau.set(true); }
  modifier(l: Livre) { this.form = { ...l }; this.panneau.set(true); }

  enregistrer() {
    const { numero, titre, auteur, categorie } = this.form;
    const requete = numero
      ? this.api.livres.modifier(numero, { titre, auteur, categorie })
      : this.api.livres.creer({ titre, auteur, categorie });

    requete.subscribe({
      next: () => {
        this.notif.ok(numero ? 'Livre modifié' : 'Livre ajouté');
        this.panneau.set(false);
        this.charger();
      },
      error: e => this.notif.ko(e)
    });
  }

  supprimer(l: Livre) {
    if (!confirm(`Supprimer « ${l.titre} » ?`)) return;
    this.api.livres.supprimer(l.numero!).subscribe({
      next: () => { this.notif.ok('Livre supprimé'); this.charger(); },
      error: e => this.notif.ko(e)
    });
  }
}
