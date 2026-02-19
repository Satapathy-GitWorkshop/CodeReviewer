export interface Repository {
  id: number;
  owner: string;
  name: string;
  fullName: string;
  active: boolean;
  lastAnalyzedAt?: string;
  language?: string;
  defaultBranch: string;
}

export interface Commit {
  id: number;
  sha: string;
  message: string;
  author: string;
  authorEmail: string;
  commitDate: string;
  htmlUrl: string;
  filesChanged: number;
  additions: number;
  deletions: number;
  repositoryId: number;
  repositoryName: string;
  riskScore?: number;
  riskLevel?: 'HIGH' | 'MEDIUM' | 'LOW';
  aiSummaryPreview?: string;
}

export interface AnalysisResult {
  id: number;
  commitSha: string;
  language: string;
  toolName: string;
  issues: string;
  issueCount: number;
  createdAt: string;
}

export interface AiSummary {
  id: number;
  commitSha: string;
  summary: string;
  riskScore: number;
  riskLevel: string;
  improvements: string;
  securityConcerns: string;
  createdAt: string;
}

export interface CommitDetail {
  commit: Commit;
  analysisResults: AnalysisResult[];
  aiSummary?: AiSummary;
}

export interface RiskTrendPoint {
  date: string;
  riskScore: number;
  commitSha: string;
  commitMessage: string;
}

export interface DashboardMetrics {
  repositoryId: number;
  repositoryName: string;
  totalCommits: number;
  avgRiskScore: number;
  highRiskCount: number;
  mediumRiskCount: number;
  lowRiskCount: number;
  riskTrend: RiskTrendPoint[];
  recentCommits: Commit[];
  primaryLanguage: string;
}
