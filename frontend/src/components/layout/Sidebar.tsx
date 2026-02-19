import React from 'react';
import { NavLink } from 'react-router-dom';
import { useApp } from '../../context/AppContext';

const Sidebar: React.FC = () => {
  const { theme, repositories, selectedRepo, setSelectedRepo } = useApp();

  const bg = theme === 'dark' ? '#161b22' : '#ffffff';
  const border = theme === 'dark' ? '#30363d' : '#d0d7de';
  const hoverBg = theme === 'dark' ? '#21262d' : '#f6f8fa';
  const activeBg = theme === 'dark' ? '#1f3a5c' : '#ddf4ff';

  const navStyle = (active: boolean) => ({
    display: 'flex', alignItems: 'center', gap: '8px',
    padding: '8px 12px', borderRadius: '6px',
    textDecoration: 'none',
    color: theme === 'dark' ? '#e6edf3' : '#24292f',
    backgroundColor: active ? activeBg : 'transparent',
    fontSize: '13px', fontWeight: active ? 600 : 400,
    transition: 'background 0.1s'
  });

  return (
    <aside style={{
      width: '220px', minHeight: 'calc(100vh - 57px)',
      backgroundColor: bg, borderRight: `1px solid ${border}`,
      padding: '16px', display: 'flex', flexDirection: 'column', gap: '8px'
    }}>
      <p style={{ fontSize: '11px', fontWeight: 600, textTransform: 'uppercase', color: '#8b949e', margin: '0 0 4px' }}>Navigation</p>

      <NavLink to="/" style={({ isActive }) => navStyle(isActive)}>
        📊 Dashboard
      </NavLink>
      <NavLink to="/commits" style={({ isActive }) => navStyle(isActive)}>
        📝 Commits
      </NavLink>

      <hr style={{ border: 'none', borderTop: `1px solid ${border}`, margin: '8px 0' }} />

      <p style={{ fontSize: '11px', fontWeight: 600, textTransform: 'uppercase', color: '#8b949e', margin: '0 0 4px' }}>Repositories</p>

      {repositories.map(repo => (
        <button key={repo.id} onClick={() => setSelectedRepo(repo)} style={{
          display: 'flex', flexDirection: 'column', alignItems: 'flex-start',
          padding: '8px 12px', borderRadius: '6px',
          border: 'none', cursor: 'pointer',
          backgroundColor: selectedRepo?.id === repo.id ? activeBg : 'transparent',
          color: theme === 'dark' ? '#e6edf3' : '#24292f',
          width: '100%', textAlign: 'left', fontSize: '12px'
        }}>
          <span style={{ fontWeight: 600 }}>{repo.name}</span>
          <span style={{ color: '#8b949e', fontSize: '11px' }}>{repo.owner}</span>
          {repo.language && (
            <span style={{
              marginTop: '4px', padding: '1px 6px', borderRadius: '12px',
              backgroundColor: theme === 'dark' ? '#30363d' : '#e1e4e8',
              fontSize: '10px'
            }}>{repo.language}</span>
          )}
        </button>
      ))}

      {repositories.length === 0 && (
        <p style={{ fontSize: '12px', color: '#8b949e' }}>No repositories yet. Add one using the button above.</p>
      )}
    </aside>
  );
};

export default Sidebar;
