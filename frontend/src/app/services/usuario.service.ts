import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  UsuarioResponse,
  ObterUsuarioResponse,
  CriarUsuarioRequest,
  AtualizarUsuarioRequest
} from '../models/usuario.model';
import { Page } from '../models/page.model';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/usuarios`;

  listar(page: number = 0, size: number = 10): Observable<Page<UsuarioResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<Page<UsuarioResponse>>(this.baseUrl, { params });
  }

  obter(id: string): Observable<ObterUsuarioResponse> {
    return this.http.get<ObterUsuarioResponse>(`${this.baseUrl}/${id}`);
  }

  criar(request: CriarUsuarioRequest): Observable<UsuarioResponse> {
    return this.http.post<UsuarioResponse>(this.baseUrl, request);
  }

  alterar(request: AtualizarUsuarioRequest): Observable<ObterUsuarioResponse> {
    return this.http.patch<ObterUsuarioResponse>(this.baseUrl, request);
  }

  deletar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
