import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { reconciliationService } from '../services/reconciliationService';
import { useReconciliationProgress } from '../hooks/useReconciliationProgress';

export const Dashboard: React.FC = () => {
  const { user, logout } = useAuth();
  const [fileFinanceiro, setFileFinanceiro] = useState<File | null>(null);
  const [fileCadastro, setFileCadastro] = useState<File | null>(null);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState<any>(null);

  const { progress, result: progressResult, error: progressError, isComplete } = useReconciliationProgress(sessionId);

  // Quando o resultado chegar via SSE, guardamos
  React.useEffect(() => {
    if (progressResult) {
      setResult(progressResult);
      setLoading(false);
    }
    if (progressError) {
      setError(progressError);
      setLoading(false);
    }
  }, [progressResult, progressError]);

  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!fileFinanceiro || !fileCadastro) {
      setError('Selecione ambas as planilhas');
      return;
    }

    setLoading(true);
    setError('');
    setResult(null);
    try {
      const sessionId = await reconciliationService.reconciliarAsync({
        fileFinanceiro,
        fileCadastro,
      });
      setSessionId(sessionId);
    } catch (err: any) {
      setError(err.message || 'Erro ao iniciar processamento');
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
    window.URL.revokeObjectURL(url);
  };

  return (
    <div className="p-8 max-w-4xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Dashboard</h1>
        <div>
          <span className="mr-4">Olá, {user?.username}</span>
          <button onClick={logout} className="text-red-500 hover:text-red-700">Sair</button>
        </div>
      </div>

      <form onSubmit={handleUpload} className="space-y-4 bg-white p-6 rounded-lg shadow">
        <div>
          <label className="block text-sm font-medium text-gray-700">Planilha Financeiro</label>
          <input
            type="file"
            accept=".xlsx,.xls"
            onChange={(e) => setFileFinanceiro(e.target.files?.[0] || null)}
            className="mt-1 block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded file:border-0 file:text-sm file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
            required
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">Planilha Cadastro</label>
          <input
            type="file"
            accept=".xlsx,.xls"
            onChange={(e) => setFileCadastro(e.target.files?.[0] || null)}
            className="mt-1 block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded file:border-0 file:text-sm file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
            required
          />
        </div>
        <button
          type="submit"
          disabled={loading}
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loading ? 'Processando...' : 'Comparar Planilhas'}
        </button>
        {error && <p className="text-red-500">{error}</p>}
      </form>

      {/* Barra de progresso */}
      {loading && (
        <div className="mt-6 bg-white p-4 rounded-lg shadow">
          <div className="flex justify-between text-sm">
            <span>{progress.message}</span>
            <span>{progress.percentage}%</span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-4 mt-2">
            <div
              className="bg-blue-600 h-4 rounded-full transition-all duration-300"
              style={{ width: `${progress.percentage}%` }}
            />
          </div>
        </div>
      )}

      {/* Mensagem de Conclusão */}
      {isComplete && (
        <div className="mt-4 p-3 bg-green-50 border border-green-200 rounded-lg text-green-700">
          ✅ Processamento finalizado! Clique em "Baixar Relatório" para obter o arquivo.
        </div>
      )}

      {/* Resultado */}
      {result && (
        <div className="mt-8 bg-white p-6 rounded-lg shadow">
          <h2 className="text-xl font-bold mb-4">Resultado da Comparação</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="p-4 bg-green-100 rounded-lg text-center">
              <div className="text-2xl font-bold">{result.totalConformes}</div>
              <div className="text-sm text-gray-600">Conformes</div>
            </div>
            <div className="p-4 bg-red-100 rounded-lg text-center">
              <div className="text-2xl font-bold">{result.totalFaltantes}</div>
              <div className="text-sm text-gray-600">Faltantes</div>
            </div>
            <div className="p-4 bg-yellow-100 rounded-lg text-center">
              <div className="text-2xl font-bold">{result.totalExcedentes}</div>
              <div className="text-sm text-gray-600">Excedentes</div>
            </div>
            <div className="p-4 bg-purple-100 rounded-lg text-center">
              <div className="text-2xl font-bold">{result.totalDivergencias}</div>
              <div className="text-sm text-gray-600">Divergências</div>
            </div>
          </div>
          <button
            onClick={downloadReport}
            className="mt-4 px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
          >
            📥 Baixar Relatório Excel
          </button>
        </div>
      )}
    </div>
  );
};