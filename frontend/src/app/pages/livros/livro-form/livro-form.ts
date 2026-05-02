import { Component, EventEmitter, inject, Input, OnInit, Output, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { LivroService } from '../../../services/livro.service';
import { LivroResponse } from '../../../models/livro.model';

@Component({
  selector: 'app-livro-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './livro-form.html',
  styleUrl: './livro-form.css'
})
export class LivroFormComponent implements OnInit {
  @Input() livro: LivroResponse | null = null;
  @Output() saved = new EventEmitter<void>();
  @Output() closed = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private livroService = inject(LivroService);

  form!: FormGroup;
  submitting = signal(false);
  errorMessage = signal('');

  get isEditing(): boolean {
    return this.livro !== null;
  }

  ngOnInit() {
    this.form = this.fb.group({
      titulo: [this.livro?.titulo || '', [Validators.required, Validators.minLength(2)]],
      autor: [this.livro?.autor || '', [Validators.required, Validators.minLength(2)]],
      isbn: [this.livro?.isbn || '', [Validators.required, Validators.minLength(10)]],
      dataPublicacao: [this.formatDateForInput(this.livro?.dataPublicacao), [Validators.required]],
      categoria: [this.livro?.categoria || '', [Validators.required]]
    });
  }

  private formatDateForInput(date: string | undefined): string {
    if (!date) return '';
    return date.substring(0, 10);
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
        id: this.livro!.id,
        ...this.form.value
      };
      this.livroService.alterar(request).subscribe({
        next: () => {
          this.submitting.set(false);
          this.saved.emit();
        },
        error: (err) => {
          this.submitting.set(false);
          this.errorMessage.set(err?.error?.message || 'Erro ao atualizar livro');
        }
      });
    } else {
      this.livroService.criar(this.form.value).subscribe({
        next: () => {
          this.submitting.set(false);
          this.saved.emit();
        },
        error: (err) => {
          this.submitting.set(false);
          this.errorMessage.set(err?.error?.message || 'Erro ao criar livro');
        }
      });
    }
  }

  onOverlayClick(event: MouseEvent) {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.closed.emit();
    }
  }

  isInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!(control && control.invalid && control.touched);
  }
}
