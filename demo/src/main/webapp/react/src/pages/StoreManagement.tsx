/**
 * Store Management page demonstrating MetaObject forms
 */

import React, { useState } from 'react';
import { MetaObjectForm } from '../components/MetaObjectForm';
import { ViewMode } from '../types/metadata';

const sampleStores = [
  { id: 1, name: 'Downtown Aquarium', maxTanks: 25 },
  { id: 2, name: 'Seaside Fish Market', maxTanks: 15 },
  { id: 3, name: 'Pet Paradise', maxTanks: 40 },
];

export const StoreManagement: React.FC = () => {
  const [selectedStore, setSelectedStore] = useState<any>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [stores, setStores] = useState(sampleStores);

  const handleCreateStore = () => {
    setSelectedStore({ id: 0, name: '', maxTanks: 1 });
    setIsEditing(true);
  };

  const handleEditStore = (store: any) => {
    setSelectedStore(store);
    setIsEditing(true);
  };

  const handleViewStore = (store: any) => {
    setSelectedStore(store);
    setIsEditing(false);
  };

  const handleSaveStore = async (values: Record<string, unknown>) => {
    if (selectedStore?.id === 0) {
      // Create new store
      const newStore = {
        id: Math.max(...stores.map(s => s.id)) + 1,
        ...values,
      };
      setStores([...stores, newStore]);
    } else {
      // Update existing store
      setStores(stores.map(s => 
        s.id === selectedStore?.id ? { ...s, ...values } : s
      ));
    }
    setSelectedStore(null);
    setIsEditing(false);
  };

  const handleCancel = () => {
    setSelectedStore(null);
    setIsEditing(false);
  };

  const handleDeleteStore = (id: number) => {
    setStores(stores.filter(s => s.id !== id));
  };

  return (
    <div className="page">
      <div className="page__header">
        <h2>Store Management</h2>
        <p>Manage fish stores using MetaObject forms</p>
      </div>

      <div className="page__content">
        <div className="page__section">
          <div className="section__header">
            <h3>Stores</h3>
            <button 
              className="btn btn--primary"
              onClick={handleCreateStore}
            >
              Add New Store
            </button>
          </div>

          <div className="table-container">
            <table className="table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Max Tanks</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {stores.map((store) => (
                  <tr key={store.id}>
                    <td>{store.id}</td>
                    <td>{store.name}</td>
                    <td>{store.maxTanks}</td>
                    <td>
                      <div className="actions">
                        <button 
                          className="btn btn--small btn--secondary"
                          onClick={() => handleViewStore(store)}
                        >
                          View
                        </button>
                        <button 
                          className="btn btn--small btn--primary"
                          onClick={() => handleEditStore(store)}
                        >
                          Edit
                        </button>
                        <button 
                          className="btn btn--small btn--danger"
                          onClick={() => handleDeleteStore(store.id)}
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {selectedStore && (
          <div className="page__section">
            <div className="section__header">
              <h3>
                {selectedStore.id === 0 ? 'Create New Store' : 
                 isEditing ? 'Edit Store' : 'View Store'}
              </h3>
            </div>

            <div className="form-container">
              <MetaObjectForm
                formId={`store-${selectedStore.id || 'new'}`}
                objectName="Store"
                initialValues={selectedStore}
                mode={isEditing ? ViewMode.EDIT : ViewMode.READ}
                onSubmit={handleSaveStore}
                onCancel={handleCancel}
                excludeFields={['id']} // Don't show ID field
              />
            </div>
          </div>
        )}
      </div>
    </div>
  );
};