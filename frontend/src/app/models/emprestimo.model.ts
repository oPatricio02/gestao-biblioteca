export enum StatusEmprestimo {
  DEVOLVIDO = 'DEVOLVIDO',
  ATIVO = 'ATIVO',
  ATRASADO = 'ATRASADO'
}

export interface EmprestimoResponse {
  id: string;
  usuarioId: string;
  nomeUsuario: string;
  livroId: string;
  tituloLivro: string;
  dataEmprestimo: string;
  dataDevolucao: string;
  status: StatusEmprestimo;
}

export interface CriarEmprestimoRequest {
  usuarioId: string;
  livroId: string;
  dataEmprestimo: string;
  dataDevolucao: string;
}

export interface AtualizarEmprestimoRequest {
  id: string;
  dataDevolucao?: string;
  status?: StatusEmprestimo;
}
