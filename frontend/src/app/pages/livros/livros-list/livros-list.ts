import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { LivroService } from '../../../services/livro.service';
import { LivroResponse } from '../../../models/livro.model';
import { Page } from '../../../models/page.model';
import { LivroFormComponent } from '../livro-form/livro-form';
import { LivrosImportacaoModalComponent } from '../livros-importacao-modal/livros-importacao-modal';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-livros-list',
  standalone: true,
  imports: [LivroFormComponent, LivrosImportacaoModalComponent, FormsModule],
  templateUrl: './livros-list.html',
  styleUrl: './livros-list.css'
})
export class LivrosListComponent implements OnInit {
  private livroService = inject(LivroService);

  livros = signal<LivroResponse[]>([]);
  searchTerm = signal('');
  loading = signal(false);
  showForm = signal(false);
  showImportacao = signal(false);
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
  
  searchSubject = new Subject<string>();

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
    
    this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(term => {
      this.searchTerm.set(term);
      if (term) {
        this.buscarNoBackend(term);
      } else {
        this.currentPage.set(0);
        this.carregarLivros();
      }
    });
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

  onSearch(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    this.searchSubject.next(value.trim());
  }

  buscarNoBackend(term: string) {
    this.loading.set(true);
    this.livroService.buscarPorTitulo(term).subscribe({
      next: (resultados) => {
        this.livros.set(resultados);
        this.totalElements.set(resultados.length);
        this.totalPages.set(1);
        this.isFirst.set(true);
        this.isLast.set(true);
        this.currentPage.set(0);
        this.loading.set(false);
      },
      error: () => {
        this.showToast('Erro ao buscar livros', 'error');
        this.loading.set(false);
      }
    });
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

  abrirImportacao() {
    this.showImportacao.set(true);
  }

  fecharImportacao() {
    this.showImportacao.set(false);
  }

  onImported() {
    this.fecharImportacao();
    this.carregarLivros();
    this.showToast('Livros importados com sucesso!', 'success');
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
      error: (error: HttpErrorResponse) => {
        this.cancelarExclusao();
        this.showToast(this.extractErrorMessage(error, 'Erro ao excluir livro'), 'error');
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

  private extractErrorMessage(error: HttpErrorResponse, fallback: string): string {
    return error.error?.message || fallback;
  }

  formatIsbn(value: string): string {
    if (!value) return '';
    let cleaned = value.replace(/[^\dX]/gi, '').toUpperCase();
    
    if (cleaned.startsWith('978') || cleaned.startsWith('979') || cleaned.length > 10) {
      cleaned = cleaned.substring(0, 13);
      let formatted = cleaned.substring(0, 3);
      if (cleaned.length > 3) formatted += '-' + cleaned.substring(3, 4);
      if (cleaned.length > 4) formatted += '-' + cleaned.substring(4, 6);
      if (cleaned.length > 6) formatted += '-' + cleaned.substring(6, 12);
      if (cleaned.length > 12) formatted += '-' + cleaned.substring(12, 13);
      return formatted;
    } else {
      cleaned = cleaned.substring(0, 10);
      let formatted = cleaned.substring(0, 1);
      if (cleaned.length > 1) formatted += '-' + cleaned.substring(1, 3);
      if (cleaned.length > 3) formatted += '-' + cleaned.substring(3, 9);
      if (cleaned.length > 9) formatted += '-' + cleaned.substring(9, 10);
      return formatted;
    }
  }
}
