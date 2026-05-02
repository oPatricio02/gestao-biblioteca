export interface UsuarioResponse {
  id: string;
  nome: string;
  email: string;
}

export interface ObterUsuarioResponse {
  id: string;
  nome: string;
  email: string;
  dataCadastro: string;
  telefone: string;
}

export interface CriarUsuarioRequest {
  nome: string;
  email: string;
  telefone: string;
}

export interface AtualizarUsuarioRequest {
  id: string;
  nome?: string;
  email?: string;
  telefone?: string;
}
