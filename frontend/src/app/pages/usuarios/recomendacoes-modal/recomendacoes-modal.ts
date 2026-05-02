import { Component, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';
import { UsuarioService } from '../../../services/usuario.service';
import { LivroResponse } from '../../../models/livro.model';

@Component({
  selector: 'app-recomendacoes-modal',
  standalone: true,
  imports: [],
  templateUrl: './recomendacoes-modal.html',
  styleUrl: './recomendacoes-modal.css'
})
export class RecomendacoesModalComponent implements OnInit {
  @Input({ required: true }) usuarioId!: string;
  @Input({ required: true }) usuarioNome!: string;
  @Output() closed = new EventEmitter<void>();

  private usuarioService = inject(UsuarioService);

  loading = signal(true);
  error = signal(false);
  livros = signal<LivroResponse[]>([]);

  ngOnInit() {
    this.carregarRecomendacoes();
  }

  carregarRecomendacoes() {
    this.loading.set(true);
    this.error.set(false);
    
    this.usuarioService.recomendarLivros(this.usuarioId).subscribe({
      next: (data: LivroResponse[]) => {
        this.livros.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      }
    });
  }

  onOverlayClick(event: MouseEvent) {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.closed.emit();
    }
  }
}
