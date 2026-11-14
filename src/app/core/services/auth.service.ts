import { HttpClient, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of, tap, throwError } from 'rxjs';

interface Utilisateur { email: string; }

@Injectable({ providedIn: 'root' })
export class Auth {
  private http = inject(HttpClient);
  utilisateur = signal<Utilisateur | null>(null);

  connecter(email: string, motDePasse: string) {
    return this.http.post<Utilisateur>('/api/auth/login', { email, motDePasse })
      .pipe(tap(u => this.utilisateur.set(u)));
  }

  session() {
    return this.http.get<Utilisateur>('/api/auth/session').pipe(tap(u => this.utilisateur.set(u)));
  }

  deconnecter() {
    return this.http.post<void>('/api/auth/logout', null).pipe(tap(() => this.utilisateur.set(null)));
  }
}

export const connexionRequise: CanActivateFn = (_route, state) => {
  const auth = inject(Auth);
  const router = inject(Router);
  return auth.session().pipe(
    map(() => true),
    catchError(() => {
      auth.utilisateur.set(null);
      return of(router.createUrlTree(['/login'], { queryParams: { retour: state.url } }));
    })
  );
};

export const sessionInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(Auth);
  const router = inject(Router);
  return next(request).pipe(catchError((e: HttpErrorResponse) => {
    if (e.status === 401 && request.url.startsWith('/api/') && !request.url.startsWith('/api/auth/')) {
      auth.utilisateur.set(null);
      void router.navigate(['/login'], { queryParams: { retour: router.url } });
    }
    return throwError(() => e);
  }));
};
