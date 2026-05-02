import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'usuarios',
    pathMatch: 'full'
  },
  {
    path: 'usuarios',
    loadComponent: () =>
      import('./pages/usuarios/usuarios-list/usuarios-list').then(m => m.UsuariosListComponent)
  },
  {
    path: 'livros',
    loadComponent: () =>
      import('./pages/livros/livros-list/livros-list').then(m => m.LivrosListComponent)
  }
];
