import { api } from './api';

export interface HistoryListItem {
  id: number;
  username: string;
  executedAt: string;
  filenameFinanceiro: string;
  filenameCadastro: string;
  totalConformes: number;
  totalFaltantes: number;
  totalExcedentes: number;
  totalDivergencias: number;
  totalErros: number;
  totalAvisos: number;
  totalConflitos: number;
  totalCancelados: number;
  totalPossiveisAbreviacoes: number;
}

export interface HistoryDetail extends HistoryListItem {
  reportPath: string;
  reportDownloadUrl: string;
}

export interface HistoryFilters {
  startDate?: string;
  endDate?: string;
  username?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

export const historyService = {
  async list(filters: HistoryFilters = {}): Promise<{ content: HistoryListItem[]; totalElements: number }> {
    const params = new URLSearchParams();
    if (filters.startDate) params.append('startDate', filters.startDate);
    if (filters.endDate) params.append('endDate', filters.endDate);
    if (filters.username) params.append('username', filters.username);
    if (filters.page !== undefined) params.append('page', String(filters.page));
    if (filters.size) params.append('size', String(filters.size));
    if (filters.sortBy) params.append('sortBy', filters.sortBy);
    if (filters.sortDirection) params.append('sortDirection', filters.sortDirection);

    const response = await api.get(`/history?${params.toString()}`);
    return response.data;
  },

  async getDetail(id: number): Promise<HistoryDetail> {
    const response = await api.get(`/history/${id}`);
    return response.data;
  },

  async delete(id: number): Promise<void> {
    await api.delete(`/history/${id}`);
  },

  downloadReport(id: number): string {
    return `/api/report/${id}`;
  },

  async downloadReportBlob(id: number): Promise<Blob> {
    const response = await api.get(`/report/${id}`, {
      responseType: 'blob',
    });
    return response.data;
  }
};