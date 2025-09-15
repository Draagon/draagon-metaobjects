/**
 * Dashboard page showing overview of the fishstore
 */

import React from 'react';
import { Link } from 'react-router-dom';

export const Dashboard: React.FC = () => {
  const stats = [
    { label: 'Total Stores', value: 3, icon: '🏪', link: '/stores' },
    { label: 'Active Tanks', value: 18, icon: '🐠', link: '/tanks' },
    { label: 'Fish Breeds', value: 12, icon: '🐟', link: '/breeds' },
    { label: 'Total Fish', value: 247, icon: '🐡', link: '/fish' },
  ];

  const features = [
    {
      title: 'Metadata-Driven Forms',
      description: 'Forms are automatically generated from MetaObject definitions, ensuring consistency and reducing code duplication.',
      icon: '📝',
    },
    {
      title: 'Type-Safe Components',
      description: 'TypeScript interfaces ensure type safety between Java metadata and React components.',
      icon: '🔒',
    },
    {
      title: 'Validation Integration',
      description: 'Client-side validation rules are derived from metadata, providing immediate feedback.',
      icon: '✅',
    },
    {
      title: 'Flexible Rendering',
      description: 'Different view modes (READ, EDIT, HIDE) and custom view components for different field types.',
      icon: '🎨',
    },
  ];

  return (
    <div className="page">
      <div className="page__header">
        <h2>Dashboard</h2>
        <p>Welcome to the Fishstore Demo - showcasing MetaObjects React components</p>
      </div>

      <div className="page__content">
        {/* Stats Overview */}
        <div className="page__section">
          <h3>Overview</h3>
          <div className="stats-grid">
            {stats.map((stat) => (
              <Link 
                key={stat.label} 
                to={stat.link}
                className="stat-card"
              >
                <div className="stat-card__icon">{stat.icon}</div>
                <div className="stat-card__content">
                  <div className="stat-card__value">{stat.value}</div>
                  <div className="stat-card__label">{stat.label}</div>
                </div>
              </Link>
            ))}
          </div>
        </div>

        {/* Features */}
        <div className="page__section">
          <h3>MetaObjects React Features</h3>
          <div className="features-grid">
            {features.map((feature) => (
              <div key={feature.title} className="feature-card">
                <div className="feature-card__icon">{feature.icon}</div>
                <h4 className="feature-card__title">{feature.title}</h4>
                <p className="feature-card__description">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>

        {/* Architecture Info */}
        <div className="page__section">
          <h3>Architecture</h3>
          <div className="architecture-info">
            <div className="architecture-info__diagram">
              <div className="arch-layer">
                <div className="arch-box">React Components</div>
                <div className="arch-description">MetaView components (TextView, DateView, etc.)</div>
              </div>
              <div className="arch-arrow">↓</div>
              <div className="arch-layer">
                <div className="arch-box">Redux State</div>
                <div className="arch-description">Form state management with Redux Toolkit</div>
              </div>
              <div className="arch-arrow">↓</div>
              <div className="arch-layer">
                <div className="arch-box">REST API</div>
                <div className="arch-description">JSON metadata served from Java backend</div>
              </div>
              <div className="arch-arrow">↓</div>
              <div className="arch-layer">
                <div className="arch-box">MetaData Registry</div>
                <div className="arch-description">Java MetaObjects metadata system</div>
              </div>
            </div>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="page__section">
          <h3>Quick Actions</h3>
          <div className="quick-actions">
            <Link to="/stores" className="action-button">
              <span className="action-button__icon">🏪</span>
              <span className="action-button__text">Manage Stores</span>
            </Link>
            <Link to="/tanks" className="action-button">
              <span className="action-button__icon">🐠</span>
              <span className="action-button__text">Setup Tanks</span>
            </Link>
            <Link to="/breeds" className="action-button">
              <span className="action-button__icon">🐟</span>
              <span className="action-button__text">Add Breeds</span>
            </Link>
            <Link to="/fish" className="action-button">
              <span className="action-button__icon">🐡</span>
              <span className="action-button__text">Stock Fish</span>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};