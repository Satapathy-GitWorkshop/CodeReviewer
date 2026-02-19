import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import { commitApi } from '../api';
import RiskBadge from '../components/commits/RiskBadge';
import type { CommitDetail } from '../types';

const CommitDetailPage: React.FC = () => {
  const { sha } = useParams<{ sha: string }>();
  const { theme } = useApp();
  const navigate = useNavigate();
  const [detail, setDetail] = useState<CommitDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const cardBg = theme === 'dark' ? '#161b22' : '#ffffff';
  const border = theme === 'dark' ? '#30363d' : '#d0d7de';
  const codeBg = theme === 'dark' ? '#0d1117' : '#f6f8fa';

  useEffect(() => {
    if (!sha) return;
    commitApi.getDetail(sha)
      .then(setDetail)
      .catch(() => setError('Commit not found'))
      .finally(() => setLoading(false));
  }, [sha]);

  if (loading) return <div style={{ padding: '40px', textAlign: 'center', color: '#8b949e' }}>Loading...</div>;
  if (error || !detail) return <div style={{ padding: '40px', textAlign: 'center', color: '#f85149' }}>{error || 'Not found'}</div>;

  const { commit, analysisResults, aiSummary } = detail;

  const sectionStyle = {
    backgroundColor: cardBg,
    border: `1px solid ${border}`,
    borderRadius: '10px',
    marginBottom: '20px',
    overflow: 'hidden' as const
  };

  const headerStyle = {
    padding: '14px 20px',
    borderBottom: `1px solid ${border}`,
    fontWeight: 600,
    fontSize: '14px'
  };

  const bodyStyle = { padding: '20px' };

  const parseIssues = (issuesStr: string) => {
    try { return JSON.parse(issuesStr); } catch { return []; }
  };

  return (
    <div>
      <button onClick={() => navigate(-1)} style={{
        background: 'none', border: 'none', color: '#58a6ff', cursor: 'pointer',
        fontSize: '13px', marginBottom: '16px', padding: 0
      }}>← Back</button>

      {/* Commit Header */}
      <div style={sectionStyle}>
        <div style={headerStyle}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span>📝 Commit Details</span>
            <RiskBadge score={commit.riskScore} level={commit.riskLevel} />
          </div>
        </div>
        <div style={bodyStyle}>
          <h2 style={{ margin: '0 0 12px', fontSize: '18px' }}>
            {commit.message?.split('\n')[0]}
          </h2>
          <div style={{ display: 'flex', gap: '20px', fontSize: '13px', color: '#8b949e', flexWrap: 'wrap' }}>
            <span>👤 {commit.author} &lt;{commit.authorEmail}&gt;</span>
            <span>🕐 {new Date(commit.commitDate).toLocaleString()}</span>
            <span>📄 {commit.filesChanged} files (+{commit.additions}/-{commit.deletions})</span>
            <a href={commit.htmlUrl} target="_blank" rel="noopener noreferrer" style={{ color: '#58a6ff' }}>
              View on GitHub ↗
            </a>
          </div>
          <code style={{ display: 'block', marginTop: '10px', fontSize: '12px', color: '#58a6ff', backgroundColor: codeBg, padding: '4px 8px', borderRadius: '4px', width: 'fit-content' }}>
            {commit.sha}
          </code>
        </div>
      </div>

      {/* AI Summary */}
      {aiSummary && (
        <div style={sectionStyle}>
          <div style={headerStyle}>🤖 AI Code Review</div>
          <div style={bodyStyle}>
            <div style={{ marginBottom: '16px' }}>
              <h4 style={{ margin: '0 0 8px', color: '#8b949e', fontSize: '12px', textTransform: 'uppercase' }}>Summary</h4>
              <p style={{ margin: 0, lineHeight: 1.6 }}>{aiSummary.summary}</p>
            </div>

            {aiSummary.improvements && (
              <div style={{ marginBottom: '16px' }}>
                <h4 style={{ margin: '0 0 8px', color: '#8b949e', fontSize: '12px', textTransform: 'uppercase' }}>💡 Improvements</h4>
                <div style={{ backgroundColor: codeBg, padding: '12px', borderRadius: '6px', whiteSpace: 'pre-line', fontSize: '13px', lineHeight: 1.6 }}>
                  {aiSummary.improvements}
                </div>
              </div>
            )}

            {aiSummary.securityConcerns && aiSummary.securityConcerns.trim() && (
              <div>
                <h4 style={{ margin: '0 0 8px', color: '#f85149', fontSize: '12px', textTransform: 'uppercase' }}>🔒 Security Concerns</h4>
                <div style={{ backgroundColor: '#3d1414', padding: '12px', borderRadius: '6px', whiteSpace: 'pre-line', fontSize: '13px', lineHeight: 1.6, color: '#ffa198', border: '1px solid #f8514933' }}>
                  {aiSummary.securityConcerns}
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Static Analysis */}
      {analysisResults.length > 0 && (
        <div style={sectionStyle}>
          <div style={headerStyle}>🔬 Static Analysis Results</div>
          <div style={bodyStyle}>
            {analysisResults.map((result, i) => {
              const issues = parseIssues(result.issues);
              return (
                <div key={i} style={{ marginBottom: i < analysisResults.length - 1 ? '20px' : 0 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                    <div>
                      <span style={{ fontWeight: 600, fontSize: '13px' }}>{result.toolName}</span>
                      <span style={{ marginLeft: '8px', padding: '2px 8px', borderRadius: '12px', backgroundColor: theme === 'dark' ? '#30363d' : '#e1e4e8', fontSize: '11px' }}>
                        {result.language}
                      </span>
                    </div>
                    <span style={{ fontSize: '12px', color: result.issueCount > 0 ? '#f85149' : '#3fb950' }}>
                      {result.issueCount} issue{result.issueCount !== 1 ? 's' : ''}
                    </span>
                  </div>
                  {issues.length > 0 ? (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                      {issues.map((issue: any, j: number) => (
                        <div key={j} style={{
                          backgroundColor: codeBg, padding: '10px 12px',
                          borderRadius: '6px', fontSize: '12px',
                          borderLeft: `3px solid ${issue.category === 'Security' ? '#f85149' : '#d29922'}`
                        }}>
                          <div style={{ display: 'flex', gap: '8px', marginBottom: '3px' }}>
                            <span style={{ fontWeight: 600, color: issue.category === 'Security' ? '#f85149' : '#d29922' }}>
                              {issue.category}
                            </span>
                            <code style={{ color: '#58a6ff' }}>{issue.file}</code>
                          </div>
                          <p style={{ margin: 0, color: '#8b949e' }}>{issue.message}</p>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p style={{ color: '#3fb950', fontSize: '13px', margin: 0 }}>✅ No issues found</p>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}

      {analysisResults.length === 0 && !aiSummary && (
        <div style={{ ...sectionStyle, ...{ textAlign: 'center', padding: '40px', color: '#8b949e' } }}>
          Analysis not yet available for this commit.
        </div>
      )}
    </div>
  );
};

export default CommitDetailPage;
