import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AppProvider, useApp } from './context/AppContext';
import Header from './components/layout/Header';
import Sidebar from './components/layout/Sidebar';
import Dashboard from './pages/Dashboard';
import CommitList from './pages/CommitList';
import CommitDetailPage from './pages/CommitDetailPage';

const AppLayout: React.FC = () => {
  const { theme } = useApp();

  return (
    <div className={`app ${theme}`} style={{
      minHeight: '100vh',
      backgroundColor: theme === 'dark' ? '#0d1117' : '#f6f8fa',
      color: theme === 'dark' ? '#e6edf3' : '#24292f',
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
      display: 'flex',
      flexDirection: 'column'
    }}>
      <Header />
      <div style={{ display: 'flex', flex: 1 }}>
        <Sidebar />
        <main style={{ flex: 1, padding: '24px', maxWidth: '1200px', margin: '0 auto', width: '100%' }}>
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/commits" element={<CommitList />} />
            <Route path="/commits/:sha" element={<CommitDetailPage />} />
          </Routes>
        </main>
      </div>
    </div>
  );
};

function App() {
  return (
    <AppProvider>
      <BrowserRouter>
        <AppLayout />
      </BrowserRouter>
    </AppProvider>
  );
}

export default App;
