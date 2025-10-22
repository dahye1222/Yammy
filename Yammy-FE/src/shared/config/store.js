import { create } from 'zustand';
import { devtools } from 'zustand/middleware';

// Example store - modify according to your needs
export const useAppStore = create(
  devtools((set) => ({
    // State
    user: null,
    theme: 'light',

    // Actions
    setUser: (user) => set({ user }),
    setTheme: (theme) => set({ theme }),
    clearUser: () => set({ user: null }),
  }))
);

// You can create multiple stores for different features
// Example: useAuthStore, useCartStore, etc.
