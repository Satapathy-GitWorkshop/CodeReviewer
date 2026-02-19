import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';
import type { Repository, DashboardMetrics } from '../types';
import { repositoryApi, dashboardApi } from '../api';

interface AppContextType {
  repositories: Repository[];
  selectedRepo: Repository | null;
  metrics: DashboardMetrics | null;
  loading: boolean;
  error: string | null;
  theme: 'dark' | 'light';
  setSelectedRepo: (repo: Repository) => void;
  toggleTheme: () => void;
  refreshMetrics: () => void;
  loadRepositories: () => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [repositories, setRepositories] = useState<Repository[]>([]);
  const [selectedRepo, setSelectedRepoState] = useState<Repository | null>(null);
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [theme, setTheme] = useState<'dark' | 'light'>('dark');

  const loadRepositories = useCallback(async () => {
    try {
      const repos = await repositoryApi.getAll();
      setRepositories(repos);
      if (repos.length > 0 && !selectedRepo) {
        setSelectedRepoState(repos[0]);
      }
    } catch (e) {
      setError('Failed to load repositories');
    }
  }, [selectedRepo]);

  const refreshMetrics = useCallback(async () => {
    if (!selectedRepo) return;
    setLoading(true);
    setError(null);
    try {
      const m = await dashboardApi.getMetrics(selectedRepo.id);
      setMetrics(m);
    } catch (e) {
      setError('Failed to load metrics');
    } finally {
      setLoading(false);
    }
  }, [selectedRepo]);

  const setSelectedRepo = useCallback((repo: Repository) => {
    setSelectedRepoState(repo);
    setMetrics(null);
  }, []);

  const toggleTheme = () => setTheme(t => t === 'dark' ? 'light' : 'dark');

  useEffect(() => { loadRepositories(); }, []);
  useEffect(() => { refreshMetrics(); }, [selectedRepo]);

  // Auto-refresh every 5 minutes
  useEffect(() => {
    const interval = setInterval(refreshMetrics, 5 * 60 * 1000);
    return () => clearInterval(interval);
  }, [refreshMetrics]);

  return (
    <AppContext.Provider value={{
      repositories, selectedRepo, metrics, loading, error, theme,
      setSelectedRepo, toggleTheme, refreshMetrics, loadRepositories
    }}>
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const ctx = useContext(AppContext);
  if (!ctx) throw new Error('useApp must be used within AppProvider');
  return ctx;
};
