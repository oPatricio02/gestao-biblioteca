import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { LivroService } from '../../../services/livro.service';
import { LivroResponse } from '../../../models/livro.model';
import { Page } from '../../../models/page.model';
import { LivroFormComponent } from '../livro-form/livro-form';

@Component({
  selector: 'app-livros-list',
  standalone: true,
  imports: [LivroFormComponent],
  templateUrl: './livros-list.html',
  styleUrl: './livros-list.css'
})
export class LivrosListComponent implements OnInit {
  private livroService = inject(LivroService);

  livros = signal<LivroResponse[]>([]);
  loading = signal(false);
  showForm = signal(false);
  editingLivro = signal<LivroResponse | null>(null);
  showDeleteConfirm = signal(false);
  deletingId = signal<string | null>(null);
  deletingTitulo = signal('');

  currentPage = signal(0);
  pageSize = signal(10);
  totalElements = signal(0);
  totalPages = signal(0);
  isFirst = signal(true);
  isLast = signal(true);

  toast = signal<{ message: string; type: 'success' | 'error' } | null>(null);



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
    this.carregarLivros();
  }

  carregarLivros() {
    this.loading.set(true);
    this.livroService.listar(this.currentPage(), this.pageSize()).subscribe({
      next: (page: Page<LivroResponse>) => {
        this.livros.set(page.content);
        this.totalElements.set(page.totalElements);
        this.totalPages.set(page.totalPages);
        this.isFirst.set(page.first);
        this.isLast.set(page.last);
        this.currentPage.set(page.number);
        this.loading.set(false);
      },
      error: () => {
        this.showToast('Erro ao carregar livros', 'error');
        this.loading.set(false);
      }
    });
  }

  goToPage(page: number) {
    if (page < 0 || page >= this.totalPages()) return;
    this.currentPage.set(page);
    this.carregarLivros();
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



  abrirFormCriacao() {
    this.editingLivro.set(null);
    this.showForm.set(true);
  }

  abrirFormEdicao(livro: LivroResponse) {
    this.loading.set(true);
    this.livroService.obter(livro.id).subscribe({
      next: (data) => {
        this.editingLivro.set(data);
        this.showForm.set(true);
        this.loading.set(false);
      },
      error: () => {
        this.showToast('Erro ao carregar dados do livro', 'error');
        this.loading.set(false);
      }
    });
  }

  fecharForm() {
    this.showForm.set(false);
    this.editingLivro.set(null);
  }

  onFormSaved() {
    const wasEditing = this.editingLivro();
    this.fecharForm();
    this.carregarLivros();
    this.showToast(
      wasEditing ? 'Livro atualizado com sucesso!' : 'Livro cadastrado com sucesso!',
      'success'
    );
  }

  confirmarExclusao(livro: LivroResponse) {
    this.deletingId.set(livro.id);
    this.deletingTitulo.set(livro.titulo);
    this.showDeleteConfirm.set(true);
  }

  cancelarExclusao() {
    this.showDeleteConfirm.set(false);
    this.deletingId.set(null);
    this.deletingTitulo.set('');
  }

  executarExclusao() {
    const id = this.deletingId();
    if (!id) return;

    this.livroService.deletar(id).subscribe({
      next: () => {
        this.cancelarExclusao();
        this.carregarLivros();
        this.showToast('Livro excluído com sucesso!', 'success');
      },
      error: () => {
        this.cancelarExclusao();
        this.showToast('Erro ao excluir livro', 'error');
      }
    });
  }

  formatDate(date: string): string {
    if (!date) return '';
    const parts = date.split('-');
    if (parts.length === 3) {
      return `${parts[2]}/${parts[1]}/${parts[0]}`;
    }
    return date;
  }

  showToast(message: string, type: 'success' | 'error') {
    this.toast.set({ message, type });
    setTimeout(() => this.toast.set(null), 3500);
  }
}
