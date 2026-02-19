import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../../context/AppContext';
import RiskBadge from '../commits/RiskBadge';
import type { Commit } from '../../types';

interface RecentCommitsProps {
  commits: Commit[];
}

const RecentCommits: React.FC<RecentCommitsProps> = ({ commits }) => {
  const { theme } = useApp();
  const navigate = useNavigate();
  const cardBg = theme === 'dark' ? '#161b22' : '#ffffff';
  const border = theme === 'dark' ? '#30363d' : '#d0d7de';
  const hoverBg = theme === 'dark' ? '#21262d' : '#f6f8fa';

  const formatDate = (dateStr: string) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  };

  return (
    <div style={{ backgroundColor: cardBg, border: `1px solid ${border}`, borderRadius: '10px', overflow: 'hidden' }}>
      <div style={{ padding: '16px 20px', borderBottom: `1px solid ${border}` }}>
        <h3 style={{ margin: 0, fontSize: '14px', fontWeight: 600 }}>🕐 Recent Commits</h3>
      </div>

      {commits.length === 0 ? (
        <div style={{ padding: '32px', textAlign: 'center', color: '#8b949e', fontSize: '14px' }}>
          No commits analyzed yet. Click "Run Analysis" to fetch commits.
        </div>
      ) : (
        <div>
          {commits.map((commit, i) => (
            <div key={commit.sha}
              onClick={() => navigate(`/commits/${commit.sha}`)}
              style={{
                padding: '14px 20px',
                borderBottom: i < commits.length - 1 ? `1px solid ${border}` : 'none',
                cursor: 'pointer',
                transition: 'background 0.1s',
              }}
              onMouseEnter={e => (e.currentTarget.style.backgroundColor = hoverBg)}
              onMouseLeave={e => (e.currentTarget.style.backgroundColor = 'transparent')}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '12px' }}>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <p style={{ margin: '0 0 4px', fontWeight: 600, fontSize: '13px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {commit.message?.split('\n')[0] || 'No message'}
                  </p>
                  {commit.aiSummaryPreview && (
                    <p style={{ margin: '0 0 6px', fontSize: '12px', color: '#8b949e', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {commit.aiSummaryPreview}
                    </p>
                  )}
                  <div style={{ display: 'flex', gap: '12px', fontSize: '11px', color: '#8b949e' }}>
                    <span>👤 {commit.author}</span>
                    <span>🕐 {formatDate(commit.commitDate)}</span>
                    <span style={{ fontFamily: 'monospace' }}>#{commit.sha.substring(0, 7)}</span>
                    {commit.filesChanged != null && <span>📄 {commit.filesChanged} files</span>}
                  </div>
                </div>
                <RiskBadge score={commit.riskScore} level={commit.riskLevel} size="sm" />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default RecentCommits;
