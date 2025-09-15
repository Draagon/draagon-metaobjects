/**
 * Main App component for Fishstore Demo
 */

import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Layout } from './components/Layout';
import { StoreManagement } from './pages/StoreManagement';
import { TankManagement } from './pages/TankManagement';
import { BreedManagement } from './pages/BreedManagement';
import { FishManagement } from './pages/FishManagement';
import { Dashboard } from './pages/Dashboard';

const App: React.FC = () => {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/stores" element={<StoreManagement />} />
        <Route path="/tanks" element={<TankManagement />} />
        <Route path="/breeds" element={<BreedManagement />} />
        <Route path="/fish" element={<FishManagement />} />
      </Routes>
    </Layout>
  );
};

export default App;