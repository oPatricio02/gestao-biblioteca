import { Component, EventEmitter, inject, Input, OnInit, Output, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UsuarioService } from '../../../services/usuario.service';
import { ObterUsuarioResponse } from '../../../models/usuario.model';

@Component({
  selector: 'app-usuario-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './usuario-form.html',
  styleUrl: './usuario-form.css'
})
export class UsuarioFormComponent implements OnInit {
  @Input() usuario: ObterUsuarioResponse | null = null;
  @Output() saved = new EventEmitter<void>();
  @Output() closed = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private usuarioService = inject(UsuarioService);

  form!: FormGroup;
  submitting = signal(false);
  errorMessage = signal('');

  get isEditing(): boolean {
    return this.usuario !== null;
  }

  ngOnInit() {
    this.form = this.fb.group({
      nome: [this.usuario?.nome || '', [Validators.required, Validators.minLength(2)]],
      email: [this.usuario?.email || '', [Validators.required, Validators.email]],
      telefone: [this.formatPhone(this.usuario?.telefone || ''), [Validators.required, Validators.minLength(14)]]
    });
  }

  formatPhone(value: string): string {
    if (!value) return '';
    const cleaned = value.replace(/\D/g, '');
    let formatted = cleaned;
    if (cleaned.length > 0) {
      formatted = '(' + cleaned.substring(0, 2);
    }
    if (cleaned.length > 2) {
      formatted += ') ' + cleaned.substring(2, 7);
    }
    if (cleaned.length > 7) {
      formatted += '-' + cleaned.substring(7, 11);
    }
    return formatted;
  }

  onTelefoneInput(event: Event) {
    const input = event.target as HTMLInputElement;
    const formatted = this.formatPhone(input.value);
    this.form.patchValue({ telefone: formatted }, { emitEvent: false });
    input.value = formatted;
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
        id: this.usuario!.id,
        ...this.form.value,
        telefone: this.form.value.telefone.replace(/\D/g, '')
      };
      this.usuarioService.alterar(request).subscribe({
        next: () => {
          this.submitting.set(false);
          this.saved.emit();
        },
        error: (err) => {
          this.submitting.set(false);
          this.errorMessage.set(err?.error?.message || 'Erro ao atualizar usuário');
        }
      });
    } else {
      const request = {
        ...this.form.value,
        telefone: this.form.value.telefone.replace(/\D/g, '')
      };
      this.usuarioService.criar(request).subscribe({
        next: () => {
          this.submitting.set(false);
          this.saved.emit();
        },
        error: (err) => {
          this.submitting.set(false);
          this.errorMessage.set(err?.error?.message || 'Erro ao criar usuário');
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
