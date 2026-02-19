import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, ReferenceLine } from 'recharts';
import { useApp } from '../../context/AppContext';
import type { RiskTrendPoint } from '../../types';

interface RiskTrendChartProps {
  data: RiskTrendPoint[];
}

const RiskTrendChart: React.FC<RiskTrendChartProps> = ({ data }) => {
  const { theme } = useApp();
  const cardBg = theme === 'dark' ? '#161b22' : '#ffffff';
  const border = theme === 'dark' ? '#30363d' : '#d0d7de';
  const gridColor = theme === 'dark' ? '#21262d' : '#eaecef';
  const textColor = theme === 'dark' ? '#8b949e' : '#57606a';

  const CustomTooltip = ({ active, payload, label }: any) => {
    if (!active || !payload || !payload.length) return null;
    const point = payload[0].payload as RiskTrendPoint;
    return (
      <div style={{
        backgroundColor: cardBg, border: `1px solid ${border}`,
        borderRadius: '8px', padding: '12px', fontSize: '12px', maxWidth: '200px'
      }}>
        <p style={{ margin: '0 0 4px', fontWeight: 700 }}>Risk Score: {point.riskScore}</p>
        <p style={{ margin: '0 0 4px', color: '#8b949e' }}>{point.date}</p>
        <p style={{ margin: 0, color: '#58a6ff', wordBreak: 'break-all' }}>
          {point.commitMessage?.substring(0, 80)}
        </p>
      </div>
    );
  };

  if (!data || data.length === 0) {
    return (
      <div style={{ backgroundColor: cardBg, border: `1px solid ${border}`, borderRadius: '10px', padding: '24px', textAlign: 'center', color: '#8b949e' }}>
        No risk trend data available yet. Run an analysis to see results.
      </div>
    );
  }

  return (
    <div style={{ backgroundColor: cardBg, border: `1px solid ${border}`, borderRadius: '10px', padding: '20px', marginBottom: '24px' }}>
      <h3 style={{ margin: '0 0 16px', fontSize: '14px', fontWeight: 600, color: theme === 'dark' ? '#e6edf3' : '#24292f' }}>
        📈 Risk Score Trend
      </h3>
      <ResponsiveContainer width="100%" height={250}>
        <LineChart data={data} margin={{ top: 5, right: 20, left: -10, bottom: 5 }}>
          <CartesianGrid strokeDasharray="3 3" stroke={gridColor} />
          <XAxis dataKey="date" tick={{ fill: textColor, fontSize: 11 }} tickLine={false} />
          <YAxis domain={[0, 10]} tick={{ fill: textColor, fontSize: 11 }} tickLine={false} />
          <Tooltip content={<CustomTooltip />} />
          <ReferenceLine y={7} stroke="#f85149" strokeDasharray="5 5" label={{ value: 'High Risk', fill: '#f85149', fontSize: 10 }} />
          <ReferenceLine y={4} stroke="#d29922" strokeDasharray="5 5" label={{ value: 'Med Risk', fill: '#d29922', fontSize: 10 }} />
          <Line
            type="monotone" dataKey="riskScore"
            stroke="#58a6ff" strokeWidth={2} dot={{ fill: '#58a6ff', r: 4 }}
            activeDot={{ r: 6, fill: '#bc8cff' }}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};

export default RiskTrendChart;
