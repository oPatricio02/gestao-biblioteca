import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  LivroResponse,
  CriarLivroRequest,
  AtualizarLivroRequest
} from '../models/livro.model';
import { Page } from '../models/page.model';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LivroService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/livros`;

  listar(page: number = 0, size: number = 10): Observable<Page<LivroResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<Page<LivroResponse>>(this.baseUrl, { params });
  }

  obter(id: string): Observable<LivroResponse> {
    return this.http.get<LivroResponse>(`${this.baseUrl}/${id}`);
  }

  criar(request: CriarLivroRequest): Observable<LivroResponse> {
    return this.http.post<LivroResponse>(this.baseUrl, request);
  }

  alterar(request: AtualizarLivroRequest): Observable<LivroResponse> {
    return this.http.patch<LivroResponse>(this.baseUrl, request);
  }

  deletar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  buscarPorTitulo(titulo: string): Observable<LivroResponse[]> {
    const params = new HttpParams().set('titulo', titulo);
    return this.http.get<LivroResponse[]>(`${this.baseUrl}/buscar`, { params });
  }
}
