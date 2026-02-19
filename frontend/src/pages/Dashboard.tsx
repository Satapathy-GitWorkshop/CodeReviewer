import React from 'react';
import { useApp } from '../context/AppContext';
import KpiCards from '../components/dashboard/KpiCards';
import RiskTrendChart from '../components/dashboard/RiskTrendChart';
import RecentCommits from '../components/dashboard/RecentCommits';

const Dashboard: React.FC = () => {
  const { metrics, loading, error, selectedRepo, theme } = useApp();

  if (!selectedRepo) {
    return (
      <div style={{ textAlign: 'center', padding: '60px 20px', color: '#8b949e' }}>
        <p style={{ fontSize: '48px', margin: '0 0 16px' }}>📂</p>
        <h2 style={{ margin: '0 0 8px', color: theme === 'dark' ? '#e6edf3' : '#24292f' }}>No Repository Selected</h2>
        <p>Select a repository from the sidebar or add a new one.</p>
      </div>
    );
  }

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '60px', color: '#8b949e' }}>
        <div style={{ fontSize: '32px', marginBottom: '16px' }}>⏳</div>
        <p>Loading dashboard metrics...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ textAlign: 'center', padding: '60px', color: '#f85149' }}>
        <div style={{ fontSize: '32px', marginBottom: '16px' }}>❌</div>
        <p>{error}</p>
      </div>
    );
  }

  if (!metrics) {
    return (
      <div style={{ textAlign: 'center', padding: '60px', color: '#8b949e' }}>
        <p style={{ fontSize: '48px', margin: '0 0 16px' }}>🔍</p>
        <h2 style={{ margin: '0 0 8px', color: theme === 'dark' ? '#e6edf3' : '#24292f' }}>No Analysis Data</h2>
        <p>Click "Run Analysis" to analyze <strong>{selectedRepo.fullName}</strong></p>
      </div>
    );
  }

  return (
    <div>
      <div style={{ marginBottom: '20px' }}>
        <h2 style={{ margin: '0 0 4px', fontSize: '20px', fontWeight: 700 }}>
          {metrics.repositoryName}
        </h2>
        <p style={{ margin: 0, color: '#8b949e', fontSize: '13px' }}>
          Code review analytics dashboard
        </p>
      </div>

      <KpiCards metrics={metrics} />
      <RiskTrendChart data={metrics.riskTrend} />
      <RecentCommits commits={metrics.recentCommits} />
    </div>
  );
};

export default Dashboard;
