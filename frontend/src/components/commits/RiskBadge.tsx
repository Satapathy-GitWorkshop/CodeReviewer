import React from 'react';

interface RiskBadgeProps {
  score?: number;
  level?: string;
  size?: 'sm' | 'md';
}

const RiskBadge: React.FC<RiskBadgeProps> = ({ score, level, size = 'md' }) => {
  const riskLevel = level || (score ? (score >= 7 ? 'HIGH' : score >= 4 ? 'MEDIUM' : 'LOW') : null);

  if (!riskLevel && score === undefined) {
    return <span style={{ color: '#8b949e', fontSize: '12px' }}>Pending</span>;
  }

  const colors: Record<string, { bg: string; text: string }> = {
    HIGH: { bg: '#3d1414', text: '#f85149' },
    MEDIUM: { bg: '#2d2000', text: '#d29922' },
    LOW: { bg: '#0d2a0d', text: '#3fb950' }
  };

  const c = colors[riskLevel || 'LOW'] || colors.LOW;
  const padding = size === 'sm' ? '2px 6px' : '4px 10px';
  const fontSize = size === 'sm' ? '11px' : '12px';

  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: '4px',
      backgroundColor: c.bg, color: c.text,
      padding, borderRadius: '20px', fontSize, fontWeight: 700,
      border: `1px solid ${c.text}44`
    }}>
      {riskLevel === 'HIGH' ? '🔴' : riskLevel === 'MEDIUM' ? '🟡' : '🟢'}
      {score !== undefined ? `${score}/10` : riskLevel}
    </span>
  );
};

export default RiskBadge;
