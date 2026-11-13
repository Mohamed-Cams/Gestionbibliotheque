import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'a-traiter' },
  { path: 'a-traiter', title: 'À traiter', loadComponent: () => import('./features/retours/a-traiter/a-traiter.component').then(m => m.ATraiterComponent) },
  { path: 'emprunts', title: 'Emprunts', loadComponent: () => import('./features/emprunts/liste-emprunts/liste-emprunts.component').then(m => m.ListeEmpruntsComponent) },
  { path: 'livres', title: 'Livres', loadComponent: () => import('./features/livres/liste-livres/liste-livres.component').then(m => m.ListeLivresComponent) },
  { path: 'membres', title: 'Membres', loadComponent: () => import('./features/membres/gestion-membres/gestion-membres.component').then(m => m.GestionMembresComponent) },
  { path: '**', redirectTo: 'a-traiter' }
];
