/**
 * Simple Redux store for demo
 */

import { configureStore } from '@reduxjs/toolkit';

// Simplified store for demo
export const store = configureStore({
  reducer: {
    // Demo reducers would go here
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;