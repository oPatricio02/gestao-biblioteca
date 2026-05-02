import { Component, EventEmitter, inject, Input, OnInit, Output, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { EmprestimoService } from '../../../services/emprestimo.service';
import { EmprestimoResponse, StatusEmprestimo } from '../../../models/emprestimo.model';
import { UsuarioService } from '../../../services/usuario.service';
import { LivroService } from '../../../services/livro.service';
import { ObterUsuarioResponse } from '../../../models/usuario.model';
import { LivroResponse } from '../../../models/livro.model';

@Component({
  selector: 'app-emprestimo-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './emprestimo-form.html',
  styleUrl: './emprestimo-form.css'
})
export class EmprestimoFormComponent implements OnInit {
  @Input() emprestimo: EmprestimoResponse | null = null;
  @Output() saved = new EventEmitter<void>();
  @Output() closed = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private emprestimoService = inject(EmprestimoService);
  private usuarioService = inject(UsuarioService);
  private livroService = inject(LivroService);

  form!: FormGroup;
  submitting = signal(false);
  errorMessage = signal('');

  // Autocomplete state
  usuariosResult = signal<ObterUsuarioResponse[]>([]);
  livrosResult = signal<LivroResponse[]>([]);
  
  showUsuariosDropdown = signal(false);
  showLivrosDropdown = signal(false);

  selectedUsuario = signal<ObterUsuarioResponse | null>(null);
  selectedLivro = signal<LivroResponse | null>(null);

  get isEditing(): boolean {
    return this.emprestimo !== null;
  }

  ngOnInit() {
    this.form = this.fb.group({
      usuarioBusca: [{ value: this.emprestimo?.nomeUsuario || '', disabled: this.isEditing }],
      usuarioId: [this.emprestimo?.usuarioId || '', [Validators.required]],
      
      livroBusca: [{ value: this.emprestimo?.tituloLivro || '', disabled: this.isEditing }],
      livroId: [this.emprestimo?.livroId || '', [Validators.required]],
      
      dataEmprestimo: [{ value: this.formatDateForInput(this.emprestimo?.dataEmprestimo) || this.getTodayDate(), disabled: this.isEditing }, [Validators.required]],
      dataDevolucao: [this.formatDateForInput(this.emprestimo?.dataDevolucao), [Validators.required]],
      status: [this.emprestimo?.status || StatusEmprestimo.ATIVO]
    });
  }

  private formatDateForInput(date: string | undefined): string {
    if (!date) return '';
    return date.substring(0, 10);
  }

  private getTodayDate(): string {
    return new Date().toISOString().substring(0, 10);
  }

  buscarUsuarios(event: Event) {
    if (this.isEditing) return;
    const termo = (event.target as HTMLInputElement).value;
    
    if (termo.length < 2) {
      this.usuariosResult.set([]);
      this.showUsuariosDropdown.set(false);
      return;
    }

    this.usuarioService.buscarPorNome(termo).subscribe({
      next: (resultados) => {
        this.usuariosResult.set(resultados);
        this.showUsuariosDropdown.set(true);
      }
    });
  }

  selecionarUsuario(usuario: ObterUsuarioResponse) {
    this.selectedUsuario.set(usuario);
    this.form.patchValue({
      usuarioBusca: usuario.nome,
      usuarioId: usuario.id
    });
    this.showUsuariosDropdown.set(false);
  }

  buscarLivros(event: Event) {
    if (this.isEditing) return;
    const termo = (event.target as HTMLInputElement).value;
    
    if (termo.length < 2) {
      this.livrosResult.set([]);
      this.showLivrosDropdown.set(false);
      return;
    }

    this.livroService.buscarPorTitulo(termo).subscribe({
      next: (resultados) => {
        this.livrosResult.set(resultados);
        this.showLivrosDropdown.set(true);
      }
    });
  }

  selecionarLivro(livro: LivroResponse) {
    this.selectedLivro.set(livro);
    this.form.patchValue({
      livroBusca: livro.titulo,
      livroId: livro.id
    });
    this.showLivrosDropdown.set(false);
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set('');

    if (this.isEditing) {
      const request = {
        id: this.emprestimo!.id,
        dataDevolucao: this.form.value.dataDevolucao,
        status: this.form.value.status
      };
      this.emprestimoService.alterar(request).subscribe({
        next: () => {
          this.submitting.set(false);
          this.saved.emit();
        },
        error: (err) => {
          this.submitting.set(false);
          this.errorMessage.set(err?.error?.message || 'Erro ao atualizar empréstimo');
        }
      });
    } else {
      const request = {
        usuarioId: this.form.value.usuarioId,
        livroId: this.form.value.livroId,
        dataEmprestimo: this.form.getRawValue().dataEmprestimo, // because it's disabled in edit, we use getRawValue for consistency if needed
        dataDevolucao: this.form.value.dataDevolucao
      };
      this.emprestimoService.criar(request).subscribe({
        next: () => {
          this.submitting.set(false);
          this.saved.emit();
        },
        error: (err) => {
          this.submitting.set(false);
          this.errorMessage.set(err?.error?.message || 'Erro ao criar empréstimo');
        }
      });
    }
  }

  onOverlayClick(event: MouseEvent) {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.closed.emit();
    }
  }

  fecharDropdowns() {
    setTimeout(() => {
      this.showUsuariosDropdown.set(false);
      this.showLivrosDropdown.set(false);
    }, 200);
  }

  isInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!(control && control.invalid && control.touched);
  }
}
