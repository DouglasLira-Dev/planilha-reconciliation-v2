import { api } from './api';

interface ReconciliationRequest {
  fileFinanceiro: File;
  fileCadastro: File;
  dataInicioMin?: string;
  dataInicioMax?: string;
  abasCadastro?: string[];
}

export interface ReconciliationResult {
  totalFinanceiro: number;
  totalCadastro: number;
  totalConformes: number;
  totalFaltantes: number;
  totalExcedentes: number;
  totalDivergencias: number;
  totalErros: number;
  totalAvisos: number;
  totalConflitos: number;
  totalCancelados: number;
  totalPossiveisAbreviacoes: number;
  divergencias: Record<string, any[]>;
  faltantes: any[];
  excedentes: any[];
  conflitos: any[];
  possiveisAbreviacoes: any[];
  historyId: number;
  reportDownloadUrl: string;
}

export const reconciliationService = {
  async reconciliar(data: ReconciliationRequest): Promise<ReconciliationResult> {
    const formData = new FormData();
    formData.append('fileFinanceiro', data.fileFinanceiro);
    formData.append('fileCadastro', data.fileCadastro);
    if (data.dataInicioMin) formData.append('dataInicioMin', data.dataInicioMin);
    if (data.dataInicioMax) formData.append('dataInicioMax', data.dataInicioMax);
    if (data.abasCadastro) {
      data.abasCadastro.forEach(aba => formData.append('abasCadastro', aba));
    }

    const response = await api.post('/reconcile', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },

  async downloadReport(historyId: number): Promise<Blob> {
    const response = await api.get(`/report/${historyId}`, {
      responseType: 'blob',
    });
    return response.data;
  },
};