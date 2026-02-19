import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import { commitApi } from '../api';
import RiskBadge from '../components/commits/RiskBadge';
import type { Commit } from '../types';

const CommitList: React.FC = () => {
  const { selectedRepo, theme } = useApp();
  const navigate = useNavigate();
  const [commits, setCommits] = useState<Commit[]>([]);
  const [loading, setLoading] = useState(false);
  const [filter, setFilter] = useState<'ALL' | 'HIGH' | 'MEDIUM' | 'LOW'>('ALL');

  const cardBg = theme === 'dark' ? '#161b22' : '#ffffff';
  const border = theme === 'dark' ? '#30363d' : '#d0d7de';
  const hoverBg = theme === 'dark' ? '#21262d' : '#f6f8fa';
  const inputBg = theme === 'dark' ? '#0d1117' : '#f6f8fa';

  useEffect(() => {
    if (!selectedRepo) return;
    setLoading(true);
    commitApi.getByRepo(selectedRepo.id)
      .then(setCommits)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [selectedRepo]);

  const filteredCommits = filter === 'ALL' ? commits
    : commits.filter(c => c.riskLevel === filter);

  if (!selectedRepo) {
    return <div style={{ color: '#8b949e', padding: '40px', textAlign: 'center' }}>Select a repository first.</div>;
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h2 style={{ margin: 0, fontSize: '20px' }}>Commits — {selectedRepo.fullName}</h2>
        <div style={{ display: 'flex', gap: '8px' }}>
          {(['ALL', 'HIGH', 'MEDIUM', 'LOW'] as const).map(f => (
            <button key={f} onClick={() => setFilter(f)} style={{
              padding: '6px 14px', borderRadius: '20px', fontSize: '12px', fontWeight: 600,
              border: `1px solid ${border}`, cursor: 'pointer',
              backgroundColor: filter === f ? (f === 'ALL' ? '#388bfd' : f === 'HIGH' ? '#f85149' : f === 'MEDIUM' ? '#d29922' : '#3fb950') : 'transparent',
              color: filter === f ? '#fff' : theme === 'dark' ? '#e6edf3' : '#24292f'
            }}>{f}</button>
          ))}
        </div>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '40px', color: '#8b949e' }}>Loading commits...</div>
      ) : (
        <div style={{ backgroundColor: cardBg, border: `1px solid ${border}`, borderRadius: '10px', overflow: 'hidden' }}>
          {filteredCommits.length === 0 ? (
            <div style={{ padding: '40px', textAlign: 'center', color: '#8b949e' }}>
              No commits found. Run analysis to fetch commits from GitHub.
            </div>
          ) : (
            filteredCommits.map((commit, i) => (
              <div key={commit.sha}
                onClick={() => navigate(`/commits/${commit.sha}`)}
                style={{
                  padding: '14px 20px',
                  borderBottom: i < filteredCommits.length - 1 ? `1px solid ${border}` : 'none',
                  cursor: 'pointer'
                }}
                onMouseEnter={e => (e.currentTarget.style.backgroundColor = hoverBg)}
                onMouseLeave={e => (e.currentTarget.style.backgroundColor = 'transparent')}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '12px' }}>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                      <code style={{ fontSize: '11px', color: '#58a6ff', backgroundColor: theme === 'dark' ? '#1f3a5c' : '#ddf4ff', padding: '1px 5px', borderRadius: '4px' }}>
                        {commit.sha.substring(0, 7)}
                      </code>
                      <span style={{ fontWeight: 600, fontSize: '13px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {commit.message?.split('\n')[0]}
                      </span>
                    </div>
                    <div style={{ display: 'flex', gap: '12px', fontSize: '11px', color: '#8b949e' }}>
                      <span>👤 {commit.author}</span>
                      <span>🕐 {new Date(commit.commitDate).toLocaleDateString()}</span>
                      {commit.filesChanged != null && <span>📄 {commit.filesChanged} files (+{commit.additions}/-{commit.deletions})</span>}
                    </div>
                  </div>
                  <RiskBadge score={commit.riskScore} level={commit.riskLevel} />
                </div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
};

export default CommitList;
