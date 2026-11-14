import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Auth } from '../../../core/services/auth.service';
import { Notif } from '../../../core/services/notif.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private auth = inject(Auth);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private notif = inject(Notif);
  email = '';
  motDePasse = '';
  envoi = signal(false);

  connecter() {
    if (this.envoi()) return;
    this.envoi.set(true);
    this.auth.connecter(this.email, this.motDePasse).subscribe({
      next: () => {
        this.envoi.set(false);
        const retour = this.route.snapshot.queryParamMap.get('retour');
        void this.router.navigateByUrl(retour?.startsWith('/') && !retour.startsWith('//')
          && !retour.startsWith('/login') ? retour : '/a-traiter');
      },
      error: e => { this.envoi.set(false); this.notif.ko(e); }
    });
  }
}
