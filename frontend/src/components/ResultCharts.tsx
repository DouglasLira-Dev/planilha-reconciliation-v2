import React from 'react';
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

interface ResultChartsProps {
  data: {
    totalConformes: number;
    totalFaltantes: number;
    totalExcedentes: number;
    totalDivergencias: number;
    totalCancelados: number;
    totalErros: number;
    totalAvisos: number;
  };
}

const COLORS = ['#10B981', '#EF4444', '#F59E0B', '#8B5CF6', '#6B7280'];
const PIE_COLORS = {
  conformes: '#10B981',
  faltantes: '#EF4444',
  excedentes: '#F59E0B',
  divergencias: '#8B5CF6',
  cancelados: '#6B7280'
};

export const ResultCharts: React.FC<ResultChartsProps> = ({ data }) => {
  // Dados para o gráfico de pizza
  const pieData = [
    { name: 'Conformes', value: data.totalConformes || 0 },
    { name: 'Faltantes', value: data.totalFaltantes || 0 },
    { name: 'Excedentes', value: data.totalExcedentes || 0 },
    { name: 'Divergências', value: data.totalDivergencias || 0 },
    { name: 'Cancelados', value: data.totalCancelados || 0 },
  ].filter(item => item.value > 0);

  // Dados para o gráfico de barras (apenas erros vs avisos)
  const barData = [
    { name: 'Erros', valor: data.totalErros || 0 },
    { name: 'Avisos', valor: data.totalAvisos || 0 },
  ];

  // Dados para o gráfico de barras comparativo (todas as categorias)
  const allCategoriesData = [
    { name: 'Conformes', valor: data.totalConformes || 0 },
    { name: 'Faltantes', valor: data.totalFaltantes || 0 },
    { name: 'Excedentes', valor: data.totalExcedentes || 0 },
    { name: 'Divergências', valor: data.totalDivergencias || 0 },
    { name: 'Cancelados', valor: data.totalCancelados || 0 },
  ];

  const renderCustomizedLabel = ({ cx, cy, midAngle, innerRadius, outerRadius, percent, index, name }: any) => {
    const radius = innerRadius + (outerRadius - innerRadius) * 0.5;
    const x = cx + radius * Math.cos(-midAngle * Math.PI / 180);
    const y = cy + radius * Math.sin(-midAngle * Math.PI / 180);

    return (
      <text x={x} y={y} fill="white" textAnchor="middle" dominantBaseline="central" fontSize={12} fontWeight="bold">
        {`${(percent * 100).toFixed(1)}%`}
      </text>
    );
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
      {/* Gráfico de Pizza */}
      <div className="bg-white p-4 rounded-lg shadow">
        <h3 className="text-lg font-semibold mb-4 text-center">Distribuição por Categoria</h3>
        {pieData.length === 0 ? (
          <div className="text-center text-gray-500 py-8">Nenhum dado para exibir</div>
        ) : (
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={pieData}
                cx="50%"
                cy="50%"
                labelLine={true}
                label={renderCustomizedLabel}
                outerRadius={100}
                fill="#8884d8"
                dataKey="value"
              >
                {pieData.map((entry, index) => (
                  <Cell 
                    key={`cell-${index}`} 
                    fill={Object.values(PIE_COLORS)[index % Object.values(PIE_COLORS).length]} 
                  />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        )}
      </div>

      {/* Gráfico de Barras (Erros vs Avisos) */}
      <div className="bg-white p-4 rounded-lg shadow">
        <h3 className="text-lg font-semibold mb-4 text-center">Erros vs Avisos</h3>
        {barData.every(item => item.valor === 0) ? (
          <div className="text-center text-gray-500 py-8">Nenhum erro ou aviso encontrado</div>
        ) : (
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={barData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Bar dataKey="valor" fill="#8B5CF6" radius={[4, 4, 0, 0]}>
                {barData.map((entry, index) => (
                  <Cell 
                    key={`cell-${index}`} 
                    fill={entry.name === 'Erros' ? '#EF4444' : '#F59E0B'} 
                  />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>

      {/* Gráfico de Barras Comparativo (Todas as Categorias) */}
      <div className="bg-white p-4 rounded-lg shadow md:col-span-2">
        <h3 className="text-lg font-semibold mb-4 text-center">Comparação por Categoria</h3>
        {allCategoriesData.every(item => item.valor === 0) ? (
          <div className="text-center text-gray-500 py-8">Nenhum dado para exibir</div>
        ) : (
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={allCategoriesData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Bar dataKey="valor" fill="#3B82F6" radius={[4, 4, 0, 0]}>
                {allCategoriesData.map((entry, index) => (
                  <Cell 
                    key={`cell-${index}`} 
                    fill={Object.values(PIE_COLORS)[index % Object.values(PIE_COLORS).length]} 
                  />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  );
};