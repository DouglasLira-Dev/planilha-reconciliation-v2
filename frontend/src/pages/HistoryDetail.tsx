import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { historyService, HistoryDetail } from '../services/historyService';
import { ResultCharts } from '../components/ResultCharts';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';

export const HistoryDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [detail, setDetail] = useState<HistoryDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [downloading, setDownloading] = useState(false);

  const isAdmin = user?.role === 'ADMIN';

  useEffect(() => {
    loadDetail();
  }, [id]);

  const loadDetail = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const data = await historyService.getDetail(parseInt(id));
      setDetail(data);
    } catch (err: any) {
      setError(err.message || 'Erro ao carregar detalhes');
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async () => {
    if (!detail) return;
    setDownloading(true);
    try {
      const blob = await historyService.downloadReportBlob(detail.id);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `relatorio_${detail.id}.xlsx`;
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (err: any) {
      setError('Erro ao baixar relatório');
    } finally {
      setDownloading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-gray-500">Carregando detalhes...</div>
      </div>
    );
  }

  if (error || !detail) {
    return (
      <div className="p-8 max-w-4xl mx-auto">
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700">
          {error || 'Detalhes não encontrados'}
        </div>
        <button
          onClick={() => navigate('/history')}
          className="mt-4 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
        >
          Voltar ao Histórico
        </button>
      </div>
    );
  }

  return (
    <div className="p-8 max-w-6xl mx-auto">
      {/* Header */}
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold">Detalhes da Reconciliação</h1>
          <p className="text-gray-500 text-sm">ID: #{detail.id}</p>
        </div>
        <div className="flex space-x-2">
          <button
            onClick={() => navigate('/history')}
            className="px-4 py-2 border rounded hover:bg-gray-50"
          >
            ← Voltar
          </button>
          {isAdmin && (
            <button
              onClick={() => {
                if (confirm('Tem certeza que deseja excluir este registro?')) {
                  historyService.delete(detail.id).then(() => navigate('/history'));
                }
              }}
              className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
            >
              🗑️ Excluir
            </button>
          )}
        </div>
      </div>

      {/* Informações básicas */}
      <div className="bg-white p-6 rounded-lg shadow mb-6">
        <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
          <div>
            <div className="text-sm text-gray-500">Usuário</div>
            <div className="font-medium">{detail.username}</div>
          </div>
          <div>
            <div className="text-sm text-gray-500">Data/Hora</div>
            <div className="font-medium">
              {format(new Date(detail.executedAt), "dd/MM/yyyy HH:mm", { locale: ptBR })}
            </div>
          </div>
          <div>
            <div className="text-sm text-gray-500">Arquivos</div>
            <div className="font-medium text-sm">
              <div>Financeiro: {detail.filenameFinanceiro}</div>
              <div>Cadastro: {detail.filenameCadastro}</div>
            </div>
          </div>
        </div>
      </div>

      {/* Estatísticas */}
      <div className="bg-white p-6 rounded-lg shadow mb-6">
        <h2 className="text-lg font-semibold mb-4">Resumo da Comparação</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div className="p-4 bg-green-100 rounded-lg text-center">
            <div className="text-2xl font-bold">{detail.totalConformes}</div>
            <div className="text-sm text-gray-600">Conformes</div>
          </div>
          <div className="p-4 bg-red-100 rounded-lg text-center">
            <div className="text-2xl font-bold">{detail.totalFaltantes}</div>
            <div className="text-sm text-gray-600">Faltantes</div>
          </div>
          <div className="p-4 bg-yellow-100 rounded-lg text-center">
            <div className="text-2xl font-bold">{detail.totalExcedentes}</div>
            <div className="text-sm text-gray-600">Excedentes</div>
          </div>
          <div className="p-4 bg-purple-100 rounded-lg text-center">
            <div className="text-2xl font-bold">{detail.totalDivergencias}</div>
            <div className="text-sm text-gray-600">Divergências</div>
          </div>
          <div className="p-4 bg-gray-100 rounded-lg text-center">
            <div className="text-2xl font-bold">{detail.totalCancelados}</div>
            <div className="text-sm text-gray-600">Cancelados</div>
          </div>
          <div className="p-4 bg-red-50 rounded-lg text-center">
            <div className="text-2xl font-bold text-red-600">{detail.totalErros}</div>
            <div className="text-sm text-gray-600">Erros</div>
          </div>
          <div className="p-4 bg-yellow-50 rounded-lg text-center">
            <div className="text-2xl font-bold text-yellow-600">{detail.totalAvisos}</div>
            <div className="text-sm text-gray-600">Avisos</div>
          </div>
          <div className="p-4 bg-blue-50 rounded-lg text-center">
            <div className="text-2xl font-bold text-blue-600">{detail.totalPossiveisAbreviacoes}</div>
            <div className="text-sm text-gray-600">Abreviações</div>
          </div>
        </div>
      </div>

      {/* Gráficos */}
      <div className="bg-white p-6 rounded-lg shadow mb-6">
        <h2 className="text-lg font-semibold mb-4">Visualização Gráfica</h2>
        <ResultCharts data={detail} />
      </div>

      {/* Botão de download */}
      <div className="flex justify-center">
        <button
          onClick={handleDownload}
          disabled={downloading}
          className="px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed text-lg"
        >
          {downloading ? 'Baixando...' : '📥 Baixar Relatório Excel'}
        </button>
      </div>
    </div>
  );
};