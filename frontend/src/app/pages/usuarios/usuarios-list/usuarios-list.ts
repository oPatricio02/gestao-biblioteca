import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { UsuarioService } from '../../../services/usuario.service';
import { UsuarioResponse, ObterUsuarioResponse } from '../../../models/usuario.model';
import { Page } from '../../../models/page.model';
import { UsuarioFormComponent } from '../usuario-form/usuario-form';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-usuarios-list',
  standalone: true,
  imports: [UsuarioFormComponent, FormsModule],
  templateUrl: './usuarios-list.html',
  styleUrl: './usuarios-list.css'
})
export class UsuariosListComponent implements OnInit {
  private usuarioService = inject(UsuarioService);

  usuarios = signal<UsuarioResponse[]>([]);
  searchTerm = signal('');
  loading = signal(false);
  showForm = signal(false);
  editingUsuario = signal<ObterUsuarioResponse | null>(null);
  showDeleteConfirm = signal(false);
  deletingId = signal<string | null>(null);
  deletingNome = signal('');

  currentPage = signal(0);
  pageSize = signal(10);
  totalElements = signal(0);
  totalPages = signal(0);
  isFirst = signal(true);
  isLast = signal(true);

  toast = signal<{ message: string; type: 'success' | 'error' } | null>(null);

  filteredUsuarios = computed(() => {
    const term = this.searchTerm().toLowerCase().trim();
    if (!term) return this.usuarios();
    return this.usuarios().filter(u =>
      u.nome.toLowerCase().includes(term) ||
      u.email.toLowerCase().includes(term)
    );
  });

  pages = computed(() => {
    const total = this.totalPages();
    const current = this.currentPage();
    const pageNumbers: number[] = [];

    let start = Math.max(0, current - 2);
    let end = Math.min(total - 1, start + 4);
    start = Math.max(0, end - 4);

    for (let i = start; i <= end; i++) {
      pageNumbers.push(i);
    }
    return pageNumbers;
  });

  ngOnInit() {
    this.carregarUsuarios();
  }

  carregarUsuarios() {
    this.loading.set(true);
    this.usuarioService.listar(this.currentPage(), this.pageSize()).subscribe({
      next: (page: Page<UsuarioResponse>) => {
        this.usuarios.set(page.content);
        this.totalElements.set(page.totalElements);
        this.totalPages.set(page.totalPages);
        this.isFirst.set(page.first);
        this.isLast.set(page.last);
        this.currentPage.set(page.number);
        this.loading.set(false);
      },
      error: () => {
        this.showToast('Erro ao carregar usuários', 'error');
        this.loading.set(false);
      }
    });
  }

  goToPage(page: number) {
    if (page < 0 || page >= this.totalPages()) return;
    this.currentPage.set(page);
    this.carregarUsuarios();
  }

  previousPage() {
    if (!this.isFirst()) {
      this.goToPage(this.currentPage() - 1);
    }
  }

  nextPage() {
    if (!this.isLast()) {
      this.goToPage(this.currentPage() + 1);
    }
  }

  onSearch(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    this.searchTerm.set(value);
  }

  abrirFormCriacao() {
    this.editingUsuario.set(null);
    this.showForm.set(true);
  }

  abrirFormEdicao(usuario: UsuarioResponse) {
    this.loading.set(true);
    this.usuarioService.obter(usuario.id).subscribe({
      next: (data) => {
        this.editingUsuario.set(data);
        this.showForm.set(true);
        this.loading.set(false);
      },
      error: () => {
        this.showToast('Erro ao carregar dados do usuário', 'error');
        this.loading.set(false);
      }
    });
  }

  fecharForm() {
    this.showForm.set(false);
    this.editingUsuario.set(null);
  }

  onFormSaved() {
    const wasEditing = this.editingUsuario();
    this.fecharForm();
    this.carregarUsuarios();
    this.showToast(
      wasEditing ? 'Usuário atualizado com sucesso!' : 'Usuário criado com sucesso!',
      'success'
    );
  }

  confirmarExclusao(usuario: UsuarioResponse) {
    this.deletingId.set(usuario.id);
    this.deletingNome.set(usuario.nome);
    this.showDeleteConfirm.set(true);
  }

  cancelarExclusao() {
    this.showDeleteConfirm.set(false);
    this.deletingId.set(null);
    this.deletingNome.set('');
  }

  executarExclusao() {
    const id = this.deletingId();
    if (!id) return;

    this.usuarioService.deletar(id).subscribe({
      next: () => {
        this.cancelarExclusao();
        this.carregarUsuarios();
        this.showToast('Usuário excluído com sucesso!', 'success');
      },
      error: () => {
        this.cancelarExclusao();
        this.showToast('Erro ao excluir usuário', 'error');
      }
    });
  }

  showToast(message: string, type: 'success' | 'error') {
    this.toast.set({ message, type });
    setTimeout(() => this.toast.set(null), 3500);
  }
}
