import { Routes } from '@angular/router';
import { connexionRequise } from './core/services/auth.service';

export const routes: Routes = [
  { path: 'login', title: 'Connexion', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
  { path: '', canActivateChild: [connexionRequise], children: [
  { path: '', pathMatch: 'full', redirectTo: 'a-traiter' },
  { path: 'a-traiter', title: 'À traiter', loadComponent: () => import('./features/retours/a-traiter/a-traiter.component').then(m => m.ATraiterComponent) },
  { path: 'emprunts', title: 'Emprunts', loadComponent: () => import('./features/emprunts/liste-emprunts/liste-emprunts.component').then(m => m.ListeEmpruntsComponent) },
  { path: 'emprunts/nouveau', title: 'Nouvel emprunt', loadComponent: () => import('./features/emprunts/creer-emprunt/creer-emprunt.component').then(m => m.CreerEmpruntComponent) },
  { path: 'emprunts/:id/finaliser', title: 'Finaliser un emprunt', loadComponent: () => import('./features/emprunts/finaliser-emprunt/finaliser-emprunt.component').then(m => m.FinaliserEmpruntComponent) },
  { path: 'livres', title: 'Livres', loadComponent: () => import('./features/livres/liste-livres/liste-livres.component').then(m => m.ListeLivresComponent) },
  { path: 'livres/:numero', title: 'Détail du livre', loadComponent: () => import('./features/livres/detail-livre/detail-livre.component').then(m => m.DetailLivreComponent) },
  { path: 'membres', title: 'Membres', loadComponent: () => import('./features/membres/gestion-membres/gestion-membres.component').then(m => m.GestionMembresComponent) },
  { path: '**', redirectTo: 'a-traiter' }
  ] }
];
