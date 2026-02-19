import React from 'react';
import { useApp } from '../../context/AppContext';
import type { DashboardMetrics } from '../../types';

interface KpiCardsProps {
  metrics: DashboardMetrics;
}

const KpiCards: React.FC<KpiCardsProps> = ({ metrics }) => {
  const { theme } = useApp();
  const cardBg = theme === 'dark' ? '#161b22' : '#ffffff';
  const border = theme === 'dark' ? '#30363d' : '#d0d7de';

  const cards = [
    {
      label: 'Total Commits',
      value: metrics.totalCommits.toString(),
      icon: '📝',
      color: '#58a6ff',
      subtitle: metrics.repositoryName
    },
    {
      label: 'Avg Risk Score',
      value: metrics.avgRiskScore ? metrics.avgRiskScore.toFixed(1) : 'N/A',
      icon: '⚠️',
      color: getRiskColor(metrics.avgRiskScore),
      subtitle: 'out of 10'
    },
    {
      label: 'High Risk',
      value: metrics.highRiskCount.toString(),
      icon: '🔴',
      color: '#f85149',
      subtitle: 'commits (score ≥ 7)'
    },
    {
      label: 'Language',
      value: metrics.primaryLanguage || 'Mixed',
      icon: '💻',
      color: '#3fb950',
      subtitle: `${metrics.lowRiskCount} low risk`
    }
  ];

  return (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginBottom: '24px' }}>
      {cards.map((card, i) => (
        <div key={i} style={{
          backgroundColor: cardBg,
          border: `1px solid ${border}`,
          borderRadius: '10px',
          padding: '20px',
          borderLeft: `3px solid ${card.color}`
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <div>
              <p style={{ margin: '0 0 4px', fontSize: '12px', color: '#8b949e', textTransform: 'uppercase', fontWeight: 600 }}>
                {card.label}
              </p>
              <p style={{ margin: '0 0 4px', fontSize: '28px', fontWeight: 700, color: card.color }}>
                {card.value}
              </p>
              <p style={{ margin: 0, fontSize: '11px', color: '#8b949e' }}>{card.subtitle}</p>
            </div>
            <span style={{ fontSize: '28px' }}>{card.icon}</span>
          </div>
        </div>
      ))}
    </div>
  );
};

function getRiskColor(score: number): string {
  if (!score) return '#8b949e';
  if (score >= 7) return '#f85149';
  if (score >= 4) return '#d29922';
  return '#3fb950';
}

export default KpiCards;
