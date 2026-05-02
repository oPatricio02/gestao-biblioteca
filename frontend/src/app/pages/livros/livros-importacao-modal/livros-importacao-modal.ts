import { Component, EventEmitter, inject, Output, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { GoogleBooksService, GoogleBookItem } from '../../../services/google-books.service';
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
  results = signal<GoogleBookItem[]>([]);
  
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

    this.googleBooksService.buscarLivros(term, 0, 20).subscribe({
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

  getThumbnail(item: GoogleBookItem): string {
    return item.volumeInfo.imageLinks?.thumbnail || 
           item.volumeInfo.imageLinks?.smallThumbnail || 
           'assets/placeholder-book.png';
  }

  getAuthor(item: GoogleBookItem): string {
    return item.volumeInfo.authors?.join(', ') || 'Autor Desconhecido';
  }

  getIsbn(item: GoogleBookItem): string {
    const identifiers = item.volumeInfo.industryIdentifiers || [];
    const isbn13 = identifiers.find(i => i.type === 'ISBN_13');
    const isbn10 = identifiers.find(i => i.type === 'ISBN_10');
    return (isbn13?.identifier || isbn10?.identifier || '0000000000000').replace(/[^0-9X]/gi, '');
  }

  getCategory(item: GoogleBookItem): string {
    return item.volumeInfo.categories?.[0] || 'Geral';
  }

  getPublishedDate(item: GoogleBookItem): string {
    const dateStr = item.volumeInfo.publishedDate;
    if (!dateStr) return new Date().toISOString().split('T')[0];
    
    if (dateStr.length === 4) return `${dateStr}-01-01`;
    if (dateStr.length === 7) return `${dateStr}-01`;
    return dateStr;
  }

  importarSelecionados() {
    if (!this.hasSelection()) return;

    this.importing.set(true);
    this.error.set('');

    const selectedItems = this.results().filter(item => this.selectedBookIds().has(item.id));
    
    const requests: CriarLivroRequest[] = selectedItems.map(item => ({
      titulo: item.volumeInfo.title || 'Título Desconhecido',
      autor: this.getAuthor(item),
      isbn: this.getIsbn(item),
      categoria: this.getCategory(item),
      dataPublicacao: this.getPublishedDate(item)
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
