import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface LivroExternoDto {
  id: string;
  titulo: string;
  autores: string[];
  isbn: string | null;
  categoria: string;
  dataPublicacao: string;
  thumbnailUrl?: string;
}

@Injectable({
  providedIn: 'root'
})
export class GoogleBooksService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/livros/pesquisa-externa`;

  buscarLivros(termo: string): Observable<LivroExternoDto[]> {
    const params = new HttpParams().set('titulo', termo);
    return this.http.get<LivroExternoDto[]>(this.apiUrl, { params });
  }
}
