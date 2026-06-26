import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { historyService, HistoryListItem, HistoryFilters } from '../services/historyService';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';

export const History: React.FC = () => {
  const { user } = useAuth();
  const [history, setHistory] = useState<HistoryListItem[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filters, setFilters] = useState<HistoryFilters>({
    page: 0,
    size: 20,
    sortBy: 'executedAt',
    sortDirection: 'desc',
  });
  const [showDeleteModal, setShowDeleteModal] = useState<number | null>(null);

  const isAdmin = user?.role === 'ADMIN';

  useEffect(() => {
    loadHistory();
  }, [filters]);

  const loadHistory = async () => {
    setLoading(true);
    try {
      const data = await historyService.list(filters);
      setHistory(data.content);
      setTotalElements(data.totalElements);
    } catch (err: any) {
      setError(err.message || 'Erro ao carregar histórico');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await historyService.delete(id);
      setShowDeleteModal(null);
      loadHistory();
    } catch (err: any) {
      setError(err.message || 'Erro ao deletar registro');
    }
  };

  const handleDownload = (id: number) => {
    const url = historyService.downloadReport(id);
    window.open(url, '_blank');
  };

  const totalPages = Math.ceil(totalElements / (filters.size || 20));

  return (
    <div className="p-8 max-w-6xl mx-auto">
      <h1 className="text-2xl font-bold mb-6">Histórico de Reconciliações</h1>

      {/* Filtros */}
      <div className="bg-white p-4 rounded-lg shadow mb-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">Data Início</label>
            <input
              type="date"
              value={filters.startDate || ''}
              onChange={(e) => setFilters({ ...filters, startDate: e.target.value || undefined })}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">Data Fim</label>
            <input
              type="date"
              value={filters.endDate || ''}
              onChange={(e) => setFilters({ ...filters, endDate: e.target.value || undefined })}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"
            />
          </div>
          {isAdmin && (
            <div>
              <label className="block text-sm font-medium text-gray-700">Usuário</label>
              <input
                type="text"
                value={filters.username || ''}
                onChange={(e) => setFilters({ ...filters, username: e.target.value || undefined })}
                placeholder="Filtrar por usuário"
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm"
              />
            </div>
          )}
          <div className="flex items-end">
            <button
              onClick={() => setFilters({ ...filters, page: 0 })}
              className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
            >
              Filtrar
            </button>
          </div>
        </div>
      </div>

      {/* Lista */}
      {loading ? (
        <div className="text-center py-8">Carregando...</div>
      ) : error ? (
        <div className="text-red-500 text-center py-8">{error}</div>
      ) : history.length === 0 ? (
        <div className="text-center py-8 text-gray-500">Nenhum registro encontrado.</div>
      ) : (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Usuário</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Data/Hora</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Arquivos</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Resumo</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Ações</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {history.map((item) => (
                <tr key={item.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{item.id}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{item.username}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {format(new Date(item.executedAt), "dd/MM/yyyy HH:mm", { locale: ptBR })}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-900">
                    <div className="truncate max-w-xs" title={item.filenameFinanceiro}>
                      {item.filenameFinanceiro}
                    </div>
                    <div className="truncate max-w-xs text-gray-500" title={item.filenameCadastro}>
                      {item.filenameCadastro}
                    </div>
                  </td>
                  <td className="px-6 py-4 text-sm">
                    <div className="flex space-x-2">
                      <span className="text-green-600">{item.totalConformes} ✓</span>
                      <span className="text-red-600">{item.totalFaltantes} ✗</span>
                      <span className="text-yellow-600">{item.totalExcedentes} ⚠</span>
                      <span className="text-purple-600">{item.totalDivergencias} 🔄</span>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm">
                    <button
                      onClick={() => handleDownload(item.id)}
                      className="text-blue-600 hover:text-blue-900 mr-3"
                    >
                      📥
                    </button>
                    {isAdmin && (
                      <button
                        onClick={() => setShowDeleteModal(item.id)}
                        className="text-red-600 hover:text-red-900"
                      >
                        🗑️
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Paginação */}
      {totalPages > 1 && (
        <div className="flex justify-between items-center mt-4">
          <div className="text-sm text-gray-500">
            {totalElements} registros encontrados
          </div>
          <div className="flex space-x-2">
            <button
              onClick={() => setFilters({ ...filters, page: Math.max(0, (filters.page || 0) - 1) })}
              disabled={(filters.page || 0) === 0}
              className="px-3 py-1 border rounded disabled:opacity-50"
            >
              Anterior
            </button>
            <span className="px-3 py-1">
              Página {(filters.page || 0) + 1} de {totalPages}
            </span>
            <button
              onClick={() => setFilters({ ...filters, page: Math.min(totalPages - 1, (filters.page || 0) + 1) })}
              disabled={(filters.page || 0) >= totalPages - 1}
              className="px-3 py-1 border rounded disabled:opacity-50"
            >
              Próxima
            </button>
          </div>
        </div>
      )}

      {/* Modal de confirmação de exclusão */}
      {showDeleteModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white p-6 rounded-lg max-w-md">
            <h3 className="text-lg font-bold mb-4">Confirmar exclusão</h3>
            <p className="mb-4">Tem certeza que deseja excluir este registro do histórico?</p>
            <div className="flex justify-end space-x-2">
              <button
                onClick={() => setShowDeleteModal(null)}
                className="px-4 py-2 border rounded hover:bg-gray-50"
              >
                Cancelar
              </button>
              <button
                onClick={() => handleDelete(showDeleteModal)}
                className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
              >
                Excluir
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};