import React from 'react';
import AppRouter from './router/AppRouter';
import NavigationBar from './shared/components/NavagationBar'

function App() {
  return (
    <div>
      <AppRouter />
      <NavigationBar />
    </div>
  );
}

export default App;
