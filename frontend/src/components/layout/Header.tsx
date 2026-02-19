import React, { useState } from 'react';
import { useApp } from '../../context/AppContext';
import { repositoryApi, analyzeApi } from '../../api';

const Header: React.FC = () => {
  const { theme, toggleTheme, selectedRepo, refreshMetrics, loadRepositories, repositories } = useApp();
  const [showAddRepo, setShowAddRepo] = useState(false);
  const [newOwner, setNewOwner] = useState('');
  const [newName, setNewName] = useState('');
  const [analyzing, setAnalyzing] = useState(false);

  const bg = theme === 'dark' ? '#161b22' : '#ffffff';
  const border = theme === 'dark' ? '#30363d' : '#d0d7de';
  const inputBg = theme === 'dark' ? '#0d1117' : '#f6f8fa';

  const handleAddRepo = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newOwner || !newName) return;
    try {
      await repositoryApi.add(newOwner, newName);
      setShowAddRepo(false);
      setNewOwner(''); setNewName('');
      loadRepositories();
    } catch (err) {
      alert('Failed to add repository. Check owner/name and GitHub token.');
    }
  };

  const handleAnalyze = async () => {
    if (!selectedRepo) return;
    setAnalyzing(true);
    try {
      await analyzeApi.triggerManual(selectedRepo.id);
      setTimeout(() => { refreshMetrics(); setAnalyzing(false); }, 3000);
    } catch {
      setAnalyzing(false);
    }
  };

  return (
    <header style={{
      backgroundColor: bg,
      borderBottom: `1px solid ${border}`,
      padding: '12px 24px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      position: 'sticky',
      top: 0,
      zIndex: 100
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        <span style={{ fontSize: '22px' }}>🔍</span>
        <span style={{ fontWeight: 700, fontSize: '18px', background: 'linear-gradient(135deg, #58a6ff, #bc8cff)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
          CodeReview AI
        </span>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        {selectedRepo && (
          <button onClick={handleAnalyze} disabled={analyzing} style={{
            padding: '6px 16px', borderRadius: '6px', border: 'none',
            backgroundColor: analyzing ? '#388bfd44' : '#238636',
            color: '#fff', cursor: analyzing ? 'not-allowed' : 'pointer',
            fontSize: '13px', fontWeight: 600
          }}>
            {analyzing ? '⏳ Analyzing...' : '▶ Run Analysis'}
          </button>
        )}

        <button onClick={() => setShowAddRepo(!showAddRepo)} style={{
          padding: '6px 16px', borderRadius: '6px',
          border: `1px solid ${border}`,
          backgroundColor: 'transparent', color: theme === 'dark' ? '#e6edf3' : '#24292f',
          cursor: 'pointer', fontSize: '13px'
        }}>
          + Add Repo
        </button>

        <button onClick={toggleTheme} style={{
          padding: '6px 12px', borderRadius: '6px',
          border: `1px solid ${border}`,
          backgroundColor: 'transparent', color: theme === 'dark' ? '#e6edf3' : '#24292f',
          cursor: 'pointer', fontSize: '16px'
        }}>
          {theme === 'dark' ? '☀️' : '🌙'}
        </button>
      </div>

      {showAddRepo && (
        <div style={{
          position: 'absolute', top: '60px', right: '24px',
          backgroundColor: bg, border: `1px solid ${border}`,
          borderRadius: '8px', padding: '16px', zIndex: 200,
          boxShadow: '0 8px 24px rgba(0,0,0,0.3)', minWidth: '300px'
        }}>
          <h4 style={{ margin: '0 0 12px', fontSize: '14px' }}>Add GitHub Repository</h4>
          <form onSubmit={handleAddRepo}>
            <input placeholder="Owner (e.g. facebook)" value={newOwner}
              onChange={e => setNewOwner(e.target.value)}
              style={{ width: '100%', marginBottom: '8px', padding: '8px', borderRadius: '6px', border: `1px solid ${border}`, backgroundColor: inputBg, color: 'inherit', boxSizing: 'border-box' }} />
            <input placeholder="Repository name (e.g. react)" value={newName}
              onChange={e => setNewName(e.target.value)}
              style={{ width: '100%', marginBottom: '12px', padding: '8px', borderRadius: '6px', border: `1px solid ${border}`, backgroundColor: inputBg, color: 'inherit', boxSizing: 'border-box' }} />
            <div style={{ display: 'flex', gap: '8px' }}>
              <button type="submit" style={{ flex: 1, padding: '8px', borderRadius: '6px', border: 'none', backgroundColor: '#238636', color: '#fff', cursor: 'pointer' }}>Add</button>
              <button type="button" onClick={() => setShowAddRepo(false)} style={{ flex: 1, padding: '8px', borderRadius: '6px', border: `1px solid ${border}`, backgroundColor: 'transparent', color: 'inherit', cursor: 'pointer' }}>Cancel</button>
            </div>
          </form>
        </div>
      )}
    </header>
  );
};

export default Header;
