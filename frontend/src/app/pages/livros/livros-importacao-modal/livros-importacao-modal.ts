import { Component, EventEmitter, inject, Output, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { GoogleBooksService, LivroExternoDto } from '../../../services/google-books.service';
import { LivroService } from '../../../services/livro.service';
import { CriarLivroRequest } from '../../../models/livro.model';

@Component({
  selector: 'app-livros-importacao-modal',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './livros-importacao-modal.html',
  styleUrl: './livros-importacao-modal.css'
})
export class LivrosImportacaoModalComponent {
  private googleBooksService = inject(GoogleBooksService);
  private livroService = inject(LivroService);

  @Output() closed = new EventEmitter<void>();
  @Output() imported = new EventEmitter<void>();

  searchTerm = signal('');
  loading = signal(false);
  importing = signal(false);
  error = signal('');
  results = signal<LivroExternoDto[]>([]);
  
  selectedBookIds = signal<Set<string>>(new Set());

  hasSelection = computed(() => this.selectedBookIds().size > 0);
  selectedCount = computed(() => this.selectedBookIds().size);

  onOverlayClick(event: MouseEvent) {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.closed.emit();
    }
  }

  buscar() {
    const term = this.searchTerm().trim();
    if (!term) return;

    this.loading.set(true);
    this.error.set('');
    this.results.set([]);
    this.selectedBookIds.set(new Set());

    this.googleBooksService.buscarLivros(term).subscribe({
      next: (items) => {
        this.results.set(items);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Erro ao buscar livros no Google. Tente novamente mais tarde.');
        this.loading.set(false);
      }
    });
  }

  onSearchKeydown(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.buscar();
    }
  }

  toggleSelection(bookId: string) {
    const current = new Set(this.selectedBookIds());
    if (current.has(bookId)) {
      current.delete(bookId);
    } else {
      current.add(bookId);
    }
    this.selectedBookIds.set(current);
  }

  isSelected(bookId: string): boolean {
    return this.selectedBookIds().has(bookId);
  }

  getThumbnail(item: LivroExternoDto): string {
    return item.thumbnailUrl || '';
  }

  getAuthor(item: LivroExternoDto): string {
    return item.autores && item.autores.length > 0 ? item.autores.join(', ') : 'Autor Desconhecido';
  }

  canImport(item: LivroExternoDto): boolean {
    return !!item.isbn?.trim();
  }

  importarSelecionados() {
    if (!this.hasSelection()) return;

    this.importing.set(true);
    this.error.set('');

    const selectedItems = this.results().filter(item => this.selectedBookIds().has(item.id) && this.canImport(item));
    
    const requests: CriarLivroRequest[] = selectedItems.map(item => ({
      titulo: item.titulo,
      autor: this.getAuthor(item),
      isbn: item.isbn!,
      categoria: item.categoria,
      dataPublicacao: item.dataPublicacao
    }));

    this.livroService.criarEmLote(requests).subscribe({
      next: () => {
        this.importing.set(false);
        this.imported.emit();
      },
      error: () => {
        this.error.set('Erro ao importar livros. Verifique se os ISBNs não estão duplicados.');
        this.importing.set(false);
      }
    });
  }
}
