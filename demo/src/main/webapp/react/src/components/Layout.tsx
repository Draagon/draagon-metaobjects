/**
 * Main layout component with navigation
 */

import React from 'react';
import { Link, useLocation } from 'react-router-dom';

interface LayoutProps {
  children: React.ReactNode;
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {
  const location = useLocation();

  const navItems = [
    { path: '/dashboard', label: 'Dashboard', icon: '📊' },
    { path: '/stores', label: 'Stores', icon: '🏪' },
    { path: '/tanks', label: 'Tanks', icon: '🐠' },
    { path: '/breeds', label: 'Breeds', icon: '🐟' },
    { path: '/fish', label: 'Fish', icon: '🐡' },
  ];

  return (
    <div className="layout">
      <header className="layout__header">
        <div className="layout__header-content">
          <h1 className="layout__title">
            🐠 Fishstore Demo
          </h1>
          <p className="layout__subtitle">
            MetaObjects React Components Demo
          </p>
        </div>
      </header>

      <div className="layout__container">
        <nav className="layout__nav">
          <ul className="layout__nav-list">
            {navItems.map((item) => (
              <li key={item.path} className="layout__nav-item">
                <Link
                  to={item.path}
                  className={`layout__nav-link ${
                    location.pathname === item.path ? 'layout__nav-link--active' : ''
                  }`}
                >
                  <span className="layout__nav-icon">{item.icon}</span>
                  <span className="layout__nav-label">{item.label}</span>
                </Link>
              </li>
            ))}
          </ul>
        </nav>

        <main className="layout__main">
          {children}
        </main>
      </div>

      <footer className="layout__footer">
        <p>
          Powered by <strong>MetaObjects</strong> - Metadata-driven React Forms
        </p>
      </footer>
    </div>
  );
};