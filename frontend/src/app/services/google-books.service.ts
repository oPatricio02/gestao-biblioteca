import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface GoogleBooksVolumeInfo {
  title: string;
  authors?: string[];
  publisher?: string;
  publishedDate?: string;
  description?: string;
  industryIdentifiers?: { type: string; identifier: string }[];
  categories?: string[];
  imageLinks?: {
    smallThumbnail?: string;
    thumbnail?: string;
  };
}

export interface GoogleBookItem {
  id: string;
  volumeInfo: GoogleBooksVolumeInfo;
}

export interface GoogleBooksResponse {
  items?: GoogleBookItem[];
  totalItems: number;
}

@Injectable({
  providedIn: 'root'
})
export class GoogleBooksService {
  private http = inject(HttpClient);
  private apiUrl = 'https://www.googleapis.com/books/v1/volumes';

  buscarLivros(termo: string, startIndex = 0, maxResults = 10): Observable<GoogleBookItem[]> {
    const url = `${this.apiUrl}?q=${encodeURIComponent(termo)}&startIndex=${startIndex}&maxResults=${maxResults}`;
    return this.http.get<GoogleBooksResponse>(url).pipe(
      map(response => response.items || [])
    );
  }
}
