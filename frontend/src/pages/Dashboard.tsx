import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { reconciliationService } from '../services/reconciliationService';

export const Dashboard: React.FC = () => {
  const { user, logout } = useAuth();
  const [fileFinanceiro, setFileFinanceiro] = useState<File | null>(null);
  const [fileCadastro, setFileCadastro] = useState<File | null>(null);
  const [result, setResult] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!fileFinanceiro || !fileCadastro) {
      setError('Selecione ambas as planilhas');
      return;
    }

    setLoading(true);
    setError('');
    try {
      const data = await reconciliationService.reconciliar({
        fileFinanceiro,
        fileCadastro,
        // dataInicioMin: '2025-05-01', // opcional
      });
      setResult(data);
    } catch (err: any) {
      setError(err.message || 'Erro ao processar');
    } finally {
      setLoading(false);
    }
  };

  const downloadReport = async () => {
    if (!result?.historyId) return;
    const blob = await reconciliationService.downloadReport(result.historyId);
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `relatorio_${result.historyId}.xlsx`;
    a.click();
  };

  return (
    <div className="p-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Dashboard</h1>
        <div>
          <span className="mr-4">Olá, {user?.username}</span>
          <button onClick={logout} className="text-red-500">Sair</button>
        </div>
      </div>

      <form onSubmit={handleUpload} className="space-y-4 max-w-xl">
        <div>
          <label className="block text-sm font-medium">Planilha Financeiro</label>
          <input
            type="file"
            accept=".xlsx,.xls"
            onChange={(e) => setFileFinanceiro(e.target.files?.[0] || null)}
            className="mt-1 block w-full"
            required
          />
        </div>
        <div>
          <label className="block text-sm font-medium">Planilha Cadastro</label>
          <input
            type="file"
            accept=".xlsx,.xls"
            onChange={(e) => setFileCadastro(e.target.files?.[0] || null)}
            className="mt-1 block w-full"
            required
          />
        </div>
        <button
          type="submit"
          disabled={loading}
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
        >
          {loading ? 'Processando...' : 'Comparar Planilhas'}
        </button>
        {error && <p className="text-red-500">{error}</p>}
      </form>

      {result && (
        <div className="mt-8">
          <h2 className="text-xl font-bold">Resultado</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-4">
            <div className="p-4 bg-green-100 rounded">Conformes: {result.totalConformes}</div>
            <div className="p-4 bg-red-100 rounded">Faltantes: {result.totalFaltantes}</div>
            <div className="p-4 bg-yellow-100 rounded">Excedentes: {result.totalExcedentes}</div>
            <div className="p-4 bg-purple-100 rounded">Divergências: {result.totalDivergencias}</div>
          </div>
          <button
            onClick={downloadReport}
            className="mt-4 px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
          >
            Baixar Relatório Excel
          </button>
        </div>
      )}
    </div>
  );
};