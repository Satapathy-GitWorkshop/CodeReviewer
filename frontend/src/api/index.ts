import axios from 'axios';
import type { Repository, Commit, CommitDetail, DashboardMetrics } from '../types';

const API_BASE = process.env.REACT_APP_API_URL || '/api';

const api = axios.create({
  baseURL: API_BASE,
  timeout: 30000,
});

export const repositoryApi = {
  getAll: () => api.get<Repository[]>('/repositories').then(r => r.data),
  add: (owner: string, name: string) =>
    api.post<Repository>('/repositories', { owner, name }).then(r => r.data),
};

export const commitApi = {
  getByRepo: (repoId: number, since?: string) =>
    api.get<Commit[]>('/commits', { params: { repoId, since } }).then(r => r.data),
  getDetail: (sha: string) =>
    api.get<CommitDetail>(`/commits/${sha}/analysis`).then(r => r.data),
};

export const dashboardApi = {
  getMetrics: (repoId: number) =>
    api.get<DashboardMetrics>('/dashboard/metrics', { params: { repoId } }).then(r => r.data),
};

export const analyzeApi = {
  triggerManual: (repositoryId: number) =>
    api.post<{ status: string }>('/analyze/manual', { repositoryId }).then(r => r.data),
};

export default api;
