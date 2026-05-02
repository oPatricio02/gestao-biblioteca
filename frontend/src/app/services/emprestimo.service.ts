import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  EmprestimoResponse,
  CriarEmprestimoRequest,
  AtualizarEmprestimoRequest
} from '../models/emprestimo.model';
import { Page } from '../models/page.model';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EmprestimoService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/emprestimos`;

  listar(page: number = 0, size: number = 10): Observable<Page<EmprestimoResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<Page<EmprestimoResponse>>(this.baseUrl, { params });
  }

  obter(id: string): Observable<EmprestimoResponse> {
    return this.http.get<EmprestimoResponse>(`${this.baseUrl}/${id}`);
  }

  criar(request: CriarEmprestimoRequest): Observable<EmprestimoResponse> {
    return this.http.post<EmprestimoResponse>(this.baseUrl, request);
  }

  alterar(request: AtualizarEmprestimoRequest): Observable<EmprestimoResponse> {
    return this.http.patch<EmprestimoResponse>(this.baseUrl, request);
  }
}
