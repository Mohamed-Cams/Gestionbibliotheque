import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Notif } from './core/services/notif.service';
import { Auth } from './core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected notif = inject(Notif);
  protected auth = inject(Auth);
  private router = inject(Router);

  deconnecter() {
    this.auth.deconnecter().subscribe({
      next: () => { void this.router.navigate(['/login']); },
      error: e => this.notif.ko(e)
    });
  }
}
