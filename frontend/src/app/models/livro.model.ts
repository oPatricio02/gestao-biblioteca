export interface LivroResponse {
  id: string;
  titulo: string;
  autor: string;
  isbn: string;
  dataPublicacao: string;
  categoria: string;
  disponivel: boolean;
}

export interface CriarLivroRequest {
  titulo: string;
  autor: string;
  isbn: string;
  dataPublicacao: string;
  categoria: string;
}

export interface AtualizarLivroRequest {
  id: string;
  titulo?: string;
  autor?: string;
  isbn?: string;
  dataPublicacao?: string;
  categoria?: string;
}
