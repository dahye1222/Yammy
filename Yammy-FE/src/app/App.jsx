import { RouterProvider } from 'react-router-dom';
import { router } from './providers/router';

function App() {
  return <RouterProvider router={router} />;
}

export default App;
