/**
 * Fish Management page (simplified for demo)
 */

import React from 'react';

export const FishManagement: React.FC = () => {
  return (
    <div className="page">
      <div className="page__header">
        <h2>Fish Management</h2>
        <p>Manage individual fish using MetaObject forms</p>
      </div>
      <div className="page__content">
        <p>Fish management functionality would be implemented here, with numeric views for length and weight.</p>
      </div>
    </div>
  );
};