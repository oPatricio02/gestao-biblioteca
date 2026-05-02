import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'emprestimos',
    loadComponent: () => import('./pages/emprestimos/emprestimos-list/emprestimos-list').then(m => m.EmprestimosListComponent)
  },
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
