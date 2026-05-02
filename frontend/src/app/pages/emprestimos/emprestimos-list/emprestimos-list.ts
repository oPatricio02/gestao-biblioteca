import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { EmprestimoService } from '../../../services/emprestimo.service';
import { EmprestimoResponse } from '../../../models/emprestimo.model';
import { Page } from '../../../models/page.model';
import { EmprestimoFormComponent } from '../emprestimo-form/emprestimo-form';

@Component({
  selector: 'app-emprestimos-list',
  standalone: true,
  imports: [EmprestimoFormComponent],
  templateUrl: './emprestimos-list.html',
  styleUrl: './emprestimos-list.css'
})
export class EmprestimosListComponent implements OnInit {
  private emprestimoService = inject(EmprestimoService);

  emprestimos = signal<EmprestimoResponse[]>([]);
  loading = signal(false);
  showForm = signal(false);
  editingEmprestimo = signal<EmprestimoResponse | null>(null);

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
    this.carregarEmprestimos();
  }

  carregarEmprestimos() {
    this.loading.set(true);
    this.emprestimoService.listar(this.currentPage(), this.pageSize()).subscribe({
      next: (page: Page<EmprestimoResponse>) => {
        this.emprestimos.set(page.content);
        this.totalElements.set(page.totalElements);
        this.totalPages.set(page.totalPages);
        this.isFirst.set(page.first);
        this.isLast.set(page.last);
        this.currentPage.set(page.number);
        this.loading.set(false);
      },
      error: () => {
        this.showToast('Erro ao carregar empréstimos', 'error');
        this.loading.set(false);
      }
    });
  }

  goToPage(page: number) {
    if (page < 0 || page >= this.totalPages()) return;
    this.currentPage.set(page);
    this.carregarEmprestimos();
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
    this.editingEmprestimo.set(null);
    this.showForm.set(true);
  }

  abrirFormEdicao(emprestimo: EmprestimoResponse) {
    this.loading.set(true);
    this.emprestimoService.obter(emprestimo.id).subscribe({
      next: (data) => {
        this.editingEmprestimo.set(data);
        this.showForm.set(true);
        this.loading.set(false);
      },
      error: () => {
        this.showToast('Erro ao carregar dados do empréstimo', 'error');
        this.loading.set(false);
      }
    });
  }

  fecharForm() {
    this.showForm.set(false);
    this.editingEmprestimo.set(null);
  }

  onFormSaved() {
    const wasEditing = this.editingEmprestimo();
    this.fecharForm();
    this.carregarEmprestimos();
    this.showToast(
      wasEditing ? 'Empréstimo atualizado com sucesso!' : 'Empréstimo cadastrado com sucesso!',
      'success'
    );
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
