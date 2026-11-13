import { Emprunt } from './core/models';

/** "2026-10-31" -> "31/10/2026" */
export function fr(iso?: string | null): string {
  if (!iso) return '—';
  return new Date(iso.length === 10 ? `${iso}T00:00:00` : iso).toLocaleDateString('fr-FR');
}

export function frHeure(iso?: string | null): string {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('fr-FR', { dateStyle: 'short', timeStyle: 'short' });
}

/** Date du jour au format AAAA-MM-JJ (heure locale) */
export function aujourdhui(): string {
  return new Date().toLocaleDateString('sv-SE');
}

export function enRetard(e: Emprunt): boolean {
  return e.statut === 'EN_COURS' && e.dateRetourPrevue < aujourdhui();
}
